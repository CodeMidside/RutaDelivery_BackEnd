package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EntregaRepository
        extends JpaRepository<Entrega, Long> {

    /*
     * Se conserva porque otros módulos pueden
     * necesitar consultar todas las entregas
     * históricas del repartidor.
     */
    List<Entrega>
    findByRepartidorIdOrderByOrdenRutaAsc(
            Long repartidorId
    );

    List<Entrega>
    findByRepartidorIdAndEstadoNotOrderByOrdenRutaAsc(
            Long repartidorId,
            Entrega.Estado estado
    );

    /*
     * Método principal para trabajar por jornada.
     *
     * Permite obtener únicamente las entregas
     * registradas dentro de un día específico.
     */
    List<Entrega>
    findByRepartidorIdAndFechaRegistroGreaterThanEqualAndFechaRegistroLessThanOrderByOrdenRutaAsc(
            Long repartidorId,
            LocalDateTime inicio,
            LocalDateTime fin
    );
}