package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 100,
                message = "El nombre no puede superar los 100 caracteres"
        )
        String nombre,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        Repartidor.TipoVehiculo tipoVehiculo

) {
}