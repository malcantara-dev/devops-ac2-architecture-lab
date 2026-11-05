package org.example.grupo_7_praticaatdd.gamificacao.application;

import org.example.grupo_7_praticaatdd.domain.Assinatura;

// Porta de saida: o que o caso de uso precisa da persistencia, sem saber que existe JPA.
public interface AssinaturaRepositorio {

    Assinatura buscarPorUsuario(Long usuarioId);

    void salvar(Long usuarioId, Assinatura assinatura);
}
