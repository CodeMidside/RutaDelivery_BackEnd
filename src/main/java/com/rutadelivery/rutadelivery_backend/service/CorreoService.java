package com.rutadelivery.rutadelivery_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class CorreoService {

    private final RestClient restClient;
    private final String apiKey;
    private final String remitente;

    public CorreoService(
            @Value("${rutadelivery.resend.api-key}")
            String apiKey,

            @Value("${rutadelivery.resend.remitente}")
            String remitente
    ) {
        this.apiKey = apiKey;
        this.remitente = remitente;

        this.restClient =
                RestClient.builder()
                        .baseUrl("https://api.resend.com")
                        .build();
    }

    public void enviarCodigoRecuperacion(
            String destinatario,
            String nombre,
            String codigo
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY no está configurada."
            );
        }

        if (remitente == null || remitente.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_FROM no está configurado."
            );
        }

        String texto =
                "Hola " + nombre + ",\n\n" +
                "Recibimos una solicitud para restablecer " +
                "la contraseña de tu cuenta RutaDelivery.\n\n" +
                "Tu código de verificación es:\n\n" +
                codigo +
                "\n\nEste código vence en 10 minutos.\n\n" +
                "Si tú no solicitaste este cambio, " +
                "puedes ignorar este correo.\n\n" +
                "RutaDelivery";

        Map<String, Object> cuerpo =
                Map.of(
                        "from",
                        remitente,

                        "to",
                        new String[]{
                                destinatario
                        },

                        "subject",
                        "Código de recuperación - RutaDelivery",

                        "text",
                        texto
                );

        try {
            restClient
                    .post()
                    .uri("/emails")
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException exception) {
            System.err.println(
                    "Error Resend HTTP "
                            + exception.getStatusCode()
                            + ": "
                            + exception.getResponseBodyAsString()
            );

            throw new OperacionNoPermitidaException(
                    "No se pudo enviar el código de recuperación."
            );

        } catch (Exception exception) {
            System.err.println(
                    "Error conectando con Resend: "
                            + exception.getMessage()
            );

            throw new OperacionNoPermitidaException(
                    "No se pudo enviar el código de recuperación."
            );
        }
    }
}
