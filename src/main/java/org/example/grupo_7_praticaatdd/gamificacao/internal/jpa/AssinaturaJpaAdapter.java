package org.example.grupo_7_praticaatdd.gamificacao.internal.jpa;

import org.example.grupo_7_praticaatdd.domain.Assinatura;
import org.example.grupo_7_praticaatdd.entity.UsuarioEntity;
import org.example.grupo_7_praticaatdd.gamificacao.application.AssinaturaRepositorio;
import org.example.grupo_7_praticaatdd.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

// Adapter de saida JPA: traduz entre a tabela usuarios e o objeto de dominio Assinatura.
@Component
public class AssinaturaJpaAdapter implements AssinaturaRepositorio {

    private final UsuarioRepository usuarios;

    public AssinaturaJpaAdapter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public Assinatura buscarPorUsuario(Long usuarioId) {
        UsuarioEntity u = buscar(usuarioId);
        return Assinatura.restaurar(u.getCreditosCursos(), u.getCursosConcluidosComSucesso(), u.getPlano(), u.getMoedas());
    }

    @Override
    public void salvar(Long usuarioId, Assinatura assinatura) {
        UsuarioEntity u = buscar(usuarioId);
        u.setCreditosCursos(assinatura.getCreditosCursos());
        u.setCursosConcluidosComSucesso(assinatura.getCursosConcluidosComSucesso());
        u.setPlano(assinatura.getPlano());
        u.setMoedas(assinatura.getMoedas());
        usuarios.save(u);
    }

    private UsuarioEntity buscar(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }
}
