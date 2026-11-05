package org.example.grupo_7_praticaatdd.notificacao.internal;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Fila duravel + fila de mensagens mortas (DLQ) para o que o worker rejeitar.
@Configuration
public class FilasConfig {

    @Bean
    Queue filaCursoConcluido(@Value("${notificacao.fila}") String fila) {
        return QueueBuilder.durable(fila)
                .deadLetterExchange("")
                .deadLetterRoutingKey(fila + ".dlq")
                .build();
    }

    @Bean
    Queue filaCursoConcluidoDlq(@Value("${notificacao.fila}") String fila) {
        return QueueBuilder.durable(fila + ".dlq").build();
    }
}
