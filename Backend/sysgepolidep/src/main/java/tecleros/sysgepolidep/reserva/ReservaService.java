package tecleros.sysgepolidep.reserva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.reservaAFavor.ReservaAFavor;
import tecleros.sysgepolidep.reservaAFavor.ReservaAFavorRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private static final LocalTime HORA_APERTURA = LocalTime.of(10, 0);
    private static final LocalTime HORA_CIERRE = LocalTime.of(3, 0);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private ReservaAFavorRepository reservaAFavorRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;


    // ============================================================
    // LISTAR RESERVAS
    // ============================================================

    public List<Reserva> listarTodas() {
        Usuario usuarioActual = obtenerUsuarioActual();

        if (esAdministrador() || esEmpleado()) {
            return reservaRepository.findAll();
        }

        return reservaRepository.findAll()
                .stream()
                .filter(r ->
                        r.getUsuario() != null &&
                                r.getUsuario().getIdUsuario()
                                        .equals(usuarioActual.getIdUsuario())
                )
                .toList();
    }


    // ============================================================
    // BUSCAR RESERVA POR ID
    // ============================================================

    public Optional<Reserva> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la reserva es obligatorio.");
        }

        Optional<Reserva> reservaOpt = reservaRepository.findById(id);

        if (reservaOpt.isEmpty()) {
            return Optional.empty();
        }

        Reserva reserva = reservaOpt.get();

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(reserva);
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        if (reserva.getUsuario() == null ||
                !reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
            throw new IllegalArgumentException("No tenés permiso para consultar esta reserva.");
        }

        return Optional.of(reserva);
    }


    // ============================================================
    // CREAR RESERVA
    // ============================================================

    @Transactional
    public Reserva crearReserva(Reserva nuevaReserva) {
        if (nuevaReserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        // VALIDAR USUARIO RECIBIDO
        if (nuevaReserva.getUsuario() == null ||
                nuevaReserva.getUsuario().getIdUsuario() == null) {
            nuevaReserva.setUsuario(usuarioActual);
        } else {
            Long idUsuarioSolicitado = nuevaReserva.getUsuario().getIdUsuario();
            Usuario usuarioExistente = usuarioRepository.findById(idUsuarioSolicitado)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario asociado no existe."));

            if (esAdministrador() || esEmpleado()) {
                nuevaReserva.setUsuario(usuarioExistente);
            } else {
                if (!usuarioExistente.getIdUsuario().equals(usuarioActual.getIdUsuario())) {
                    throw new IllegalArgumentException("No podés crear una reserva para otro usuario.");
                }
                nuevaReserva.setUsuario(usuarioActual);
            }
        }

        // VALIDAR INSTALACIÓN
        if (nuevaReserva.getInstalacion() == null ||
                nuevaReserva.getInstalacion().getIdInstalacion() == null) {
            throw new IllegalArgumentException("La reserva debe estar asociada a una instalación.");
        }

        Long idInstalacion = nuevaReserva.getInstalacion().getIdInstalacion();
        Instalacion instalacion = instalacionRepository.findById(idInstalacion)
                .orElseThrow(() -> new IllegalArgumentException("La instalación asociada no existe."));

        nuevaReserva.setInstalacion(instalacion);

        if (!"DISPONIBLE".equalsIgnoreCase(instalacion.getEstado())) {
            throw new IllegalArgumentException("La instalación no está disponible para reservar.");
        }

        // VALIDAR FECHA Y HORA
        if (nuevaReserva.getFechaReserva() == null) {
            throw new IllegalArgumentException("La fecha de la reserva es obligatoria.");
        }

        if (nuevaReserva.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria.");
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaReserva = nuevaReserva.getFechaReserva();
        LocalTime horaInicio = nuevaReserva.getHoraInicio();

        LocalDateTime inicioDT = LocalDateTime.of(fechaReserva, horaInicio);

        if (inicioDT.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede realizar una reserva para una fecha u horario que ya pasó.");
        }

        // VALIDAR DURACIÓN
        if (nuevaReserva.getDuracionHoras() == null || nuevaReserva.getDuracionHoras() <= 0) {
            throw new IllegalArgumentException("La duración de la reserva debe ser mayor a 0.");
        }

        if (nuevaReserva.getDuracionHoras() > 17) {
            throw new IllegalArgumentException("La duración de la reserva supera el horario de atención.");
        }

        // VALIDAR HORARIO DE ATENCIÓN (10:00 A 03:00)
        boolean fueraDeHorarioInicio = !horaInicio.isBefore(HORA_CIERRE) && horaInicio.isBefore(HORA_APERTURA);
        if (fueraDeHorarioInicio) {
            throw new IllegalArgumentException("El horario de reservas es de 10:00 a 03:00.");
        }

        LocalDateTime finDT = inicioDT.plusHours(nuevaReserva.getDuracionHoras());
        LocalDateTime limiteCierre = horaInicio.isBefore(HORA_CIERRE)
                ? LocalDateTime.of(fechaReserva, HORA_CIERRE)
                : LocalDateTime.of(fechaReserva.plusDays(1), HORA_CIERRE);

        if (finDT.isAfter(limiteCierre)) {
            throw new IllegalArgumentException("La reserva no puede extenderse más allá de las 03:00.");
        }

        // CALCULAR MONTO Y DESCUENTO CON MEMBRESÍA VIGENTE REAL
        if (instalacion.getTarifaBase() == null || instalacion.getTarifaBase() < 0) {
            throw new IllegalArgumentException("La tarifa base de la instalación no es válida.");
        }

        double montoBase = instalacion.getTarifaBase() * nuevaReserva.getDuracionHoras();
        double descuento = 0.0;

        List<Membresia> membresiasVigentes = membresiaRepository
                .findBySocioIdUsuarioAndEstado(
                        nuevaReserva.getUsuario().getIdUsuario(),
                        "VIGENTE"
                )
                .stream()
                .filter(m ->
                        m.getFechaInicio() != null &&
                                m.getFechaVenc() != null &&
                                !hoy.isBefore(m.getFechaInicio()) &&
                                !hoy.isAfter(m.getFechaVenc())
                )
                .toList();

        if (!membresiasVigentes.isEmpty()) {
            Membresia membresia = membresiasVigentes.get(0);
            if (membresia.getCategoria() != null &&
                    membresia.getCategoria().getPorcDescuento() != null) {
                descuento = membresia.getCategoria().getPorcDescuento();
            }
        }

        double montoFinal = montoBase - (montoBase * descuento / 100.0);
        if (montoFinal < 0) {
            montoFinal = 0;
        }

        nuevaReserva.setMontoTotal(montoFinal);
        nuevaReserva.setEstado("PENDIENTE");

        if (nuevaReserva.getFechaCreacion() == null) {
            nuevaReserva.setFechaCreacion(LocalDateTime.now());
        }

        // COMPROBAR SOLAPAMIENTO (INCLUYENDO CRUCE DE MEDIANOCHE)
        List<Reserva> existentes = new ArrayList<>();
        existentes.addAll(reservaRepository.findReservasActivasPorInstalacionYFecha(idInstalacion, fechaReserva.minusDays(1)));
        existentes.addAll(reservaRepository.findReservasActivasPorInstalacionYFecha(idInstalacion, fechaReserva));
        existentes.addAll(reservaRepository.findReservasActivasPorInstalacionYFecha(idInstalacion, fechaReserva.plusDays(1)));

        for (Reserva r : existentes) {
            if (r.getFechaReserva() == null || r.getHoraInicio() == null || r.getDuracionHoras() == null) {
                continue;
            }

            LocalDateTime rInicio = LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());
            LocalDateTime rFin = rInicio.plusHours(r.getDuracionHoras());

            boolean haySolapamiento = inicioDT.isBefore(rFin) && finDT.isAfter(rInicio);

            if (haySolapamiento) {
                throw new IllegalArgumentException(
                        "Conflicto de horario: la instalación ya se encuentra ocupada o reservada en ese rango."
                );
            }
        }

        return reservaRepository.save(nuevaReserva);
    }


    // ============================================================
    // CANCELAR RESERVA
    // ============================================================

    @Transactional
    public void cancelarReserva(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la reserva es obligatorio.");
        }

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        Usuario usuarioActual = obtenerUsuarioActual();

        boolean esPropietario = reserva.getUsuario() != null &&
                reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario());

        if (!esAdministrador() && !esEmpleado() && !esPropietario) {
            throw new IllegalArgumentException("No tenés permiso para cancelar esta reserva.");
        }

        if ("CANCELADA".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalArgumentException("La reserva ya se encuentra cancelada.");
        }

        boolean estabaConfirmada = "CONFIRMADA".equalsIgnoreCase(reserva.getEstado());

        // La regla de las 48 hs hábiles aplica a reservas ya confirmadas (pagas)
        if (estabaConfirmada) {
            LocalDateTime inicioReserva = LocalDateTime.of(
                    reserva.getFechaReserva(),
                    reserva.getHoraInicio()
            );

            LocalDateTime limiteCancelacion = calcularLimiteCancelacion(inicioReserva);

            if (LocalDateTime.now().isAfter(limiteCancelacion)) {
                throw new IllegalArgumentException(
                        "La reserva debe cancelarse con al menos 48 horas hábiles de anticipación."
                );
            }
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);

        // SOLO genera reserva a favor si el pago ya estaba confirmado
        if (estabaConfirmada && reservaAFavorRepository.findByReservaOrigenIdReserva(id).isEmpty()) {
            ReservaAFavor reservaAFavor = new ReservaAFavor();
            LocalDate fechaGeneracion = LocalDate.now();

            reservaAFavor.setFechaGeneracion(fechaGeneracion);
            reservaAFavor.setFechaVencimiento(fechaGeneracion.plusMonths(3));
            reservaAFavor.setUtilizada(false);
            reservaAFavor.setReservaOrigen(reserva);
            reservaAFavor.setUsuario(reserva.getUsuario());

            reservaAFavorRepository.save(reservaAFavor);
        }
    }


    // ============================================================
    // CALCULAR LÍMITE DE CANCELACIÓN
    // ============================================================

    private LocalDateTime calcularLimiteCancelacion(LocalDateTime inicioReserva) {
        LocalDateTime limite = inicioReserva;
        int horasHabiles = 0;

        while (horasHabiles < 48) {
            limite = limite.minusHours(1);
            if (limite.getDayOfWeek().getValue() <= 5) {
                horasHabiles++;
            }
        }

        return limite;
    }


    // ============================================================
    // HELPERS DE SEGURIDAD
    // ============================================================

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