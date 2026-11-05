package org.example.grupo_7_praticaatdd.notificacao.api;

// Contrato do evento publicado pelo modulo matricula quando um curso e concluido.
public record CursoConcluidoEvento(Long matriculaId, Long usuarioId, String cursoTitulo,
                                   double nota, boolean aproveitamento) {

    public String toJson() {
        return "{\"tipo\":\"CursoConcluido\",\"matriculaId\":" + matriculaId
                + ",\"usuarioId\":" + usuarioId
                + ",\"cursoTitulo\":\"" + cursoTitulo.replace("\\", "\\\\").replace("\"", "\\\"")
                + "\",\"nota\":" + nota
                + ",\"aproveitamento\":" + aproveitamento + "}";
    }
}
