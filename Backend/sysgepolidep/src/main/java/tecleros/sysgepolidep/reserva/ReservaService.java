package tecleros.sysgepolidep.reserva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public Reserva crearReserva(Reserva nuevaReserva) {
        // 1. Validar solapamiento de horarios
        List<Reserva> existentes = reservaRepository.findReservasActivasPorInstalacionYFecha(
                nuevaReserva.getInstalacion().getIdInstalacion(),
                nuevaReserva.getFechaReserva()
        );

        LocalTime nuevaInicio = nuevaReserva.getHoraInicio();
        LocalTime nuevaFin = nuevaInicio.plusHours(nuevaReserva.getDuracionHoras());

        for (Reserva r : existentes) {
            LocalTime rInicio = r.getHoraInicio();
            LocalTime rFin = rInicio.plusHours(r.getDuracionHoras());

            // Validación de intersección de rangos horarios
            boolean haySolapamiento = nuevaInicio.isBefore(rFin) && nuevaFin.isAfter(rInicio);
            if (haySolapamiento) {
                throw new RuntimeException("Conflicto de horario: La instalación ya se encuentra ocupada o reservada en ese rango.");
            }
        }

        // 2. Guardar si no hay conflictos
        return reservaRepository.save(nuevaReserva);
    }

    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);
    }
}