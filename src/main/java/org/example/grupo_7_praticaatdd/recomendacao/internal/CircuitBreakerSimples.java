package org.example.grupo_7_praticaatdd.recomendacao.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

// Circuit breaker minimo: depois de N falhas seguidas, para de chamar a IA por um tempo e
// falha na hora, sem esperar o timeout. Passado esse tempo, deixa uma chamada tentar de novo.
public class CircuitBreakerSimples {

    private final int falhasParaAbrir;
    private final Duration tempoAberto;
    private final Clock relogio;

    private int falhasSeguidas;
    private Instant abertoAte = Instant.MIN;

    public CircuitBreakerSimples(int falhasParaAbrir, Duration tempoAberto, Clock relogio) {
        this.falhasParaAbrir = falhasParaAbrir;
        this.tempoAberto = tempoAberto;
        this.relogio = relogio;
    }

    public synchronized boolean permiteChamada() {
        return !relogio.instant().isBefore(abertoAte);
    }

    public synchronized void registrarSucesso() {
        falhasSeguidas = 0;
        abertoAte = Instant.MIN;
    }

    public synchronized void registrarFalha() {
        falhasSeguidas++;
        if (falhasSeguidas >= falhasParaAbrir) {
            abertoAte = relogio.instant().plus(tempoAberto);
        }
    }
}
