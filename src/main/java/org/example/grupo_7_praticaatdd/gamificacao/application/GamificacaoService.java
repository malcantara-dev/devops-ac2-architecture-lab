package org.example.grupo_7_praticaatdd.gamificacao.application;

import org.example.grupo_7_praticaatdd.domain.Assinatura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Casos de uso da gamificacao: carrega a assinatura completa pela porta, aplica a regra do
// dominio e grava o resultado. Nao depende de JPA nem de nenhuma tabela.
@Service
public class GamificacaoService {

    private final AssinaturaRepositorio repositorio;

    public GamificacaoService(AssinaturaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Cenarios 1, 7 e 8: credita a conclusao e promove para Premium no 12o curso.
    @Transactional
    public Assinatura registrarConclusaoComSucesso(Long usuarioId) {
        Assinatura assinatura = repositorio.buscarPorUsuario(usuarioId);
        assinatura.registrarConclusaoComSucesso();
        repositorio.salvar(usuarioId, assinatura);
        return assinatura;
    }

    // Cenarios 5 e 6: consome um credito ou recusa com "Creditos insuficientes".
    @Transactional
    public Assinatura consumirCredito(Long usuarioId) {
        Assinatura assinatura = repositorio.buscarPorUsuario(usuarioId);
        assinatura.consumirCredito();
        repositorio.salvar(usuarioId, assinatura);
        return assinatura;
    }
}
