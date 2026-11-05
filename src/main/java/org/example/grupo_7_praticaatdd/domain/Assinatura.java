package org.example.grupo_7_praticaatdd.domain;

public class Assinatura {
    public static final String MENSAGEM_CREDITOS_INSUFICIENTES = "Creditos insuficientes";

    public static final int CREDITOS_POR_CONCLUSAO = 3;

    private int creditosCursos = 0;
    private int cursosConcluidosComSucesso = 0;
    private PlanoAssinatura plano = PlanoAssinatura.BASICO;
    private int moedas = 0;

    public int getCreditosCursos() {
        return creditosCursos;
    }

    public int getCursosConcluidosComSucesso() {
        return cursosConcluidosComSucesso;
    }

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public int getMoedas() {
        return moedas;
    }

    public void adicionarCreditos(int quantidade) {
        this.creditosCursos = this.creditosCursos + quantidade;
    }

    public void registrarConclusaoComSucesso() {
        this.cursosConcluidosComSucesso = this.cursosConcluidosComSucesso + 1;
        adicionarCreditos(CREDITOS_POR_CONCLUSAO);
        if (PoliticaDeGamificacao.devePromoverParaPremium(this.cursosConcluidosComSucesso, this.plano)) {
            promoverParaPremium();
        }
    }

    private void promoverParaPremium() {
        this.plano = PlanoAssinatura.PREMIUM;
        this.moedas = this.moedas + PoliticaDeGamificacao.MOEDAS_NA_PROMOCAO;
    }

    public void consumirCredito() {
        if (this.creditosCursos < 1) {
            throw new IllegalStateException(MENSAGEM_CREDITOS_INSUFICIENTES);
        }
        this.creditosCursos = this.creditosCursos - 1;
    }
}