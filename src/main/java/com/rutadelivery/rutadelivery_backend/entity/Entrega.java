package com.rutadelivery.rutadelivery_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String cliente;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(length = 255)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estado estado = Estado.PENDIENTE;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private Integer ordenRuta;

    /*
     * Por ahora lo dejamos sin nullable = false.
     *
     * Esto es importante porque ya tienes entregas
     * antiguas en MySQL que no tienen fecha.
     */
    @Column(
            name = "fecha_registro"
            
    )
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "repartidor_id",
            nullable = false
    )
    private Repartidor repartidor;

    /*
     * Se ejecuta automáticamente justo antes
     * de insertar una nueva entrega en MySQL.
     */
    @PrePersist
    public void asignarFechaRegistro() {

        if (fechaRegistro == null) {
            fechaRegistro =
                    LocalDateTime.now();
        }
    }

    public enum Prioridad {
        BAJA,
        NORMAL,
        ALTA,
        URGENTE
    }

    public enum Estado {
        PENDIENTE,
        EN_CAMINO,
        ENTREGADO,
        NO_ENTREGADO
    }
}