package com.activa.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a un monitor encargado de impartir y dirigir actividades en el sistema[cite: 1].
 * Se mapea contra la tabla relacional "monitor" de la base de datos[cite: 1].
 */
@Entity
@Table(name = "monitor")
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String especialidad;

    /**
     * Relación uno a muchos (One-to-Many) bidireccional con las actividades que dirige el monitor[cite: 1].
     * Mapeada por el atributo 'monitor' en la entidad Actividad.
     */
    @OneToMany(mappedBy = "monitor", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Actividad> actividades = new ArrayList<>();

    /**
     * Constructor por defecto vacío requerido por la especificación JPA.
     */
    public Monitor() {}

    /**
     * Constructor con parámetros para inicializar un nuevo monitor.
     * 
     * @param nombre nombre completo del monitor[cite: 1].
     * @param especialidad área de especialización profesional del monitor[cite: 1].
     */
    public Monitor(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public List<Actividad> getActividades() { return actividades; }
    public void setActividades(List<Actividad> actividades) { this.actividades = actividades; }
}