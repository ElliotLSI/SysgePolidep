
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

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * todos los créditos.
         *
         * Los demás usuarios solamente pueden consultar
         * sus propios créditos.
         */

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

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * cualquier crédito.
         */

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(reservaAFavor);
        }

        /*
         * Usuario normal solamente puede consultar
         * su propio crédito.
         */

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

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * créditos de cualquier usuario.
         */

        if (esAdministrador() || esEmpleado()) {

            return reservaAFavorRepository
                    .findByUsuarioIdUsuario(idUsuario);
        }

        /*
         * Usuario normal solamente puede consultar
         * sus propios créditos.
         */

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
    // BUSCAR DISPONIBLES POR USUARIO
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
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * los créditos de cualquier usuario.
         */

        if (esAdministrador() || esEmpleado()) {

            return reservaAFavorRepository
                    .findByUsuarioIdUsuarioAndUtilizadaFalse(
                            idUsuario
                    );
        }

        /*
         * Usuario normal solamente puede consultar
         * sus propios créditos.
         */

        Usuario usuarioActual =
                obtenerUsuarioActual();

        if (!usuarioActual.getIdUsuario()
                .equals(idUsuario)) {

            throw new IllegalArgumentException(
                    "No podés consultar las reservas a favor de otro usuario."
            );
        }

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

        /*
         * Este método actualmente no debería ser llamado
         * directamente por un usuario.
         *
         * Los créditos se generan automáticamente cuando
         * se cancela una reserva desde ReservaService.
         */

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
        // VERIFICAR ESTADO DE RESERVA DE ORIGEN
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

        /*
         * El usuario del crédito debe ser el mismo usuario
         * propietario de la reserva cancelada.
         *
         * No confiamos en un usuario enviado por el cliente.
         */

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

        /*
         * El monto sale directamente de la reserva cancelada.
         * El cliente no puede decidir cuánto crédito generar.
         */

        Double monto =
                reservaExistente.getMontoTotal();

        if (monto == null || monto <= 0) {

            throw new IllegalArgumentException(
                    "La reserva de origen no tiene un monto válido para generar crédito."
            );
        }


        // ========================================================
        // FECHA DE GENERACIÓN
        // ========================================================

        LocalDate fechaGeneracion =
                LocalDate.now();


        // ========================================================
        // FECHA DE VENCIMIENTO
        // ========================================================

        LocalDate fechaVencimiento =
                fechaGeneracion.plusMonths(3);


        // ========================================================
        // CREAR OBJETO
        // ========================================================

        reservaAFavor.setMontoAcreditado(monto);

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
    // REPROGRAMAR RESERVA
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

        /*
         * Un usuario normal solamente puede utilizar
         * su propio crédito.
         *
         * ADMINISTRADOR y EMPLEADO pueden realizar
         * operaciones administrativas.
         */

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
                nuevaHora.plusHours(nuevaDuracion);

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
        // VERIFICAR ESTADO DE INSTALACIÓN
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
        // OBTENER USUARIO DEL CRÉDITO
        // ========================================================

        Usuario usuario =
                reservaAFavor.getUsuario();

        if (usuario == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor no tiene un usuario asociado."
            );
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

        nuevaReserva.setDuracionHoras(
                nuevaDuracion
        );

        /*
         * El crédito ya representa dinero previamente
         * acreditado, por eso la nueva reserva queda
         * confirmada.
         */

        nuevaReserva.setEstado(
                "CONFIRMADA"
        );

        /*
         * El monto utilizado sale del crédito.
         * No se recibe desde el cliente.
         */

        nuevaReserva.setMontoTotal(
                reservaAFavor.getMontoAcreditado()
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
        // MARCAR CRÉDITO COMO UTILIZADO
        // ========================================================

        reservaAFavor.setUtilizada(true);

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

        /*
         * Este método es administrativo/interno.
         * Los usuarios normales no deberían poder
         * marcar créditos manualmente como utilizados.
         */

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

        /*
         * No permitimos que un usuario normal elimine
         * créditos.
         */

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

