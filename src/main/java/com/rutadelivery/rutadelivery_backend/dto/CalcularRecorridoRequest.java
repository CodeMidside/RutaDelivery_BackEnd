package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.NotNull;

public record CalcularRecorridoRequest(

        @NotNull(message = "La latitud inicial es obligatoria")
        Double latitudeInicial,

        @NotNull(message = "La longitud inicial es obligatoria")
        Double longitudeInicial

) {
}