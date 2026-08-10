package com.rutadelivery.rutadelivery_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResumenJornadaResponse(

        Long id,

        Long repartidorId,

        LocalDate fechaJornada,

        LocalDateTime fechaFinalizacion,

        Integer totalEntregas,

        Integer entregadas,

        Integer noEntregadas,

        Integer incidenciasPendientes,

        Integer incidenciasResueltas,

        Double distanciaKm,

        Integer duracionMinutos

) {
}