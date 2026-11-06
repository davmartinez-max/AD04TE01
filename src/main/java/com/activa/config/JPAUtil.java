package com.activa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Clase de utilidad para la gestión de la persistencia (JPA).
 * Se encarga de inicializar y proporcionar instancias de EntityManagerFactory 
 * y EntityManager para toda la aplicación.
 */
public class JPAUtil {
    
    /** 
     * Fábrica de EntityManagers estática y única (patrón Singleton implícito) 
     * basada en la unidad de persistencia definida en el persistence.xml ("ActivaPU").
     */
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ActivaPU");

    /**
     * Proporciona un nuevo EntityManager para interactuar con la base de datos.
     * 
     * @return una instancia activa de EntityManager.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Cierra de forma segura el EntityManagerFactory al finalizar la aplicación,
     * liberando los recursos de conexión con la base de datos.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}