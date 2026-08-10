package com.rutadelivery.rutadelivery_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias_entrega")
public class EvidenciaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nombre_receptor",
            nullable = false,
            length = 120
    )
    private String nombreReceptor;

    @Column(
            name = "nombre_archivo",
            nullable = false,
            length = 255
    )
    private String nombreArchivo;

    @Column(
            name = "ruta_archivo",
            nullable = false,
            length = 500
    )
    private String rutaArchivo;

    @Column(
            name = "tipo_contenido",
            length = 100
    )
    private String tipoContenido;

    @Column(
            name = "fecha_registro",
            nullable = false
    )
    private LocalDateTime fechaRegistro;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entrega_id",
            nullable = false,
            unique = true
    )
    private Entrega entrega;

    public EvidenciaEntrega() {
    }

    @PrePersist
    public void asignarFecha() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(
            String nombreReceptor
    ) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(
            String nombreArchivo
    ) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(
            String rutaArchivo
    ) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(
            String tipoContenido
    ) {
        this.tipoContenido = tipoContenido;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }
}