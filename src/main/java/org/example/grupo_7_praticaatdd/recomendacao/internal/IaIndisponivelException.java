package org.example.grupo_7_praticaatdd.recomendacao.internal;

public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
