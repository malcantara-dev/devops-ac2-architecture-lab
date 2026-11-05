package org.example.grupo_7_praticaatdd.notificacao.application;

import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;

// Porta de saida: entregar o evento a quem notifica o aluno.
public interface PublicadorDeNotificacoes {

    void publicar(CursoConcluidoEvento evento);
}
