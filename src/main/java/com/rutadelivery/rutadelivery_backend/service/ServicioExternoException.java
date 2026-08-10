package com.rutadelivery.rutadelivery_backend.service;

public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }

    public ServicioExternoException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}