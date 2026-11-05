package org.example.grupo_7_praticaatdd.ranking.api;

public record RankingItemDTO(
        int posicao,
        Long usuarioId,
        String nome,
        int cursosConcluidos,
        int creditos,
        int pontuacao
) {
}
