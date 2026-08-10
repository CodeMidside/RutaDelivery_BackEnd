package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.IncidenciaEntregaResponse;
import com.rutadelivery.rutadelivery_backend.dto.RegistrarIncidenciaRequest;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.entity.IncidenciaEntrega;
import com.rutadelivery.rutadelivery_backend.entity.Notificacion;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.IncidenciaEntregaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidenciaEntregaService {

    private final IncidenciaEntregaRepository incidenciaRepository;
    private final EntregaRepository entregaRepository;
    private final NotificacionService notificacionService;
    private final SeguridadUsuarioService seguridadUsuarioService;

    public IncidenciaEntregaService(
            IncidenciaEntregaRepository incidenciaRepository,
            EntregaRepository entregaRepository,
            NotificacionService notificacionService,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.incidenciaRepository = incidenciaRepository;
        this.entregaRepository = entregaRepository;
        this.notificacionService = notificacionService;
        this.seguridadUsuarioService = seguridadUsuarioService;
    }

    @Transactional
    public IncidenciaEntregaResponse registrar(
            Long entregaId,
            RegistrarIncidenciaRequest request
    ) {
        Entrega entrega =
                obtenerEntrega(entregaId);

        validarPropietarioEntrega(entrega);

        if (
                entrega.getEstado() ==
                        Entrega.Estado.ENTREGADO
        ) {
            throw new OperacionNoPermitidaException(
                    "No puedes registrar una incidencia en una entrega completada."
            );
        }

        IncidenciaEntrega incidencia =
                incidenciaRepository
                        .findByEntregaId(entregaId)
                        .orElseGet(
                                IncidenciaEntrega::new
                        );

        incidencia.setEntrega(entrega);
        incidencia.setMotivo(request.motivo());

        incidencia.setObservacion(
                request.observacion() == null
                        ? ""
                        : request.observacion()
                                .trim()
        );

        incidencia.setEstadoIncidencia(
                IncidenciaEntrega
                        .EstadoIncidencia
                        .PENDIENTE
        );

        incidencia.setFechaResolucion(null);
        incidencia.setAccionResolucion("");

        entrega.setEstado(
                Entrega.Estado.NO_ENTREGADO
        );

        entregaRepository.save(entrega);

        IncidenciaEntrega guardada =
                incidenciaRepository.save(
                        incidencia
                );

        notificacionService.crear(
                entrega
                        .getRepartidor()
                        .getId(),

                "Incidencia registrada",

                "La entrega #"
                        + entrega.getId()
                        + " de "
                        + entrega.getCliente()
                        + " fue marcada como no entregada. Motivo: "
                        + obtenerNombreMotivo(
                                guardada.getMotivo()
                        )
                        + ".",

                Notificacion.Tipo.INCIDENCIA,

                entrega.getId()
        );

        return convertirAResponse(
                guardada
        );
    }

    @Transactional(readOnly = true)
    public IncidenciaEntregaResponse obtenerPorEntrega(
            Long entregaId
    ) {
        IncidenciaEntrega incidencia =
                obtenerIncidencia(
                        entregaId
                );

        validarPropietarioEntrega(
                incidencia.getEntrega()
        );

        return convertirAResponse(
                incidencia
        );
    }

    @Transactional(readOnly = true)
    public List<IncidenciaEntregaResponse> listarPorRepartidor(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        return incidenciaRepository
                .findByEntregaRepartidorIdOrderByFechaRegistroDesc(
                        repartidorId
                )
                .stream()
                .map(
                        this::convertirAResponse
                )
                .toList();
    }

    @Transactional
    public IncidenciaEntregaResponse resolverYReactivar(
            Long entregaId
    ) {
        IncidenciaEntrega incidencia =
                obtenerIncidencia(
                        entregaId
                );

        Entrega entrega =
                incidencia.getEntrega();

        validarPropietarioEntrega(
                entrega
        );

        if (
                incidencia.getEstadoIncidencia() ==
                        IncidenciaEntrega
                                .EstadoIncidencia
                                .RESUELTA
        ) {
            throw new OperacionNoPermitidaException(
                    "La incidencia ya se encuentra resuelta."
            );
        }

        incidencia.setEstadoIncidencia(
                IncidenciaEntrega
                        .EstadoIncidencia
                        .RESUELTA
        );

        incidencia.setFechaResolucion(
                LocalDateTime.now()
        );

        incidencia.setAccionResolucion(
                "Entrega reactivada para un nuevo intento."
        );

        entrega.setEstado(
                Entrega.Estado.PENDIENTE
        );

        entrega.setOrdenRuta(
                null
        );

        entregaRepository.save(
                entrega
        );

        IncidenciaEntrega guardada =
                incidenciaRepository.save(
                        incidencia
                );

        notificacionService.crear(
                entrega
                        .getRepartidor()
                        .getId(),

                "Entrega reactivada",

                "La incidencia de la entrega #"
                        + entrega.getId()
                        + " fue resuelta. La entrega volvió a estado pendiente para un nuevo intento.",

                Notificacion.Tipo.REACTIVACION,

                entrega.getId()
        );

        return convertirAResponse(
                guardada
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

    private Entrega obtenerEntrega(
            Long entregaId
    ) {
        return entregaRepository
                .findById(entregaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la entrega con id "
                                        + entregaId
                        )
                );
    }

    private IncidenciaEntrega obtenerIncidencia(
            Long entregaId
    ) {
        return incidenciaRepository
                .findByEntregaId(entregaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La entrega no tiene una incidencia registrada."
                        )
                );
    }

    private String obtenerNombreMotivo(
            IncidenciaEntrega.Motivo motivo
    ) {
        return switch (motivo) {
            case CLIENTE_AUSENTE ->
                    "Cliente ausente";
            case DIRECCION_INCORRECTA ->
                    "Dirección incorrecta";
            case CLIENTE_RECHAZO ->
                    "Cliente rechazó el pedido";
            case NO_RESPONDE ->
                    "Cliente no responde";
            case ZONA_INACCESIBLE ->
                    "Zona inaccesible";
            case VEHICULO_AVERIADO ->
                    "Vehículo averiado";
            case OTRO ->
                    "Otro motivo";
        };
    }

    private IncidenciaEntregaResponse convertirAResponse(
            IncidenciaEntrega incidencia
    ) {
        return new IncidenciaEntregaResponse(
                incidencia.getId(),
                incidencia.getEntrega()
                        .getId(),
                incidencia.getMotivo()
                        .name(),
                incidencia.getObservacion(),
                incidencia.getFechaRegistro(),
                incidencia.getEntrega()
                        .getEstado()
                        .name(),
                incidencia.getEstadoIncidencia()
                        .name(),
                incidencia.getFechaResolucion(),
                incidencia.getAccionResolucion()
        );
    }
}