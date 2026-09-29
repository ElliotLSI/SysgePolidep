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

        // ADMINISTRADOR y EMPLEADO pueden consultar todas
        if (esAdministrador() || esEmpleado()) {
            return reservaRepository.findAll();
        }

        // Usuario normal solamente puede consultar sus reservas
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

        // ADMINISTRADOR y EMPLEADO pueden consultar cualquiera
        if (esAdministrador() || esEmpleado()) {
            return Optional.of(reserva);
        }

        // Usuario normal solamente puede consultar la propia
        Usuario usuarioActual = obtenerUsuarioActual();

        if (reserva.getUsuario() == null ||
                !reserva.getUsuario().getIdUsuario()
                        .equals(usuarioActual.getIdUsuario())) {

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


        // ========================================================
        // OBTENER USUARIO AUTENTICADO
        // ========================================================

        Usuario usuarioActual = obtenerUsuarioActual();


        // ========================================================
        // VALIDAR USUARIO RECIBIDO
        // ========================================================

        if (nuevaReserva.getUsuario() == null ||
                nuevaReserva.getUsuario().getIdUsuario() == null) {

            // Para usuarios normales usamos automáticamente
            // el usuario autenticado.
            nuevaReserva.setUsuario(usuarioActual);

        } else {

            Long idUsuarioSolicitado =
                    nuevaReserva.getUsuario().getIdUsuario();

            Optional<Usuario> usuarioExistente =
                    usuarioRepository.findById(idUsuarioSolicitado);

            if (usuarioExistente.isEmpty()) {
                throw new IllegalArgumentException(
                        "El usuario asociado no existe."
                );
            }

            // ADMINISTRADOR y EMPLEADO pueden crear reservas
            // para otros usuarios.
            if (esAdministrador() || esEmpleado()) {

                nuevaReserva.setUsuario(
                        usuarioExistente.get()
                );

            } else {

                // SOCIO / USUARIO solamente pueden reservar
                // para sí mismos.
                if (!usuarioExistente.get().getIdUsuario()
                        .equals(usuarioActual.getIdUsuario())) {

                    throw new IllegalArgumentException(
                            "No podés crear una reserva para otro usuario."
                    );
                }

                nuevaReserva.setUsuario(usuarioActual);
            }
        }


        // ========================================================
        // VALIDAR INSTALACIÓN
        // ========================================================

        if (nuevaReserva.getInstalacion() == null ||
                nuevaReserva.getInstalacion().getIdInstalacion() == null) {

            throw new IllegalArgumentException(
                    "La reserva debe estar asociada a una instalación."
            );
        }

        Long idInstalacion =
                nuevaReserva.getInstalacion().getIdInstalacion();

        Optional<Instalacion> instalacionExistente =
                instalacionRepository.findById(idInstalacion);

        if (instalacionExistente.isEmpty()) {
            throw new IllegalArgumentException(
                    "La instalación asociada no existe."
            );
        }

        Instalacion instalacion =
                instalacionExistente.get();

        nuevaReserva.setInstalacion(instalacion);


        // ========================================================
        // VALIDAR ESTADO DE INSTALACIÓN
        // ========================================================

        if (!"DISPONIBLE".equalsIgnoreCase(
                instalacion.getEstado())) {

            throw new IllegalArgumentException(
                    "La instalación no está disponible para reservar."
            );
        }


        // ========================================================
        // VALIDAR FECHA
        // ========================================================

        if (nuevaReserva.getFechaReserva() == null) {
            throw new IllegalArgumentException(
                    "La fecha de la reserva es obligatoria."
            );
        }

        LocalDate fechaReserva =
                nuevaReserva.getFechaReserva();

        if (fechaReserva.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede realizar una reserva para una fecha pasada."
            );
        }


        // ========================================================
        // VALIDAR HORA
        // ========================================================

        if (nuevaReserva.getHoraInicio() == null) {
            throw new IllegalArgumentException(
                    "La hora de inicio es obligatoria."
            );
        }


        // ========================================================
        // VALIDAR DURACIÓN
        // ========================================================

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


        // ========================================================
        // CALCULAR HORARIO
        // ========================================================

        LocalTime nuevaInicio =
                nuevaReserva.getHoraInicio();

        LocalTime nuevaFin =
                nuevaInicio.plusHours(
                        nuevaReserva.getDuracionHoras()
                );

        // Si da una hora anterior o igual significa que
        // la reserva pasa de medianoche.
        if (!nuevaFin.isAfter(nuevaInicio)) {

            throw new IllegalArgumentException(
                    "La reserva no puede extenderse más allá de la medianoche."
            );
        }


        // ========================================================
        // CALCULAR MONTO
        // ========================================================

        if (instalacion.getTarifaBase() == null ||
                instalacion.getTarifaBase() < 0) {

            throw new IllegalArgumentException(
                    "La tarifa base de la instalación no es válida."
            );
        }

        double montoBase =
                instalacion.getTarifaBase()
                        * nuevaReserva.getDuracionHoras();

        double descuento = 0.0;


        // ========================================================
        // BUSCAR MEMBRESÍA VIGENTE
        // ========================================================

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


        // ========================================================
        // CALCULAR MONTO FINAL
        // ========================================================

        double montoFinal =
                montoBase -
                        (montoBase * descuento / 100.0);

        if (montoFinal < 0) {
            montoFinal = 0;
        }

        nuevaReserva.setMontoTotal(montoFinal);


        // ========================================================
        // ESTADO
        // ========================================================

        /*
         * IMPORTANTE:
         *
         * Una reserva creada desde este endpoint siempre
         * comienza PENDIENTE.
         *
         * El cliente NO puede mandar "CONFIRMADA" para
         * saltear el pago.
         *
         * El PagoService será el encargado de confirmar
         * la reserva cuando el pago sea aprobado.
         */

        nuevaReserva.setEstado("PENDIENTE");


        // ========================================================
        // FECHA DE CREACIÓN
        // ========================================================

        if (nuevaReserva.getFechaCreacion() == null) {

            nuevaReserva.setFechaCreacion(
                    LocalDateTime.now()
            );
        }


        // ========================================================
        // BUSCAR RESERVAS EXISTENTES
        // ========================================================

        List<Reserva> existentes =
                reservaRepository
                        .findReservasActivasPorInstalacionYFecha(
                                idInstalacion,
                                fechaReserva
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
                    nuevaInicio.isBefore(rFin) &&
                            nuevaFin.isAfter(rInicio);

            if (haySolapamiento) {

                throw new IllegalArgumentException(
                        "Conflicto de horario: la instalación ya se encuentra ocupada o reservada en ese rango."
                );
            }
        }


        // ========================================================
        // GUARDAR
        // ========================================================

        return reservaRepository.save(nuevaReserva);
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


        // ========================================================
        // BUSCAR RESERVA
        // ========================================================

        Reserva reserva =
                reservaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva no encontrada con ID: " + id
                                )
                        );


        // ========================================================
        // VERIFICAR PROPIETARIO
        // ========================================================

        Usuario usuarioActual =
                obtenerUsuarioActual();

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


        // ========================================================
        // VERIFICAR ESTADO
        // ========================================================

        if ("CANCELADA".equals(
                reserva.getEstado())) {

            throw new IllegalArgumentException(
                    "La reserva ya se encuentra cancelada."
            );
        }


        // ========================================================
        // VERIFICAR ANTICIPACIÓN
        // ========================================================

        /*
         * Por ahora controlamos que la reserva no haya comenzado.
         *
         * La regla de negocio del proyecto establece una
         * anticipación mínima de 48 horas hábiles.
         *
         * Como todavía no tenemos definido en el sistema
         * el horario laboral oficial para calcular "horas hábiles",
         * no inventamos ese horario acá.
         *
         * Sí impedimos cancelar una reserva que ya comenzó.
         */

        LocalDateTime inicioReserva =
                LocalDateTime.of(
                        reserva.getFechaReserva(),
                        reserva.getHoraInicio()
                );

        if (LocalDateTime.now()
                .isAfter(inicioReserva)) {

            throw new IllegalArgumentException(
                    "No se puede cancelar una reserva que ya comenzó."
            );
        }


        // ========================================================
        // CANCELAR
        // ========================================================

        reserva.setEstado("CANCELADA");

        reservaRepository.save(reserva);


        // ========================================================
        // CREAR RESERVA A FAVOR
        // ========================================================

        if (reservaAFavorRepository
                .findByReservaOrigenIdReserva(id)
                .isEmpty()) {

            ReservaAFavor reservaAFavor =
                    new ReservaAFavor();


            // ----------------------------------------------------
            // MONTO ACREDITADO
            // ----------------------------------------------------

            reservaAFavor.setMontoAcreditado(
                    reserva.getMontoTotal()
            );


            // ----------------------------------------------------
            // FECHA DE GENERACIÓN
            // ----------------------------------------------------

            LocalDate fechaGeneracion =
                    LocalDate.now();

            reservaAFavor.setFechaGeneracion(
                    fechaGeneracion
            );


            // ----------------------------------------------------
            // FECHA DE VENCIMIENTO
            // ----------------------------------------------------

            reservaAFavor.setFechaVencimiento(
                    fechaGeneracion.plusMonths(3)
            );


            // ----------------------------------------------------
            // NO UTILIZADA
            // ----------------------------------------------------

            reservaAFavor.setUtilizada(false);


            // ----------------------------------------------------
            // RESERVA DE ORIGEN
            // ----------------------------------------------------

            reservaAFavor.setReservaOrigen(
                    reserva
            );


            // ----------------------------------------------------
            // USUARIO DUEÑO
            // ----------------------------------------------------

            reservaAFavor.setUsuario(
                    reserva.getUsuario()
            );


            // ----------------------------------------------------
            // GUARDAR CRÉDITO
            // ----------------------------------------------------

            reservaAFavorRepository.save(
                    reservaAFavor
            );
        }
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
                .findByNombreUsuario(nombreUsuario)
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

