package org.example.grupo_7_praticaatdd.recomendacao.application;

import org.example.grupo_7_praticaatdd.dto.CursoResponseDTO;
import org.example.grupo_7_praticaatdd.matricula.api.MatriculaResponseDTO;
import org.example.grupo_7_praticaatdd.matricula.application.MatriculaService;
import org.example.grupo_7_praticaatdd.recomendacao.api.RecomendacaoDTO;
import org.example.grupo_7_praticaatdd.service.CursoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Desafio 3: o caso de uso nunca propaga a falha da IA para o aluno.
class RecomendacaoServiceTest {

    private final MotorDeRecomendacao motor = mock(MotorDeRecomendacao.class);
    private final MatriculaService matriculas = mock(MatriculaService.class);
    private final CursoService cursos = mock(CursoService.class);
    private final RecomendacaoService service = new RecomendacaoService(motor, matriculas, cursos);

    @BeforeEach
    void dados() {
        when(matriculas.listarPorUsuario(7L)).thenReturn(List.of(
                new MatriculaResponseDTO(1L, 7L, "Spring Boot", "CONCLUIDO", 9.0, null, false),
                new MatriculaResponseDTO(2L, 7L, "Git", "EM_ANDAMENTO", null, 8.0, false)));
        when(cursos.listar()).thenReturn(List.of(
                new CursoResponseDTO(1L, "Spring Boot"), new CursoResponseDTO(2L, "Git"),
                new CursoResponseDTO(3L, "Docker"), new CursoResponseDTO(4L, "Kubernetes"),
                new CursoResponseDTO(5L, "Jenkins")));
    }

    @Test
    @DisplayName("IA disponivel: origem IA e so os cursos concluidos vao como historico")
    void usaIaQuandoDisponivel() {
        when(motor.recomendar(7L, List.of("Spring Boot"), List.of("Spring Boot", "Git", "Docker", "Kubernetes", "Jenkins")))
                .thenReturn(List.of("Docker"));

        RecomendacaoDTO r = service.recomendar(7L);

        assertEquals("IA", r.origem());
        assertEquals(List.of("Docker"), r.cursos());
    }

    @Test
    @DisplayName("IA fora do ar: origem FALLBACK com cursos ainda nao concluidos")
    void degradaParaFallback() {
        when(motor.recomendar(any(), anyList(), anyList())).thenThrow(new RuntimeException("Connection refused"));

        RecomendacaoDTO r = service.recomendar(7L);

        assertEquals("FALLBACK", r.origem());
        assertEquals(List.of("Git", "Docker", "Kubernetes"), r.cursos());
        assertEquals("ia-service indisponivel: Connection refused", r.motivo());
    }
}
