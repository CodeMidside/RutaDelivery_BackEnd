package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.ActualizarEstadoRequest;
import com.rutadelivery.rutadelivery_backend.dto.ActualizarOrdenRutaRequest;
import com.rutadelivery.rutadelivery_backend.dto.EntregaRequest;
import com.rutadelivery.rutadelivery_backend.dto.EntregaResponse;
import com.rutadelivery.rutadelivery_backend.dto.OrdenEntregaRequest;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.entity.Notificacion;
import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.EvidenciaEntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.IncidenciaEntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final RepartidorRepository repartidorRepository;
    private final EvidenciaEntregaRepository evidenciaEntregaRepository;
    private final IncidenciaEntregaRepository incidenciaEntregaRepository;
    private final NotificacionService notificacionService;
    private final SeguridadUsuarioService seguridadUsuarioService;

    public EntregaService(
            EntregaRepository entregaRepository,
            RepartidorRepository repartidorRepository,
            EvidenciaEntregaRepository evidenciaEntregaRepository,
            IncidenciaEntregaRepository incidenciaEntregaRepository,
            NotificacionService notificacionService,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.entregaRepository = entregaRepository;
        this.repartidorRepository = repartidorRepository;
        this.evidenciaEntregaRepository = evidenciaEntregaRepository;
        this.incidenciaEntregaRepository = incidenciaEntregaRepository;
        this.notificacionService = notificacionService;
        this.seguridadUsuarioService = seguridadUsuarioService;
    }

    @Transactional
    public EntregaResponse crear(
            EntregaRequest request
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        request.repartidorId()
                );

        Repartidor repartidor =
                repartidorRepository
                        .findById(request.repartidorId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el repartidor"
                                )
                        );

        Entrega entrega = new Entrega();

        entrega.setCliente(
                request.cliente().trim()
        );

        entrega.setTelefono(
                request.telefono().trim()
        );

        entrega.setDireccion(
                request.direccion().trim()
        );

        entrega.setReferencia(
                request.referencia() == null
                        ? ""
                        : request.referencia().trim()
        );

        entrega.setPrioridad(
                request.prioridad()
        );

        entrega.setEstado(
                Entrega.Estado.PENDIENTE
        );

        entrega.setLatitude(
                request.latitude()
        );

        entrega.setLongitude(
                request.longitude()
        );

        entrega.setRepartidor(
                repartidor
        );

        List<Entrega> entregasActuales =
                obtenerEntregasDelDia(
                        repartidor.getId(),
                        LocalDate.now()
                );

        entrega.setOrdenRuta(
                entregasActuales.size() + 1
        );

        Entrega guardada =
                entregaRepository.save(
                        entrega
                );

        notificacionService.crear(
                repartidor.getId(),
                "Nueva entrega registrada",
                "Se registró la entrega #" + guardada.getId()
                        + " para " + guardada.getCliente() + ".",
                Notificacion.Tipo.NUEVA_ENTREGA,
                guardada.getId()
        );

        return convertirAResponse(
                guardada
        );
    }

    @Transactional(readOnly = true)
    public List<EntregaResponse> listarPorRepartidor(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        /*
         * Historial completo.
         * Se conserva este comportamiento porque
         * varias pantallas históricas del frontend
         * utilizan este endpoint.
         */
        return entregaRepository
                .findByRepartidorIdOrderByOrdenRutaAsc(
                        repartidorId
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntregaResponse> listarDeHoy(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        return obtenerEntregasDelDia(
                repartidorId,
                LocalDate.now()
        )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntregaResponse> listarPorFecha(
            Long repartidorId,
            LocalDate fecha
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        return obtenerEntregasDelDia(
                repartidorId,
                fecha
        )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EntregaResponse obtenerPorId(
            Long id
    ) {
        Entrega entrega =
                buscarEntidad(id);

        validarPropietario(
                entrega
        );

        return convertirAResponse(
                entrega
        );
    }

    @Transactional
    public EntregaResponse actualizar(
            Long id,
            EntregaRequest request
    ) {
        Entrega entrega =
                buscarEntidad(id);

        validarPropietario(
                entrega
        );

        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        request.repartidorId()
                );

        Repartidor repartidor =
                repartidorRepository
                        .findById(request.repartidorId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el repartidor"
                                )
                        );

        entrega.setCliente(
                request.cliente().trim()
        );

        entrega.setTelefono(
                request.telefono().trim()
        );

        entrega.setDireccion(
                request.direccion().trim()
        );

        entrega.setReferencia(
                request.referencia() == null
                        ? ""
                        : request.referencia().trim()
        );

        entrega.setPrioridad(
                request.prioridad()
        );

        entrega.setLatitude(
                request.latitude()
        );

        entrega.setLongitude(
                request.longitude()
        );

        entrega.setRepartidor(
                repartidor
        );

        Entrega actualizada =
                entregaRepository.save(
                        entrega
                );

        return convertirAResponse(
                actualizada
        );
    }

    @Transactional
    public EntregaResponse actualizarEstado(
            Long id,
            ActualizarEstadoRequest request
    ) {
        Entrega entrega =
                buscarEntidad(id);

        validarPropietario(
                entrega
        );

        Entrega.Estado estadoAnterior =
                entrega.getEstado();

        entrega.setEstado(
                request.estado()
        );

        Entrega actualizada =
                entregaRepository.save(
                        entrega
                );

        if (estadoAnterior != request.estado()) {
            notificacionService.crear(
                    actualizada.getRepartidor().getId(),
                    "Estado de entrega actualizado",
                    "La entrega #" + actualizada.getId()
                            + " cambió de " + estadoAnterior.name()
                            + " a " + actualizada.getEstado().name() + ".",
                    Notificacion.Tipo.CAMBIO_ESTADO,
                    actualizada.getId()
            );
        }

        return convertirAResponse(
                actualizada
        );
    }

    @Transactional
    public List<EntregaResponse> actualizarOrdenRuta(
            Long repartidorId,
            ActualizarOrdenRutaRequest request
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(
                repartidorId
        );

        Set<Long> idsProcesados =
                new HashSet<>();

        Set<Integer> ordenesProcesados =
                new HashSet<>();

        for (
                OrdenEntregaRequest elemento
                : request.entregas()
        ) {
            if (
                    !idsProcesados.add(
                            elemento.entregaId()
                    )
            ) {
                throw new OperacionNoPermitidaException(
                        "No puedes repetir una entrega en el orden"
                );
            }

            if (
                    !ordenesProcesados.add(
                            elemento.ordenRuta()
                    )
            ) {
                throw new OperacionNoPermitidaException(
                        "No puedes repetir una posición de la ruta"
                );
            }

            Entrega entrega =
                    buscarEntidad(
                            elemento.entregaId()
                    );

            if (
                    entrega.getRepartidor() == null
                            ||
                            !entrega.getRepartidor()
                                    .getId()
                                    .equals(repartidorId)
            ) {
                throw new OperacionNoPermitidaException(
                        "La entrega "
                                + entrega.getId()
                                + " no pertenece al repartidor"
                );
            }

            entrega.setOrdenRuta(
                    elemento.ordenRuta()
            );

            entregaRepository.save(
                    entrega
            );
        }

        entregaRepository.flush();

        return obtenerEntregasDelDia(
                repartidorId,
                LocalDate.now()
        )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional
    public void eliminar(
            Long id
    ) {
        Entrega entrega =
                buscarEntidad(id);

        validarPropietario(
                entrega
        );

        boolean tieneEvidencia =
                evidenciaEntregaRepository
                        .existsByEntregaId(id);

        if (tieneEvidencia) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar esta entrega porque tiene una evidencia registrada."
            );
        }

        boolean tieneIncidencia =
                incidenciaEntregaRepository
                        .existsByEntregaId(id);

        if (tieneIncidencia) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar esta entrega porque tiene una incidencia registrada."
            );
        }

        Long repartidorId =
                entrega.getRepartidor()
                        .getId();

        entregaRepository.delete(
                entrega
        );

        entregaRepository.flush();

        LocalDate fechaEntrega =
                entrega.getFechaRegistro()
                        .toLocalDate();

        reordenarEntregasRestantes(
                repartidorId,
                fechaEntrega
        );
    }

    private void reordenarEntregasRestantes(
            Long repartidorId,
            LocalDate fecha
    ) {
        List<Entrega> entregasRestantes =
                obtenerEntregasDelDia(
                        repartidorId,
                        fecha
                );

        for (
                int indice = 0;
                indice < entregasRestantes.size();
                indice++
        ) {
            Entrega entrega =
                    entregasRestantes.get(
                            indice
                    );

            entrega.setOrdenRuta(
                    10000 + indice
            );
        }

        entregaRepository.saveAll(
                entregasRestantes
        );

        entregaRepository.flush();

        for (
                int indice = 0;
                indice < entregasRestantes.size();
                indice++
        ) {
            Entrega entrega =
                    entregasRestantes.get(
                            indice
                    );

            entrega.setOrdenRuta(
                    indice + 1
            );
        }

        entregaRepository.saveAll(
                entregasRestantes
        );

        entregaRepository.flush();
    }

    private List<Entrega> obtenerEntregasDelDia(
            Long repartidorId,
            LocalDate fecha
    ) {
        LocalDateTime inicio =
                fecha.atStartOfDay();

        LocalDateTime fin =
                inicio.plusDays(1);

        return entregaRepository
                .findByRepartidorIdAndFechaRegistroGreaterThanEqualAndFechaRegistroLessThanOrderByOrdenRutaAsc(
                        repartidorId,
                        inicio,
                        fin
                );
    }

    private void validarPropietario(
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

    private void validarRepartidor(
            Long repartidorId
    ) {
        if (
                !repartidorRepository
                        .existsById(repartidorId)
        ) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el repartidor con id "
                            + repartidorId
            );
        }
    }

    private Entrega buscarEntidad(
            Long id
    ) {
        return entregaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la entrega con id "
                                        + id
                        )
                );
    }

    private EntregaResponse convertirAResponse(
            Entrega entrega
    ) {
        return new EntregaResponse(
                entrega.getId(),
                entrega.getCliente(),
                entrega.getTelefono(),
                entrega.getDireccion(),
                entrega.getReferencia(),
                entrega.getPrioridad(),
                entrega.getEstado(),
                entrega.getLatitude(),
                entrega.getLongitude(),
                entrega.getOrdenRuta(),
                entrega.getRepartidor().getId(),
                entrega.getFechaRegistro()
        );
    }
}