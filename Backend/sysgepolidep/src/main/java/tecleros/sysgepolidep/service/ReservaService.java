package tecleros.sysgepolidep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.entity.Reserva;
import tecleros.sysgepolidep.repository.ReservaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> obtenerPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public Reserva guardar(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public void eliminar(Long id) {
        reservaRepository.deleteById(id);
    }
}
 
