package com.rutadelivery.rutadelivery_backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenService {

    private final String webClientId;

    public GoogleTokenService(
            @Value("${rutadelivery.google.web-client-id}")
            String webClientId
    ) {
        this.webClientId =
                webClientId;
    }

    public GoogleUsuario verificar(
            String idTokenString
    ) {
        if (
                idTokenString == null ||
                idTokenString.isBlank()
        ) {
            throw new OperacionNoPermitidaException(
                    "Google no devolvió un token válido."
            );
        }

        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport
                                    .newTrustedTransport(),
                            GsonFactory
                                    .getDefaultInstance()
                    )
                            /*
                             * MUY IMPORTANTE:
                             * El audience debe ser el WEB CLIENT ID,
                             * no el Android Client ID.
                             */
                            .setAudience(
                                    Collections.singletonList(
                                            webClientId
                                    )
                            )
                            .build();

            GoogleIdToken idToken =
                    verifier.verify(
                            idTokenString
                    );

            if (idToken == null) {
                throw new OperacionNoPermitidaException(
                        "El inicio de sesión con Google no es válido."
                );
            }

            GoogleIdToken.Payload payload =
                    idToken.getPayload();

            Boolean correoVerificado =
                    payload.getEmailVerified();

            if (
                    !Boolean.TRUE.equals(
                            correoVerificado
                    )
            ) {
                throw new OperacionNoPermitidaException(
                        "Google no confirmó el correo electrónico."
                );
            }

            String correo =
                    payload.getEmail();

            if (
                    correo == null ||
                    correo.isBlank()
            ) {
                throw new OperacionNoPermitidaException(
                        "Google no devolvió un correo electrónico."
                );
            }

            String nombre =
                    (String) payload.get(
                            "name"
                    );

            if (
                    nombre == null ||
                    nombre.isBlank()
            ) {
                nombre =
                        correo.substring(
                                0,
                                correo.indexOf("@")
                        );
            }

            return new GoogleUsuario(
                    payload.getSubject(),
                    correo
                            .trim()
                            .toLowerCase(),
                    nombre.trim()
            );

        } catch (
                GeneralSecurityException |
                IOException exception
        ) {
            throw new OperacionNoPermitidaException(
                    "No se pudo validar la cuenta de Google."
            );
        }
    }

    public record GoogleUsuario(
            String googleId,
            String correo,
            String nombre
    ) {
    }
}