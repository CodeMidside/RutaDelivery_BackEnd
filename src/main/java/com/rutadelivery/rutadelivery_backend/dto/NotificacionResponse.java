package com.rutadelivery.rutadelivery_backend.dto;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        String titulo,
        String mensaje,
        String tipo,
        Boolean leida,
        LocalDateTime fechaCreacion,
        Long entregaId,
        Long repartidorId
) {
}