package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.*;
import com.rutadelivery.rutadelivery_backend.service.RepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RepartidorService repartidorService;

    public AuthController(
            RepartidorService repartidorService
    ) {
        this.repartidorService = repartidorService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RepartidorResponse> registrar(
            @Valid
            @RequestBody RegistroRepartidorRequest request
    ) {
        RepartidorResponse repartidor =
                repartidorService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(repartidor);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.iniciarSesion(request)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> iniciarSesionGoogle(
            @Valid
            @RequestBody GoogleLoginRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.iniciarSesionGoogle(request)
        );
    }

    @PostMapping("/recuperacion/solicitar")
    public ResponseEntity<RecuperacionContrasenaResponse>
    solicitarRecuperacion(
            @Valid
            @RequestBody SolicitarRecuperacionRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.solicitarRecuperacion(request)
        );
    }

    @PostMapping("/recuperacion/verificar")
    public ResponseEntity<RecuperacionContrasenaResponse>
    verificarCodigoRecuperacion(
            @Valid
            @RequestBody VerificarCodigoRecuperacionRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.verificarCodigoRecuperacion(request)
        );
    }

    @PostMapping("/recuperacion/restablecer")
    public ResponseEntity<RecuperacionContrasenaResponse>
    restablecerContrasena(
            @Valid
            @RequestBody RestablecerContrasenaRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.restablecerContrasena(request)
        );
    }

    @GetMapping("/repartidores/{id}")
    public ResponseEntity<RepartidorResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                repartidorService.obtenerPorId(id)
        );
    }

    @PutMapping("/repartidores/{id}/perfil")
    public ResponseEntity<RepartidorResponse> actualizarPerfil(
            @PathVariable Long id,
            @Valid
            @RequestBody ActualizarPerfilRequest request
    ) {
        return ResponseEntity.ok(
                repartidorService.actualizarPerfil(
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
        repartidorService.cambiarContrasena(
                id,
                request
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
