package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.FinalizarJornadaRequest;
import com.rutadelivery.rutadelivery_backend.dto.ResumenJornadaResponse;
import com.rutadelivery.rutadelivery_backend.service.ResumenJornadaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jornadas")
public class ResumenJornadaController {

    private final ResumenJornadaService
            resumenJornadaService;

    public ResumenJornadaController(
            ResumenJornadaService resumenJornadaService
    ) {
        this.resumenJornadaService =
                resumenJornadaService;
    }

    @PostMapping(
            "/repartidor/{repartidorId}/finalizar"
    )
    public ResponseEntity<ResumenJornadaResponse>
    finalizar(
            @PathVariable Long repartidorId,

            @Valid
            @RequestBody FinalizarJornadaRequest request
    ) {
        return ResponseEntity.ok(
                resumenJornadaService
                        .finalizar(
                                repartidorId,
                                request
                        )
        );
    }

    @GetMapping(
            "/repartidor/{repartidorId}/estado"
    )
    public ResponseEntity<Map<String, Boolean>>
    obtenerEstado(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "finalizada",
                        resumenJornadaService
                                .estaFinalizadaHoy(
                                        repartidorId
                                )
                )
        );
    }

    @GetMapping(
            "/repartidor/{repartidorId}/hoy"
    )
    public ResponseEntity<ResumenJornadaResponse>
    obtenerHoy(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                resumenJornadaService
                        .obtenerHoy(
                                repartidorId
                        )
        );
    }

    @GetMapping(
            "/repartidor/{repartidorId}/historial"
    )
    public ResponseEntity<List<ResumenJornadaResponse>>
    listarHistorial(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                resumenJornadaService
                        .listarHistorial(
                                repartidorId
                        )
        );
    }
}