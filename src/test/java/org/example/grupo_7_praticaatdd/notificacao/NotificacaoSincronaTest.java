package org.example.grupo_7_praticaatdd.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.grupo_7_praticaatdd.notificacao.NotificacaoTestSupport.criarMatricula;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Desafio 4, modo sync: worker fora do ar (porta 1, conexao recusada) derruba a conclusao.
@SpringBootTest(properties = {"notificacao.modo=sync", "notificacao.worker-url=http://localhost:1"})
@AutoConfigureMockMvc
class NotificacaoSincronaTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Worker fora do ar no modo sync: 503 e a matricula continua EM_ANDAMENTO (rollback)")
    void workerForaDoArDesfazConclusao() throws Exception {
        long matricula = criarMatricula(mvc, "Quezia");

        mvc.perform(put("/api/matriculas/" + matricula + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":9.0}"))
                .andExpect(status().isServiceUnavailable());

        mvc.perform(get("/api/matriculas/usuario/" + NotificacaoTestSupport.ultimoUsuario))
                .andExpect(jsonPath("$[?(@.id == " + matricula + ")].status", hasItem("EM_ANDAMENTO")));
    }
}
