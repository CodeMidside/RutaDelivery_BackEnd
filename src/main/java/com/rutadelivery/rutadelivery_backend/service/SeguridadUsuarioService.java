package com.rutadelivery.rutadelivery_backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class SeguridadUsuarioService {

    public Long obtenerRepartidorIdAutenticado() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {
            throw new OperacionNoPermitidaException(
                    "No existe una sesión autenticada."
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof Jwt jwt)) {
            throw new OperacionNoPermitidaException(
                    "La sesión autenticada no contiene un JWT válido."
            );
        }

        Object claim =
                jwt.getClaim(
                        "repartidorId"
                );

        if (claim == null) {
            throw new OperacionNoPermitidaException(
                    "El token no contiene el identificador del repartidor."
            );
        }

        if (claim instanceof Number numero) {
            return numero.longValue();
        }

        try {
            return Long.valueOf(
                    claim.toString()
            );
        } catch (NumberFormatException exception) {
            throw new OperacionNoPermitidaException(
                    "El identificador del repartidor en el token no es válido."
            );
        }
    }

    public void validarRepartidorAutenticado(
            Long repartidorId
    ) {
        Long autenticado =
                obtenerRepartidorIdAutenticado();

        if (
                !autenticado.equals(
                        repartidorId
                )
        ) {
            throw new OperacionNoPermitidaException(
                    "No puedes acceder a información de otro repartidor."
            );
        }
    }
}