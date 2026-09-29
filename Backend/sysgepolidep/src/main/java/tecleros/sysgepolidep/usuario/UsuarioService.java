package tecleros.sysgepolidep.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // ==========================================================
    // LISTAR USUARIOS
    // ==========================================================

    public List<Usuario> listarTodos() {

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * todos los usuarios.
         *
         * Los usuarios normales solamente pueden
         * consultar su propio registro.
         */

        if (esAdministrador() || esEmpleado()) {
            return usuarioRepository.findAll();
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        return usuarioRepository.findById(
                usuarioActual.getIdUsuario()
        ).map(List::of).orElse(List.of());
    }


    // ==========================================================
    // BUSCAR USUARIO POR ID
    // ==========================================================

    public Optional<Usuario> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        Optional<Usuario> usuarioOpt =
                usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * cualquier usuario.
         */

        if (esAdministrador() || esEmpleado()) {
            return usuarioOpt;
        }

        /*
         * Usuario normal solamente puede consultar
         * su propio registro.
         */

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (!usuarioActual.getIdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar este usuario."
            );
        }

        return usuarioOpt;
    }


    // ==========================================================
    // REGISTRAR USUARIO
    // ==========================================================

    public Usuario guardarUsuario(
            Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo."
            );
        }


        // ------------------------------------------------------
        // NOMBRE
        // ------------------------------------------------------

        if (usuario.getNombre() == null ||
                usuario.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }


        // ------------------------------------------------------
        // APELLIDO
        // ------------------------------------------------------

        if (usuario.getApellido() == null ||
                usuario.getApellido().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }


        // ------------------------------------------------------
        // DNI
        // ------------------------------------------------------

        if (usuario.getDni() == null ||
                usuario.getDni().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El DNI es obligatorio."
            );
        }

        String dni =
                usuario.getDni().trim();

        if (!dni.matches("\\d{8}")) {

            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos."
            );
        }

        if (usuarioRepository
                .findByDni(dni)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese DNI."
            );
        }

        usuario.setDni(dni);


        // ------------------------------------------------------
        // EMAIL
        // ------------------------------------------------------

        if (usuario.getEmail() == null ||
                usuario.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El email es obligatorio."
            );
        }

        String email =
                usuario.getEmail()
                        .trim()
                        .toLowerCase();

        if (usuarioRepository
                .findByEmail(email)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese email."
            );
        }

        usuario.setEmail(email);


        // ------------------------------------------------------
        // NOMBRE DE USUARIO
        // ------------------------------------------------------

        if (usuario.getNombreUsuario() == null ||
                usuario.getNombreUsuario()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        String nombreUsuario =
                usuario.getNombreUsuario()
                        .trim();

        if (usuarioRepository
                .findByNombreUsuario(nombreUsuario)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese nombre de usuario."
            );
        }

        usuario.setNombreUsuario(nombreUsuario);


        // ------------------------------------------------------
        // CONTRASEÑA
        // ------------------------------------------------------

        if (usuario.getPassword() == null ||
                usuario.getPassword()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        usuario.getPassword()
                )
        );


        // ------------------------------------------------------
        // PERTENENCIA
        // ------------------------------------------------------

        if (usuario.getPertenencia() == null ||
                usuario.getPertenencia()
                        .trim()
                        .isEmpty()) {

            usuario.setPertenencia(
                    "NINGUNO"
            );
        }

        String pertenencia =
                usuario.getPertenencia()
                        .trim()
                        .toUpperCase();

        if (!pertenencia.equals("DOCENTE") &&
                !pertenencia.equals("ALUMNO") &&
                !pertenencia.equals("ADMINISTRATIVO") &&
                !pertenencia.equals("NINGUNO")) {

            throw new IllegalArgumentException(
                    "La pertenencia debe ser DOCENTE, ALUMNO, ADMINISTRATIVO o NINGUNO."
            );
        }

        usuario.setPertenencia(
                pertenencia
        );


        // ------------------------------------------------------
        // ESTADO
        // ------------------------------------------------------

        if (usuario.getEstado() == null ||
                usuario.getEstado()
                        .trim()
                        .isEmpty()) {

            usuario.setEstado(
                    "ACTIVO"
            );
        }

        String estado =
                usuario.getEstado()
                        .trim()
                        .toUpperCase();

        if (!estado.equals("ACTIVO") &&
                !estado.equals("INACTIVO")) {

            throw new IllegalArgumentException(
                    "El estado debe ser ACTIVO o INACTIVO."
            );
        }

        usuario.setEstado(
                estado
        );


        return usuarioRepository.save(
                usuario
        );
    }


    // ==========================================================
    // ACTUALIZAR USUARIO
    // ==========================================================

    @Transactional
    public Usuario actualizarUsuario(
            Long id,
            Usuario datos) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios."
            );
        }


        // ------------------------------------------------------
        // BUSCAR USUARIO
        // ------------------------------------------------------

        Usuario usuarioExistente =
                usuarioRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe un usuario con el ID indicado."
                                )
                        );


        // ------------------------------------------------------
        // VERIFICAR PERMISOS
        // ------------------------------------------------------

        boolean administrador =
                esAdministrador();

        boolean empleado =
                esEmpleado();

        /*
         * ADMINISTRADOR y EMPLEADO pueden modificar
         * cualquier usuario.
         *
         * Un usuario normal solamente puede modificar
         * su propio registro.
         */

        if (!administrador && !empleado) {

            Usuario usuarioActual =
                    obtenerUsuarioActual();

            if (!usuarioActual.getIdUsuario()
                    .equals(id)) {

                throw new IllegalArgumentException(
                        "No tenés permiso para modificar este usuario."
                );
            }


            /*
             * Para un usuario normal solamente permitimos
             * modificar datos de contacto.
             *
             * No puede cambiar:
             * - DNI
             * - nombre de usuario
             * - pertenencia
             * - legajo
             * - estado
             */

            if (datos.getEmail() == null ||
                    datos.getEmail().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "El email es obligatorio."
                );
            }

            String email =
                    datos.getEmail()
                            .trim()
                            .toLowerCase();

            Optional<Usuario> usuarioPorEmail =
                    usuarioRepository
                            .findByEmail(email);

            if (usuarioPorEmail.isPresent() &&
                    !usuarioPorEmail.get()
                            .getIdUsuario()
                            .equals(id)) {

                throw new IllegalArgumentException(
                        "Ya existe otro usuario registrado con ese email."
                );
            }

            usuarioExistente.setEmail(
                    email
            );

            usuarioExistente.setDomicilio(
                    datos.getDomicilio()
            );

            usuarioExistente.setTelefono(
                    datos.getTelefono()
            );


            /*
             * Permitimos cambiar la contraseña del propio
             * usuario porque es una operación sobre su
             * propia cuenta.
             */

            if (datos.getPassword() != null &&
                    !datos.getPassword()
                            .trim()
                            .isEmpty()) {

                usuarioExistente.setPassword(
                        passwordEncoder.encode(
                                datos.getPassword()
                        )
                );
            }

            return usuarioRepository.save(
                    usuarioExistente
            );
        }


        // ======================================================
        // ACTUALIZACIÓN ADMINISTRATIVA
        // ======================================================

        // ------------------------------------------------------
        // NOMBRE
        // ------------------------------------------------------

        if (datos.getNombre() == null ||
                datos.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }


        // ------------------------------------------------------
        // APELLIDO
        // ------------------------------------------------------

        if (datos.getApellido() == null ||
                datos.getApellido().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }


        // ------------------------------------------------------
        // DNI
        // ------------------------------------------------------

        if (datos.getDni() == null ||
                datos.getDni().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El DNI es obligatorio."
            );
        }

        String dni =
                datos.getDni().trim();

        if (!dni.matches("\\d{8}")) {

            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos."
            );
        }

        Optional<Usuario> usuarioPorDni =
                usuarioRepository.findByDni(dni);

        if (usuarioPorDni.isPresent() &&
                !usuarioPorDni.get()
                        .getIdUsuario()
                        .equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con ese DNI."
            );
        }


        // ------------------------------------------------------
        // EMAIL
        // ------------------------------------------------------

        if (datos.getEmail() == null ||
                datos.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El email es obligatorio."
            );
        }

        String email =
                datos.getEmail()
                        .trim()
                        .toLowerCase();

        Optional<Usuario> usuarioPorEmail =
                usuarioRepository.findByEmail(email);

        if (usuarioPorEmail.isPresent() &&
                !usuarioPorEmail.get()
                        .getIdUsuario()
                        .equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con ese email."
            );
        }


        // ------------------------------------------------------
        // NOMBRE DE USUARIO
        // ------------------------------------------------------

        if (datos.getNombreUsuario() == null ||
                datos.getNombreUsuario()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        String nombreUsuario =
                datos.getNombreUsuario()
                        .trim();

        Optional<Usuario> usuarioPorNombre =
                usuarioRepository.findByNombreUsuario(
                        nombreUsuario
                );

        if (usuarioPorNombre.isPresent() &&
                !usuarioPorNombre.get()
                        .getIdUsuario()
                        .equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario con ese nombre de usuario."
            );
        }


        // ------------------------------------------------------
        // PERTENENCIA
        // ------------------------------------------------------

        if (datos.getPertenencia() == null ||
                datos.getPertenencia()
                        .trim()
                        .isEmpty()) {

            datos.setPertenencia(
                    "NINGUNO"
            );
        }

        String pertenencia =
                datos.getPertenencia()
                        .trim()
                        .toUpperCase();

        if (!pertenencia.equals("DOCENTE") &&
                !pertenencia.equals("ALUMNO") &&
                !pertenencia.equals("ADMINISTRATIVO") &&
                !pertenencia.equals("NINGUNO")) {

            throw new IllegalArgumentException(
                    "La pertenencia debe ser DOCENTE, ALUMNO, ADMINISTRATIVO o NINGUNO."
            );
        }


        // ------------------------------------------------------
        // ESTADO
        // ------------------------------------------------------

        String estado =
                datos.getEstado();

        if (estado == null ||
                estado.trim().isEmpty()) {

            estado =
                    usuarioExistente.getEstado();
        }

        estado =
                estado.trim()
                        .toUpperCase();

        if (!estado.equals("ACTIVO") &&
                !estado.equals("INACTIVO")) {

            throw new IllegalArgumentException(
                    "El estado debe ser ACTIVO o INACTIVO."
            );
        }


        // ------------------------------------------------------
        // ACTUALIZAR DATOS
        // ------------------------------------------------------

        usuarioExistente.setNombre(
                datos.getNombre().trim()
        );

        usuarioExistente.setApellido(
                datos.getApellido().trim()
        );

        usuarioExistente.setDni(
                dni
        );

        usuarioExistente.setFechaNacimiento(
                datos.getFechaNacimiento()
        );

        usuarioExistente.setDomicilio(
                datos.getDomicilio()
        );

        usuarioExistente.setTelefono(
                datos.getTelefono()
        );

        usuarioExistente.setEmail(
                email
        );

        usuarioExistente.setNombreUsuario(
                nombreUsuario
        );

        usuarioExistente.setPertenencia(
                pertenencia
        );

        usuarioExistente.setLegajo(
                datos.getLegajo()
        );

        usuarioExistente.setEstado(
                estado
        );


        // ------------------------------------------------------
        // CONTRASEÑA
        // ------------------------------------------------------

        /*
         * Si el administrador no envía una contraseña,
         * conservamos la actual.
         */

        if (datos.getPassword() != null &&
                !datos.getPassword()
                        .trim()
                        .isEmpty()) {

            usuarioExistente.setPassword(
                    passwordEncoder.encode(
                            datos.getPassword()
                    )
            );
        }


        return usuarioRepository.save(
                usuarioExistente
        );
    }


    // ==========================================================
    // ELIMINAR USUARIO
    // ==========================================================

    @Transactional
    public void eliminarUsuario(Long id) {

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar usuarios."
            );
        }

        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (!usuarioRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        usuarioRepository.deleteById(id);
    }


    // ==========================================================
    // USUARIO AUTENTICADO
    // ==========================================================

    private Usuario obtenerUsuarioActual() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "No hay un usuario autenticado."
            );
        }

        String nombreUsuario =
                authentication.getName();

        if (nombreUsuario == null ||
                nombreUsuario.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario autenticado."
            );
        }

        return usuarioRepository
                .findByNombreUsuario(
                        nombreUsuario
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario autenticado no existe."
                        )
                );
    }


    // ==========================================================
    // VERIFICAR ADMINISTRADOR
    // ==========================================================

    private boolean esAdministrador() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMINISTRADOR"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }


    // ==========================================================
    // VERIFICAR EMPLEADO
    // ==========================================================

    private boolean esEmpleado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_EMPLEADO"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }
}