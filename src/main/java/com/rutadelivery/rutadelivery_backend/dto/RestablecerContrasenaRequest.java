package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestablecerContrasenaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo,

        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "El código debe contener 6 números")
        String codigo,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String nuevaContrasena,

        @NotBlank(message = "Debes confirmar la nueva contraseña")
        String confirmarContrasena
) {
}