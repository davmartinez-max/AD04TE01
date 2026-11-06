package com.activa.main;

import com.activa.config.JPAUtil;
import com.activa.model.Actividad;
import com.activa.service.ActivaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal (Main) que gestiona la interfaz de usuario por consola
 * para la aplicación "ACTIVA" de gestión de actividades deportivas.
 */
public class Main {

    // Servicio que encapsula la lógica de negocio y la persistencia
    private static final ActivaService service = new ActivaService();
    
    // Scanner para la lectura de datos por consola
    private static final Scanner scanner = new Scanner(System.in);
    
    // Formateadores para manejar fechas y horas con la API moderna java.time
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Punto de entrada de la aplicación. Mantiene el bucle principal del menú
     * hasta que el usuario decide salir.
     */
    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1" -> crearMonitor();
                case "2" -> crearActividad();
                case "3" -> buscarActividadesDisponibles();
                case "4" -> crearCliente();
                case "5" -> crearReserva();
                case "6" -> cancelarReserva();
                case "7" -> listarActividades();
                case "8" -> {
                    salir = true;
                    System.out.println("Cerrando la aplicación... ¡Hasta pronto!");
                }
                default -> System.out.println("Opción no válida. Por favor, introduzca un número del 1 al 8.");
            }
            System.out.println();
        }
        // Cierre de la factoría de JPA / EntityManagerFactory al finalizar el programa
        JPAUtil.shutdown();
    }

    /**
     * Muestra por consola las opciones disponibles del menú principal.
     */
    private static void mostrarMenu() {
        System.out.println("=== ACTIVA · Gestión de actividades ===");
        System.out.println("1. Crear monitor");
        System.out.println("2. Crear actividad");
        System.out.println("3. Buscar actividades disponibles");
        System.out.println("4. Crear cliente");
        System.out.println("5. Crear reserva");
        System.out.println("6. Cancelar reserva");
        System.out.println("7. Listar actividades");
        System.out.println("8. Salir");
        System.out.print("Seleccione una opción: ");
    }

    /**
     * Solicita los datos necesarios para registrar un nuevo monitor en el sistema.
     */
    private static void crearMonitor() {
        System.out.println("\n--- 1. Crear Monitor ---");
        String nombre = pedirTextoNoVacio("Nombre del monitor: ");
        String especialidad = pedirTextoNoVacio("Especialidad del monitor: ");

        try {
            Long id = service.crearMonitor(nombre, especialidad);
            System.out.println("Monitor creado con éxito. ID asignado: " + id);
        } catch (Exception e) {
            System.out.println("Error al crear el monitor: " + e.getMessage());
        }
    }

    /**
     * Solicita los datos para dar de alta una nueva actividad deportiva vinculada a un monitor.
     */
    private static void crearActividad() {
        System.out.println("\n--- 2. Crear Actividad ---");
        String nombre = pedirTextoNoVacio("Nombre de la actividad: ");
        String localidad = pedirTextoNoVacio("Localidad: ");
        LocalDateTime fechaHora = pedirFechaHora("Fecha y hora (formato dd/MM/yyyy HH:mm): ");
        int plazas = pedirEnteroPositivo("Plazas totales (número entero positivo): ");
        Long monitorId = pedirLong("ID del monitor asignado: ");

        try {
            Long id = service.crearActividad(nombre, localidad, fechaHora, plazas, monitorId);
            System.out.println("Actividad creada con éxito. ID asignado: " + id);
        } catch (Exception e) {
            System.out.println("Error al crear la actividad: " + e.getMessage());
        }
    }

    /**
     * Permite filtrar y buscar actividades que cumplan con los criterios de localidad, fecha y plazas.
     */
    private static void buscarActividadesDisponibles() {
        System.out.println("\n--- 3. Buscar Actividades Disponibles ---");
        String localidad = pedirTextoNoVacio("Localidad a buscar: ");
        LocalDate fecha = pedirFecha("Fecha a consultar (formato dd/MM/yyyy): ");
        int plazas = pedirEnteroPositivo("Número de plazas requeridas: ");

        try {
            List<Actividad> resultados = service.buscarActividadesDisponibles(localidad, fecha, plazas);
            if (resultados.isEmpty()) {
                System.out.println("No se encontraron actividades disponibles para esos criterios.");
            } else {
                System.out.println("\nActividades encontradas:");
                for (Actividad act : resultados) {
                    System.out.printf("- ID: %d | Nombre: %s | Fecha/Hora: %s | Monitor: %s | Plazas Disponibles: %d%d%n",
                            act.getId(), act.getNombre(), act.getFechaHora().format(FORMATO_FECHA_HORA),
                            act.getMonitor().getNombre(), act.getPlazasDisponibles(), act.getPlazasTotales());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al realizar la búsqueda: " + e.getMessage());
        }
    }

    /**
     * Registra un nuevo cliente en el sistema solicitando su nombre y correo electrónico válido.
     */
    private static void crearCliente() {
        System.out.println("\n--- 4. Crear Cliente ---");
        String nombre = pedirTextoNoVacio("Nombre del cliente: ");
        String email = pedirEmailValido("Correo electrónico: ");

        try {
            Long id = service.crearCliente(nombre, email);
            System.out.println("Cliente dado de alta con éxito. ID asignado: " + id);
        } catch (Exception e) {
            System.out.println("Error al dar de alta al cliente: " + e.getMessage());
        }
    }

    /**
     * Realiza una reserva de plazas para un cliente en una actividad específica.
     */
    private static void crearReserva() {
        System.out.println("\n--- 5. Crear Reserva ---");
        Long clienteId = pedirLong("ID del cliente: ");
        Long actividadId = pedirLong("ID de la actividad: ");
        int plazas = pedirEnteroPositivo("Número de plazas a reservar: ");

        try {
            ActivaService.ReservaResultado res = service.crearReserva(clienteId, actividadId, plazas);
            System.out.printf("Reserva confirmada con éxito. ID Reserva: %d | Plazas restantes en la actividad: %d%n",
                    res.reservaId(), res.plazasRestantes());
        } catch (Exception e) {
            System.out.println("No se pudo realizar la reserva: " + e.getMessage());
        }
    }

    /**
     * Cancela una reserva existente a partir de su identificador único.
     */
    private static void cancelarReserva() {
        System.out.println("\n--- 6. Cancelar Reserva ---");
        Long reservaId = pedirLong("ID de la reserva a cancelar: ");

        try {
            ActivaService.CancelacionResultado res = service.cancelarReserva(reservaId);
            System.out.printf("Reserva %d cancelada con éxito. Plazas recuperadas: %d | Plazas disponibles actualizadas: %d%n",
                    reservaId, res.plazasRecuperadas(), res.plazasDisponibles());
        } catch (Exception e) {
            System.out.println("No se pudo cancelar la reserva: " + e.getMessage());
        }
    }

    /**
     * Muestra un listado general de todas las actividades registradas en el sistema.
     */
    private static void listarActividades() {
        System.out.println("\n--- 7. Listar Actividades ---");
        try {
            List<Actividad> actividades = service.listarTodasActividades();
            if (actividades.isEmpty()) {
                System.out.println("No existen actividades registradas en el sistema.");
            } else {
                for (Actividad act : actividades) {
                    System.out.printf("ID: %d | Nombre: %s | Localidad: %s | Fecha: %s | Monitor: %s | Plazas Totales: %d | Plazas Disponibles: %d%n",
                            act.getId(), act.getNombre(), act.getLocalidad(),
                            act.getFechaHora().format(FORMATO_FECHA_HORA), act.getMonitor().getNombre(),
                            act.getPlazasTotales(), act.getPlazasDisponibles());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al recuperar el listado de actividades: " + e.getMessage());
        }
    }

    // =========================================================================
    // Métodos auxiliares de lectura y validación robusta por consola
    // =========================================================================

    /**
     * Lee un texto por consola asegurándose de que no esté vacío.
     */
    private static String pedirTextoNoVacio(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) return texto;
            System.out.println("El campo no puede estar vacío.");
        }
    }

    /**
     * Lee y valida un correo electrónico básico que contenga '@' y un punto '.' posterior.
     */
    private static String pedirEmailValido(String mensaje) {
        while (true) {
            String email = pedirTextoNoVacio(mensaje);
            if (email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.')) {
                return email;
            }
            System.out.println("El formato de correo no es válido. Ejemplo: usuario@dominio.com");
        }
    }

    /**
     * Solicita un número entero positivo (mayor que cero) controlando errores de formato.
     */
    private static int pedirEnteroPositivo(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                int num = Integer.parseInt(scanner.nextLine().trim());
                if (num > 0) return num;
                System.out.println("Debe introducir un número entero estrictamente mayor que cero.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Introduzca un número entero válido.");
            }
        }
    }

    /**
     * Solicita un identificador de tipo Long por consola controlando errores de formato.
     */
    private static Long pedirLong(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Introduzca un identificador numérico válido.");
            }
        }
    }

    /**
     * Solicita y valida una fecha y hora bajo el formato estricto dd/MM/yyyy HH:mm.
     */
    private static LocalDateTime pedirFechaHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(entrada, FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato incorrecto. Ejemplo correcto: 15/11/2026 10:00");
            }
        }
    }

    /**
     * Solicita y valida una fecha simple bajo el formato estricto dd/MM/yyyy.
     */
    private static LocalDate pedirFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return LocalDate.parse(entrada, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato incorrecto. Ejemplo correcto: 15/11/2026");
            }
        }
    }
}