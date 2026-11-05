package org.example.grupo_7_praticaatdd.ranking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.grupo_7_praticaatdd.ranking.application.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@Tag(name = "Ranking")
public class RankingRestController {

    private final RankingService service;

    public RankingRestController(RankingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Ranking dos alunos por cursos concluidos com aproveitamento")
    public List<RankingItemDTO> listar() {
        return service.listar();
    }
}
