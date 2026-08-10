package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(

        @NotBlank(message = "La contraseña actual es obligatoria")
        String contrasenaActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(
                min = 6,
                message = "La nueva contraseña debe tener al menos 6 caracteres"
        )
        String nuevaContrasena,

        @NotBlank(message = "Debes confirmar la nueva contraseña")
        String confirmarContrasena

) {
}