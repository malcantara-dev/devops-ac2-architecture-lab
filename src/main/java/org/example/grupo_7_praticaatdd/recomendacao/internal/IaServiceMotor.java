package org.example.grupo_7_praticaatdd.recomendacao.internal;

import org.example.grupo_7_praticaatdd.recomendacao.application.MotorDeRecomendacao;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Adapter HTTP para o ia-service (FastAPI). Timeout curto + circuit breaker: a IA pode cair
// sem segurar threads do Core.
public class IaServiceMotor implements MotorDeRecomendacao {

    record Item(String curso, double score) {
    }

    record Resposta(List<Item> recomendacoes) {
    }

    private final RestClient http;
    private final CircuitBreakerSimples circuito;

    public IaServiceMotor(RestClient http, CircuitBreakerSimples circuito) {
        this.http = http;
        this.circuito = circuito;
    }

    @Override
    public List<String> recomendar(Long usuarioId, List<String> concluidos, List<String> catalogo) {
        if (!circuito.permiteChamada()) {
            throw new IaIndisponivelException("circuito aberto apos falhas seguidas do ia-service", null);
        }
        try {
            Resposta resposta = http.post()
                    .uri("/recomendacoes")
                    .body(Map.of("usuarioId", usuarioId, "concluidos", concluidos, "catalogo", catalogo))
                    .retrieve()
                    .body(Resposta.class);
            circuito.registrarSucesso();
            return resposta == null ? List.of() : resposta.recomendacoes().stream().map(Item::curso).toList();
        } catch (RuntimeException falha) {
            circuito.registrarFalha();
            throw new IaIndisponivelException("falha ao chamar o ia-service (" + falha.getClass().getSimpleName() + ")", falha);
        }
    }
}
