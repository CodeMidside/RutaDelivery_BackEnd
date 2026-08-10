package com.rutadelivery.rutadelivery_backend.repository;

import com.rutadelivery.rutadelivery_backend.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    List<Notificacion>
    findByRepartidorIdOrderByFechaCreacionDesc(Long repartidorId);

    List<Notificacion>
    findByRepartidorIdAndLeidaFalseOrderByFechaCreacionDesc(Long repartidorId);

    long countByRepartidorIdAndLeidaFalse(Long repartidorId);
}