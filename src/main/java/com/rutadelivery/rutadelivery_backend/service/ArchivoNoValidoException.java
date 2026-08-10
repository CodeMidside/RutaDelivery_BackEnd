package com.rutadelivery.rutadelivery_backend.service;

public class ArchivoNoValidoException
        extends RuntimeException {

    public ArchivoNoValidoException(
            String mensaje
    ) {
        super(mensaje);
    }
}