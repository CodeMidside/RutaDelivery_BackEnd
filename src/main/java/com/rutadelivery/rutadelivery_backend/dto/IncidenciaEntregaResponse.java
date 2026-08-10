package com.rutadelivery.rutadelivery_backend.dto;

import java.time.LocalDateTime;

public record IncidenciaEntregaResponse(

        Long id,

        Long entregaId,

        String motivo,

        String observacion,

        LocalDateTime fechaRegistro,

        String estadoEntrega,

        String estadoIncidencia,

        LocalDateTime fechaResolucion,

        String accionResolucion

) {
}