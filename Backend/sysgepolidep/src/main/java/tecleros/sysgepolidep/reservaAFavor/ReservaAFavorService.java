package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaAFavorService {

    @Autowired
    private ReservaAFavorRepository reservaAFavorRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;


    // ============================================================
    // LISTAR TODAS
    // ============================================================

    public List<ReservaAFavor> listarTodas() {

        if (esAdministrador() || esEmpleado()) {
            return reservaAFavorRepository.findAll();
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        return reservaAFavorRepository
                .findByUsuarioIdUsuario(
                        usuarioActual.getIdUsuario()
                );
    }


    // ============================================================
    // BUSCAR POR ID
    // ============================================================

    public Optional<ReservaAFavor> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la reserva a favor es obligatorio."
            );
        }

        Optional<ReservaAFavor> reservaAFavorOpt =
                reservaAFavorRepository.findById(id);

        if (reservaAFavorOpt.isEmpty()) {
            return Optional.empty();
        }

        ReservaAFavor reservaAFavor =
                reservaAFavorOpt.get();

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(reservaAFavor);
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (reservaAFavor.getUsuario() == null ||
                !reservaAFavor.getUsuario()
                        .getIdUsuario()
                        .equals(
                                usuarioActual.getIdUsuario()
                        )) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar esta reserva a favor."
            );
        }

        return Optional.of(reservaAFavor);
    }


    // ============================================================
    // BUSCAR POR USUARIO
    // ============================================================

    public List<ReservaAFavor> buscarPorUsuario(
            Long idUsuario) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (usuarioRepository.findById(idUsuario).isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        if (esAdministrador() || esEmpleado()) {

            return reservaAFavorRepository
                    .findByUsuarioIdUsuario(idUsuario);
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (!usuarioActual.getIdUsuario()
                .equals(idUsuario)) {

            throw new IllegalArgumentException(
                    "No podés consultar las reservas a favor de otro usuario."
            );
        }

        return reservaAFavorRepository
                .findByUsuarioIdUsuario(idUsuario);
    }


    // ============================================================
    // BUSCAR RESERVAS A FAVOR DISPONIBLES
    // ============================================================

    public List<ReservaAFavor> buscarDisponiblesPorUsuario(
            Long idUsuario) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (usuarioRepository.findById(idUsuario).isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (!esAdministrador() &&
                !esEmpleado() &&
                !usuarioActual.getIdUsuario()
                        .equals(idUsuario)) {

            throw new IllegalArgumentException(
                    "No podés consultar las reservas a favor de otro usuario."
            );
        }


        /*
         * Una reserva a favor disponible debe:
         *
         * 1. Pertenecer al usuario.
         * 2. No haber sido utilizada.
         * 3. No estar vencida.
         */

        List<ReservaAFavor> reservas =
                reservaAFavorRepository
                        .findByUsuarioIdUsuarioAndUtilizadaFalse(
                                idUsuario
                        );

        LocalDate hoy =
                LocalDate.now();


        /*
         * Si una reserva a favor ya venció,
         * se marca automáticamente como utilizada
         * para que deje de aparecer como disponible.
         */

        for (ReservaAFavor reservaAFavor : reservas) {

            if (reservaAFavor.getFechaVencimiento() != null &&
                    !hoy.isBefore(
                            reservaAFavor.getFechaVencimiento()
                    )) {

                reservaAFavor.setUtilizada(true);

                reservaAFavorRepository.save(
                        reservaAFavor
                );
            }
        }


        /*
         * Volvemos a consultar para obtener solamente
         * las reservas que siguen disponibles.
         */

        return reservaAFavorRepository
                .findByUsuarioIdUsuarioAndUtilizadaFalse(
                        idUsuario
                );
    }


    // ============================================================
    // CREAR RESERVA A FAVOR
    // ============================================================

    @Transactional
    public ReservaAFavor crearReservaAFavor(
            ReservaAFavor reservaAFavor) {

        if (reservaAFavor == null) {
            throw new IllegalArgumentException(
                    "La reserva a favor no puede ser nula."
            );
        }


        // ========================================================
        // RESERVA DE ORIGEN
        // ========================================================

        if (reservaAFavor.getReservaOrigen() == null ||
                reservaAFavor.getReservaOrigen()
                        .getIdReserva() == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor debe estar asociada a una reserva de origen."
            );
        }

        Long idReserva =
                reservaAFavor.getReservaOrigen()
                        .getIdReserva();

        Reserva reservaExistente =
                reservaRepository.findById(idReserva)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La reserva de origen no existe."
                                )
                        );


        // ========================================================
        // VERIFICAR ESTADO
        // ========================================================

        if (!"CANCELADA".equals(
                reservaExistente.getEstado())) {

            throw new IllegalArgumentException(
                    "Solo se puede generar una reserva a favor a partir de una reserva cancelada."
            );
        }


        // ========================================================
        // VERIFICAR INSTALACIÓN
        // ========================================================

        if (reservaExistente.getInstalacion() == null ||
                reservaExistente.getInstalacion()
                        .getIdInstalacion() == null) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene una instalación válida."
            );
        }


        // ========================================================
        // EVITAR DUPLICADOS
        // ========================================================

        if (reservaAFavorRepository
                .findByReservaOrigenIdReserva(idReserva)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "La reserva ya tiene una reserva a favor asociada."
            );
        }


        // ========================================================
        // USUARIO
        // ========================================================

        Usuario usuario =
                reservaExistente.getUsuario();

        if (usuario == null ||
                usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene un usuario válido."
            );
        }


        // ========================================================
        // FECHAS
        // ========================================================

        LocalDate fechaGeneracion =
                LocalDate.now();

        LocalDate fechaVencimiento =
                fechaGeneracion.plusMonths(3);


        // ========================================================
        // CREAR RESERVA A FAVOR
        // ========================================================

        reservaAFavor.setFechaGeneracion(
                fechaGeneracion
        );

        reservaAFavor.setFechaVencimiento(
                fechaVencimiento
        );

        reservaAFavor.setUtilizada(false);

        reservaAFavor.setReservaOrigen(
                reservaExistente
        );

        reservaAFavor.setUsuario(
                usuario
        );


        return reservaAFavorRepository
                .save(reservaAFavor);
    }


    // ============================================================
    // UTILIZAR RESERVA A FAVOR
    // ============================================================

    @Transactional
    public Reserva reprogramarReserva(
            Long idReservaAFavor,
            LocalDate nuevaFecha,
            LocalTime nuevaHora) {


        // ========================================================
        // VALIDAR ID
        // ========================================================

        if (idReservaAFavor == null) {

            throw new IllegalArgumentException(
                    "El ID de la reserva a favor es obligatorio."
            );
        }


        // ========================================================
        // BUSCAR RESERVA A FAVOR
        // ========================================================

        ReservaAFavor reservaAFavor =
                reservaAFavorRepository.findById(
                        idReservaAFavor
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reserva a favor no encontrada con ID: "
                                        + idReservaAFavor
                        )
                );


        // ========================================================
        // VERIFICAR PROPIETARIO
        // ========================================================

        Usuario usuarioActual =
                obtenerUsuarioActual();

        boolean esPropietario =
                reservaAFavor.getUsuario() != null &&
                        reservaAFavor.getUsuario()
                                .getIdUsuario()
                                .equals(
                                        usuarioActual.getIdUsuario()
                                );

        if (!esAdministrador() &&
                !esEmpleado() &&
                !esPropietario) {

            throw new IllegalArgumentException(
                    "No tenés permiso para utilizar esta reserva a favor."
            );
        }


        // ========================================================
        // VERIFICAR UTILIZACIÓN
        // ========================================================

        if (Boolean.TRUE.equals(
                reservaAFavor.getUtilizada())) {

            throw new IllegalArgumentException(
                    "La reserva a favor ya fue utilizada."
            );
        }


        // ========================================================
        // VERIFICAR VENCIMIENTO
        // ========================================================

        if (reservaAFavor.getFechaVencimiento() == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene fecha de vencimiento."
            );
        }

        if (!LocalDate.now().isBefore(
                reservaAFavor.getFechaVencimiento()
        )) {

            reservaAFavor.setUtilizada(true);

            reservaAFavorRepository.save(
                    reservaAFavor
            );

            throw new IllegalArgumentException(
                    "La reserva a favor se encuentra vencida."
            );
        }


        // ========================================================
        // OBTENER RESERVA ORIGINAL
        // ========================================================

        Reserva reservaOrigen =
                reservaAFavor.getReservaOrigen();

        if (reservaOrigen == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene una reserva de origen."
            );
        }


        // ========================================================
        // OBTENER INSTALACIÓN ORIGINAL
        // ========================================================

        Instalacion instalacionOriginal =
                reservaOrigen.getInstalacion();

        if (instalacionOriginal == null ||
                instalacionOriginal.getIdInstalacion() == null) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene una instalación válida."
            );
        }


        // ========================================================
        // OBTENER DURACIÓN ORIGINAL
        // ========================================================

        /*
         * La duración NO la elige el usuario.
         *
         * Se conserva exactamente la duración
         * de la reserva que fue cancelada.
         */

        Integer duracionOriginal =
                reservaOrigen.getDuracionHoras();

        if (duracionOriginal == null ||
                duracionOriginal <= 0) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene una duración válida."
            );
        }


        // ========================================================
        // VALIDAR FECHA
        // ========================================================

        if (nuevaFecha == null) {

            throw new IllegalArgumentException(
                    "La nueva fecha es obligatoria."
            );
        }

        if (nuevaFecha.isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "No se puede realizar una reserva para una fecha pasada."
            );
        }


        // ========================================================
        // VALIDAR HORA
        // ========================================================

        if (nuevaHora == null) {

            throw new IllegalArgumentException(
                    "La nueva hora de inicio es obligatoria."
            );
        }


        // ========================================================
        // CALCULAR FIN
        // ========================================================

        LocalTime nuevaFin =
                nuevaHora.plusHours(
                        duracionOriginal
                );

        if (!nuevaFin.isAfter(nuevaHora)) {

            throw new IllegalArgumentException(
                    "La reserva no puede extenderse más allá de la medianoche."
            );
        }


        // ========================================================
        // VERIFICAR INSTALACIÓN ORIGINAL
        // ========================================================

        Long idInstalacion =
                instalacionOriginal.getIdInstalacion();

        Instalacion instalacion =
                instalacionRepository.findById(
                        idInstalacion
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "La instalación original ya no existe."
                        )
                );


        // ========================================================
        // VERIFICAR ESTADO
        // ========================================================

        if (!"DISPONIBLE".equalsIgnoreCase(
                instalacion.getEstado())) {

            throw new IllegalArgumentException(
                    "La instalación original no está disponible para reservar."
            );
        }


        // ========================================================
        // BUSCAR RESERVAS EXISTENTES
        // ========================================================

        List<Reserva> existentes =
                reservaRepository
                        .findReservasActivasPorInstalacionYFecha(
                                idInstalacion,
                                nuevaFecha
                        );


        // ========================================================
        // COMPROBAR SOLAPAMIENTO
        // ========================================================

        for (Reserva r : existentes) {

            if (r.getHoraInicio() == null ||
                    r.getDuracionHoras() == null) {

                continue;
            }

            LocalTime rInicio =
                    r.getHoraInicio();

            LocalTime rFin =
                    rInicio.plusHours(
                            r.getDuracionHoras()
                    );

            boolean haySolapamiento =
                    nuevaHora.isBefore(rFin) &&
                            nuevaFin.isAfter(rInicio);

            if (haySolapamiento) {

                throw new IllegalArgumentException(
                        "Conflicto de horario: la instalación ya se encuentra ocupada o reservada en ese rango."
                );
            }
        }


        // ========================================================
        // CREAR NUEVA RESERVA
        // ========================================================

        Reserva nuevaReserva =
                new Reserva();


        nuevaReserva.setFechaReserva(
                nuevaFecha
        );

        nuevaReserva.setHoraInicio(
                nuevaHora
        );


        /*
         * IMPORTANTE:
         *
         * Se conserva la duración de la reserva original.
         *
         * Ejemplo:
         *
         * Reserva original = 2 horas
         * Reserva a favor  = 2 horas
         *
         * El usuario NO puede cambiarla a 4 horas.
         */

        nuevaReserva.setDuracionHoras(
                duracionOriginal
        );


        /*
         * La reserva queda confirmada porque
         * se está utilizando una reserva a favor.
         */

        nuevaReserva.setEstado((esAdministrador() || esEmpleado()) ? "CONFIRMADA" : "PENDIENTE_APROBACION");

        /*
         * No se realiza un nuevo pago.
         */

        nuevaReserva.setMontoTotal(
                0.0
        );


        nuevaReserva.setFechaCreacion(
                LocalDateTime.now()
        );


        nuevaReserva.setUsuario(
                reservaAFavor.getUsuario()
        );


        nuevaReserva.setInstalacion(
                instalacion
        );


        // ========================================================
        // GUARDAR NUEVA RESERVA
        // ========================================================

        Reserva reservaCreada =
                reservaRepository.save(
                        nuevaReserva
                );


        // ========================================================
        // MARCAR RESERVA A FAVOR COMO UTILIZADA
        // ========================================================

        reservaAFavor.setUtilizada(
                true
        );

        reservaAFavorRepository.save(
                reservaAFavor
        );


        return reservaCreada;
    }



    // ============================================================
    // MARCAR COMO UTILIZADA
    // ============================================================

    @Transactional
    public void marcarComoUtilizada(Long id) {

        if (!esAdministrador() && !esEmpleado()) {

            throw new IllegalArgumentException(
                    "No tenés permiso para marcar una reserva a favor como utilizada."
            );
        }

        ReservaAFavor reservaAFavor =
                reservaAFavorRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva a favor no encontrada con ID: "
                                                + id
                                )
                        );


        if (Boolean.TRUE.equals(
                reservaAFavor.getUtilizada())) {

            throw new IllegalArgumentException(
                    "La reserva a favor ya fue utilizada."
            );
        }


        if (reservaAFavor.getFechaVencimiento() == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene fecha de vencimiento."
            );
        }


        if (!LocalDate.now().isBefore(
                reservaAFavor.getFechaVencimiento()
        )) {

            throw new IllegalArgumentException(
                    "La reserva a favor se encuentra vencida."
            );
        }


        reservaAFavor.setUtilizada(true);

        reservaAFavorRepository.save(
                reservaAFavor
        );
    }

    @Transactional
    public Reserva aprobarReprogramacion(Long idReserva) {
        if (!esAdministrador() && !esEmpleado()) {
            throw new IllegalArgumentException("Solo un administrador o empleado puede aprobar reprogramaciones.");
        }

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        if (!"PENDIENTE_APROBACION".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalArgumentException("Esta reserva no está pendiente de aprobación.");
        }

        reserva.setEstado("CONFIRMADA");
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva rechazarReprogramacion(Long idReserva) {
        if (!esAdministrador() && !esEmpleado()) {
            throw new IllegalArgumentException("Solo un administrador o empleado puede rechazar reprogramaciones.");
        }

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        if (!"PENDIENTE_APROBACION".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalArgumentException("Esta reserva no está pendiente de aprobación.");
        }

        reserva.setEstado("CANCELADA");
        Reserva cancelada = reservaRepository.save(reserva);

        // Devolver la reserva a favor al usuario para que pueda proponer otra fecha
        if (reserva.getUsuario() != null) {
            reservaAFavorRepository.findByUsuarioIdUsuario(reserva.getUsuario().getIdUsuario())
                    .stream()
                    .filter(rf -> Boolean.TRUE.equals(rf.getUtilizada())
                            && rf.getReservaOrigen() != null
                            && rf.getReservaOrigen().getInstalacion() != null
                            && reserva.getInstalacion() != null
                            && rf.getReservaOrigen().getInstalacion().getIdInstalacion()
                            .equals(reserva.getInstalacion().getIdInstalacion()))
                    .reduce((primera, segunda) -> segunda) // la más reciente
                    .ifPresent(rf -> {
                        rf.setUtilizada(false);
                        reservaAFavorRepository.save(rf);
                    });
        }

        return cancelada;
    }
    // ============================================================
    // ELIMINAR RESERVA A FAVOR
    // ============================================================

    @Transactional
    public void eliminarReservaAFavor(Long id) {

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar una reserva a favor."
            );
        }


        if (!reservaAFavorRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Reserva a favor no encontrada con ID: " + id
            );
        }


        reservaAFavorRepository.deleteById(id);
    }


    // ============================================================
    // OBTENER USUARIO AUTENTICADO
    // ============================================================

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


    // ============================================================
    // VERIFICAR ADMINISTRADOR
    // ============================================================

    private boolean esAdministrador() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }


        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMINISTRADOR"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }


    // ============================================================
    // VERIFICAR EMPLEADO
    // ============================================================

    private boolean esEmpleado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }


        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_EMPLEADO"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }
}