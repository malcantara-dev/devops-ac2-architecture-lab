package org.example.grupo_7_praticaatdd.ranking.application;

import org.example.grupo_7_praticaatdd.ranking.api.RankingItemDTO;
import org.example.grupo_7_praticaatdd.ranking.internal.AlunoPontuado;
import org.example.grupo_7_praticaatdd.ranking.internal.RankingConsulta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingServiceTest {

    private final RankingConsulta consulta = mock(RankingConsulta.class);
    private final RankingService service = new RankingService(consulta);

    @Test
    @DisplayName("RF01 - ordena por pontuacao e numera as posicoes")
    void deveOrdenarPorPontuacao() {
        when(consulta.buscarAlunos()).thenReturn(List.of(
                new AlunoPontuado(1L, "Ana", 1, 3, 0),
                new AlunoPontuado(2L, "Bruno", 2, 0, null),
                new AlunoPontuado(3L, "Carla", 0, 0, 5)));

        List<RankingItemDTO> ranking = service.listar();

        assertEquals(List.of("Bruno", "Ana", "Carla"), ranking.stream().map(RankingItemDTO::nome).toList());
        assertEquals(1, ranking.get(0).posicao());
        assertEquals(200, ranking.get(0).pontuacao());
        assertEquals(103, ranking.get(1).pontuacao());
        assertEquals(50, ranking.get(2).pontuacao());
    }

    @Test
    @DisplayName("RF01 - empate na pontuacao desempata pelo nome")
    void deveDesempatarPeloNome() {
        when(consulta.buscarAlunos()).thenReturn(List.of(
                new AlunoPontuado(1L, "Zeca", 1, 0, 0),
                new AlunoPontuado(2L, "Alice", 1, 0, 0)));

        assertEquals("Alice", service.listar().get(0).nome());
    }
}
