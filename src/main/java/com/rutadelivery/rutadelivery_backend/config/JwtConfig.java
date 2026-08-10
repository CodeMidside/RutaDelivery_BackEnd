package com.rutadelivery.rutadelivery_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    /*
     * En desarrollo puede venir de application.properties.
     * En producción debe venir de una variable de entorno.
     *
     * Debe tener al menos 32 caracteres para HS256.
     */
    @Value("${rutadelivery.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecretKey jwtSecretKey() {

        if (
                jwtSecret == null ||
                jwtSecret.length() < 32
        ) {
            throw new IllegalStateException(
                    "rutadelivery.jwt.secret debe tener al menos 32 caracteres."
            );
        }

        return new SecretKeySpec(
                jwtSecret.getBytes(
                        StandardCharsets.UTF_8
                ),
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(
                        jwtSecretKey
                )
                .algorithm(
                        MacAlgorithm.HS256
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey
    ) {
        return NimbusJwtDecoder
                .withSecretKey(
                        jwtSecretKey
                )
                .macAlgorithm(
                        MacAlgorithm.HS256
                )
                .build();
    }
}