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
    // ACTUALIZAR MEMBRESÍAS VENCIDAS AUTOMÁTICAMENTE
    // ==========================================================

    private void actualizarMembresiasVencidas() {
        LocalDate hoy = LocalDate.now();
        List<Membresia> todas = membresiaRepository.findAll();

        for (Membresia m : todas) {
            if ("VIGENTE".equalsIgnoreCase(m.getEstado())
                    && m.getFechaVenc() != null
                    && m.getFechaVenc().isBefore(hoy)) {
                m.setEstado("VENCIDA");
                membresiaRepository.save(m);
            }
        }
    }


    // ==========================================================
    // LISTAR TODAS LAS MEMBRESÍAS
    // ==========================================================

    @Transactional
    public List<Membresia> listarTodas() {
        actualizarMembresiasVencidas();

        if (esAdministrador() || esEmpleado()) {
            return membresiaRepository.findAll();
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        return membresiaRepository.findBySocioIdUsuario(
                usuarioActual.getIdUsuario()
        );
    }


    // ==========================================================
    // BUSCAR MEMBRESÍA POR ID
    // ==========================================================

    @Transactional
    public Optional<Membresia> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la membresía es obligatorio."
            );
        }

        actualizarMembresiasVencidas();

        Optional<Membresia> membresiaOpt = membresiaRepository.findById(id);

        if (membresiaOpt.isEmpty()) {
            return Optional.empty();
        }

        Membresia membresia = membresiaOpt.get();

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(membresia);
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null ||
                !membresia.getSocio().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
            throw new IllegalArgumentException(
                    "No tenés permiso para consultar esta membresía."
            );
        }

        return Optional.of(membresia);
    }


    // ==========================================================
    // CREAR / GUARDAR MEMBRESÍA
    // ==========================================================

    public Membresia guardarMembresia(Membresia membresia) {
        if (membresia == null) {
            throw new IllegalArgumentException("La membresía no puede ser nula.");
        }

        // VALIDAR CATEGORÍA
        if (membresia.getCategoria() == null ||
                membresia.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("La membresía debe tener una categoría.");
        }

        Long idCategoria = membresia.getCategoria().getIdCategoria();
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("La categoría asociada no existe."));
        membresia.setCategoria(categoria);

        // VALIDAR SOCIO
        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null) {
            throw new IllegalArgumentException("La membresía debe estar asociada a un socio.");
        }

        Long idSocio = membresia.getSocio().getIdUsuario();
        if (socioRepository.findById(idSocio).isEmpty()) {
            throw new IllegalArgumentException("El socio asociado no existe.");
        }

        // VALIDAR FECHAS
        if (membresia.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
        if (membresia.getFechaVenc() == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        if (!membresia.getFechaVenc().isAfter(membresia.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de inicio."
            );
        }

        // VALIDAR ESTADO
        if (membresia.getEstado() == null || membresia.getEstado().trim().isEmpty()) {
            membresia.setEstado("VIGENTE");
        }

        String estado = membresia.getEstado().trim().toUpperCase();
        if (!estado.equals("VIGENTE") &&
                !estado.equals("VENCIDA") &&
                !estado.equals("CANCELADA")) {
            throw new IllegalArgumentException(
                    "El estado debe ser VIGENTE, VENCIDA o CANCELADA."
            );
        }

        // Si se guarda con fecha de vencimiento ya pasada, marcarla VENCIDA
        if (estado.equals("VIGENTE") && membresia.getFechaVenc().isBefore(LocalDate.now())) {
            estado = "VENCIDA";
        }

        membresia.setEstado(estado);

        return membresiaRepository.save(membresia);
    }


    // ==========================================================
    // RENOVAR MEMBRESÍA
    // ==========================================================

    @Transactional
    public Membresia renovarMembresia(Long idMembresia) {
        if (idMembresia == null) {
            throw new IllegalArgumentException("El ID de la membresía es obligatorio.");
        }

        Membresia membresia = membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Membresía no encontrada con ID: " + idMembresia
                ));

        // VERIFICAR PROPIETARIO
        if (!esAdministrador() && !esEmpleado()) {
            Usuario usuarioActual = obtenerUsuarioActual();
            if (membresia.getSocio() == null ||
                    membresia.getSocio().getIdUsuario() == null ||
                    !membresia.getSocio().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
                throw new IllegalArgumentException(
                        "No tenés permiso para renovar esta membresía."
                );
            }
        }

        // VERIFICAR CATEGORÍA
        if (membresia.getCategoria() == null ||
                membresia.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("La membresía no tiene una categoría asociada.");
        }

        Categoria categoria = categoriaRepository
                .findById(membresia.getCategoria().getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("La categoría asociada no existe."));

        if (Boolean.FALSE.equals(categoria.getActivo())) {
            throw new IllegalArgumentException("La categoría de la membresía no está activa.");
        }

        if (categoria.getDuracionMeses() == null || categoria.getDuracionMeses() <= 0) {
            throw new IllegalArgumentException("La categoría no tiene una duración válida.");
        }

        if (categoria.getCosto() == null || categoria.getCosto() < 0) {
            throw new IllegalArgumentException("La categoría no tiene un costo válido.");
        }

        // VERIFICAR PAGO APROBADO POSTERIOR AL INICIO ACTUAL
        // Evita reutilizar un mismo pago viejo para renovar infinitas veces.
        LocalDate fechaInicioActual = membresia.getFechaInicio();

        List<Pago> pagosValidos = pagoRepository.findAll()
                .stream()
                .filter(p ->
                        p.getIdMembresia() != null &&
                                p.getIdMembresia().equals(idMembresia) &&
                                "APROBADO".equalsIgnoreCase(p.getEstado()) &&
                                p.getMontoTotal() != null &&
                                p.getMontoTotal() >= categoria.getCosto() &&
                                p.getFecha() != null &&
                                (fechaInicioActual == null || p.getFecha().toLocalDate().isAfter(fechaInicioActual))
                )
                .toList();

        if (pagosValidos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe un pago aprobado reciente suficiente para renovar esta membresía."
            );
        }

        // CALCULAR NUEVA VIGENCIA
        LocalDate nuevaFechaInicio = LocalDate.now();
        LocalDate nuevaFechaVencimiento = nuevaFechaInicio.plusMonths(categoria.getDuracionMeses());

        membresia.setCategoria(categoria);
        membresia.setFechaInicio(nuevaFechaInicio);
        membresia.setFechaVenc(nuevaFechaVencimiento);
        membresia.setEstado("VIGENTE");

        return membresiaRepository.save(membresia);
    }


    // ==========================================================
    // ELIMINAR MEMBRESÍA
    // ==========================================================

    @Transactional
    public void eliminarMembresia(Long id) {
        if (!esAdministrador()) {
            throw new IllegalArgumentException("Solo un administrador puede eliminar membresías.");
        }

        if (id == null) {
            throw new IllegalArgumentException("El ID de la membresía es obligatorio.");
        }

        if (!membresiaRepository.existsById(id)) {
            throw new IllegalArgumentException("Membresía no encontrada con ID: " + id);
        }

        membresiaRepository.deleteById(id);
    }


    // ==========================================================
    // SINCRONIZAR ESTADO DEL USUARIO (DESACTIVADO)
    // ==========================================================

    /**
     * Se mantiene el método vacío por compatibilidad con otras clases.
     * El estado ACTIVO/INACTIVO del usuario controla el acceso al sistema
     * y no debe modificarse por el vencimiento de la membresía.
     */
    @Transactional
    public void sincronizarEstadoUsuario(Long idUsuario) {
        // Sin cambios en el estado de acceso del usuario.
    }


    // ==========================================================
    // HELPERS DE SEGURIDAD
    // ==========================================================

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No hay un usuario autenticado.");
        }

        String nombreUsuario = authentication.getName();
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("No se pudo identificar al usuario autenticado.");
        }

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe."));
    }

    private boolean esAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
    }

    private boolean esEmpleado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_EMPLEADO".equals(a.getAuthority()));
    }
}