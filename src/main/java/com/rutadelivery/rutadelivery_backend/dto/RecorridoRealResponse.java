package com.rutadelivery.rutadelivery_backend.dto;

public record RecorridoRealResponse(

        String mensaje,

        Long repartidorId,

        Integer totalParadas,

        Long distanciaMetros,

        Double distanciaKilometros,

        Long duracionSegundos,

        Integer duracionMinutos,

        String polylineCodificada

) {
}