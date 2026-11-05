package org.example.grupo_7_praticaatdd.gamificacao;

import org.example.grupo_7_praticaatdd.gamificacao.application.AssinaturaRepositorio;

// Contrato rodando com o adapter em memoria: nenhum Spring, nenhum banco.
class AssinaturaEmMemoriaTest extends AssinaturaRepositorioContrato {

    private final AssinaturaEmMemoria memoria = new AssinaturaEmMemoria();

    @Override
    protected AssinaturaRepositorio repositorio() {
        return memoria;
    }

    @Override
    protected Long novoUsuario(String nome) {
        return memoria.novoUsuario();
    }
}
