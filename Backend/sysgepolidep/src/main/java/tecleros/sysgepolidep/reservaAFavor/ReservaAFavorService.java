package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
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

    @Autowired
    private MembresiaRepository membresiaRepository;


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

        /*
         * Una reserva a favor disponible debe:
         *
         * 1. Pertenecer al usuario.
         * 2. No estar utilizada.
         * 3. Tener saldo mayor a $0.
         */

        if (esAdministrador() || esEmpleado()) {

            return reservaAFavorRepository
                    .findByUsuarioIdUsuarioAndUtilizadaFalseAndMontoAcreditadoGreaterThan(
                            idUsuario,
                            0.0
                    );
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
                .findByUsuarioIdUsuarioAndUtilizadaFalseAndMontoAcreditadoGreaterThan(
                        idUsuario,
                        0.0
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
        // MONTO
        // ========================================================

        Double monto =
                reservaExistente.getMontoTotal();

        if (monto == null || monto <= 0) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene un monto válido para generar la reserva a favor."
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

        reservaAFavor.setMontoAcreditado(
                monto
        );

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
            LocalTime nuevaHora,
            Integer nuevaDuracion,
            Long idInstalacion) {


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

            throw new IllegalArgumentException(
                    "La reserva a favor se encuentra vencida."
            );
        }


        // ========================================================
        // VALIDAR SALDO
        // ========================================================

        Double saldoDisponible =
                reservaAFavor.getMontoAcreditado();

        if (saldoDisponible == null ||
                saldoDisponible <= 0) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene saldo disponible."
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
                    "No se puede reprogramar una reserva para una fecha pasada."
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
        // VALIDAR DURACIÓN
        // ========================================================

        if (nuevaDuracion == null ||
                nuevaDuracion <= 0) {

            throw new IllegalArgumentException(
                    "La duración debe ser mayor a 0."
            );
        }

        if (nuevaDuracion > 24) {

            throw new IllegalArgumentException(
                    "La duración no puede superar las 24 horas."
            );
        }


        // ========================================================
        // CALCULAR FIN
        // ========================================================

        LocalTime nuevaFin =
                nuevaHora.plusHours(
                        nuevaDuracion
                );

        if (!nuevaFin.isAfter(nuevaHora)) {

            throw new IllegalArgumentException(
                    "La reserva no puede extenderse más allá de la medianoche."
            );
        }


        // ========================================================
        // VALIDAR INSTALACIÓN
        // ========================================================

        if (idInstalacion == null) {

            throw new IllegalArgumentException(
                    "La instalación es obligatoria."
            );
        }

        Instalacion instalacion =
                instalacionRepository.findById(
                        idInstalacion
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "La instalación no existe."
                        )
                );


        // ========================================================
        // VERIFICAR ESTADO
        // ========================================================

        if (!"DISPONIBLE".equalsIgnoreCase(
                instalacion.getEstado())) {

            throw new IllegalArgumentException(
                    "La instalación no está disponible para reservar."
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
        // CALCULAR PRECIO
        // ========================================================

        if (instalacion.getTarifaBase() == null ||
                instalacion.getTarifaBase() < 0) {

            throw new IllegalArgumentException(
                    "La tarifa base de la instalación no es válida."
            );
        }

        double montoBase =
                instalacion.getTarifaBase()
                        * nuevaDuracion;

        double descuento = 0.0;


        // ========================================================
        // BUSCAR MEMBRESÍA VIGENTE
        // ========================================================

        Usuario usuario =
                reservaAFavor.getUsuario();

        List<Membresia> membresiasVigentes =
                membresiaRepository
                        .findBySocioIdUsuarioAndEstado(
                                usuario.getIdUsuario(),
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
        // CALCULAR MONTO DE NUEVA RESERVA
        // ========================================================

        double montoNuevaReserva =
                montoBase -
                        (montoBase * descuento / 100.0);

        if (montoNuevaReserva < 0) {
            montoNuevaReserva = 0;
        }


        // ========================================================
        // VERIFICAR SI EL SALDO ALCANZA
        // ========================================================

        if (saldoDisponible < montoNuevaReserva) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene saldo suficiente. "
                            + "Saldo disponible: $"
                            + String.format(
                            "%.2f",
                            saldoDisponible
                    )
                            + ". Monto de la nueva reserva: $"
                            + String.format(
                            "%.2f",
                            montoNuevaReserva
                    )
                            + "."
            );
        }


        // ========================================================
        // CALCULAR SALDO RESTANTE
        // ========================================================

        double saldoRestante =
                saldoDisponible -
                        montoNuevaReserva;

        saldoRestante =
                Math.round(
                        saldoRestante * 100.0
                ) / 100.0;


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

        nuevaReserva.setDuracionHoras(
                nuevaDuracion
        );

        /*
         * Una reserva utilizada con reserva a favor
         * queda confirmada directamente.
         */

        nuevaReserva.setEstado(
                "CONFIRMADA"
        );

        nuevaReserva.setMontoTotal(
                montoNuevaReserva
        );

        nuevaReserva.setFechaCreacion(
                LocalDateTime.now()
        );

        nuevaReserva.setUsuario(
                usuario
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
        // CALCULAR TARIFA BASE MÍNIMA
        // ========================================================

        /*
         * Buscamos la instalación disponible con la
         * tarifa base más baja.
         *
         * Ese valor determina el saldo mínimo que
         * tiene sentido conservar en una reserva a favor.
         */

        double tarifaBaseMinima =
                instalacionRepository
                        .findAll()
                        .stream()
                        .filter(instalacionDisponible ->
                                "DISPONIBLE".equalsIgnoreCase(
                                        instalacionDisponible.getEstado()
                                )
                        )
                        .filter(instalacionDisponible ->
                                instalacionDisponible.getTarifaBase() != null &&
                                        instalacionDisponible.getTarifaBase() > 0
                        )
                        .mapToDouble(
                                instalacionDisponible ->
                                        instalacionDisponible.getTarifaBase()
                        )
                        .min()
                        .orElse(0.0);


        // ========================================================
        // ACTUALIZAR RESERVA A FAVOR
        // ========================================================

        /*
         * Si el saldo llega a $0 o queda por debajo
         * de la tarifa base más barata disponible,
         * se descarta.
         */

        if (saldoRestante <= 0 ||
                (
                        tarifaBaseMinima > 0 &&
                                saldoRestante < tarifaBaseMinima
                )) {

            reservaAFavor.setMontoAcreditado(
                    0.0
            );

            reservaAFavor.setUtilizada(
                    true
            );

        } else {

            /*
             * El saldo todavía permite realizar al menos
             * una reserva mínima.
             */

            reservaAFavor.setMontoAcreditado(
                    saldoRestante
            );

            reservaAFavor.setUtilizada(
                    false
            );
        }


        // ========================================================
        // GUARDAR CAMBIO
        // ========================================================

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