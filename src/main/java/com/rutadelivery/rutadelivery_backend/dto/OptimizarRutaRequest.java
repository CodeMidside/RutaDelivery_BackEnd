package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.NotNull;

public record OptimizarRutaRequest(

        @NotNull(message = "La latitud actual es obligatoria")
        Double latitudeActual,

        @NotNull(message = "La longitud actual es obligatoria")
        Double longitudeActual

) {
}