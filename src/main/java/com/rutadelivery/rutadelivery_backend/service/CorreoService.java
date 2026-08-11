package com.rutadelivery.rutadelivery_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String correoRemitente;

    public CorreoService(
            JavaMailSender mailSender,
            @Value("${rutadelivery.correo.remitente}")
            String correoRemitente
    ) {
        this.mailSender = mailSender;
        this.correoRemitente = correoRemitente;
    }

    public void enviarCodigoRecuperacion(
            String destinatario,
            String nombre,
            String codigo
    ) {
        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Código de recuperación - RutaDelivery");

        mensaje.setText(
                "Hola " + nombre + ",\n\n" +
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta RutaDelivery.\n\n" +
                "Tu código de verificación es:\n\n" +
                codigo +
                "\n\nEste código vence en 10 minutos.\n\n" +
                "Si tú no solicitaste este cambio, puedes ignorar este correo.\n\n" +
                "RutaDelivery"
        );

        mailSender.send(mensaje);
    }
}