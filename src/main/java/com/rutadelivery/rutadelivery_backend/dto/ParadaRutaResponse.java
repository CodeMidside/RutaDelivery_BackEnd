package com.rutadelivery.rutadelivery_backend.dto;

public record ParadaRutaResponse(

        Integer numeroParada,

        Double distanciaDesdeAnteriorKm,

        EntregaResponse entrega

) {
}