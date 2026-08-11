package com.rutadelivery.rutadelivery_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recuperaciones_contrasena",
        indexes = {
                @Index(name = "idx_recuperacion_repartidor", columnList = "repartidor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecuperacionContrasena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_hash", nullable = false, length = 100)
    private String codigoHash;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;

    @Column(nullable = false)
    private Integer intentos = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;

    @PrePersist
    public void prepararRegistro() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (usado == null) {
            usado = false;
        }

        if (intentos == null) {
            intentos = 0;
        }
    }

    public boolean estaExpirado() {
        return fechaExpiracion == null ||
                LocalDateTime.now().isAfter(fechaExpiracion);
    }
}