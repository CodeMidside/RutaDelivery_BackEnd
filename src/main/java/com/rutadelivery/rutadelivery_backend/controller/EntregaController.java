package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.ActualizarEstadoRequest;
import com.rutadelivery.rutadelivery_backend.dto.ActualizarOrdenRutaRequest;
import com.rutadelivery.rutadelivery_backend.dto.EntregaRequest;
import com.rutadelivery.rutadelivery_backend.dto.EntregaResponse;
import com.rutadelivery.rutadelivery_backend.service.EntregaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(
            EntregaService entregaService
    ) {
        this.entregaService = entregaService;
    }

    @PostMapping
    public ResponseEntity<EntregaResponse> crear(
            @Valid
            @RequestBody EntregaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        entregaService.crear(
                                request
                        )
                );
    }

    /*
     * Historial completo de entregas del repartidor.
     * Se mantiene por compatibilidad con las
     * pantallas históricas del frontend.
     */
    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<List<EntregaResponse>>
    listarPorRepartidor(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                entregaService
                        .listarPorRepartidor(
                                repartidorId
                        )
        );
    }

    /*
     * Jornada actual.
     * Este es el endpoint que utiliza
     * listarEntregasDeHoyBackend() en el frontend.
     */
    @GetMapping(
            "/repartidor/{repartidorId}/hoy"
    )
    public ResponseEntity<List<EntregaResponse>>
    listarDeHoy(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                entregaService
                        .listarDeHoy(
                                repartidorId
                        )
        );
    }

    /*
     * El historial utiliza este endpoint
     * para consultar una jornada anterior.
     *
     * Ejemplo:
     * /api/entregas/repartidor/1/fecha/2026-08-09
     */
    @GetMapping(
            "/repartidor/{repartidorId}/fecha/{fecha}"
    )
    public ResponseEntity<List<EntregaResponse>>
    listarPorFecha(
            @PathVariable Long repartidorId,
            @PathVariable LocalDate fecha
    ) {
        return ResponseEntity.ok(
                entregaService
                        .listarPorFecha(
                                repartidorId,
                                fecha
                        )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaResponse>
    obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                entregaService
                        .obtenerPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntregaResponse>
    actualizar(
            @PathVariable Long id,
            @Valid
            @RequestBody EntregaRequest request
    ) {
        return ResponseEntity.ok(
                entregaService
                        .actualizar(
                                id,
                                request
                        )
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntregaResponse>
    actualizarEstado(
            @PathVariable Long id,
            @Valid
            @RequestBody ActualizarEstadoRequest request
    ) {
        return ResponseEntity.ok(
                entregaService
                        .actualizarEstado(
                                id,
                                request
                        )
        );
    }

    @PutMapping(
            "/repartidor/{repartidorId}/orden"
    )
    public ResponseEntity<List<EntregaResponse>>
    actualizarOrdenRuta(
            @PathVariable Long repartidorId,
            @Valid
            @RequestBody ActualizarOrdenRutaRequest request
    ) {
        return ResponseEntity.ok(
                entregaService
                        .actualizarOrdenRuta(
                                repartidorId,
                                request
                        )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        entregaService.eliminar(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}