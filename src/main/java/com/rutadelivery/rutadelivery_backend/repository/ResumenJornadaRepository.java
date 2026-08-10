package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.ResumenJornada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResumenJornadaRepository
        extends JpaRepository<ResumenJornada, Long> {

    Optional<ResumenJornada>
    findByRepartidorIdAndFechaJornada(
            Long repartidorId,
            LocalDate fechaJornada
    );

    boolean existsByRepartidorIdAndFechaJornada(
            Long repartidorId,
            LocalDate fechaJornada
    );

    List<ResumenJornada>
    findByRepartidorIdOrderByFechaJornadaDesc(
            Long repartidorId
    );
}