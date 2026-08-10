package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.ResumenRepartidorResponse;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadisticasService {

    private final EntregaRepository
            entregaRepository;

    private final RepartidorRepository
            repartidorRepository;

    private final SeguridadUsuarioService
            seguridadUsuarioService;

    public EstadisticasService(
            EntregaRepository entregaRepository,
            RepartidorRepository repartidorRepository,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.entregaRepository =
                entregaRepository;

        this.repartidorRepository =
                repartidorRepository;

        this.seguridadUsuarioService =
                seguridadUsuarioService;
    }

    @Transactional(readOnly = true)
    public ResumenRepartidorResponse obtenerResumen(
            Long repartidorId
    ) {
        /*
         * El repartidor solicitado en la URL
         * debe coincidir con el repartidorId
         * firmado dentro del JWT.
         */
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

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

        List<Entrega> entregas =
                entregaRepository
                        .findByRepartidorIdOrderByOrdenRutaAsc(
                                repartidorId
                        );

        int pendientes = 0;
        int enCamino = 0;
        int entregadas = 0;
        int noEntregadas = 0;

        int prioridadBaja = 0;
        int prioridadNormal = 0;
        int prioridadAlta = 0;
        int prioridadUrgente = 0;

        for (
                Entrega entrega
                : entregas
        ) {

            switch (
                    entrega.getEstado()
            ) {
                case PENDIENTE ->
                        pendientes++;

                case EN_CAMINO ->
                        enCamino++;

                case ENTREGADO ->
                        entregadas++;

                case NO_ENTREGADO ->
                        noEntregadas++;
            }

            switch (
                    entrega.getPrioridad()
            ) {
                case BAJA ->
                        prioridadBaja++;

                case NORMAL ->
                        prioridadNormal++;

                case ALTA ->
                        prioridadAlta++;

                case URGENTE ->
                        prioridadUrgente++;
            }
        }

        int totalFinalizadas =
                entregadas +
                noEntregadas;

        double porcentajeExito =
                totalFinalizadas == 0
                        ? 0.0
                        : (
                                entregadas
                                        * 100.0
                                        / totalFinalizadas
                        );

        porcentajeExito =
                Math.round(
                        porcentajeExito
                                * 100.0
                ) / 100.0;

        return new ResumenRepartidorResponse(
                repartidorId,
                entregas.size(),
                pendientes,
                enCamino,
                entregadas,
                noEntregadas,
                prioridadBaja,
                prioridadNormal,
                prioridadAlta,
                prioridadUrgente,
                porcentajeExito
        );
    }
}