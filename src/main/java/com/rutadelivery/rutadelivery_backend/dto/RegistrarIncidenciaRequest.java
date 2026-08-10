package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.IncidenciaEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarIncidenciaRequest(

        @NotNull(
                message =
                        "El motivo de la incidencia es obligatorio"
        )
        IncidenciaEntrega.Motivo motivo,

        @Size(
                max = 500,
                message =
                        "La observación no puede superar los 500 caracteres"
        )
        String observacion

) {
}