package org.example.grupo_7_praticaatdd.gamificacao;

import org.example.grupo_7_praticaatdd.entity.UsuarioEntity;
import org.example.grupo_7_praticaatdd.gamificacao.application.AssinaturaRepositorio;
import org.example.grupo_7_praticaatdd.gamificacao.internal.jpa.AssinaturaJpaAdapter;
import org.example.grupo_7_praticaatdd.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

// Contrato rodando com o adapter JPA sobre o banco H2 do teste.
@DataJpaTest
@Import(AssinaturaJpaAdapter.class)
class AssinaturaJpaAdapterTest extends AssinaturaRepositorioContrato {

    @Autowired
    private AssinaturaJpaAdapter adapter;

    @Autowired
    private UsuarioRepository usuarios;

    @Override
    protected AssinaturaRepositorio repositorio() {
        return adapter;
    }

    @Override
    protected Long novoUsuario(String nome) {
        return usuarios.save(new UsuarioEntity(nome)).getId();
    }
}
