package com.activa.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a un cliente registrado en el sistema, 
 * capaz de realizar y gestionar reservas de actividades[cite: 1].
 * Se mapea contra la tabla relacional "cliente" de la base de datos[cite: 1].
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Relación uno a muchos (One-to-Many) con las reservas realizadas por el cliente[cite: 1].
     * Mapeada por el atributo 'cliente' en la entidad Reserva.
     */
    @OneToMany(mappedBy = "cliente")
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor por defecto vacío requerido por la especificación JPA.
     */
    public Cliente() {}

    /**
     * Constructor con parámetros para inicializar un nuevo cliente.
     * 
     * @param nombre nombre del cliente[cite: 1].
     * @param email correo electrónico único del cliente[cite: 1].
     */
    public Cliente(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
}