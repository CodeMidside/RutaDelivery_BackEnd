package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.NotificacionResponse;
import com.rutadelivery.rutadelivery_backend.entity.Notificacion;
import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import com.rutadelivery.rutadelivery_backend.repository.NotificacionRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository
            notificacionRepository;

    private final RepartidorRepository
            repartidorRepository;

    private final SeguridadUsuarioService
            seguridadUsuarioService;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            RepartidorRepository repartidorRepository,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.notificacionRepository =
                notificacionRepository;

        this.repartidorRepository =
                repartidorRepository;

        this.seguridadUsuarioService =
                seguridadUsuarioService;
    }

    /*
     * Este método se usa internamente desde otros
     * services (EntregaService, IncidenciaEntregaService).
     *
     * No hacemos aquí la validación del JWT porque
     * la operación de negocio que origina la
     * notificación ya valida al propietario.
     */
    @Transactional
    public NotificacionResponse crear(
            Long repartidorId,
            String titulo,
            String mensaje,
            Notificacion.Tipo tipo,
            Long entregaId
    ) {
        Repartidor repartidor =
                repartidorRepository
                        .findById(
                                repartidorId
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el repartidor con id "
                                                + repartidorId
                                )
                        );

        Notificacion notificacion =
                new Notificacion();

        notificacion.setTitulo(
                titulo
        );

        notificacion.setMensaje(
                mensaje
        );

        notificacion.setTipo(
                tipo
        );

        notificacion.setLeida(
                false
        );

        notificacion.setEntregaId(
                entregaId
        );

        notificacion.setRepartidor(
                repartidor
        );

        return convertir(
                notificacionRepository
                        .save(
                                notificacion
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        return notificacionRepository
                .findByRepartidorIdOrderByFechaCreacionDesc(
                        repartidorId
                )
                .stream()
                .map(
                        this::convertir
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public Long contarNoLeidas(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        return notificacionRepository
                .countByRepartidorIdAndLeidaFalse(
                        repartidorId
                );
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(
            Long id
    ) {
        Notificacion notificacion =
                notificacionRepository
                        .findById(
                                id
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la notificación con id "
                                                + id
                                )
                        );

        validarPropietario(
                notificacion
        );

        notificacion.setLeida(
                true
        );

        return convertir(
                notificacionRepository
                        .save(
                                notificacion
                        )
        );
    }

    @Transactional
    public void marcarTodasComoLeidas(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        List<Notificacion> noLeidas =
                notificacionRepository
                        .findByRepartidorIdAndLeidaFalseOrderByFechaCreacionDesc(
                                repartidorId
                        );

        for (
                Notificacion notificacion
                : noLeidas
        ) {
            notificacion.setLeida(
                    true
            );
        }

        notificacionRepository
                .saveAll(
                        noLeidas
                );
    }

    private void validarPropietario(
            Notificacion notificacion
    ) {
        if (
                notificacion.getRepartidor() == null ||
                notificacion
                        .getRepartidor()
                        .getId() == null
        ) {
            throw new OperacionNoPermitidaException(
                    "La notificación no tiene un repartidor asociado."
            );
        }

        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        notificacion
                                .getRepartidor()
                                .getId()
                );
    }

    private void validarRepartidor(
            Long repartidorId
    ) {
        if (
                !repartidorRepository
                        .existsById(
                                repartidorId
                        )
        ) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el repartidor con id "
                            + repartidorId
            );
        }
    }

    private NotificacionResponse convertir(
            Notificacion notificacion
    ) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getTipo()
                        .name(),
                notificacion.getLeida(),
                notificacion.getFechaCreacion(),
                notificacion.getEntregaId(),
                notificacion.getRepartidor()
                        .getId()
        );
    }
}