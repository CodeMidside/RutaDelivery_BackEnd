package com.rutadelivery.rutadelivery_backend.dto;

import java.time.LocalDateTime;

public record EvidenciaEntregaResponse(

        Long id,

        Long entregaId,

        String nombreReceptor,

        String nombreArchivo,

        String urlArchivo,

        String tipoContenido,

        LocalDateTime fechaRegistro,

        String estadoEntrega

) {
}