package com.activa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una sesión o actividad organizada en el sistema (talleres, rutas, visitas)[cite: 1].
 * Se mapea contra la tabla relacional "actividad" de la base de datos[cite: 1].
 */
@Entity
@Table(name = "actividad")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String localidad;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "plazas_totales", nullable = false)
    private int plazasTotales;

    @Column(name = "plazas_disponibles", nullable = false)
    private int plazasDisponibles;

    /**
     * Relación muchos a uno (Many-to-One) con el monitor que dirige la actividad[cite: 1].
     * Es obligatoria (not null) y utiliza carga perezosa (LAZY) por optimización de rendimiento.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    /**
     * Relación uno a muchos (One-to-Many) con las reservas asociadas a esta actividad[cite: 1].
     * Mapeada por el atributo 'actividad' en la entidad Reserva.
     */
    @OneToMany(mappedBy = "actividad")
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor por defecto vacío requerido por la especificación JPA.
     */
    public Actividad() {}

    /**
     * Constructor con parámetros para inicializar una nueva actividad.
     * Al crear la actividad, las plazas disponibles se inicializan automáticamente con el valor de las plazas totales[cite: 1].
     * 
     * @       nombre de la actividad[cite: 1].
     * @param localidad localidad donde se realiza[cite: 1].
     * @param fechaHora fecha y hora de la sesión[cite: 1].
     * @param plazasTotales aforo máximo permitido[cite: 1].
     * @param monitor monitor responsable de impartirla[cite: 1].
     */
    public Actividad(String nombre, String localidad, LocalDateTime fechaHora, int plazasTotales, Monitor monitor) {
        this.nombre = nombre;
        this.localidad = localidad;
        this.fechaHora = fechaHora;
        this.plazasTotales = plazasTotales;
        this.plazasDisponibles = plazasTotales; // Coinciden al inicio según los requisitos[cite: 1]
        this.monitor = monitor;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public int getPlazasTotales() { return plazasTotales; }
    public void setPlazasTotales(int plazasTotales) { this.plazasTotales = plazasTotales; }

    public int getPlazasDisponibles() { return plazasDisponibles; }
    public void setPlazasDisponibles(int plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }

    public Monitor getMonitor() { return monitor; }
    public void setMonitor(Monitor monitor) { this.monitor = monitor; }

    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
}