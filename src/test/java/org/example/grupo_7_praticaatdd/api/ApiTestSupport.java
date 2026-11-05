package org.example.grupo_7_praticaatdd.api;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Apoio dos testes de API: cria os dados pela propria API e devolve o id gerado.
final class ApiTestSupport {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private ApiTestSupport() {
    }

    static long criarUsuario(MockMvc mvc, String nome) throws Exception {
        return postar(mvc, "/api/usuarios", "{\"nome\":\"" + nome + "\"}");
    }

    static long criarCurso(MockMvc mvc, String titulo) throws Exception {
        return postar(mvc, "/api/cursos", "{\"titulo\":\"" + titulo + "\"}");
    }

    static long matricular(MockMvc mvc, long usuarioId, long cursoId) throws Exception {
        return postar(mvc, "/api/matriculas",
                "{\"usuarioId\":" + usuarioId + ",\"cursoId\":" + cursoId + "}");
    }

    static void concluir(MockMvc mvc, long matriculaId, double nota) throws Exception {
        mvc.perform(put("/api/matriculas/" + matriculaId + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":" + nota + "}"))
                .andExpect(status().isOk());
    }

    private static long postar(MockMvc mvc, String url, String json) throws Exception {
        MvcResult resultado = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher m = ID.matcher(resultado.getResponse().getContentAsString());
        if (!m.find()) {
            throw new IllegalStateException("resposta sem id: " + resultado.getResponse().getContentAsString());
        }
        return Long.parseLong(m.group(1));
    }
}
