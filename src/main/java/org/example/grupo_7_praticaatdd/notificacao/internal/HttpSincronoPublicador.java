package org.example.grupo_7_praticaatdd.notificacao.internal;

import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.example.grupo_7_praticaatdd.notificacao.application.PublicadorDeNotificacoes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

// Adapter sync (mini-lab): chamada HTTP direta ao worker, com timeout de 2 s.
@Component
@ConditionalOnProperty(name = "notificacao.modo", havingValue = "sync")
public class HttpSincronoPublicador implements PublicadorDeNotificacoes {

    private final RestClient http;

    public HttpSincronoPublicador(@Value("${notificacao.worker-url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(2));
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public void publicar(CursoConcluidoEvento evento) {
        try {
            http.post().uri("/notificar").contentType(MediaType.APPLICATION_JSON).body(evento.toJson())
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException falha) {
            throw new NotificacaoIndisponivelException("Servico de notificacao indisponivel", falha);
        }
    }
}
