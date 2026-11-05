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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RankingApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("RF01 - GET /api/ranking traz o aluno com a pontuacao calculada")
    void deveListarRankingComPontuacao() throws Exception {
        long usuario = criarUsuario(mvc, "Rita Ranking");
        concluir(mvc, matricular(mvc, usuario, criarCurso(mvc, "Observabilidade")), 9.5);

        mvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[?(@.usuarioId == " + usuario + ")].cursosConcluidos", hasItem(1)))
                .andExpect(jsonPath("$[?(@.usuarioId == " + usuario + ")].pontuacao", hasItem(103)));
    }
}
