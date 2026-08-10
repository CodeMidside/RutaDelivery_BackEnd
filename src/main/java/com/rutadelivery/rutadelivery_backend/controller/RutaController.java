package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.CalcularRecorridoRequest;
import com.rutadelivery.rutadelivery_backend.dto.OptimizarRutaRequest;
import com.rutadelivery.rutadelivery_backend.dto.RecorridoRealResponse;
import com.rutadelivery.rutadelivery_backend.dto.RutaOptimizadaResponse;
import com.rutadelivery.rutadelivery_backend.service.GoogleRoutesService;
import com.rutadelivery.rutadelivery_backend.service.RutaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;
    private final GoogleRoutesService googleRoutesService;

    public RutaController(
            RutaService rutaService,
            GoogleRoutesService googleRoutesService
    ) {
        this.rutaService =
                rutaService;

        this.googleRoutesService =
                googleRoutesService;
    }

    @PostMapping(
            "/repartidor/{repartidorId}/optimizar"
    )
    public ResponseEntity<RutaOptimizadaResponse>
    optimizarRuta(
            @PathVariable Long repartidorId,

            @Valid
            @RequestBody OptimizarRutaRequest request
    ) {
        return ResponseEntity.ok(
                rutaService
                        .optimizarRuta(
                                repartidorId,
                                request
                        )
        );
    }

    @PostMapping(
            "/repartidor/{repartidorId}/recorrido-real"
    )
    public ResponseEntity<RecorridoRealResponse>
    calcularRecorridoReal(
            @PathVariable Long repartidorId,

            @Valid
            @RequestBody CalcularRecorridoRequest request
    ) {
        return ResponseEntity.ok(
                googleRoutesService
                        .calcularRecorridoReal(
                                repartidorId,
                                request
                        )
        );
    }
}