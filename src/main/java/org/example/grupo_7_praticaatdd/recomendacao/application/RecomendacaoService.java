package org.example.grupo_7_praticaatdd.recomendacao.application;

import org.example.grupo_7_praticaatdd.dto.CursoResponseDTO;
import org.example.grupo_7_praticaatdd.matricula.api.MatriculaResponseDTO;
import org.example.grupo_7_praticaatdd.matricula.application.MatriculaService;
import org.example.grupo_7_praticaatdd.recomendacao.api.RecomendacaoDTO;
import org.example.grupo_7_praticaatdd.service.CursoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Caso de uso da recomendacao: tenta a IA e, se ela falhar, degrada para o fallback.
// A indisponibilidade da IA nunca vira erro para o aluno (RPC do Desafio 3).
@Service
public class RecomendacaoService {

    static final int LIMITE = 3;
    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);

    private final MotorDeRecomendacao motor;
    private final MatriculaService matriculas;
    private final CursoService cursos;

    public RecomendacaoService(MotorDeRecomendacao motor, MatriculaService matriculas, CursoService cursos) {
        this.motor = motor;
        this.matriculas = matriculas;
        this.cursos = cursos;
    }

    public RecomendacaoDTO recomendar(Long usuarioId) {
        List<String> concluidos = matriculas.listarPorUsuario(usuarioId).stream()
                .filter(m -> "CONCLUIDO".equals(m.getStatus()))
                .map(MatriculaResponseDTO::getCursoTitulo)
                .toList();
        List<String> catalogo = cursos.listar().stream().map(CursoResponseDTO::getTitulo).toList();

        try {
            return new RecomendacaoDTO(usuarioId, "IA", motor.recomendar(usuarioId, concluidos, catalogo), null);
        } catch (RuntimeException falha) {
            log.warn("ia-service indisponivel, usando fallback: {}", falha.getMessage());
            return new RecomendacaoDTO(usuarioId, "FALLBACK", fallback(concluidos, catalogo),
                    "ia-service indisponivel: " + falha.getMessage());
        }
    }

    // Degradacao aceita: cursos do catalogo que o aluno ainda nao concluiu, na ordem do catalogo.
    static List<String> fallback(List<String> concluidos, List<String> catalogo) {
        Set<String> feitos = concluidos.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return catalogo.stream().filter(c -> !feitos.contains(c.toLowerCase())).limit(LIMITE).toList();
    }
}
