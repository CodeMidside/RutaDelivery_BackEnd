package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.ActualizarPerfilRequest;
import com.rutadelivery.rutadelivery_backend.dto.CambiarContrasenaRequest;
import com.rutadelivery.rutadelivery_backend.dto.GoogleLoginRequest;
import com.rutadelivery.rutadelivery_backend.dto.LoginRequest;
import com.rutadelivery.rutadelivery_backend.dto.LoginResponse;
import com.rutadelivery.rutadelivery_backend.dto.RegistroRepartidorRequest;
import com.rutadelivery.rutadelivery_backend.dto.RepartidorResponse;
import com.rutadelivery.rutadelivery_backend.service.RepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RepartidorService
            repartidorService;

    public AuthController(
            RepartidorService repartidorService
    ) {
        this.repartidorService =
                repartidorService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RepartidorResponse> registrar(
            @Valid
            @RequestBody RegistroRepartidorRequest request
    ) {
        RepartidorResponse repartidor =
                repartidorService.registrar(
                        request
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        repartidor
                );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService
                        .iniciarSesion(
                                request
                        )
        );
    }

    /*
     * LOGIN CON GOOGLE
     *
     * Recibe el ID Token generado en Android,
     * lo verifica en el backend y devuelve
     * el JWT normal de RutaDelivery.
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> iniciarSesionGoogle(
            @Valid
            @RequestBody GoogleLoginRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService
                        .iniciarSesionGoogle(
                                request
                        )
        );
    }

    @GetMapping("/repartidores/{id}")
    public ResponseEntity<RepartidorResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                repartidorService
                        .obtenerPorId(
                                id
                        )
        );
    }

    @PutMapping("/repartidores/{id}/perfil")
    public ResponseEntity<RepartidorResponse> actualizarPerfil(
            @PathVariable Long id,

            @Valid
            @RequestBody ActualizarPerfilRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService
                        .actualizarPerfil(
                                id,
                                request
                        )
        );
    }

    @PatchMapping("/repartidores/{id}/contrasena")
    public ResponseEntity<Void> cambiarContrasena(
            @PathVariable Long id,

            @Valid
            @RequestBody CambiarContrasenaRequest request
    ) {
        repartidorService
                .cambiarContrasena(
                        id,
                        request
                );

        return ResponseEntity
                .noContent()
                .build();
    }
}