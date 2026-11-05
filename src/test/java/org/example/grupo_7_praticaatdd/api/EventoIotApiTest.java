package org.example.grupo_7_praticaatdd.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Desafio 5: QoS 1 entrega pelo menos uma vez; o Core precisa ser idempotente pelo eventId.
@SpringBootTest
@AutoConfigureMockMvc
class EventoIotApiTest {

    @Autowired
    private MockMvc mvc;

    private static String evento(String id, String tipo) {
        return "{\"eventId\":\"" + id + "\",\"dispositivoId\":\"catraca-1\",\"laboratorio\":\"LAB-1\","
                + "\"tipo\":\"" + tipo + "\",\"usuarioId\":1,\"ocorridoEm\":\"2026-11-04T20:00:00+00:00\"}";
    }

    @Test
    @DisplayName("RF06 - evento novo e registrado (201)")
    void eventoNovo() throws Exception {
        mvc.perform(post("/api/eventos-iot").contentType(MediaType.APPLICATION_JSON)
                        .content(evento(UUID.randomUUID().toString(), "PRESENCA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("registrado"));
    }

    @Test
    @DisplayName("RF06 - o mesmo eventId entregue de novo e descartado (200 duplicado)")
    void eventoDuplicado() throws Exception {
        String id = UUID.randomUUID().toString();
        mvc.perform(post("/api/eventos-iot").contentType(MediaType.APPLICATION_JSON).content(evento(id, "CONCLUSAO_ATIVIDADE")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/eventos-iot").contentType(MediaType.APPLICATION_JSON).content(evento(id, "CONCLUSAO_ATIVIDADE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("duplicado"));
    }

    @Test
    @DisplayName("RF06 - evento sem eventId e recusado (400)")
    void eventoInvalido() throws Exception {
        mvc.perform(post("/api/eventos-iot").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dispositivoId\":\"catraca-1\"}"))
                .andExpect(status().isBadRequest());
    }
}
