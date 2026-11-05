package org.example.grupo_7_praticaatdd.iot.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventoIotDTO(
        @NotBlank String eventId,
        @NotBlank String dispositivoId,
        @NotBlank String laboratorio,
        @NotBlank String tipo,
        @NotNull Long usuarioId,
        @NotBlank String ocorridoEm
) {
}
