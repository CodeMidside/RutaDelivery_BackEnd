package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${rutadelivery.jwt.duracion-horas:8}")
    private long duracionHoras;

    public JwtService(
            JwtEncoder jwtEncoder
    ) {
        this.jwtEncoder =
                jwtEncoder;
    }

    public String generarToken(
            Repartidor repartidor
    ) {
        Instant ahora =
                Instant.now();

        Instant expiracion =
                ahora.plus(
                        duracionHoras,
                        ChronoUnit.HOURS
                );

        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(
                                "rutadelivery-backend"
                        )

                        .issuedAt(
                                ahora
                        )

                        .expiresAt(
                                expiracion
                        )

                        /*
                         * El subject identifica de forma
                         * estable al repartidor autenticado.
                         */
                        .subject(
                                repartidor
                                        .getCorreo()
                        )

                        .claim(
                                "repartidorId",
                                repartidor
                                        .getId()
                        )

                        .claim(
                                "nombre",
                                repartidor
                                        .getNombre()
                        )

                        .claim(
                                "tipoVehiculo",
                                repartidor
                                        .getTipoVehiculo()
                                        .name()
                        )

                        .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters
                                .from(
                                        claims
                                )
                )
                .getTokenValue();
    }
}