package teclerosmonte.sysgepolidep;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioValidatorTest {

    // Clase auxiliar que simula la lógica de validación de entrada de credenciales del usuario
    static class ValidadorUsuarioInput {
        public static boolean validarEntradaLogin(String username, String password) {
            // Regla: El usuario no puede estar vacío y la contraseña debe tener al menos 5 caracteres
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            if (password == null || password.length() < 5) {
                return false;
            }
            return true;
        }
    }

    @Test
    @DisplayName("Caso-05: Rechazar login si el campo de usuario está vacío")
    void testUsuarioVacio() {
        boolean resultado = ValidadorUsuarioInput.validarEntradaLogin("", "Admin123");
        assertFalse(resultado, "El sistema no debe permitir un nombre de usuario vacío.");
    }

    @Test
    @DisplayName("Caso-06: Rechazar login si la contraseña es menor al mínimo requerido")
    void testPasswordInvalido() {
        boolean resultado = ValidadorUsuarioInput.validarEntradaLogin("admin", "123");
        assertFalse(resultado, "La contraseña no cumple con la longitud mínima de seguridad.");
    }

    @Test
    @DisplayName("Caso-07: Permitir el ingreso si las credenciales de entrada son válidas")
    void testCredencialesCorrectas() {
        boolean resultado = ValidadorUsuarioInput.validarEntradaLogin("admin", "Admin123");
        assertTrue(resultado, "Las credenciales correctas deben ser validadas exitosamente.");
    }
}