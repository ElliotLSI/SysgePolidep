package tecleros.sysgepolidep.pago;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;


    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }


    public Optional<Pago> buscarPorId(Long id) {
        return pagoRepository.findById(id);
    }


    public Pago crearPago(Pago pago) {

        if (pago == null) {
            throw new IllegalArgumentException(
                    "El pago no puede ser nulo.");
        }

        /*
         * Un pago debe estar asociado
         * a una reserva O a una membresía.
         */
        if (pago.getReserva() == null && pago.getIdMembresia() == null) {
            throw new IllegalArgumentException(
                    "El pago debe estar asociado a una reserva o a una membresía.");
        }

        /*
         * No puede estar asociado a ambas.
         */
        if (pago.getReserva() != null && pago.getIdMembresia() != null) {
            throw new IllegalArgumentException(
                    "El pago no puede estar asociado simultáneamente a una reserva y a una membresía.");
        }


        /*
         * Si es pago de una reserva,
         * verificamos que la reserva exista.
         */
        if (pago.getReserva() != null) {

            Long idReserva = pago.getReserva().getIdReserva();

            if (idReserva == null) {
                throw new IllegalArgumentException(
                        "La reserva debe tener un ID.");
            }

            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La reserva no existe."));

            pago.setReserva(reserva);
        }


        /*
         * Si es pago de membresía,
         * verificamos que la membresía exista.
         */
        if (pago.getIdMembresia() != null) {

            Long idMembresia = pago.getIdMembresia();

            Membresia membresia = membresiaRepository.findById(idMembresia)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La membresía no existe."));
        }


        /*
         * Validamos el monto.
         */
        if (pago.getMontoTotal() == null) {
            throw new IllegalArgumentException(
                    "El monto del pago es obligatorio.");
        }

        if (pago.getMontoTotal() < 0) {
            throw new IllegalArgumentException(
                    "El monto del pago no puede ser negativo.");
        }


        /*
         * Validamos el medio de pago.
         */
        if (pago.getMedioPago() == null ||
                pago.getMedioPago().isBlank()) {

            throw new IllegalArgumentException(
                    "El medio de pago es obligatorio.");
        }


        /*
         * Estado por defecto.
         */
        if (pago.getEstado() == null ||
                pago.getEstado().isBlank()) {

            pago.setEstado("APROBADO");
        }


        /*
         * Estados permitidos.
         */
        if (!pago.getEstado().equals("APROBADO") &&
                !pago.getEstado().equals("PENDIENTE") &&
                !pago.getEstado().equals("RECHAZADO")) {

            throw new IllegalArgumentException(
                    "El estado del pago debe ser APROBADO, PENDIENTE o RECHAZADO.");
        }


        /*
         * Fecha automática.
         */
        if (pago.getFecha() == null) {
            pago.setFecha(LocalDateTime.now());
        }


        return pagoRepository.save(pago);
    }


    public void eliminarPago(Long id) {

        if (!pagoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "El pago no existe.");
        }

        pagoRepository.deleteById(id);
    }
}