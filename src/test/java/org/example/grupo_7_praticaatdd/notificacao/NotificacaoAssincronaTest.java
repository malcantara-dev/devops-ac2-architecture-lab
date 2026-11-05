package org.example.grupo_7_praticaatdd.notificacao;

import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.example.grupo_7_praticaatdd.notificacao.application.PublicadorDeNotificacoes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.ConnectException;

import static org.example.grupo_7_praticaatdd.notificacao.NotificacaoTestSupport.criarMatricula;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Desafio 4, modo async: o evento so sai depois do commit e a falha do broker nao desfaz nada.
@SpringBootTest(properties = "notificacao.modo=async")
@AutoConfigureMockMvc
class NotificacaoAssincronaTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PublicadorDeNotificacoes publicador;

    @Test
    @DisplayName("Concluir o curso publica CursoConcluido uma vez, com aproveitamento")
    void publicaDepoisDoCommit() throws Exception {
        long matricula = criarMatricula(mvc, "Nina");

        mvc.perform(put("/api/matriculas/" + matricula + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":8.0}"))
                .andExpect(status().isOk());

        verify(publicador).publicar(argThat((CursoConcluidoEvento e) -> e.matriculaId() == matricula && e.aproveitamento()));
    }

    @Test
    @DisplayName("Nota parcial nao conclui o curso e nao publica nada")
    void notaParcialNaoPublica() throws Exception {
        long matricula = criarMatricula(mvc, "Otavio");

        mvc.perform(put("/api/matriculas/" + matricula + "/nota-parcial")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":9.0}"))
                .andExpect(status().isOk());

        verifyNoInteractions(publicador);
    }

    @Test
    @DisplayName("Matricula inexistente: a transacao falha e nenhum evento e publicado")
    void rollbackNaoPublica() throws Exception {
        mvc.perform(put("/api/matriculas/999999/concluir")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":8.0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publicador);
    }

    @Test
    @DisplayName("Broker fora do ar: a conclusao continua 200 e gravada")
    void brokerForaDoArNaoBloqueia() throws Exception {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused"))).when(publicador).publicar(any());
        long matricula = criarMatricula(mvc, "Paula");

        mvc.perform(put("/api/matriculas/" + matricula + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":9.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }
}
