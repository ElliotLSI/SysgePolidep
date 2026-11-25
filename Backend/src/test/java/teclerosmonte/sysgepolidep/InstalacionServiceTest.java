package teclerosmonte.sysgepolidep;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstalacionServiceTest {

    // Clase auxiliar interna para simular la entidad y las reglas de negocio del Polideportivo
    static class Instalacion {
        private String nombre;
        private String estado; // "DISPONIBLE", "MANTENIMIENTO", "CLAUSURADA"

        public Instalacion(String nombre, String estado) {
            this.nombre = nombre;
            this.estado = estado;
        }

        public boolean puedeReservarse() {
            return "DISPONIBLE".equals(this.estado);
        }
    }

    // Clase auxiliar para simular la regla de superposición de reservas
    static class ReservaSimulada {
        private String instalacion;
        private LocalDateTime inicio;
        private LocalDateTime fin;

        public ReservaSimulada(String instalacion, LocalDateTime inicio, LocalDateTime fin) {
            this.instalacion = instalacion;
            this.inicio = inicio;
            this.fin = fin;
        }

        public static boolean seSuperpone(List<ReservaSimulada> reservasExistentes, ReservaSimulada nueva) {
            for (ReservaSimulada r : reservasExistentes) {
                if (r.instalacion.equals(nueva.instalacion)) {
                    // Hay superposición si la nueva reserva empieza antes de que termine la existente
                    // y termina después de que empiece la existente
                    boolean seCruzan = nueva.inicio.isBefore(r.fin) && nueva.fin.isAfter(r.inicio);
                    if (seCruzan) {
                        return true; // ¡Chocan los horarios!
                    }
                }
            }
            return false;
        }
    }

    @Test
    @DisplayName("Caso-01: Validar que una instalación en MANTENIMIENTO no permita reservas")
    void testInstalacionEnMantenimientoNoPermiteReserva() {
        Instalacion cancha = new Instalacion("Cancha Principal", "MANTENIMIENTO");
        boolean resultado = cancha.puedeReservarse();
        assertFalse(resultado, "La instalación en mantenimiento no debería permitir reservas.");
    }

    @Test
    @DisplayName("Caso-02: Validar que una instalación CLAUSURADA no permita reservas")
    void testInstalacionClausuradaNoPermiteReserva() {
        Instalacion pileta = new Instalacion("Pileta Olímpica", "CLAUSURADA");
        boolean resultado = pileta.puedeReservarse();
        assertFalse(resultado, "La instalación clausurada no debería permitir reservas.");
    }

    @Test
    @DisplayName("Caso-03: Validar que una instalación DISPONIBLE permita reservas")
    void testInstalacionDisponiblePermiteReserva() {
        Instalacion sum = new Instalacion("SUM", "DISPONIBLE");
        boolean resultado = sum.puedeReservarse();
        assertTrue(resultado, "La instalación disponible debería permitir reservas.");
    }

    @Test
    @DisplayName("Caso-04: Validar el rechazo de reserva por superposición de horario en la misma cancha")
    void testEvitarReservaSuperpuesta() {
        // Simulamos una reserva ya existente en la Cancha 1 de 14:00 a 16:00
        LocalDateTime inicioExistente = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime finExistente = LocalDateTime.of(2026, 11, 10, 16, 0);

        List<ReservaSimulada> listaReservas = new ArrayList<>();
        listaReservas.add(new ReservaSimulada("Cancha 1", inicioExistente, finExistente));

        // Intentamos crear una nueva reserva que choca en el mismo horario (15:00 a 17:00)
        LocalDateTime inicioNueva = LocalDateTime.of(2026, 11, 10, 15, 0);
        LocalDateTime finNueva = LocalDateTime.of(2026, 11, 10, 17, 0);
        ReservaSimulada nuevaReserva = new ReservaSimulada("Cancha 1", inicioNueva, finNueva);

        // Verificamos si el sistema detecta la superposición
        boolean haySuperposicion = ReservaSimulada.seSuperpone(listaReservas, nuevaReserva);

        assertTrue(haySuperposicion, "El sistema debería detectar el solapamiento y rechazar la reserva.");
    }
}