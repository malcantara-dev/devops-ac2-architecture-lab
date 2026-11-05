package org.example.grupo_7_praticaatdd.ranking.application;

import io.micrometer.core.instrument.Metrics;
import org.example.grupo_7_praticaatdd.domain.PoliticaDePontuacao;
import org.example.grupo_7_praticaatdd.ranking.api.RankingItemDTO;
import org.example.grupo_7_praticaatdd.ranking.internal.AlunoPontuado;
import org.example.grupo_7_praticaatdd.ranking.internal.RankingConsulta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Modulo ranking, camada application: ordena os alunos e numera as posicoes.
// A formula da pontuacao e regra de negocio e mora no dominio (PoliticaDePontuacao).
@Service
public class RankingService {

    private final RankingConsulta consulta;

    public RankingService(RankingConsulta consulta) {
        this.consulta = consulta;
    }

    // Metrica do ASR de escalabilidade do D1: latencia do ranking (ranking_consulta_seconds).
    @Transactional(readOnly = true)
    public List<RankingItemDTO> listar() {
        return Metrics.timer("ranking.consulta").record(this::classificar);
    }

    private List<RankingItemDTO> classificar() {
        List<AlunoPontuado> alunos = new ArrayList<>(consulta.buscarAlunos());
        alunos.sort(Comparator.comparingInt(this::pontuacao).reversed()
                .thenComparing(AlunoPontuado::nome));

        List<RankingItemDTO> ranking = new ArrayList<>();
        for (int i = 0; i < alunos.size(); i++) {
            AlunoPontuado a = alunos.get(i);
            ranking.add(new RankingItemDTO(i + 1, a.id(), a.nome(), a.cursosConcluidos(), a.creditos(), moedas(a), pontuacao(a)));
        }
        return ranking;
    }

    private int pontuacao(AlunoPontuado aluno) {
        return PoliticaDePontuacao.pontuar(aluno.cursosConcluidos(), moedas(aluno), aluno.creditos());
    }

    private static int moedas(AlunoPontuado aluno) {
        return aluno.moedas() == null ? 0 : aluno.moedas();
    }
}
