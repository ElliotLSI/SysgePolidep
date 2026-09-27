package tecleros.sysgepolidep.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

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

    public Usuario actualizarUsuario(Long id, Usuario datos) {

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un usuario con el ID indicado."
                        )
                );

        // Validar nombre
        if (datos.getNombre() == null ||
                datos.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        // Validar apellido
        if (datos.getApellido() == null ||
                datos.getApellido().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }

        // Validar DNI
        if (datos.getDni() == null ||
                datos.getDni().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El DNI es obligatorio."
            );
        }

        if (!datos.getDni().matches("\\d{8}")) {

            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos."
            );
        }

        // Verificar que el DNI no pertenezca a otro usuario
        Optional<Usuario> usuarioPorDni =
                usuarioRepository.findByDni(datos.getDni());

        if (usuarioPorDni.isPresent() &&
                !usuarioPorDni.get().getIdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con ese DNI."
            );
        }

        // Validar email
        if (datos.getEmail() == null ||
                datos.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El email es obligatorio."
            );
        }

        // Verificar que el email no pertenezca a otro usuario
        Optional<Usuario> usuarioPorEmail =
                usuarioRepository.findByEmail(datos.getEmail());

        if (usuarioPorEmail.isPresent() &&
                !usuarioPorEmail.get().getIdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con ese email."
            );
        }

        // Validar nombre de usuario
        if (datos.getNombreUsuario() == null ||
                datos.getNombreUsuario().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        // Verificar que el nombre de usuario no pertenezca a otro usuario
        Optional<Usuario> usuarioPorNombre =
                usuarioRepository.findByNombreUsuario(
                        datos.getNombreUsuario()
                );

        if (usuarioPorNombre.isPresent() &&
                !usuarioPorNombre.get().getIdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario con ese nombre de usuario."
            );
        }

        // Validar pertenencia
        if (datos.getPertenencia() == null ||
                datos.getPertenencia().trim().isEmpty()) {

            datos.setPertenencia("NINGUNO");
        }

        String pertenencia = datos.getPertenencia().toUpperCase();

        if (!pertenencia.equals("DOCENTE") &&
                !pertenencia.equals("ALUMNO") &&
                !pertenencia.equals("ADMINISTRATIVO") &&
                !pertenencia.equals("NINGUNO")) {

            throw new IllegalArgumentException(
                    "La pertenencia debe ser DOCENTE, ALUMNO, ADMINISTRATIVO o NINGUNO."
            );
        }

        // Actualizar datos
        usuarioExistente.setNombre(datos.getNombre());
        usuarioExistente.setApellido(datos.getApellido());
        usuarioExistente.setDni(datos.getDni());
        usuarioExistente.setFechaNacimiento(datos.getFechaNacimiento());
        usuarioExistente.setDomicilio(datos.getDomicilio());
        usuarioExistente.setTelefono(datos.getTelefono());
        usuarioExistente.setEmail(datos.getEmail());
        usuarioExistente.setNombreUsuario(datos.getNombreUsuario());
        usuarioExistente.setPertenencia(pertenencia);
        usuarioExistente.setLegajo(datos.getLegajo());
        usuarioExistente.setEstado(datos.getEstado());

        // Cambiar contraseña solamente si se escribió una nueva
        if (datos.getPassword() != null &&
                !datos.getPassword().trim().isEmpty()) {

            usuarioExistente.setPassword(
                    passwordEncoder.encode(datos.getPassword())
            );
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}