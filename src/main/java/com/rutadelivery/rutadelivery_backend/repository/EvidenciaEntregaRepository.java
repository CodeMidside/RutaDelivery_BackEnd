package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.EvidenciaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvidenciaEntregaRepository
        extends JpaRepository<EvidenciaEntrega, Long> {

    Optional<EvidenciaEntrega> findByEntregaId(
            Long entregaId
    );

    boolean existsByEntregaId(
            Long entregaId
    );
}