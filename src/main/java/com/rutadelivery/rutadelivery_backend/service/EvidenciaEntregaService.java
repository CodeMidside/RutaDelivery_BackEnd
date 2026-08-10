package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.EvidenciaEntregaResponse;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.entity.EvidenciaEntrega;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.EvidenciaEntregaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class EvidenciaEntregaService {

    private final EvidenciaEntregaRepository evidenciaRepository;
    private final EntregaRepository entregaRepository;
    private final Path directorioArchivos;
    private final SeguridadUsuarioService seguridadUsuarioService;

    public EvidenciaEntregaService(
            EvidenciaEntregaRepository evidenciaRepository,
            EntregaRepository entregaRepository,
            @Value("${rutadelivery.archivos.directorio}")
            String directorio,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.evidenciaRepository = evidenciaRepository;
        this.entregaRepository = entregaRepository;

        this.directorioArchivos =
                Paths.get(directorio)
                        .toAbsolutePath()
                        .normalize();

        this.seguridadUsuarioService =
                seguridadUsuarioService;

        crearDirectorio();
    }

    @Transactional
    public EvidenciaEntregaResponse registrar(
            Long entregaId,
            String nombreReceptor,
            MultipartFile archivo
    ) {
        Entrega entrega =
                entregaRepository
                        .findById(entregaId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la entrega con id " +
                                                entregaId
                                )
                        );

        validarPropietarioEntrega(
                entrega
        );

        validarNombreReceptor(
                nombreReceptor
        );

        validarArchivo(
                archivo
        );

        EvidenciaEntrega evidencia =
                evidenciaRepository
                        .findByEntregaId(entregaId)
                        .orElseGet(
                                EvidenciaEntrega::new
                        );

        if (
                evidencia.getRutaArchivo() != null &&
                !evidencia.getRutaArchivo().isBlank()
        ) {
            eliminarArchivoAnterior(
                    evidencia.getRutaArchivo()
            );
        }

        String extension =
                obtenerExtension(
                        archivo.getOriginalFilename()
                );

        String nombreGuardado =
                UUID.randomUUID() +
                        extension;

        Path destino =
                directorioArchivos
                        .resolve(nombreGuardado)
                        .normalize();

        if (
                !destino.startsWith(
                        directorioArchivos
                )
        ) {
            throw new ArchivoNoValidoException(
                    "La ruta del archivo no es válida."
            );
        }

        try {
            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new ArchivoNoValidoException(
                    "No se pudo guardar la fotografía."
            );
        }

        evidencia.setNombreReceptor(
                nombreReceptor.trim()
        );

        evidencia.setNombreArchivo(
                nombreGuardado
        );

        evidencia.setRutaArchivo(
                destino.toString()
        );

        evidencia.setTipoContenido(
                archivo.getContentType()
        );

        evidencia.setEntrega(
                entrega
        );

        EvidenciaEntrega guardada =
                evidenciaRepository.save(
                        evidencia
                );

        entrega.setEstado(
                Entrega.Estado.ENTREGADO
        );

        entregaRepository.save(
                entrega
        );

        return convertirAResponse(
                guardada
        );
    }

    @Transactional(readOnly = true)
    public EvidenciaEntregaResponse obtenerPorEntrega(
            Long entregaId
    ) {
        EvidenciaEntrega evidencia =
                evidenciaRepository
                        .findByEntregaId(entregaId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "La entrega todavía no tiene evidencia."
                                )
                        );

        validarPropietarioEntrega(
                evidencia.getEntrega()
        );

        return convertirAResponse(
                evidencia
        );
    }

    /*
     * Esta validación se usa también desde el controller
     * antes de servir físicamente una fotografía.
     */
    @Transactional(readOnly = true)
    public void validarAccesoArchivo(
            String nombreArchivo
    ) {
        EvidenciaEntrega evidencia =
                evidenciaRepository
                        .findAll()
                        .stream()
                        .filter(item ->
                                nombreArchivo.equals(
                                        item.getNombreArchivo()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la evidencia solicitada."
                                )
                        );

        validarPropietarioEntrega(
                evidencia.getEntrega()
        );
    }

    private void validarPropietarioEntrega(
            Entrega entrega
    ) {
        if (
                entrega.getRepartidor() == null ||
                entrega.getRepartidor()
                        .getId() == null
        ) {
            throw new OperacionNoPermitidaException(
                    "La entrega no tiene un repartidor asociado."
            );
        }

        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        entrega.getRepartidor()
                                .getId()
                );
    }

    private void validarNombreReceptor(
            String nombreReceptor
    ) {
        if (
                nombreReceptor == null ||
                nombreReceptor.isBlank()
        ) {
            throw new ArchivoNoValidoException(
                    "Debes escribir el nombre de quien recibió el pedido."
            );
        }

        if (
                nombreReceptor.trim().length()
                        > 120
        ) {
            throw new ArchivoNoValidoException(
                    "El nombre del receptor es demasiado largo."
            );
        }
    }

    private void validarArchivo(
            MultipartFile archivo
    ) {
        if (
                archivo == null ||
                archivo.isEmpty()
        ) {
            throw new ArchivoNoValidoException(
                    "Debes adjuntar una fotografía."
            );
        }

        String tipo =
                archivo.getContentType();

        if (
                tipo == null ||
                !tipo.toLowerCase(
                        Locale.ROOT
                ).startsWith("image/")
        ) {
            throw new ArchivoNoValidoException(
                    "El archivo debe ser una imagen."
            );
        }

        long maximoBytes =
                10L * 1024L * 1024L;

        if (
                archivo.getSize() >
                        maximoBytes
        ) {
            throw new ArchivoNoValidoException(
                    "La fotografía no puede superar los 10 MB."
            );
        }
    }

    private String obtenerExtension(
            String nombreOriginal
    ) {
        if (
                nombreOriginal == null ||
                !nombreOriginal.contains(".")
        ) {
            return ".jpg";
        }

        String extension =
                nombreOriginal.substring(
                        nombreOriginal.lastIndexOf(".")
                );

        if (extension.length() > 10) {
            return ".jpg";
        }

        return extension.toLowerCase(
                Locale.ROOT
        );
    }

    private void crearDirectorio() {
        try {
            Files.createDirectories(
                    directorioArchivos
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo crear el directorio de evidencias.",
                    exception
            );
        }
    }

    private void eliminarArchivoAnterior(
            String rutaArchivo
    ) {
        try {
            Files.deleteIfExists(
                    Paths.get(rutaArchivo)
            );
        } catch (IOException exception) {
            System.err.println(
                    "No se pudo eliminar la evidencia anterior: " +
                            exception.getMessage()
            );
        }
    }

    private EvidenciaEntregaResponse convertirAResponse(
            EvidenciaEntrega evidencia
    ) {
        return new EvidenciaEntregaResponse(
                evidencia.getId(),
                evidencia.getEntrega()
                        .getId(),
                evidencia.getNombreReceptor(),
                evidencia.getNombreArchivo(),
                "/api/evidencias/archivos/" +
                        evidencia.getNombreArchivo(),
                evidencia.getTipoContenido(),
                evidencia.getFechaRegistro(),
                evidencia.getEntrega()
                        .getEstado()
                        .name()
        );
    }
}