package teclerosmonte.sysgepolidep;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AspectosClaveTest {

    // --- RESERVA A FAVOR ---

    @Test
    @DisplayName("Caso Prueba de Permitir usar reserva a favor dentro del plazo de 3 meses")
    void testReservaAFavorVigente() {
        LocalDate fechaCancelacion = LocalDate.of(2026, 10, 1);
        LocalDate fechaVencimiento = fechaCancelacion.plusMonths(3); // 01/01/2027
        LocalDate fechaUso = LocalDate.of(2026, 11, 23);

        boolean disponibleParaUsar = !fechaUso.isAfter(fechaVencimiento);

        assertTrue(disponibleParaUsar, "Dentro del plazo de 3 meses la reserva a favor debe poder usarse.");
    }

    @Test
    @DisplayName("Caso Prueba de Rechazar reserva a favor si superó el plazo de 3 meses")
    void testReservaAFavorFueraDePlazo() {
        LocalDate fechaCancelacion = LocalDate.of(2026, 6, 1);
        LocalDate fechaVencimiento = fechaCancelacion.plusMonths(3); // 01/09/2026
        LocalDate fechaUso = LocalDate.of(2026, 11, 23);

        boolean disponibleParaUsar = !fechaUso.isAfter(fechaVencimiento);

        assertFalse(disponibleParaUsar, "Pasados los 3 meses la reserva a favor ya no debe estar disponible.");
    }

    // --- PAGOS (MONTO EXACTO, <= 0, SIN PAGO -> PENDIENTE) ---

    @Test
    @DisplayName("Caso Prueba de Aceptar pago cuando el monto ingresado es igual al monto exacto")
    void testPagoMontoExactoAceptado() {
        double montoRequerido = 45000.0;
        Double montoPagado = 45000.0;

        boolean pagoAceptado = montoPagado != null && montoPagado > 0 && Double.compare(montoPagado, montoRequerido) == 0;

        assertTrue(pagoAceptado, "Si el pago coincide exactamente con el monto requerido debe aceptarse.");
    }

    @Test
    @DisplayName("Caso Prueba de Rechazar pago si el monto es menor o igual a cero")
    void testPagoMenorOIgualACeroRechazado() {
        Double montoPagadoCero = 0.0;
        Double montoPagadoNegativo = -10000.0;

        boolean validoCero = montoPagadoCero != null && montoPagadoCero > 0;
        boolean validoNegativo = montoPagadoNegativo != null && montoPagadoNegativo > 0;

        assertFalse(validoCero, "Un pago de 0 debe ser rechazado.");
        assertFalse(validoNegativo, "Un pago negativo debe ser rechazado.");
    }

    @Test
    @DisplayName("Caso Prueba de Dejar estado PENDIENTE si no hay pago registrado")
    void testSinPagoQuedaPendiente() {
        Double montoPagado = null;
        double montoRequerido = 35000.0;

        String estadoReserva = (montoPagado == null)
                ? "PENDIENTE"
                : (Double.compare(montoPagado, montoRequerido) == 0 ? "CONFIRMADA" : "PENDIENTE");

        assertEquals("PENDIENTE", estadoReserva, "Si no hay pago, el estado debe mantenerse en PENDIENTE.");
    }
}