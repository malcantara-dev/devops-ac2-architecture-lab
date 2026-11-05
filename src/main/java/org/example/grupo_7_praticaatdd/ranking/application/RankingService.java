package org.example.grupo_7_praticaatdd.ranking.application;

import org.example.grupo_7_praticaatdd.ranking.api.RankingItemDTO;
import org.example.grupo_7_praticaatdd.ranking.internal.AlunoPontuado;
import org.example.grupo_7_praticaatdd.ranking.internal.RankingConsulta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Modulo ranking, camada application: ordena os alunos e numera as posicoes.
@Service
public class RankingService {

    static final int PONTOS_POR_CURSO_CONCLUIDO = 100;

    private final RankingConsulta consulta;

    public RankingService(RankingConsulta consulta) {
        this.consulta = consulta;
    }

    @Transactional(readOnly = true)
    public List<RankingItemDTO> listar() {
        List<AlunoPontuado> alunos = new ArrayList<>(consulta.buscarAlunos());
        alunos.sort(Comparator.comparingInt(this::pontuacao).reversed()
                .thenComparing(AlunoPontuado::nome));

        List<RankingItemDTO> ranking = new ArrayList<>();
        for (int i = 0; i < alunos.size(); i++) {
            AlunoPontuado a = alunos.get(i);
            ranking.add(new RankingItemDTO(i + 1, a.id(), a.nome(), a.cursosConcluidos(), a.creditos(), pontuacao(a)));
        }
        return ranking;
    }

    private int pontuacao(AlunoPontuado aluno) {
        return aluno.cursosConcluidos() * PONTOS_POR_CURSO_CONCLUIDO + aluno.creditos();
    }
}
