package com.activa.service;

import com.activa.config.JPAUtil;
import com.activa.model.Actividad;
import com.activa.model.Cliente;
import com.activa.model.Monitor;
import com.activa.model.Reserva;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Clase de servicio encargada de implementar la lógica de negocio de la aplicación Activa[cite: 1].
 * Gestiona las transacciones, las operaciones de persistencia (CRUD) y las consultas JPQL/HQL
 * para monitores, actividades, clientes y reservas[cite: 1].
 */
public class ActivaService {

    /**
     * Registra un nuevo monitor en el sistema.
     * 
     * @param nombre nombre del monitor[cite: 1].
     * @param especialidad especialidad profesional del monitor[cite: 1].
     * @return el identificador (ID) generado automáticamente para la entidad Monitor[cite: 1].
     */
    public Long crearMonitor(String nombre, String especialidad) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Monitor monitor = new Monitor(nombre, especialidad);
            em.persist(monitor);
            tx.commit();
            return monitor.getId();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Crea una nueva actividad asociada a un monitor existente.
     * Valida previamente que el monitor exista en la base de datos[cite: 1].
     * 
     * @param nombre nombre de la actividad[cite: 1].
     * @param localidad localidad donde se realiza[cite: 1].
     * @param fechaHora fecha y hora de la sesión[cite: 1].
     * @param plazasTotales aforo máximo permitido (se asigna automáticamente a las disponibles)[cite: 1].
     * @param monitorId ID del monitor responsable[cite: 1].
     * @return el identificador (ID) de la actividad creada[cite: 1].
     */
    public Long crearActividad(String nombre, String localidad, LocalDateTime fechaHora, int plazasTotales, Long monitorId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Monitor monitor = em.find(Monitor.class, monitorId);
            if (monitor == null) {
                throw new IllegalArgumentException("No existe ningún monitor con el ID proporcionado (" + monitorId + ").");
            }
            Actividad actividad = new Actividad(nombre, localidad, fechaHora, plazasTotales, monitor);
            em.persist(actividad);
            tx.commit();
            return actividad.getId();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Busca actividades disponibles para una localidad, día completo y número de plazas requeridas[cite: 1].
     * Utiliza una consulta JPQL parametrizada con una cláusula JOIN FETCH para optimizar la carga del monitor[cite: 1].
     * 
     * @param localidad localidad de búsqueda (tratada sin distinguir mayúsculas/minúsculas)[cite: 1].
     * @param fecha fecha del día a consultar[cite: 1].
     * @param plazasRequeridas número mínimo de plazas libres necesarias[cite: 1].
     * @return la lista de actividades que cumplen los criterios, ordenadas cronológicamente[cite: 1].
     */
    public List<Actividad> buscarActividadesDisponibles(String localidad, LocalDate fecha, int plazasRequeridas) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = fecha.atTime(LocalTime.MAX);

            String jpql = "SELECT a FROM Actividad a JOIN FETCH a.monitor m " +
                         "WHERE LOWER(a.localidad) = LOWER(:localidad) " +
                         "AND a.fechaHora >= :inicioDia AND a.fechaHora <= :finDia " +
                         "AND a.plazasDisponibles >= :plazas " +
                         "ORDER BY a.fechaHora ASC";

            TypedQuery<Actividad> query = em.createQuery(jpql, Actividad.class);
            query.setParameter("localidad", localidad.trim());
            query.setParameter("inicioDia", inicioDia);
            query.setParameter("finDia", finDia);
            query.setParameter("plazas", plazasRequeridas);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Da de alta a un nuevo cliente evitando correos electrónicos duplicados (ignorando mayúsculas y espacios extremos)[cite: 1].
     * 
     * @param nombre nombre del cliente[cite: 1].
     * @param email correo electrónico único[cite: 1].
     * @return el identificador (ID) asignado al nuevo cliente[cite: 1].
     */
    public Long crearCliente(String nombre, String email) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        String emailLimpio = email.trim().toLowerCase();

        try {
            // Verificar duplicados sin distinguir mayúsculas/minúsculas y quitando espacios[cite: 1]
            TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(c) FROM Cliente c WHERE LOWER(TRIM(c.email)) = :email", Long.class);
            countQuery.setParameter("email", emailLimpio);
            Long count = countQuery.getSingleResult();

            if (count > 0) {
                throw new IllegalArgumentException("Ya existe un cliente registrado con el correo: " + emailLimpio);
            }

            tx.begin();
            Cliente cliente = new Cliente(nombre.trim(), emailLimpio);
            em.persist(cliente);
            tx.commit();
            return cliente.getId();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Realiza la creación de una reserva de manera transaccional y atómica[cite: 1].
     * Comprueba la existencia del cliente y la actividad, valida el número de plazas disponibles,
     * descuenta el aforo de la actividad y guarda la reserva en una única transacción[cite: 1].
     * 
     * @param clienteId ID del cliente que realiza la reserva[cite: 1].
     * @param actividadId ID de la actividad a reservar[cite: 1].
     * @param plazas número de plazas solicitadas[cite: 1].
     * @return un registro DTO (ReservaResultado) con el ID de la reserva y las plazas restantes[cite: 1].
     */
    public ReservaResultado crearReserva(Long clienteId, Long actividadId, int plazas) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Cliente cliente = em.find(Cliente.class, clienteId);
            if (cliente == null) {
                throw new IllegalArgumentException("No existe el cliente con ID: " + clienteId);
            }

            Actividad actividad = em.find(Actividad.class, actividadId);
            if (actividad == null) {
                throw new IllegalArgumentException("No existe la actividad con ID: " + actividadId);
            }

            if (actividad.getPlazasDisponibles() < plazas) {
                throw new IllegalArgumentException("No hay suficientes plazas disponibles. Solicitadas: " +
                        plazas + ", Disponibles: " + actividad.getPlazasDisponibles());
            }

            // Descontar plazas y persistir reserva en la misma transacción[cite: 1]
            actividad.setPlazasDisponibles(actividad.getPlazasDisponibles() - plazas);
            Reserva reserva = new Reserva(LocalDateTime.now(), plazas, actividad, cliente);
            em.persist(reserva);

            tx.commit();
            return new ReservaResultado(reserva.getId(), actividad.getPlazasDisponibles());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cancela una reserva existente de manera transaccional y atómica[cite: 1].
     * Devuelve las plazas ocupadas a la actividad correspondiente y elimina la reserva del sistema[cite: 1].
     * 
     * @param reservaId ID de la reserva a cancelar[cite: 1].
     * @return un registro DTO (CancelacionResultado) con las plazas recuperadas y el nuevo aforo disponible[cite: 1].
     */
    public CancelacionResultado cancelarReserva(Long reservaId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Reserva reserva = em.find(Reserva.class, reservaId);
            if (reserva == null) {
                throw new IllegalArgumentException("No se encontró ninguna reserva activa con el ID: " + reservaId);
            }

            Actividad actividad = reserva.getActividad();
            int plazasRecuperadas = reserva.getNumeroPlazas();
            int nuevasPlazasDisponibles = actividad.getPlazasDisponibles() + plazasRecuperadas;

            actividad.setPlazasDisponibles(nuevasPlazasDisponibles);
            em.remove(reserva);

            tx.commit();
            return new CancelacionResultado(plazasRecuperadas, nuevasPlazasDisponibles);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Recupera el listado completo de todas las actividades registradas en el sistema[cite: 1].
     * 
     * @return lista de actividades ordenadas por fecha y hora de forma ascendente[cite: 1].
     */
    public List<Actividad> listarTodasActividades() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT a FROM Actividad a JOIN FETCH a.monitor m ORDER BY a.fechaHora ASC";
            return em.createQuery(jpql, Actividad.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * DTO auxiliar encargado de retornar el resultado de una reserva confirmada.
     */
    public record ReservaResultado(Long reservaId, int plazasRestantes) {}

    /**
     * DTO auxiliar encargado de retornar el resultado de una cancelación de reserva.
     */
    public record CancelacionResultado(int plazasRecuperadas, int plazasDisponibles) {}
}