package org.example.grupo_7_praticaatdd.notificacao;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

final class NotificacaoTestSupport {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    static long ultimoUsuario;

    private NotificacaoTestSupport() {
    }

    static long criarMatricula(MockMvc mvc, String nome) throws Exception {
        ultimoUsuario = id(mvc, "/api/usuarios", "{\"nome\":\"" + nome + "\"}");
        long curso = id(mvc, "/api/cursos", "{\"titulo\":\"Curso de " + nome + "\"}");
        return id(mvc, "/api/matriculas", "{\"usuarioId\":" + ultimoUsuario + ",\"cursoId\":" + curso + "}");
    }

    private static long id(MockMvc mvc, String url, String json) throws Exception {
        String corpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getContentAsString();
        Matcher m = ID.matcher(corpo);
        if (!m.find()) {
            throw new IllegalStateException("sem id: " + corpo);
        }
        return Long.parseLong(m.group(1));
    }
}
