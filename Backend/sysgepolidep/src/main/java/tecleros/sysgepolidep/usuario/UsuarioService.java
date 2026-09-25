package tecleros.sysgepolidep.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario guardarUsuario(Usuario usuario) {

        // Validar que se haya enviado un usuario
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }

        // Validar nombre
        if (usuario.getNombre() == null ||
                usuario.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        // Validar apellido
        if (usuario.getApellido() == null ||
                usuario.getApellido().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }

        // Validar DNI
        if (usuario.getDni() == null ||
                usuario.getDni().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El DNI es obligatorio."
            );
        }

        if (!usuario.getDni().matches("\\d{8}")) {

            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos."
            );
        }

        // Validar DNI repetido
        if (usuarioRepository.findByDni(usuario.getDni()).isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese DNI."
            );
        }

        // Validar email
        if (usuario.getEmail() == null ||
                usuario.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El email es obligatorio."
            );
        }

        // Validar email repetido
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese email."
            );
        }

        // Validar nombre de usuario
        if (usuario.getNombreUsuario() == null ||
                usuario.getNombreUsuario().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        // Validar nombre de usuario repetido
        if (usuarioRepository
                .findByNombreUsuario(usuario.getNombreUsuario())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese nombre de usuario."
            );
        }

        // Validar contraseña
        if (usuario.getPassword() == null ||
                usuario.getPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        // Validar pertenencia
        if (usuario.getPertenencia() == null ||
                usuario.getPertenencia().trim().isEmpty()) {

            usuario.setPertenencia("NINGUNO");
        }

        String pertenencia = usuario.getPertenencia().toUpperCase();

        if (!pertenencia.equals("DOCENTE") &&
                !pertenencia.equals("ALUMNO") &&
                !pertenencia.equals("ADMINISTRATIVO") &&
                !pertenencia.equals("NINGUNO")) {

            throw new IllegalArgumentException(
                    "La pertenencia debe ser DOCENTE, ALUMNO, ADMINISTRATIVO o NINGUNO."
            );
        }

        usuario.setPertenencia(pertenencia);

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}