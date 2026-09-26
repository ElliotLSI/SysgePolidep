package tecleros.sysgepolidep.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String estado;
    private List<String> roles;
    private String token;
}