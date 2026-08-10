package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.IncidenciaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidenciaEntregaRepository
        extends JpaRepository<IncidenciaEntrega, Long> {

    Optional<IncidenciaEntrega>
    findByEntregaId(
            Long entregaId
    );

    boolean existsByEntregaId(
            Long entregaId
    );

    List<IncidenciaEntrega>
    findByEntregaRepartidorIdOrderByFechaRegistroDesc(
            Long repartidorId
    );
}