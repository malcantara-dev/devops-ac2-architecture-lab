package org.example.grupo_7_praticaatdd.ranking.internal;

import org.example.grupo_7_praticaatdd.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

// Consulta somente leitura do ranking sobre a tabela de usuarios do legado.
public interface RankingConsulta extends Repository<UsuarioEntity, Long> {

    @Query("select new org.example.grupo_7_praticaatdd.ranking.internal.AlunoPontuado("
            + "u.id, u.nome, u.cursosConcluidosComSucesso, u.creditosCursos, u.moedas) from UsuarioEntity u")
    List<AlunoPontuado> buscarAlunos();
}
