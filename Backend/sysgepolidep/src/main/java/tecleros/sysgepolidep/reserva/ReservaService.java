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
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

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
                                r.getUsuario()
                                        .getIdUsuario()
                                        .equals(
                                                usuarioActual.getIdUsuario()
                                        )
                )
                .toList();
    }


    // ============================================================
    // BUSCAR RESERVA POR ID
    // ============================================================

    public Optional<Reserva> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la reserva es obligatorio."
            );
        }

        Optional<Reserva> reservaOpt =
                reservaRepository.findById(id);

        if (reservaOpt.isEmpty()) {
            return Optional.empty();
        }

        Reserva reserva = reservaOpt.get();

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(reserva);
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (reserva.getUsuario() == null ||
                !reserva.getUsuario()
                        .getIdUsuario()
                        .equals(
                                usuarioActual.getIdUsuario()
                        )) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar esta reserva."
            );
        }

        return Optional.of(reserva);
    }


    // ============================================================
    // CREAR RESERVA
    // ============================================================

    @Transactional
    public Reserva crearReserva(Reserva nuevaReserva) {

        if (nuevaReserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula."
            );
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();


        // --------------------------------------------------------
        // USUARIO
        // --------------------------------------------------------

        if (nuevaReserva.getUsuario() == null ||
                nuevaReserva.getUsuario().getIdUsuario() == null) {

            nuevaReserva.setUsuario(usuarioActual);

        } else {

            Long idUsuarioSolicitado =
                    nuevaReserva.getUsuario()
                            .getIdUsuario();

            Optional<Usuario> usuarioExistente =
                    usuarioRepository.findById(
                            idUsuarioSolicitado
                    );

            if (usuarioExistente.isEmpty()) {

                throw new IllegalArgumentException(
                        "El usuario asociado no existe."
                );
            }

            if (esAdministrador() || esEmpleado()) {

                nuevaReserva.setUsuario(
                        usuarioExistente.get()
                );

            } else {

                if (!usuarioExistente.get()
                        .getIdUsuario()
                        .equals(
                                usuarioActual.getIdUsuario()
                        )) {

                    throw new IllegalArgumentException(
                            "No podés crear una reserva para otro usuario."
                    );
                }

                nuevaReserva.setUsuario(
                        usuarioActual
                );
            }
        }


        // --------------------------------------------------------
        // INSTALACIÓN
        // --------------------------------------------------------

        if (nuevaReserva.getInstalacion() == null ||
                nuevaReserva.getInstalacion()
                        .getIdInstalacion() == null) {

            throw new IllegalArgumentException(
                    "La reserva debe estar asociada a una instalación."
            );
        }

        Long idInstalacion =
                nuevaReserva.getInstalacion()
                        .getIdInstalacion();

        Optional<Instalacion> instalacionExistente =
                instalacionRepository.findById(
                        idInstalacion
                );

        if (instalacionExistente.isEmpty()) {

            throw new IllegalArgumentException(
                    "La instalación asociada no existe."
            );
        }

        Instalacion instalacion =
                instalacionExistente.get();

        nuevaReserva.setInstalacion(
                instalacion
        );


        // --------------------------------------------------------
        // ESTADO DE INSTALACIÓN
        // --------------------------------------------------------

        if (!"DISPONIBLE".equalsIgnoreCase(
                instalacion.getEstado()
        )) {

            throw new IllegalArgumentException(
                    "La instalación no está disponible para reservar."
            );
        }


        // --------------------------------------------------------
        // FECHA
        // --------------------------------------------------------

        if (nuevaReserva.getFechaReserva() == null) {

            throw new IllegalArgumentException(
                    "La fecha de la reserva es obligatoria."
            );
        }

        LocalDate fechaReserva =
                nuevaReserva.getFechaReserva();

        if (fechaReserva.isBefore(
                LocalDate.now()
        )) {

            throw new IllegalArgumentException(
                    "No se puede realizar una reserva para una fecha pasada."
            );
        }


        // --------------------------------------------------------
        // HORA
        // --------------------------------------------------------

        if (nuevaReserva.getHoraInicio() == null) {

            throw new IllegalArgumentException(
                    "La hora de inicio es obligatoria."
            );
        }


        // --------------------------------------------------------
        // DURACIÓN
        // --------------------------------------------------------

        if (nuevaReserva.getDuracionHoras() == null ||
                nuevaReserva.getDuracionHoras() <= 0) {

            throw new IllegalArgumentException(
                    "La duración de la reserva debe ser mayor a 0."
            );
        }

        if (nuevaReserva.getDuracionHoras() > 24) {

            throw new IllegalArgumentException(
                    "La duración de la reserva no puede superar las 24 horas."
            );
        }


        // --------------------------------------------------------
        // HORARIO
        // --------------------------------------------------------

        LocalTime nuevaInicio =
                nuevaReserva.getHoraInicio();

        LocalTime nuevaFin =
                nuevaInicio.plusHours(
                        nuevaReserva.getDuracionHoras()
                );

        if (!nuevaFin.isAfter(nuevaInicio)) {

            throw new IllegalArgumentException(
                    "La reserva no puede extenderse más allá de la medianoche."
            );
        }


        // --------------------------------------------------------
        // TARIFA
        // --------------------------------------------------------

        if (instalacion.getTarifaBase() == null ||
                instalacion.getTarifaBase() < 0) {

            throw new IllegalArgumentException(
                    "La tarifa base de la instalación no es válida."
            );
        }


        // --------------------------------------------------------
        // MONTO BASE
        // --------------------------------------------------------

        double montoBase =
                instalacion.getTarifaBase()
                        * nuevaReserva.getDuracionHoras();


        // --------------------------------------------------------
        // DESCUENTO POR MEMBRESÍA
        // --------------------------------------------------------

        double descuento = 0.0;

        List<Membresia> membresiasVigentes =
                membresiaRepository
                        .findBySocioIdUsuarioAndEstado(
                                nuevaReserva.getUsuario()
                                        .getIdUsuario(),
                                "VIGENTE"
                        );

        if (!membresiasVigentes.isEmpty()) {

            Membresia membresia =
                    membresiasVigentes.get(0);

            if (membresia.getCategoria() != null &&
                    membresia.getCategoria()
                            .getPorcDescuento() != null) {

                descuento =
                        membresia.getCategoria()
                                .getPorcDescuento();
            }
        }


        // --------------------------------------------------------
        // MONTO FINAL
        // --------------------------------------------------------

        double montoFinal =
                montoBase -
                        (
                                montoBase
                                        * descuento
                                        / 100.0
                        );

        if (montoFinal < 0) {
            montoFinal = 0;
        }

        montoFinal =
                Math.round(
                        montoFinal * 100.0
                ) / 100.0;

        nuevaReserva.setMontoTotal(
                montoFinal
        );

        nuevaReserva.setEstado(
                "PENDIENTE"
        );


        // --------------------------------------------------------
        // FECHA DE CREACIÓN
        // --------------------------------------------------------

        if (nuevaReserva.getFechaCreacion() == null) {

            nuevaReserva.setFechaCreacion(
                    LocalDateTime.now()
            );
        }


        // --------------------------------------------------------
        // VALIDAR SOLAPAMIENTO
        // --------------------------------------------------------

        List<Reserva> existentes =
                reservaRepository
                        .findReservasActivasPorInstalacionYFecha(
                                idInstalacion,
                                fechaReserva
                        );

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
                    nuevaInicio.isBefore(rFin) &&
                            nuevaFin.isAfter(rInicio);

            if (haySolapamiento) {

                throw new IllegalArgumentException(
                        "Conflicto de horario: la instalación ya se encuentra ocupada o reservada en ese rango."
                );
            }
        }


        return reservaRepository.save(
                nuevaReserva
        );
    }


    // ============================================================
    // CANCELAR RESERVA
    // ============================================================

    @Transactional
    public void cancelarReserva(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID de la reserva es obligatorio."
            );
        }

        Reserva reserva =
                reservaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva no encontrada con ID: "
                                                + id
                                )
                        );


        // --------------------------------------------------------
        // USUARIO ACTUAL
        // --------------------------------------------------------

        Usuario usuarioActual =
                obtenerUsuarioActual();


        // --------------------------------------------------------
        // VERIFICAR PROPIETARIO
        // --------------------------------------------------------

        boolean esPropietario =
                reserva.getUsuario() != null &&
                        reserva.getUsuario()
                                .getIdUsuario()
                                .equals(
                                        usuarioActual.getIdUsuario()
                                );

        if (!esAdministrador() &&
                !esEmpleado() &&
                !esPropietario) {

            throw new IllegalArgumentException(
                    "No tenés permiso para cancelar esta reserva."
            );
        }


        // --------------------------------------------------------
        // VERIFICAR SI YA ESTÁ CANCELADA
        // --------------------------------------------------------

        if ("CANCELADA".equals(
                reserva.getEstado()
        )) {

            throw new IllegalArgumentException(
                    "La reserva ya se encuentra cancelada."
            );
        }


        // --------------------------------------------------------
        // VERIFICAR PLAZO DE CANCELACIÓN
        // --------------------------------------------------------

        LocalDateTime inicioReserva =
                LocalDateTime.of(
                        reserva.getFechaReserva(),
                        reserva.getHoraInicio()
                );

        LocalDateTime limiteCancelacion =
                calcularLimiteCancelacion(
                        inicioReserva
                );

        if (LocalDateTime.now()
                .isAfter(limiteCancelacion)) {

            throw new IllegalArgumentException(
                    "La reserva debe cancelarse con al menos 48 horas hábiles de anticipación."
            );
        }


        // --------------------------------------------------------
        // GUARDAR EL MONTO ORIGINAL
        // --------------------------------------------------------

        Double montoOriginal =
                reserva.getMontoTotal();

        if (montoOriginal == null ||
                montoOriginal <= 0) {

            throw new IllegalArgumentException(
                    "La reserva no tiene un monto válido para generar la reserva a favor."
            );
        }

        montoOriginal =
                Math.round(
                        montoOriginal * 100.0
                ) / 100.0;


        // --------------------------------------------------------
        // CANCELAR RESERVA
        // --------------------------------------------------------

        reserva.setEstado(
                "CANCELADA"
        );

        reservaRepository.save(
                reserva
        );


        // --------------------------------------------------------
        // GENERAR RESERVA A FAVOR
        // --------------------------------------------------------

        Optional<ReservaAFavor> reservaAFavorExistente =
                reservaAFavorRepository
                        .findByReservaOrigenIdReserva(id);

        if (reservaAFavorExistente.isEmpty()) {

            ReservaAFavor reservaAFavor =
                    new ReservaAFavor();


            /*
             * IMPORTANTE:
             *
             * Si la reserva cancelada vale $45.000,
             * la reserva a favor queda en $45.000.
             *
             * No se descuenta nada en este momento.
             */
            reservaAFavor.setMontoAcreditado(
                    montoOriginal
            );


            LocalDate fechaGeneracion =
                    LocalDate.now();

            reservaAFavor.setFechaGeneracion(
                    fechaGeneracion
            );

            reservaAFavor.setFechaVencimiento(
                    fechaGeneracion.plusMonths(3)
            );

            reservaAFavor.setUtilizada(
                    false
            );

            reservaAFavor.setReservaOrigen(
                    reserva
            );

            reservaAFavor.setUsuario(
                    reserva.getUsuario()
            );


            reservaAFavorRepository.save(
                    reservaAFavor
            );
        }
    }


    // ============================================================
    // CALCULAR 48 HORAS HÁBILES
    // ============================================================

    private LocalDateTime calcularLimiteCancelacion(
            LocalDateTime inicioReserva
    ) {

        LocalDateTime limite =
                inicioReserva;

        int horasHabiles = 0;

        while (horasHabiles < 48) {

            limite =
                    limite.minusHours(1);

            if (limite.getDayOfWeek()
                    .getValue() <= 5) {

                horasHabiles++;
            }
        }

        return limite;
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