package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.ResumenRepartidorResponse;
import com.rutadelivery.rutadelivery_backend.service.EstadisticasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService
            estadisticasService;

    public EstadisticasController(
            EstadisticasService estadisticasService
    ) {
        this.estadisticasService =
                estadisticasService;
    }

    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<ResumenRepartidorResponse>
    obtenerResumen(
            @PathVariable Long repartidorId
    ) {
        return ResponseEntity.ok(
                estadisticasService
                        .obtenerResumen(
                                repartidorId
                        )
        );
    }
}