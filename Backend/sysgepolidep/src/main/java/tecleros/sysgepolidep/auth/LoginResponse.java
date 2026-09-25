package tecleros.sysgepolidep.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String estado;
}