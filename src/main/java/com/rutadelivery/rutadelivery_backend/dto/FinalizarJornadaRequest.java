package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FinalizarJornadaRequest(

        @NotNull(message = "La distancia es obligatoria")
        @PositiveOrZero(message = "La distancia no puede ser negativa")
        Double distanciaKm,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 0, message = "La duración no puede ser negativa")
        Integer duracionMinutos

) {
}