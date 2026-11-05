package org.example.grupo_7_praticaatdd.matricula.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<MatriculaEntity, Long> {

    List<MatriculaEntity> findByUsuarioId(Long usuarioId);
}
