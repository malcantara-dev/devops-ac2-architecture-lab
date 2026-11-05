package org.example.grupo_7_praticaatdd.iot.internal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoIotRepository extends JpaRepository<EventoIotEntity, Long> {

    boolean existsByEventId(String eventId);
}
