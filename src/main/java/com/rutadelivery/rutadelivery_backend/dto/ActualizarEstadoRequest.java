package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        Entrega.Estado estado
) {
}