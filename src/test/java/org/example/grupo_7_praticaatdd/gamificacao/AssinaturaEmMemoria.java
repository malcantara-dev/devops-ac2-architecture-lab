package org.example.grupo_7_praticaatdd.gamificacao;

import org.example.grupo_7_praticaatdd.domain.Assinatura;
import org.example.grupo_7_praticaatdd.gamificacao.application.AssinaturaRepositorio;

import java.util.HashMap;
import java.util.Map;

// Adapter em memoria da porta: sem banco, sem Spring. Guarda uma copia do estado para
// se comportar como persistencia de verdade (alterar o objeto lido nao altera o salvo).
public class AssinaturaEmMemoria implements AssinaturaRepositorio {

    private final Map<Long, Assinatura> dados = new HashMap<>();
    private long proximoId = 1;

    public Long novoUsuario() {
        long id = proximoId++;
        dados.put(id, new Assinatura());
        return id;
    }

    @Override
    public Assinatura buscarPorUsuario(Long usuarioId) {
        Assinatura a = dados.get(usuarioId);
        if (a == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }
        return copia(a);
    }

    @Override
    public void salvar(Long usuarioId, Assinatura assinatura) {
        dados.put(usuarioId, copia(assinatura));
    }

    private static Assinatura copia(Assinatura a) {
        return Assinatura.restaurar(a.getCreditosCursos(), a.getCursosConcluidosComSucesso(), a.getPlano(), a.getMoedas());
    }
}
