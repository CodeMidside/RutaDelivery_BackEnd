package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarCodigoRecuperacionRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo,

        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "El código debe contener 6 números")
        String codigo
) {
}