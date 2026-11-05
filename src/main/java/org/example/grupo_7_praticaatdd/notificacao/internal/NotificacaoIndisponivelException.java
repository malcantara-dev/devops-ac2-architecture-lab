package org.example.grupo_7_praticaatdd.notificacao.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NotificacaoIndisponivelException extends RuntimeException {

    public NotificacaoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
