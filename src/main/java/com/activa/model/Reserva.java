package com.activa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva realizada por un cliente para una actividad concreta[cite: 1].
 * Se mapea contra la tabla relacional "reserva" de la base de datos[cite: 1].
 * Actúa como entidad propia con datos intermedios (fecha y número de plazas)[cite: 1].
 */
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "numero_plazas", nullable = false)
    private int numeroPlazas;

    /**
     * Relación muchos a uno (Many-to-One) unidireccional o bidireccional con la actividad reservada[cite: 1].
     * Es obligatoria y utiliza carga perezosa (LAZY).
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    /**
     * Relación muchos a uno (Many-to-One) con el cliente que efectúa la reserva[cite: 1].
     * Es obligatoria y utiliza carga perezosa (LAZY).
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Constructor por defecto vacío requerido por la especificación JPA.
     */
    public Reserva() {}

    /**
     * Constructor con parámetros para inicializar una nueva reserva.
     * 
     * @param fechaReserva fecha y hora en la que se registra la reserva[cite: 1].
     * @param numeroPlazas cantidad de plazas reservadas[cite: 1].
     * @param actividad    actividad objeto de la reserva[cite: 1].
     * @param cliente      cliente que realiza la reserva[cite: 1].
     */
    public Reserva(LocalDateTime fechaReserva, int numeroPlazas, Actividad actividad, Cliente cliente) {
        this.fechaReserva = fechaReserva;
        this.numeroPlazas = numeroPlazas;
        this.actividad = actividad;
        this.cliente = cliente;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public int getNumeroPlazas() { return numeroPlazas; }
    public void setNumeroPlazas(int numeroPlazas) { this.numeroPlazas = numeroPlazas; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}