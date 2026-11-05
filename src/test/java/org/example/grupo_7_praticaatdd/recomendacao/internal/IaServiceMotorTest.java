package org.example.grupo_7_praticaatdd.recomendacao.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Desafio 3: o adapter HTTP com o ia-service simulado (sem Python rodando).
class IaServiceMotorTest {

    private MockRestServiceServer ia;
    private IaServiceMotor motor;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ia-service:8000");
        ia = MockRestServiceServer.bindTo(builder).build();
        motor = new IaServiceMotor(builder.build(), new CircuitBreakerSimples(3, Duration.ofSeconds(30), Clock.systemUTC()));
    }

    @Test
    @DisplayName("IA respondendo: devolve os cursos na ordem do modelo")
    void deveDevolverRecomendacoesDaIa() {
        ia.expect(requestTo("http://ia-service:8000/recomendacoes")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"usuarioId\":1,\"recomendacoes\":[{\"curso\":\"Spring Avancado\",\"score\":0.5},"
                        + "{\"curso\":\"Docker\",\"score\":0.1}]}", MediaType.APPLICATION_JSON));

        assertEquals(List.of("Spring Avancado", "Docker"), motor.recomendar(1L, List.of("Spring"), List.of("Docker", "Spring Avancado")));
        ia.verify();
    }

    @Test
    @DisplayName("IA com timeout: o adapter falha rapido com IaIndisponivelException")
    void timeoutViraIaIndisponivel() {
        ia.expect(requestTo("http://ia-service:8000/recomendacoes"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThrows(IaIndisponivelException.class, () -> motor.recomendar(1L, List.of(), List.of()));
    }

    @Test
    @DisplayName("IA com erro 500: o adapter falha com IaIndisponivelException")
    void erro500ViraIaIndisponivel() {
        ia.expect(requestTo("http://ia-service:8000/recomendacoes")).andRespond(withServerError());

        assertThrows(IaIndisponivelException.class, () -> motor.recomendar(1L, List.of(), List.of()));
    }

    @Test
    @DisplayName("Circuit breaker: depois de 3 falhas seguidas a 4a chamada nem chega ao ia-service")
    void circuitoAbreDepoisDeTresFalhas() {
        for (int i = 0; i < 3; i++) {
            ia.expect(requestTo("http://ia-service:8000/recomendacoes")).andRespond(withServerError());
        }
        for (int i = 0; i < 3; i++) {
            assertThrows(IaIndisponivelException.class, () -> motor.recomendar(1L, List.of(), List.of()));
        }

        IaIndisponivelException aberto = assertThrows(IaIndisponivelException.class,
                () -> motor.recomendar(1L, List.of(), List.of()));

        assertEquals("circuito aberto apos falhas seguidas do ia-service", aberto.getMessage());
        ia.verify();
    }
}
