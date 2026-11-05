package org.example.grupo_7_praticaatdd.recomendacao.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.grupo_7_praticaatdd.recomendacao.application.RecomendacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recomendacoes")
@Tag(name = "Recomendacoes")
public class RecomendacaoRestController {

    private final RecomendacaoService service;

    public RecomendacaoRestController(RecomendacaoService service) {
        this.service = service;
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Recomendar cursos (ia-service com fallback no Core)")
    public RecomendacaoDTO recomendar(@PathVariable Long usuarioId) {
        return service.recomendar(usuarioId);
    }
}
