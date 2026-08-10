package com.rutadelivery.rutadelivery_backend.dto;

public record ResumenRepartidorResponse(

        Long repartidorId,

        Integer totalEntregas,

        Integer pendientes,

        Integer enCamino,

        Integer entregadas,

        Integer noEntregadas,

        Integer prioridadBaja,

        Integer prioridadNormal,

        Integer prioridadAlta,

        Integer prioridadUrgente,

        Double porcentajeExito

) {
}