package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.Repartidor;

public record RepartidorResponse(
        Long id,
        String nombre,
        String correo,
        Repartidor.TipoVehiculo tipoVehiculo,
        Boolean activo
) {
}