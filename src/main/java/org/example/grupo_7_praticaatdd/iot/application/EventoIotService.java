package org.example.grupo_7_praticaatdd.iot.application;

import org.example.grupo_7_praticaatdd.iot.api.EventoIotDTO;
import org.example.grupo_7_praticaatdd.iot.internal.EventoIotEntity;
import org.example.grupo_7_praticaatdd.iot.internal.EventoIotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

// QoS 1 entrega "pelo menos uma vez": duplicatas sao esperadas e descartadas pelo eventId.
@Service
public class EventoIotService {

    private final EventoIotRepository repositorio;

    public EventoIotService(EventoIotRepository repositorio) {
        this.repositorio = repositorio;
    }

    // true = evento novo; false = duplicata ignorada.
    public boolean registrar(EventoIotDTO e) {
        if (repositorio.existsByEventId(e.eventId())) {
            return false;
        }
        try {
            repositorio.save(new EventoIotEntity(e.eventId(), e.dispositivoId(), e.laboratorio(),
                    e.tipo(), e.usuarioId(), e.ocorridoEm()));
            return true;
        } catch (DataIntegrityViolationException corrida) {
            // duas entregas simultaneas do mesmo evento: a restricao unica decide
            return false;
        }
    }
}
