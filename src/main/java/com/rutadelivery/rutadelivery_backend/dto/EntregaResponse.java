package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.Entrega;

import java.time.LocalDateTime;

public record EntregaResponse(

        Long id,

        String cliente,

        String telefono,

        String direccion,

        String referencia,

        Entrega.Prioridad prioridad,

        Entrega.Estado estado,

        Double latitude,

        Double longitude,

        Integer ordenRuta,

        Long repartidorId,

        LocalDateTime fechaRegistro

) {
}