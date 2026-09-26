package tecleros.sysgepolidep.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private String domicilio;
    private String telefono;
    private String email;
    private String nombreUsuario;
    private String pertenencia;
    private String legajo;
    private LocalDateTime fechaRegistro;
    private String estado;
}