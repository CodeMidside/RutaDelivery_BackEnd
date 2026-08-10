package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.IncidenciaEntregaResponse;
import com.rutadelivery.rutadelivery_backend.dto.RegistrarIncidenciaRequest;
import com.rutadelivery.rutadelivery_backend.service.IncidenciaEntregaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaEntregaController {

    private final IncidenciaEntregaService
            incidenciaService;

    public IncidenciaEntregaController(
            IncidenciaEntregaService incidenciaService
    ) {
        this.incidenciaService =
                incidenciaService;
    }

    @PostMapping("/entrega/{entregaId}")
    public ResponseEntity<IncidenciaEntregaResponse>
    registrar(
            @PathVariable Long entregaId,

            @Valid
            @RequestBody RegistrarIncidenciaRequest request
    ) {
        return ResponseEntity.ok(
                incidenciaService.registrar(
                        entregaId,
                        request
                )
        );
    }

    @GetMapping("/entrega/{entregaId}")
    public ResponseEntity<IncidenciaEntregaResponse>
    obtenerPorEntrega(
            @PathVariable Long entregaId
    ) {
        return ResponseEntity.ok(
                incidenciaService
                        .obtenerPorEntrega(
                                entregaId
                        )
        );
    }

    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<List<IncidenciaEntregaResponse>>
    listarPorRepartidor(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                incidenciaService
                        .listarPorRepartidor(
                                repartidorId
                        )
        );
    }

    @PatchMapping("/entrega/{entregaId}/resolver-reactivar")
    public ResponseEntity<IncidenciaEntregaResponse>
    resolverYReactivar(
            @PathVariable Long entregaId
    ) {
        return ResponseEntity.ok(
                incidenciaService
                        .resolverYReactivar(
                                entregaId
                        )
        );
    }
}