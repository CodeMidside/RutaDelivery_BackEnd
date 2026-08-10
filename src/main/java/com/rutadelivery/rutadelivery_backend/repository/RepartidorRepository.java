package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepartidorRepository
        extends JpaRepository<Repartidor, Long> {

    Optional<Repartidor> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}