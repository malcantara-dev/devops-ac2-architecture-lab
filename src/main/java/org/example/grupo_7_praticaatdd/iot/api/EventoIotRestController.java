package org.example.grupo_7_praticaatdd.iot.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.grupo_7_praticaatdd.iot.application.EventoIotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/eventos-iot")
@Tag(name = "Eventos IoT")
public class EventoIotRestController {

    private final EventoIotService service;

    public EventoIotRestController(EventoIotService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Receber evento IoT do gateway (idempotente pelo eventId)")
    public ResponseEntity<Map<String, String>> receber(@Valid @RequestBody EventoIotDTO evento) {
        boolean novo = service.registrar(evento);
        return ResponseEntity.status(novo ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Map.of("eventId", evento.eventId(), "status", novo ? "registrado" : "duplicado"));
    }
}
