package org.example.grupo_7_praticaatdd.notificacao.application;

import io.micrometer.core.instrument.Metrics;
import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Modo async (padrao): so publica DEPOIS do commit. Se o broker falhar, a conclusao ja esta
// gravada e o aluno recebe a resposta normalmente; a falha fica no log para reprocessar.
@Component
@ConditionalOnProperty(name = "notificacao.modo", havingValue = "async", matchIfMissing = true)
public class NotificacaoAssincrona {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAssincrona.class);
    private final PublicadorDeNotificacoes publicador;

    public NotificacaoAssincrona(PublicadorDeNotificacoes publicador) {
        this.publicador = publicador;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConcluirCurso(CursoConcluidoEvento evento) {
        try {
            publicador.publicar(evento);
            Metrics.counter("notificacao.eventos", "resultado", "publicado").increment();
        } catch (RuntimeException falha) {
            // Metrica do D4: eventos que nao chegaram ao broker
            Metrics.counter("notificacao.eventos", "resultado", "falha").increment();
            log.warn("Broker indisponivel; conclusao ja gravada, notificacao da matricula {} pendente: {}",
                    evento.matriculaId(), falha.getMessage());
        }
    }
}
