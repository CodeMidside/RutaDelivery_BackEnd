package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.RecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecuperacionContrasenaRepository
        extends JpaRepository<RecuperacionContrasena, Long> {

    Optional<RecuperacionContrasena>
    findTopByRepartidorIdAndUsadoFalseOrderByFechaCreacionDesc(
            Long repartidorId
    );

    List<RecuperacionContrasena>
    findByRepartidorIdAndUsadoFalse(
            Long repartidorId
    );
}