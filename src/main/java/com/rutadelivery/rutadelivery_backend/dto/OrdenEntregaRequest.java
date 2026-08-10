package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdenEntregaRequest(

        @NotNull(message = "El ID de la entrega es obligatorio")
        Long entregaId,

        @NotNull(message = "El orden es obligatorio")
        @Positive(message = "El orden debe ser mayor que cero")
        Integer ordenRuta
) {
}