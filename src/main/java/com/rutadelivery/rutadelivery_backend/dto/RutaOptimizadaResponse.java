package com.rutadelivery.rutadelivery_backend.dto;

import java.util.List;

public record RutaOptimizadaResponse(

        String mensaje,

        Long repartidorId,

        Double latitudeInicial,

        Double longitudeInicial,

        Integer totalParadas,

        Double distanciaTotalKm,

        List<ParadaRutaResponse> paradas

) {
}