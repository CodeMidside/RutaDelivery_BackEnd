package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.FinalizarJornadaRequest;
import com.rutadelivery.rutadelivery_backend.dto.ResumenJornadaResponse;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.entity.IncidenciaEntrega;
import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import com.rutadelivery.rutadelivery_backend.entity.ResumenJornada;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.IncidenciaEntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import com.rutadelivery.rutadelivery_backend.repository.ResumenJornadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumenJornadaService {

    private final ResumenJornadaRepository
            resumenJornadaRepository;

    private final EntregaRepository
            entregaRepository;

    private final IncidenciaEntregaRepository
            incidenciaRepository;

    private final RepartidorRepository
            repartidorRepository;

    private final SeguridadUsuarioService
            seguridadUsuarioService;

    public ResumenJornadaService(
            ResumenJornadaRepository resumenJornadaRepository,
            EntregaRepository entregaRepository,
            IncidenciaEntregaRepository incidenciaRepository,
            RepartidorRepository repartidorRepository,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.resumenJornadaRepository =
                resumenJornadaRepository;

        this.entregaRepository =
                entregaRepository;

        this.incidenciaRepository =
                incidenciaRepository;

        this.repartidorRepository =
                repartidorRepository;

        this.seguridadUsuarioService =
                seguridadUsuarioService;
    }

    @Transactional
    public ResumenJornadaResponse finalizar(
            Long repartidorId,
            FinalizarJornadaRequest request
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        Repartidor repartidor =
                obtenerRepartidor(
                        repartidorId
                );

        LocalDate hoy =
                LocalDate.now();

        if (
                resumenJornadaRepository
                        .findByRepartidorIdAndFechaJornada(
                                repartidorId,
                                hoy
                        )
                        .isPresent()
        ) {
            throw new OperacionNoPermitidaException(
                    "La jornada de hoy ya fue finalizada."
            );
        }

        LocalDateTime inicio =
                hoy.atStartOfDay();

        LocalDateTime fin =
                inicio.plusDays(1);

        List<Entrega> entregasHoy =
                entregaRepository
                        .findByRepartidorIdAndFechaRegistroGreaterThanEqualAndFechaRegistroLessThanOrderByOrdenRutaAsc(
                                repartidorId,
                                inicio,
                                fin
                        );

        if (entregasHoy.isEmpty()) {
            throw new OperacionNoPermitidaException(
                    "No existen entregas registradas hoy para finalizar la jornada."
            );
        }

        long pendientes =
                entregasHoy.stream()
                        .filter(entrega ->
                                entrega.getEstado()
                                        == Entrega.Estado.PENDIENTE
                                        ||
                                entrega.getEstado()
                                        == Entrega.Estado.EN_CAMINO
                        )
                        .count();

        if (pendientes > 0) {
            throw new OperacionNoPermitidaException(
                    "No puedes finalizar la jornada porque todavía existen "
                            + pendientes
                            + " entrega(s) pendiente(s)."
            );
        }

        int entregadas =
                (int) entregasHoy.stream()
                        .filter(entrega ->
                                entrega.getEstado()
                                        == Entrega.Estado.ENTREGADO
                        )
                        .count();

        int noEntregadas =
                (int) entregasHoy.stream()
                        .filter(entrega ->
                                entrega.getEstado()
                                        == Entrega.Estado.NO_ENTREGADO
                        )
                        .count();

        List<IncidenciaEntrega> incidencias =
                incidenciaRepository
                        .findByEntregaRepartidorIdOrderByFechaRegistroDesc(
                                repartidorId
                        )
                        .stream()
                        .filter(incidencia -> {
                            LocalDateTime fechaEntrega =
                                    incidencia
                                            .getEntrega()
                                            .getFechaRegistro();

                            return fechaEntrega != null
                                    &&
                                    !fechaEntrega.isBefore(
                                            inicio
                                    )
                                    &&
                                    fechaEntrega.isBefore(
                                            fin
                                    );
                        })
                        .toList();

        int incidenciasPendientes =
                (int) incidencias.stream()
                        .filter(incidencia ->
                                incidencia.getEstadoIncidencia()
                                        == IncidenciaEntrega
                                                .EstadoIncidencia
                                                .PENDIENTE
                        )
                        .count();

        int incidenciasResueltas =
                (int) incidencias.stream()
                        .filter(incidencia ->
                                incidencia.getEstadoIncidencia()
                                        == IncidenciaEntrega
                                                .EstadoIncidencia
                                                .RESUELTA
                        )
                        .count();

        ResumenJornada resumen =
                new ResumenJornada();

        resumen.setFechaJornada(
                hoy
        );

        resumen.setFechaFinalizacion(
                LocalDateTime.now()
        );

        resumen.setTotalEntregas(
                entregasHoy.size()
        );

        resumen.setEntregadas(
                entregadas
        );

        resumen.setNoEntregadas(
                noEntregadas
        );

        resumen.setIncidenciasPendientes(
                incidenciasPendientes
        );

        resumen.setIncidenciasResueltas(
                incidenciasResueltas
        );

        resumen.setDistanciaKm(
                request.distanciaKm()
        );

        resumen.setDuracionMinutos(
                request.duracionMinutos()
        );

        resumen.setRepartidor(
                repartidor
        );

        return convertir(
                resumenJornadaRepository
                        .save(
                                resumen
                        )
        );
    }

    @Transactional(readOnly = true)
    public boolean estaFinalizadaHoy(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        obtenerRepartidor(
                repartidorId
        );

        return resumenJornadaRepository
                .existsByRepartidorIdAndFechaJornada(
                        repartidorId,
                        LocalDate.now()
                );
    }

    @Transactional(readOnly = true)
    public ResumenJornadaResponse obtenerHoy(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        obtenerRepartidor(
                repartidorId
        );

        ResumenJornada resumen =
                resumenJornadaRepository
                        .findByRepartidorIdAndFechaJornada(
                                repartidorId,
                                LocalDate.now()
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "La jornada de hoy todavía no ha sido finalizada."
                                )
                        );

        return convertir(
                resumen
        );
    }

    @Transactional(readOnly = true)
    public List<ResumenJornadaResponse> listarHistorial(
            Long repartidorId
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        obtenerRepartidor(
                repartidorId
        );

        return resumenJornadaRepository
                .findByRepartidorIdOrderByFechaJornadaDesc(
                        repartidorId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    private Repartidor obtenerRepartidor(
            Long repartidorId
    ) {
        return repartidorRepository
                .findById(
                        repartidorId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró el repartidor con id "
                                        + repartidorId
                        )
                );
    }

    private ResumenJornadaResponse convertir(
            ResumenJornada resumen
    ) {
        return new ResumenJornadaResponse(
                resumen.getId(),
                resumen.getRepartidor()
                        .getId(),
                resumen.getFechaJornada(),
                resumen.getFechaFinalizacion(),
                resumen.getTotalEntregas(),
                resumen.getEntregadas(),
                resumen.getNoEntregadas(),
                resumen.getIncidenciasPendientes(),
                resumen.getIncidenciasResueltas(),
                resumen.getDistanciaKm(),
                resumen.getDuracionMinutos()
        );
    }
}