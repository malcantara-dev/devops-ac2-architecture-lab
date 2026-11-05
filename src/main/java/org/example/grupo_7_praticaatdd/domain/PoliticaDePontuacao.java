package org.example.grupo_7_praticaatdd.domain;

// Regra de pontuacao do ranking (RF01). Fica no dominio porque muda junto com as regras de
// gamificacao; o modulo ranking so ordena e numera.
public final class PoliticaDePontuacao {

    public static final int PONTOS_POR_CURSO_CONCLUIDO = 100;
    public static final int PONTOS_POR_MOEDA = 10;
    public static final int PONTOS_POR_CREDITO = 1;

    private PoliticaDePontuacao() {
    }

    public static int pontuar(int cursosConcluidos, int moedas, int creditos) {
        return cursosConcluidos * PONTOS_POR_CURSO_CONCLUIDO
                + moedas * PONTOS_POR_MOEDA
                + creditos * PONTOS_POR_CREDITO;
    }
}
