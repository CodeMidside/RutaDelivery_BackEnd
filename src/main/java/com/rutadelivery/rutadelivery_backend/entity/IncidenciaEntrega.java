package com.rutadelivery.rutadelivery_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias_entrega")
public class IncidenciaEntrega {

    public enum Motivo {

        CLIENTE_AUSENTE,

        DIRECCION_INCORRECTA,

        CLIENTE_RECHAZO,

        NO_RESPONDE,

        ZONA_INACCESIBLE,

        VEHICULO_AVERIADO,

        OTRO
    }

    public enum EstadoIncidencia {

        PENDIENTE,

        RESUELTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private Motivo motivo;

    @Column(
            length = 500
    )
    private String observacion;

    @Column(
            name = "fecha_registro",
            nullable = false
    )
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_incidencia",
            nullable = false,
            length = 20
    )
    private EstadoIncidencia estadoIncidencia;

    @Column(
            name = "fecha_resolucion"
    )
    private LocalDateTime fechaResolucion;

    @Column(
            name = "accion_resolucion",
            length = 200
    )
    private String accionResolucion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entrega_id",
            nullable = false,
            unique = true
    )
    private Entrega entrega;

    public IncidenciaEntrega() {
    }

    @PrePersist
    public void prepararRegistro() {
        if (fechaRegistro == null) {
            fechaRegistro =
                    LocalDateTime.now();
        }

        if (estadoIncidencia == null) {
            estadoIncidencia =
                    EstadoIncidencia.PENDIENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public Motivo getMotivo() {
        return motivo;
    }

    public void setMotivo(
            Motivo motivo
    ) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(
            String observacion
    ) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro =
                fechaRegistro;
    }

    public EstadoIncidencia getEstadoIncidencia() {
        return estadoIncidencia;
    }

    public void setEstadoIncidencia(
            EstadoIncidencia estadoIncidencia
    ) {
        this.estadoIncidencia =
                estadoIncidencia;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(
            LocalDateTime fechaResolucion
    ) {
        this.fechaResolucion =
                fechaResolucion;
    }

    public String getAccionResolucion() {
        return accionResolucion;
    }

    public void setAccionResolucion(
            String accionResolucion
    ) {
        this.accionResolucion =
                accionResolucion;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(
            Entrega entrega
    ) {
        this.entrega = entrega;
    }
}