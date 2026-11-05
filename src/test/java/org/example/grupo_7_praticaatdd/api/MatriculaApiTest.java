package org.example.grupo_7_praticaatdd.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.concluir;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.criarCurso;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.criarUsuario;
import static org.example.grupo_7_praticaatdd.api.ApiTestSupport.matricular;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Rede de seguranca do Desafio 1: fixa o comportamento da API de matricula
// antes de reorganizar o codigo. Os mesmos testes precisam passar depois.
@SpringBootTest
@AutoConfigureMockMvc
class MatriculaApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Matricular cria a matricula em andamento")
    void deveMatricularEmAndamento() throws Exception {
        long usuario = criarUsuario(mvc, "Ana");
        long curso = criarCurso(mvc, "Docker na Pratica");

        mvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + usuario + ",\"cursoId\":" + curso + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.bonus").value(false));
    }

    @Test
    @DisplayName("Cenario 1 via API - concluir com nota 8 credita 3 cursos")
    void deveCreditarAoConcluirComNotaAlta() throws Exception {
        long usuario = criarUsuario(mvc, "Bruno");
        long matricula = matricular(mvc, usuario, criarCurso(mvc, "Spring Boot"));

        concluir(mvc, matricula, 8.0);

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].creditosCursos", hasItem(3)))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].cursosConcluidosComSucesso", hasItem(1)));
    }

    @Test
    @DisplayName("Cenario 2 via API - concluir com nota 5 nao credita")
    void naoDeveCreditarAoConcluirComNotaBaixa() throws Exception {
        long usuario = criarUsuario(mvc, "Carla");
        long matricula = matricular(mvc, usuario, criarCurso(mvc, "Kubernetes"));

        concluir(mvc, matricula, 5.0);

        mvc.perform(get("/api/usuarios"))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].creditosCursos", hasItem(0)));
    }

    @Test
    @DisplayName("Cenario 4 via API - nota parcial nao conclui o curso")
    void notaParcialMantemEmAndamento() throws Exception {
        long usuario = criarUsuario(mvc, "Diego");
        long matricula = matricular(mvc, usuario, criarCurso(mvc, "Git Avancado"));

        mvc.perform(put("/api/matriculas/" + matricula + "/nota-parcial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":9.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.notaParcial").value(9.0));
    }

    @Test
    @DisplayName("Cenarios 5 e 6 via API - bonus consome credito e sem saldo retorna 400")
    void bonusConsomeCreditoOuRecusa() throws Exception {
        long usuario = criarUsuario(mvc, "Elisa");
        long bonus = criarCurso(mvc, "Curso Bonus");
        String corpo = "{\"usuarioId\":" + usuario + ",\"cursoId\":" + bonus + "}";

        mvc.perform(post("/api/matriculas/bonus").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Creditos insuficientes"));

        concluir(mvc, matricular(mvc, usuario, criarCurso(mvc, "Jenkins")), 9.0);

        mvc.perform(post("/api/matriculas/bonus").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bonus").value(true));
        mvc.perform(get("/api/usuarios"))
                .andExpect(jsonPath("$[?(@.id == " + usuario + ")].creditosCursos", hasItem(2)));
    }
}
