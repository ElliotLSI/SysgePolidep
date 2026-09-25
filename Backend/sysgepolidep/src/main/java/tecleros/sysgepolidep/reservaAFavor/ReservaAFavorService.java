package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
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


    public List<ReservaAFavor> listarTodas() {
        return reservaAFavorRepository.findAll();
    }


    public Optional<ReservaAFavor> buscarPorId(Long id) {
        return reservaAFavorRepository.findById(id);
    }


    public List<ReservaAFavor> buscarPorUsuario(Long idUsuario) {

        if (usuarioRepository.findById(idUsuario).isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        return reservaAFavorRepository.findByUsuarioIdUsuario(idUsuario);
    }


    public List<ReservaAFavor> buscarDisponiblesPorUsuario(Long idUsuario) {

        if (usuarioRepository.findById(idUsuario).isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no existe."
            );
        }

        return reservaAFavorRepository
                .findByUsuarioIdUsuarioAndUtilizadaFalse(idUsuario);
    }


    public ReservaAFavor crearReservaAFavor(
            ReservaAFavor reservaAFavor) {

        if (reservaAFavor == null) {
            throw new IllegalArgumentException(
                    "La reserva a favor no puede ser nula."
            );
        }


        // Validar reserva de origen
        if (reservaAFavor.getReservaOrigen() == null ||
                reservaAFavor.getReservaOrigen().getIdReserva() == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor debe estar asociada a una reserva de origen."
            );
        }

        Long idReserva =
                reservaAFavor.getReservaOrigen().getIdReserva();

        Optional<Reserva> reservaExistente =
                reservaRepository.findById(idReserva);

        if (reservaExistente.isEmpty()) {
            throw new IllegalArgumentException(
                    "La reserva de origen no existe."
            );
        }


        // Evitar dos reservas a favor para la misma reserva
        if (reservaAFavorRepository
                .findByReservaOrigenIdReserva(idReserva)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "La reserva ya tiene una reserva a favor asociada."
            );
        }


        // Validar usuario
        if (reservaAFavor.getUsuario() == null ||
                reservaAFavor.getUsuario().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La reserva a favor debe estar asociada a un usuario."
            );
        }

        Long idUsuario =
                reservaAFavor.getUsuario().getIdUsuario();

        Optional<Usuario> usuarioExistente =
                usuarioRepository.findById(idUsuario);

        if (usuarioExistente.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario asociado no existe."
            );
        }


        // Validar monto
        if (reservaAFavor.getMontoAcreditado() == null) {
            throw new IllegalArgumentException(
                    "El monto acreditado es obligatorio."
            );
        }

        if (reservaAFavor.getMontoAcreditado() <= 0) {
            throw new IllegalArgumentException(
                    "El monto acreditado debe ser mayor a 0."
            );
        }


        // Fecha de generación
        if (reservaAFavor.getFechaGeneracion() == null) {
            reservaAFavor.setFechaGeneracion(
                    LocalDate.now()
            );
        }


        // Fecha de vencimiento
        if (reservaAFavor.getFechaVencimiento() == null) {

            reservaAFavor.setFechaVencimiento(
                    reservaAFavor.getFechaGeneracion()
                            .plusMonths(3)
            );
        }


        if (!reservaAFavor.getFechaVencimiento()
                .isAfter(reservaAFavor.getFechaGeneracion())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de generación."
            );
        }


        // Estado de utilización
        if (reservaAFavor.getUtilizada() == null) {
            reservaAFavor.setUtilizada(false);
        }


        // Asociar entidades reales
        reservaAFavor.setReservaOrigen(
                reservaExistente.get()
        );

        reservaAFavor.setUsuario(
                usuarioExistente.get()
        );


        return reservaAFavorRepository.save(reservaAFavor);
    }


    @Transactional
    public Reserva reprogramarReserva(
            Long idReservaAFavor,
            LocalDate nuevaFecha,
            LocalTime nuevaHora,
            Integer nuevaDuracion,
            Long idInstalacion) {


        // 1. Buscar ReservaAFavor
        ReservaAFavor reservaAFavor =
                reservaAFavorRepository.findById(idReservaAFavor)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva a favor no encontrada con ID: "
                                                + idReservaAFavor
                                )
                        );


        // 2. Verificar que no esté utilizada
        if (reservaAFavor.getUtilizada()) {

            throw new IllegalArgumentException(
                    "La reserva a favor ya fue utilizada."
            );
        }


        // 3. Verificar vencimiento
        if (!LocalDate.now()
                .isBefore(reservaAFavor.getFechaVencimiento())) {

            throw new IllegalArgumentException(
                    "La reserva a favor se encuentra vencida."
            );
        }


        // 4. Validar fecha
        if (nuevaFecha == null) {

            throw new IllegalArgumentException(
                    "La nueva fecha es obligatoria."
            );
        }


        // 5. Validar hora
        if (nuevaHora == null) {

            throw new IllegalArgumentException(
                    "La nueva hora de inicio es obligatoria."
            );
        }


        // 6. Validar duración
        if (nuevaDuracion == null ||
                nuevaDuracion <= 0) {

            throw new IllegalArgumentException(
                    "La duración debe ser mayor a 0."
            );
        }


        // 7. Validar instalación
        if (idInstalacion == null) {

            throw new IllegalArgumentException(
                    "La instalación es obligatoria."
            );
        }

        Instalacion instalacion =
                instalacionRepository.findById(idInstalacion)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La instalación no existe."
                                )
                        );


        // 8. Buscar reservas existentes
        List<Reserva> existentes =
                reservaRepository
                        .findReservasActivasPorInstalacionYFecha(
                                idInstalacion,
                                nuevaFecha
                        );


        // 9. Calcular horario nuevo
        LocalTime nuevaFin =
                nuevaHora.plusHours(nuevaDuracion);


        // 10. Comprobar solapamiento
        for (Reserva r : existentes) {

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


        // 11. Obtener usuario de la ReservaAFavor
        Usuario usuario =
                reservaAFavor.getUsuario();


        // 12. Crear nueva reserva
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

        nuevaReserva.setEstado(
                "CONFIRMADA"
        );

        nuevaReserva.setMontoTotal(
                reservaAFavor.getMontoAcreditado()
        );

        nuevaReserva.setFechaCreacion(
                java.time.LocalDateTime.now()
        );

        nuevaReserva.setUsuario(
                usuario
        );

        nuevaReserva.setInstalacion(
                instalacion
        );


        // 13. Guardar nueva reserva
        Reserva reservaCreada =
                reservaRepository.save(nuevaReserva);


        // 14. Marcar ReservaAFavor como utilizada
        reservaAFavor.setUtilizada(true);

        reservaAFavorRepository.save(
                reservaAFavor
        );


        return reservaCreada;
    }


    public void marcarComoUtilizada(Long id) {

        ReservaAFavor reservaAFavor =
                reservaAFavorRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva a favor no encontrada con ID: " + id
                                )
                        );


        if (reservaAFavor.getUtilizada()) {

            throw new IllegalArgumentException(
                    "La reserva a favor ya fue utilizada."
            );
        }


        if (!LocalDate.now()
                .isBefore(reservaAFavor.getFechaVencimiento())) {

            throw new IllegalArgumentException(
                    "La reserva a favor se encuentra vencida."
            );
        }


        reservaAFavor.setUtilizada(true);

        reservaAFavorRepository.save(
                reservaAFavor
        );
    }


    public void eliminarReservaAFavor(Long id) {

        if (!reservaAFavorRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Reserva a favor no encontrada con ID: " + id
            );
        }

        reservaAFavorRepository.deleteById(id);
    }
}