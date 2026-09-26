package tecleros.sysgepolidep.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolService rolService;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        // Validar que la solicitud exista
        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de login no puede ser nula."
            );
        }

        // Validar nombre de usuario
        if (request.getNombreUsuario() == null ||
                request.getNombreUsuario().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        // Validar contraseña
        if (request.getPassword() == null ||
                request.getPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository
                .findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario o contraseña incorrectos."
                        )
                );

        // Comparar contraseña con BCrypt
        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        // Verificar que el usuario esté activo
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {

            throw new IllegalArgumentException(
                    "El usuario se encuentra inactivo."
            );
        }

        // Obtener roles del usuario
        List<String> roles =
                rolService.obtenerRoles(usuario.getIdUsuario());

        // Generar JWT
        String token = jwtService.generarToken(
                usuario.getIdUsuario(),
                usuario.getNombreUsuario(),
                roles
        );

        // Devolver información del usuario + roles + token
        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getNombreUsuario(),
                usuario.getEstado(),
                roles,
                token
        );
    }
}