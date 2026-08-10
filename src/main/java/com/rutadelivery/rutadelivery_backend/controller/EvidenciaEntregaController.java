package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.EvidenciaEntregaResponse;
import com.rutadelivery.rutadelivery_backend.service.EvidenciaEntregaService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaEntregaController {

    private final EvidenciaEntregaService
            evidenciaService;

    public EvidenciaEntregaController(
            EvidenciaEntregaService evidenciaService
    ) {
        this.evidenciaService =
                evidenciaService;
    }

    @PostMapping(
            value = "/entrega/{entregaId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EvidenciaEntregaResponse>
    registrar(
            @PathVariable Long entregaId,

            @RequestParam
            @NotBlank
            String nombreReceptor,

            @RequestPart("archivo")
            MultipartFile archivo
    ) {
        return ResponseEntity.ok(
                evidenciaService.registrar(
                        entregaId,
                        nombreReceptor,
                        archivo
                )
        );
    }

    @GetMapping("/entrega/{entregaId}")
    public ResponseEntity<EvidenciaEntregaResponse>
    obtenerPorEntrega(
            @PathVariable Long entregaId
    ) {
        return ResponseEntity.ok(
                evidenciaService
                        .obtenerPorEntrega(
                                entregaId
                        )
        );
    }

    @GetMapping("/archivos/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerArchivo(
            @PathVariable String nombreArchivo
    ) {
        /*
         * Antes de entregar físicamente el archivo,
         * comprobamos que la evidencia pertenezca
         * al repartidor autenticado.
         */
        evidenciaService.validarAccesoArchivo(
                nombreArchivo
        );

        try {
            Path base =
                    Paths.get(
                            "uploads/evidencias"
                    )
                    .toAbsolutePath()
                    .normalize();

            Path archivo =
                    base
                    .resolve(nombreArchivo)
                    .normalize();

            if (
                    !archivo.startsWith(
                            base
                    )
            ) {
                return ResponseEntity
                        .badRequest()
                        .build();
            }

            Resource recurso =
                    new UrlResource(
                            archivo.toUri()
                    );

            if (!recurso.exists()) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            MediaType tipoContenido =
                    MediaType.APPLICATION_OCTET_STREAM;

            try {
                String tipo =
                        java.nio.file.Files
                                .probeContentType(
                                        archivo
                                );

                if (tipo != null) {
                    tipoContenido =
                            MediaType.parseMediaType(
                                    tipo
                            );
                }
            } catch (Exception ignored) {
            }

            return ResponseEntity
                    .ok()
                    .contentType(
                            tipoContenido
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    nombreArchivo +
                                    "\""
                    )
                    .body(recurso);

        } catch (
                MalformedURLException exception
        ) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}