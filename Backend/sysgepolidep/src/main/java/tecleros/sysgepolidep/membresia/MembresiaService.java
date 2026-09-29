package tecleros.sysgepolidep.membresia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.categoria.Categoria;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
import tecleros.sysgepolidep.pago.Pago;
import tecleros.sysgepolidep.pago.PagoRepository;
import tecleros.sysgepolidep.socio.SocioRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoRepository pagoRepository;


    // ==========================================================
    // LISTAR TODAS LAS MEMBRESÍAS
    // ==========================================================

    public List<Membresia> listarTodas() {

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * todas las membresías.
         *
         * Los demás usuarios solamente pueden consultar
         * sus propias membresías.
         */

        if (esAdministrador() || esEmpleado()) {
            return membresiaRepository.findAll();
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        return membresiaRepository
                .findBySocioIdUsuario(
                        usuarioActual.getIdUsuario()
                );
    }


    // ==========================================================
    // BUSCAR MEMBRESÍA POR ID
    // ==========================================================

    public Optional<Membresia> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la membresía es obligatorio."
            );
        }

        Optional<Membresia> membresiaOpt =
                membresiaRepository.findById(id);

        if (membresiaOpt.isEmpty()) {
            return Optional.empty();
        }

        Membresia membresia =
                membresiaOpt.get();

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * cualquier membresía.
         */

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(membresia);
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null ||
                !membresia.getSocio()
                        .getIdUsuario()
                        .equals(
                                usuarioActual.getIdUsuario()
                        )) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar esta membresía."
            );
        }

        return Optional.of(membresia);
    }


    // ==========================================================
    // CREAR / GUARDAR MEMBRESÍA
    // ==========================================================

    public Membresia guardarMembresia(
            Membresia membresia) {

        if (membresia == null) {
            throw new IllegalArgumentException(
                    "La membresía no puede ser nula."
            );
        }


        // ------------------------------------------------------
        // VALIDAR CATEGORÍA
        // ------------------------------------------------------

        if (membresia.getCategoria() == null ||
                membresia.getCategoria()
                        .getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe tener una categoría."
            );
        }

        Long idCategoria =
                membresia.getCategoria()
                        .getIdCategoria();

        Categoria categoria =
                categoriaRepository
                        .findById(idCategoria)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La categoría asociada no existe."
                                )
                        );

        membresia.setCategoria(categoria);


        // ------------------------------------------------------
        // VALIDAR SOCIO
        // ------------------------------------------------------

        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe estar asociada a un socio."
            );
        }

        Long idSocio =
                membresia.getSocio()
                        .getIdUsuario();

        if (socioRepository
                .findById(idSocio)
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "El socio asociado no existe."
            );
        }


        // ------------------------------------------------------
        // VALIDAR FECHAS
        // ------------------------------------------------------

        if (membresia.getFechaInicio() == null) {

            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria."
            );
        }

        if (membresia.getFechaVenc() == null) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento es obligatoria."
            );
        }

        if (!membresia.getFechaVenc()
                .isAfter(
                        membresia.getFechaInicio()
                )) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de inicio."
            );
        }


        // ------------------------------------------------------
        // VALIDAR ESTADO
        // ------------------------------------------------------

        if (membresia.getEstado() == null ||
                membresia.getEstado()
                        .trim()
                        .isEmpty()) {

            membresia.setEstado("VIGENTE");
        }

        String estado =
                membresia.getEstado()
                        .trim()
                        .toUpperCase();

        if (!estado.equals("VIGENTE") &&
                !estado.equals("VENCIDA") &&
                !estado.equals("CANCELADA")) {

            throw new IllegalArgumentException(
                    "El estado debe ser VIGENTE, VENCIDA o CANCELADA."
            );
        }

        membresia.setEstado(estado);


        return membresiaRepository.save(
                membresia
        );
    }


    // ==========================================================
    // RENOVAR MEMBRESÍA
    // ==========================================================

    @Transactional
    public Membresia renovarMembresia(
            Long idMembresia) {

        if (idMembresia == null) {
            throw new IllegalArgumentException(
                    "El ID de la membresía es obligatorio."
            );
        }


        // ------------------------------------------------------
        // BUSCAR MEMBRESÍA
        // ------------------------------------------------------

        Membresia membresia =
                membresiaRepository
                        .findById(idMembresia)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Membresía no encontrada con ID: "
                                                + idMembresia
                                )
                        );


        // ------------------------------------------------------
        // VERIFICAR PROPIETARIO
        // ------------------------------------------------------

        if (!esAdministrador() &&
                !esEmpleado()) {

            Usuario usuarioActual =
                    obtenerUsuarioActual();

            if (membresia.getSocio() == null ||
                    membresia.getSocio()
                            .getIdUsuario() == null ||
                    !membresia.getSocio()
                            .getIdUsuario()
                            .equals(
                                    usuarioActual.getIdUsuario()
                            )) {

                throw new IllegalArgumentException(
                        "No tenés permiso para renovar esta membresía."
                );
            }
        }


        // ------------------------------------------------------
        // VERIFICAR CATEGORÍA
        // ------------------------------------------------------

        if (membresia.getCategoria() == null ||
                membresia.getCategoria()
                        .getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "La membresía no tiene una categoría asociada."
            );
        }


        Categoria categoria =
                categoriaRepository
                        .findById(
                                membresia.getCategoria()
                                        .getIdCategoria()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La categoría asociada no existe."
                                )
                        );


        // ------------------------------------------------------
        // VERIFICAR CATEGORÍA ACTIVA
        // ------------------------------------------------------

        if (Boolean.FALSE.equals(
                categoria.getActivo())) {

            throw new IllegalArgumentException(
                    "La categoría de la membresía no está activa."
            );
        }


        // ------------------------------------------------------
        // VERIFICAR DURACIÓN
        // ------------------------------------------------------

        if (categoria.getDuracionMeses() == null ||
                categoria.getDuracionMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La categoría no tiene una duración válida."
            );
        }


        // ------------------------------------------------------
        // VERIFICAR COSTO
        // ------------------------------------------------------

        if (categoria.getCosto() == null ||
                categoria.getCosto() < 0) {

            throw new IllegalArgumentException(
                    "La categoría no tiene un costo válido."
            );
        }


        // ------------------------------------------------------
        // VERIFICAR PAGO APROBADO
        // ------------------------------------------------------

        /*
         * La membresía solamente puede renovarse si existe
         * un pago APROBADO asociado a esta membresía.
         */

        List<Pago> pagos =
                pagoRepository.findAll()
                        .stream()
                        .filter(p ->
                                p.getIdMembresia() != null &&
                                        p.getIdMembresia()
                                                .equals(idMembresia) &&
                                        "APROBADO".equals(
                                                p.getEstado()
                                        ) &&
                                        p.getMontoTotal() != null &&
                                        p.getMontoTotal()
                                                >= categoria.getCosto()
                        )
                        .toList();


        if (pagos.isEmpty()) {

            throw new IllegalArgumentException(
                    "No existe un pago aprobado suficiente para renovar esta membresía."
            );
        }


        // ------------------------------------------------------
        // CALCULAR NUEVA VIGENCIA
        // ------------------------------------------------------

        LocalDate fechaInicio =
                LocalDate.now();

        LocalDate fechaVencimiento =
                fechaInicio.plusMonths(
                        categoria.getDuracionMeses()
                );


        // ------------------------------------------------------
        // ACTUALIZAR MEMBRESÍA
        // ------------------------------------------------------

        membresia.setCategoria(categoria);

        membresia.setFechaInicio(
                fechaInicio
        );

        membresia.setFechaVenc(
                fechaVencimiento
        );

        membresia.setEstado(
                "VIGENTE"
        );


        // ------------------------------------------------------
        // GUARDAR
        // ------------------------------------------------------

        return membresiaRepository.save(
                membresia
        );
    }


    // ==========================================================
    // ELIMINAR MEMBRESÍA
    // ==========================================================

    @Transactional
    public void eliminarMembresia(
            Long id) {

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar membresías."
            );
        }


        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID de la membresía es obligatorio."
            );
        }


        if (!membresiaRepository
                .existsById(id)) {

            throw new IllegalArgumentException(
                    "Membresía no encontrada con ID: "
                            + id
            );
        }


        membresiaRepository.deleteById(id);
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