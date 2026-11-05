package org.example.grupo_7_praticaatdd.domain;

// Regras de gamificacao que mudam com frequencia, concentradas em um so lugar.
public final class PoliticaDeGamificacao {

    public static final int CURSOS_PARA_PREMIUM = 12;
    public static final int MOEDAS_NA_PROMOCAO = 3;

    private PoliticaDeGamificacao() {
    }

    public static boolean devePromoverParaPremium(int cursosConcluidos, PlanoAssinatura planoAtual) {
        return planoAtual == PlanoAssinatura.BASICO && cursosConcluidos >= CURSOS_PARA_PREMIUM;
    }
}
