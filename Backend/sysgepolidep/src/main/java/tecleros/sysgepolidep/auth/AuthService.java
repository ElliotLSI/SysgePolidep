package tecleros.sysgepolidep.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // Buscar usuario por nombre de usuario
        Usuario usuario = usuarioRepository
                .findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario o contraseña incorrectos."
                        )
                );

        // Comparar contraseña ingresada con el hash BCrypt
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

        // Crear respuesta sin devolver la contraseña
        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getNombreUsuario(),
                usuario.getEstado()
        );
    }
}