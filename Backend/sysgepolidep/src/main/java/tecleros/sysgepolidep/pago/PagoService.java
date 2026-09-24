package tecleros.sysgepolidep.pago;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    public Optional<Pago> buscarPorId(Long id) {
        return pagoRepository.findById(id);
    }

    public Pago registrarPago(Pago pago) {
        // Validar regla de negocio del concepto de pago:
        // O tiene Reserva (no nula) y Membresía nula, O Membresía (no nula) y Reserva nula.
        boolean tieneReserva = (pago.getReserva() != null);
        boolean tieneMembresia = (pago.getIdMembresia() != null);

        if (!(tieneReserva ^ tieneMembresia)) { // XOR: exactamente uno debe ser verdadero
            throw new IllegalArgumentException("El pago debe estar asociado estrictamente a una reserva o a una membresía, pero no a ambas.");
        }

        if (pago.getMontoTotal() == null || pago.getMontoTotal() < 0) {
            throw new IllegalArgumentException("El monto total del pago no puede ser negativo.");
        }

        return pagoRepository.save(pago);
    }

    public void eliminarPago(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado con ID: " + id);
        }
        pagoRepository.deleteById(id);
    }
}