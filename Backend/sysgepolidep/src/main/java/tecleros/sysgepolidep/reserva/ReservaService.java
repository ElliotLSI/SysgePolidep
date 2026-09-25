package tecleros.sysgepolidep.reserva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.reservaAFavor.ReservaAFavor;
import tecleros.sysgepolidep.reservaAFavor.ReservaAFavorRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
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


    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }


    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }


    public Reserva crearReserva(Reserva nuevaReserva) {

        if (nuevaReserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula."
            );
        }

        // Validar usuario
        if (nuevaReserva.getUsuario() == null ||
                nuevaReserva.getUsuario().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La reserva debe estar asociada a un usuario."
            );
        }

        Long idUsuario = nuevaReserva.getUsuario().getIdUsuario();

        Optional<Usuario> usuarioExistente =
                usuarioRepository.findById(idUsuario);

        if (usuarioExistente.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario asociado no existe."
            );
        }


        // Validar instalación
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


        // Validar fecha
        if (nuevaReserva.getFechaReserva() == null) {
            throw new IllegalArgumentException(
                    "La fecha de la reserva es obligatoria."
            );
        }


        // Validar hora
        if (nuevaReserva.getHoraInicio() == null) {
            throw new IllegalArgumentException(
                    "La hora de inicio es obligatoria."
            );
        }


        // Validar duración
        if (nuevaReserva.getDuracionHoras() == null ||
                nuevaReserva.getDuracionHoras() <= 0) {

            throw new IllegalArgumentException(
                    "La duración de la reserva debe ser mayor a 0."
            );
        }


        // Validar monto
        if (nuevaReserva.getMontoTotal() == null) {
            throw new IllegalArgumentException(
                    "El monto total es obligatorio."
            );
        }

        if (nuevaReserva.getMontoTotal() < 0) {
            throw new IllegalArgumentException(
                    "El monto total no puede ser negativo."
            );
        }


        // Estado por defecto
        if (nuevaReserva.getEstado() == null ||
                nuevaReserva.getEstado().trim().isEmpty()) {

            nuevaReserva.setEstado("PENDIENTE");
        }

        if (!nuevaReserva.getEstado().equals("PENDIENTE") &&
                !nuevaReserva.getEstado().equals("CONFIRMADA") &&
                !nuevaReserva.getEstado().equals("CANCELADA")) {

            throw new IllegalArgumentException(
                    "El estado debe ser PENDIENTE, CONFIRMADA o CANCELADA."
            );
        }


        // Fecha de creación
        if (nuevaReserva.getFechaCreacion() == null) {
            nuevaReserva.setFechaCreacion(
                    java.time.LocalDateTime.now()
            );
        }


        // Usamos las entidades reales de la base de datos
        nuevaReserva.setUsuario(usuarioExistente.get());
        nuevaReserva.setInstalacion(instalacionExistente.get());


        // Buscar reservas existentes para esa instalación y fecha
        List<Reserva> existentes =
                reservaRepository.findReservasActivasPorInstalacionYFecha(
                        idInstalacion,
                        nuevaReserva.getFechaReserva()
                );


        // Calcular horario de la nueva reserva
        LocalTime nuevaInicio = nuevaReserva.getHoraInicio();

        LocalTime nuevaFin =
                nuevaInicio.plusHours(
                        nuevaReserva.getDuracionHoras()
                );


        // Comprobar solapamiento
        for (Reserva r : existentes) {

            LocalTime rInicio = r.getHoraInicio();

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


        return reservaRepository.save(nuevaReserva);
    }


    @Transactional
    public void cancelarReserva(Long id) {

        Reserva reserva =
                reservaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reserva no encontrada con ID: " + id
                                )
                        );


        // Evitar cancelar una reserva que ya está cancelada
        if ("CANCELADA".equals(reserva.getEstado())) {

            throw new IllegalArgumentException(
                    "La reserva ya se encuentra cancelada."
            );
        }


        // Cambiar estado de la reserva
        reserva.setEstado("CANCELADA");

        reservaRepository.save(reserva);


        // Verificar si ya existe una reserva a favor
        if (reservaAFavorRepository
                .findByReservaOrigenIdReserva(id)
                .isEmpty()) {

            ReservaAFavor reservaAFavor =
                    new ReservaAFavor();

            // Devolver el monto de la reserva
            reservaAFavor.setMontoAcreditado(
                    reserva.getMontoTotal()
            );

            // Fecha en que se cancela
            LocalDate fechaGeneracion =
                    LocalDate.now();

            reservaAFavor.setFechaGeneracion(
                    fechaGeneracion
            );

            // Válida durante 3 meses
            reservaAFavor.setFechaVencimiento(
                    fechaGeneracion.plusMonths(3)
            );

            // Todavía no fue utilizada
            reservaAFavor.setUtilizada(false);

            // Reserva que originó el crédito
            reservaAFavor.setReservaOrigen(reserva);

            // Usuario dueño del crédito
            reservaAFavor.setUsuario(
                    reserva.getUsuario()
            );


            reservaAFavorRepository.save(reservaAFavor);
        }
    }
}