package org.example.grupo_7_praticaatdd.recomendacao.api;

import java.util.List;

// origem = IA quando veio do ia-service; FALLBACK quando o Core respondeu sozinho.
public record RecomendacaoDTO(Long usuarioId, String origem, List<String> cursos, String motivo) {
}
