package com.gestortareas.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tarea")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdTarea")
    private Integer idTarea;

    @Column(name = "Titulo", nullable = false)
    private String titulo;

    @Column(name = "Descripcion")
    private String descripcion;

    @Convert(converter = Prioridad.Conv.class)
    @Column(name = "Prioridad", nullable = false)
    private Prioridad prioridad = Prioridad.Media;

    @Convert(converter = EstadoTarea.Conv.class)
    @Column(name = "Estado", nullable = false)
    private EstadoTarea estado = EstadoTarea.Pendiente;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "FechaLimite")
    private LocalDate fechaLimite;

    @Column(name = "FechaCompletada")
    private LocalDate fechaCompletada;

    @Column(name = "FechaEliminacion")
    private LocalDateTime fechaEliminacion;

    @JsonProperty("estaEnPapelera")
    public boolean estaEnPapelera() { return fechaEliminacion != null; }

    public Integer getIdTarea() { return idTarea; }
    public void setIdTarea(Integer idTarea) { this.idTarea = idTarea; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public LocalDate getFechaCompletada() { return fechaCompletada; }
    public void setFechaCompletada(LocalDate fechaCompletada) { this.fechaCompletada = fechaCompletada; }

    public LocalDateTime getFechaEliminacion() { return fechaEliminacion; }
    public void setFechaEliminacion(LocalDateTime fechaEliminacion) { this.fechaEliminacion = fechaEliminacion; }
}