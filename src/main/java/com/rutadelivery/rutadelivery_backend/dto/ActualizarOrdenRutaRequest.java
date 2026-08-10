package com.rutadelivery.rutadelivery_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ActualizarOrdenRutaRequest(

        @NotEmpty(message = "Debes enviar al menos una entrega")
        List<@Valid OrdenEntregaRequest> entregas

) {
}