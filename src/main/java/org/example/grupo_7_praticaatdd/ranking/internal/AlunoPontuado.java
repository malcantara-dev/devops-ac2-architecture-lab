package org.example.grupo_7_praticaatdd.ranking.internal;

// Projecao de leitura do ranking: so os campos que a classificacao usa.
// moedas pode vir nulo de bancos populados antes do B1.
public record AlunoPontuado(Long id, String nome, int cursosConcluidos, int creditos, Integer moedas) {
}
