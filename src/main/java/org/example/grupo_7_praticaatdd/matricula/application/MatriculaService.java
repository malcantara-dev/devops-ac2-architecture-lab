package org.example.grupo_7_praticaatdd.matricula.application;

import org.example.grupo_7_praticaatdd.domain.Curso;
import org.example.grupo_7_praticaatdd.domain.Matricula;
import org.example.grupo_7_praticaatdd.domain.Usuario;
import org.example.grupo_7_praticaatdd.entity.CursoEntity;
import org.example.grupo_7_praticaatdd.entity.UsuarioEntity;
import org.example.grupo_7_praticaatdd.gamificacao.application.GamificacaoService;
import org.example.grupo_7_praticaatdd.matricula.api.MatriculaResponseDTO;
import org.example.grupo_7_praticaatdd.matricula.internal.persistence.MatriculaEntity;
import org.example.grupo_7_praticaatdd.matricula.internal.persistence.MatriculaRepository;
import org.example.grupo_7_praticaatdd.notificacao.api.CursoConcluidoEvento;
import org.example.grupo_7_praticaatdd.repository.CursoRepository;
import org.example.grupo_7_praticaatdd.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Modulo matricula, camada application: orquestra a persistencia e delega as regras
// de negocio para o pacote domain, que foi escrito via TDD na AC1.
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final GamificacaoService gamificacao;
    private final ApplicationEventPublisher eventos;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            UsuarioRepository usuarioRepository,
                            CursoRepository cursoRepository,
                            GamificacaoService gamificacao,
                            ApplicationEventPublisher eventos) {
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.gamificacao = gamificacao;
        this.eventos = eventos;
    }

    public List<MatriculaResponseDTO> listarPorUsuario(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public MatriculaResponseDTO matricular(Long usuarioId, Long cursoId) {
        MatriculaEntity matricula = new MatriculaEntity(buscarUsuario(usuarioId), buscarCurso(cursoId), false);
        return toDTO(matriculaRepository.save(matricula));
    }

    // Cenarios 5 e 6: o desbloqueio so acontece se a assinatura tiver credito.
    @Transactional
    public MatriculaResponseDTO desbloquear(Long usuarioId, Long cursoId) {
        UsuarioEntity usuario = buscarUsuario(usuarioId);
        gamificacao.consumirCredito(usuarioId);

        MatriculaEntity matricula = new MatriculaEntity(usuario, buscarCurso(cursoId), true);
        return toDTO(matriculaRepository.save(matricula));
    }

    // Cenarios 1 e 2: concluir com nota >= 7 rende 3 creditos, abaixo disso nao rende nada.
    @Transactional
    public MatriculaResponseDTO concluir(Long matriculaId, Double nota) {
        MatriculaEntity entity = buscarMatricula(matriculaId);

        Matricula matricula = matriculaDoDominio(entity);
        matricula.concluir(nota);

        entity.setStatus(matricula.getStatus());
        entity.setNotaFinal(matricula.getNotaFinal());

        if (matricula.concluidoComAproveitamento()) {
            gamificacao.registrarConclusaoComSucesso(entity.getUsuario().getId());
        }
        MatriculaEntity salva = matriculaRepository.save(entity);

        // Desafio 4: a notificacao e eventual. O evento so sai depois do commit (modo async).
        eventos.publishEvent(new CursoConcluidoEvento(salva.getId(), salva.getUsuario().getId(),
                salva.getCurso().getTitulo(), nota, matricula.concluidoComAproveitamento()));
        return toDTO(salva);
    }

    // Cenario 4: a nota parcial e apenas registrada, o curso segue em andamento.
    @Transactional
    public MatriculaResponseDTO registrarNotaParcial(Long matriculaId, Double nota) {
        MatriculaEntity entity = buscarMatricula(matriculaId);

        Matricula matricula = matriculaDoDominio(entity);
        matricula.registrarNotaParcial(nota);

        entity.setNotaParcial(matricula.getNotaParcial());
        return toDTO(matriculaRepository.save(entity));
    }

    private Matricula matriculaDoDominio(MatriculaEntity entity) {
        Usuario usuario = new Usuario(entity.getUsuario().getNome());
        Curso curso = new Curso(entity.getCurso().getTitulo());
        return new Matricula(usuario, curso, entity.isBonus());
    }

    private UsuarioEntity buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    private CursoEntity buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado"));
    }

    private MatriculaEntity buscarMatricula(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Matricula nao encontrada"));
    }

    private MatriculaResponseDTO toDTO(MatriculaEntity entity) {
        return new MatriculaResponseDTO(
                entity.getId(),
                entity.getUsuario().getId(),
                entity.getCurso().getTitulo(),
                entity.getStatus().name(),
                entity.getNotaFinal(),
                entity.getNotaParcial(),
                entity.isBonus()
        );
    }
}
