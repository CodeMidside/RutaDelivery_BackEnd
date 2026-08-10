package com.rutadelivery.rutadelivery_backend.dto;

import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntregaRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 120)
        String cliente,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 9, max = 20)
        String telefono,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255)
        String direccion,

        @Size(max = 255)
        String referencia,

        @NotNull(message = "La prioridad es obligatoria")
        Entrega.Prioridad prioridad,

        @NotNull(message = "La latitud es obligatoria")
        Double latitude,

        @NotNull(message = "La longitud es obligatoria")
        Double longitude,

        @NotNull(message = "El repartidor es obligatorio")
        Long repartidorId
) {
}