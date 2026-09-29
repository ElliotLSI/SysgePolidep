package tecleros.sysgepolidep.socio;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.categoria.Categoria;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private EntityManager entityManager;


    // ==========================================================
    // LISTAR SOCIOS
    // ==========================================================

    public List<Socio> listarTodos() {

        /*
         * La consulta general de socios es administrativa.
         *
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * todos los socios.
         *
         * Un usuario normal no necesita acceder al listado
         * completo de socios.
         */

        if (!esAdministrador() && !esEmpleado()) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar el listado de socios."
            );
        }

        return socioRepository.findAll();
    }


    // ==========================================================
    // BUSCAR SOCIO POR ID DE USUARIO
    // ==========================================================

    public Optional<Socio> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        Optional<Socio> socioOpt =
                socioRepository.findById(id);

        if (socioOpt.isEmpty()) {
            return Optional.empty();
        }

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * cualquier socio.
         */

        if (esAdministrador() || esEmpleado()) {
            return socioOpt;
        }

        /*
         * Un usuario normal solamente puede consultar
         * su propio registro de socio.
         */

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (!usuarioActual.getIdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar este socio."
            );
        }

        return socioOpt;
    }


    // ==========================================================
    // BUSCAR SOCIO POR NÚMERO DE SOCIO
    // ==========================================================

    public Optional<Socio> buscarPorNroSocio(
            Integer nroSocio) {

        if (nroSocio == null) {
            throw new IllegalArgumentException(
                    "El número de socio es obligatorio."
            );
        }

        Optional<Socio> socioOpt =
                socioRepository.findByNroSocio(nroSocio);

        if (socioOpt.isEmpty()) {
            return Optional.empty();
        }

        Socio socio = socioOpt.get();

        /*
         * ADMINISTRADOR y EMPLEADO pueden buscar
         * cualquier socio por número.
         */

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(socio);
        }

        /*
         * Usuario normal solamente puede consultar
         * su propio número de socio.
         */

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (socio.getIdUsuario() == null ||
                !socio.getIdUsuario()
                        .equals(usuarioActual.getIdUsuario())) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar este socio."
            );
        }

        return Optional.of(socio);
    }


    // ==========================================================
    // GUARDAR SOCIO
    // ==========================================================

    public Socio guardarSocio(Socio socio) {

        /*
         * Este método es administrativo.
         *
         * La conversión normal de un usuario a socio
         * se realiza mediante convertirEnSocio().
         */

        if (!esAdministrador() && !esEmpleado()) {

            throw new IllegalArgumentException(
                    "No tenés permiso para registrar socios directamente."
            );
        }

        if (socio == null) {
            throw new IllegalArgumentException(
                    "El socio no puede ser nulo."
            );
        }

        if (socio.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        Usuario usuario =
                usuarioRepository.findById(
                        socio.getIdUsuario()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario indicado no existe."
                        )
                );

        if (socioRepository
                .findByUsuarioIdUsuario(
                        socio.getIdUsuario()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "El usuario ya está registrado como socio."
            );
        }

        if (socio.getNroSocio() == null) {
            socio.setNroSocio(
                    generarNumeroSocio()
            );
        }

        socio.setUsuario(usuario);

        return socioRepository.save(socio);
    }


    // ==========================================================
    // GENERAR NÚMERO DE SOCIO
    // ==========================================================

    private Integer generarNumeroSocio() {

        Optional<Socio> ultimoSocio =
                socioRepository
                        .findTopByOrderByNroSocioDesc();

        if (ultimoSocio.isEmpty()) {
            return 1001;
        }

        Integer ultimoNumero =
                ultimoSocio.get().getNroSocio();

        if (ultimoNumero == null) {
            return 1001;
        }

        return ultimoNumero + 1;
    }


    // ==========================================================
    // CONVERTIR USUARIO EN SOCIO
    // ==========================================================

    @Transactional
    public Membresia convertirEnSocio(
            AltaSocioDTO dto) {

        // ------------------------------------------------------
        // VALIDACIONES BÁSICAS
        // ------------------------------------------------------

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos de alta son obligatorios."
            );
        }

        if (dto.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (dto.getIdCategoria() == null) {
            throw new IllegalArgumentException(
                    "El ID de la categoría es obligatorio."
            );
        }


        // ------------------------------------------------------
        // OBTENER USUARIO AUTENTICADO
        // ------------------------------------------------------

        Usuario usuarioActual =
                obtenerUsuarioActual();


        // ------------------------------------------------------
        // BUSCAR USUARIO
        // ------------------------------------------------------

        Usuario usuario =
                usuarioRepository.findById(
                        dto.getIdUsuario()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario indicado no existe."
                        )
                );


        // ------------------------------------------------------
        // VALIDAR PROPIETARIO
        // ------------------------------------------------------

        /*
         * ADMINISTRADOR y EMPLEADO pueden convertir
         * a cualquier usuario en socio.
         *
         * Un USUARIO normal solamente puede convertirse
         * a sí mismo.
         */

        if (!esAdministrador() &&
                !esEmpleado()) {

            if (!usuario.getIdUsuario()
                    .equals(
                            usuarioActual.getIdUsuario()
                    )) {

                throw new IllegalArgumentException(
                        "No podés convertir a otro usuario en socio."
                );
            }
        }


        // ------------------------------------------------------
        // BUSCAR CATEGORÍA
        // ------------------------------------------------------

        Categoria categoria =
                categoriaRepository.findById(
                        dto.getIdCategoria()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "La categoría indicada no existe."
                        )
                );


        // ------------------------------------------------------
        // VALIDAR CATEGORÍA ACTIVA
        // ------------------------------------------------------

        if (!Boolean.TRUE.equals(
                categoria.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "La categoría seleccionada no está activa."
            );
        }


        // ------------------------------------------------------
        // VALIDAR QUE NO SEA SOCIO
        // ------------------------------------------------------

        if (socioRepository
                .findByUsuarioIdUsuario(
                        dto.getIdUsuario()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "El usuario ya está registrado como socio."
            );
        }


        // ------------------------------------------------------
        // VALIDAR DURACIÓN
        // ------------------------------------------------------

        if (categoria.getDuracionMeses() == null ||
                categoria.getDuracionMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La categoría debe tener una duración válida."
            );
        }


        // ------------------------------------------------------
        // CREAR SOCIO
        // ------------------------------------------------------

        Socio socio =
                new Socio();

        socio.setIdUsuario(
                usuario.getIdUsuario()
        );

        socio.setNroSocio(
                generarNumeroSocio()
        );

        socio.setUsuario(usuario);


        /*
         * Como Id_Usuario es una PK asignada manualmente,
         * utilizamos persist() para indicar que se trata
         * de una entidad nueva.
         */

        entityManager.persist(socio);


        // ------------------------------------------------------
        // CALCULAR FECHAS
        // ------------------------------------------------------

        LocalDate fechaInicio =
                LocalDate.now();

        LocalDate fechaVencimiento =
                fechaInicio.plusMonths(
                        categoria.getDuracionMeses()
                );


        // ------------------------------------------------------
        // CREAR MEMBRESÍA
        // ------------------------------------------------------

        Membresia membresia =
                new Membresia();

        membresia.setCategoria(
                categoria
        );

        membresia.setSocio(
                socio
        );

        membresia.setFechaInicio(
                fechaInicio
        );

        membresia.setFechaVenc(
                fechaVencimiento
        );

        /*
         * Se mantiene VIGENTE para no romper
         * el flujo actual del MVP.
         *
         * El control del pago de la primera membresía
         * queda como una mejora posterior del flujo.
         */

        membresia.setEstado(
                "VIGENTE"
        );


        // ------------------------------------------------------
        // GUARDAR MEMBRESÍA
        // ------------------------------------------------------

        return membresiaRepository.save(
                membresia
        );
    }


    // ==========================================================
    // ELIMINAR SOCIO
    // ==========================================================

    @Transactional
    public void eliminarSocio(Long id) {

        /*
         * Solo ADMINISTRADOR puede eliminar socios.
         */

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar socios."
            );
        }

        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (!socioRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "El socio indicado no existe."
            );
        }

        socioRepository.deleteById(id);
    }


    // ==========================================================
    // OBTENER USUARIO AUTENTICADO
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