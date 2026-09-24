package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
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

        return reservaAFavorRepository
                .findByUsuarioIdUsuario(idUsuario);
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


        // Verificar que esa reserva no tenga ya una reserva a favor

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
                    reservaAFavor.getFechaGeneracion().plusMonths(3)
            );
        }


        if (!reservaAFavor.getFechaVencimiento()
                .isAfter(reservaAFavor.getFechaGeneracion())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de generación."
            );
        }


        // Utilizada

        if (reservaAFavor.getUtilizada() == null) {
            reservaAFavor.setUtilizada(false);
        }


        // Asociar las entidades reales de la BD

        reservaAFavor.setReservaOrigen(
                reservaExistente.get()
        );

        reservaAFavor.setUsuario(
                usuarioExistente.get()
        );


        return reservaAFavorRepository.save(reservaAFavor);
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

        reservaAFavorRepository.save(reservaAFavor);
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