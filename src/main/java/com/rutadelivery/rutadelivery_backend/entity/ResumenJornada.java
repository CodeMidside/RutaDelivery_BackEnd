package com.rutadelivery.rutadelivery_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "resumenes_jornada",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jornada_repartidor_fecha",
                        columnNames = {
                                "repartidor_id",
                                "fecha_jornada"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "fecha_jornada",
            nullable = false
    )
    private LocalDate fechaJornada;

    @Column(
            name = "fecha_finalizacion",
            nullable = false
    )
    private LocalDateTime fechaFinalizacion;

    @Column(
            name = "total_entregas",
            nullable = false
    )
    private Integer totalEntregas;

    @Column(
            name = "entregadas",
            nullable = false
    )
    private Integer entregadas;

    @Column(
            name = "no_entregadas",
            nullable = false
    )
    private Integer noEntregadas;

    @Column(
            name = "incidencias_pendientes",
            nullable = false
    )
    private Integer incidenciasPendientes;

    @Column(
            name = "incidencias_resueltas",
            nullable = false
    )
    private Integer incidenciasResueltas;

    @Column(
            name = "distancia_km",
            nullable = false
    )
    private Double distanciaKm;

    @Column(
            name = "duracion_minutos",
            nullable = false
    )
    private Integer duracionMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "repartidor_id",
            nullable = false
    )
    private Repartidor repartidor;

    @PrePersist
    public void prepararRegistro() {
        if (fechaJornada == null) {
            fechaJornada =
                    LocalDate.now();
        }

        if (fechaFinalizacion == null) {
            fechaFinalizacion =
                    LocalDateTime.now();
        }
    }
}