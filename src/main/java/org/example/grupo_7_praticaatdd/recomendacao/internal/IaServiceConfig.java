package org.example.grupo_7_praticaatdd.recomendacao.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class IaServiceConfig {

    @Bean
    IaServiceMotor iaServiceMotor(@Value("${ia.url}") String url,
                                  @Value("${ia.timeout-ms:2000}") long timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));

        RestClient http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        return new IaServiceMotor(http, new CircuitBreakerSimples(3, Duration.ofSeconds(30), Clock.systemUTC()));
    }
}
