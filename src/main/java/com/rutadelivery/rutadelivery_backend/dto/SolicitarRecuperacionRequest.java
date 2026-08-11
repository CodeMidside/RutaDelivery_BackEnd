package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarRecuperacionRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo
) {
}