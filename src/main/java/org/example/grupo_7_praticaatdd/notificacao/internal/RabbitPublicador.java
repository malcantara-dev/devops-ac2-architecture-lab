package org.example.grupo_7_praticaatdd.notificacao.internal;

import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.example.grupo_7_praticaatdd.notificacao.application.PublicadorDeNotificacoes;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Adapter async: mensagem persistente na fila duravel. Se o worker cair, ela espera na fila.
@Component
@ConditionalOnProperty(name = "notificacao.modo", havingValue = "async", matchIfMissing = true)
public class RabbitPublicador implements PublicadorDeNotificacoes {

    private final RabbitTemplate rabbit;
    private final String fila;

    public RabbitPublicador(RabbitTemplate rabbit, @Value("${notificacao.fila}") String fila) {
        this.rabbit = rabbit;
        this.fila = fila;
    }

    @Override
    public void publicar(CursoConcluidoEvento evento) {
        rabbit.convertAndSend("", fila, evento.toJson(), mensagem -> {
            mensagem.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            mensagem.getMessageProperties().setContentType("application/json");
            return mensagem;
        });
    }
}
