package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.dto.EvidenciaEntregaResponse;
import com.rutadelivery.rutadelivery_backend.service.EvidenciaEntregaService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaEntregaController {

    private final EvidenciaEntregaService evidenciaService;
    private final Path directorioArchivos;

    public EvidenciaEntregaController(
            EvidenciaEntregaService evidenciaService,
            @Value("${rutadelivery.archivos.directorio}")
            String directorio
    ) {
        this.evidenciaService = evidenciaService;
        this.directorioArchivos =
                Paths.get(directorio)
                        .toAbsolutePath()
                        .normalize();
    }

    @PostMapping(
            value = "/entrega/{entregaId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EvidenciaEntregaResponse> registrar(
            @PathVariable Long entregaId,
            @RequestParam @NotBlank String nombreReceptor,
            @RequestPart("archivo") MultipartFile archivo
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
    public ResponseEntity<EvidenciaEntregaResponse> obtenerPorEntrega(
            @PathVariable Long entregaId
    ) {
        return ResponseEntity.ok(
                evidenciaService.obtenerPorEntrega(entregaId)
        );
    }

    @GetMapping("/archivos/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerArchivo(
            @PathVariable String nombreArchivo
    ) {
        evidenciaService.validarAccesoArchivo(
                nombreArchivo
        );

        try {
            Path archivo =
                    directorioArchivos
                            .resolve(nombreArchivo)
                            .normalize();

            if (!archivo.startsWith(directorioArchivos)) {
                return ResponseEntity.badRequest().build();
            }

            Resource recurso =
                    new UrlResource(archivo.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            MediaType tipoContenido =
                    MediaType.APPLICATION_OCTET_STREAM;

            try {
                String tipo =
                        Files.probeContentType(archivo);

                if (tipo != null && !tipo.isBlank()) {
                    tipoContenido =
                            MediaType.parseMediaType(tipo);
                }
            } catch (Exception ignored) {
            }

            return ResponseEntity
                    .ok()
                    .contentType(tipoContenido)
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "no-store"
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    nombreArchivo +
                                    "\""
                    )
                    .body(recurso);

        } catch (MalformedURLException exception) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}
