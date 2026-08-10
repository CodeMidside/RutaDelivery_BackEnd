package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.NotificacionResponse;
import com.rutadelivery.rutadelivery_backend.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(
            NotificacionService notificacionService
    ) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<List<NotificacionResponse>> listar(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                notificacionService.listar(repartidorId)
        );
    }

    @GetMapping("/repartidor/{repartidorId}/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "cantidad",
                        notificacionService.contarNoLeidas(repartidorId)
                )
        );
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionResponse> marcarComoLeida(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificacionService.marcarComoLeida(id)
        );
    }

    @PatchMapping("/repartidor/{repartidorId}/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas(
            @PathVariable Long repartidorId
    ) {
        notificacionService.marcarTodasComoLeidas(repartidorId);

        return ResponseEntity.noContent().build();
    }
}