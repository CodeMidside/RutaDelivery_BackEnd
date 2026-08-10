package com.rutadelivery.rutadelivery_backend.dto;

public record LoginResponse(

        String mensaje,

        String token,

        RepartidorResponse repartidor,

        Boolean requiereConfigInicial

) {
}