package org.example.grupo_7_praticaatdd.notificacao.application;

import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Modo sync (so para o mini-lab): avisa o worker dentro da transacao. Se o worker estiver fora
// do ar, a excecao desfaz a conclusao do curso. E exatamente o acoplamento que o D4 evita.
@Component
@ConditionalOnProperty(name = "notificacao.modo", havingValue = "sync")
public class NotificacaoSincrona {

    private final PublicadorDeNotificacoes publicador;

    public NotificacaoSincrona(PublicadorDeNotificacoes publicador) {
        this.publicador = publicador;
    }

    @EventListener
    public void aoConcluirCurso(CursoConcluidoEvento evento) {
        publicador.publicar(evento);
    }
}
