package org.example.grupo_7_praticaatdd.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.concluir;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.criarCurso;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.criarUsuario;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.matricular;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// Desafio 2: a regra do Premium so funciona pela API se o estado completo da assinatura
// atravessar a persistencia. Antes da porta, o service remontava o dominio so com os creditos.
@SpringBootTest
@AutoConfigureMockMvc
class GamificacaoApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Cenario 7 via API - 12 cursos concluidos pela API viram Premium com 3 moedas")
    void devePromoverParaPremiumPelaApi() throws Exception {
        long usuario = criarUsuario(mvc, "Iris Premium");
        for (int i = 1; i <= 12; i++) {
            concluir(mvc, matricular(mvc, usuario, criarCurso(mvc, "Trilha " + i)), 8.0);
        }

        mvc.perform(get("/api/usuarios"))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].cursosConcluidosComSucesso", hasItem(12)))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].plano", hasItem("PREMIUM")))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].moedas", hasItem(3)))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].creditosCursos", hasItem(36)));
    }
}
