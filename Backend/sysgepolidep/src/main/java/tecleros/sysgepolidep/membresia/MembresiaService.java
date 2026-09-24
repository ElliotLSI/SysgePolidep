package tecleros.sysgepolidep.membresia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    public List<Membresia> listarTodas() {
        return membresiaRepository.findAll();
    }

    public Optional<Membresia> buscarPorId(Long id) {
        return membresiaRepository.findById(id);
    }

    public Membresia guardarMembresia(Membresia membresia) {
        if (membresia.getFechaVenc().isBefore(membresia.getFechaInicio()) ||
                membresia.getFechaVenc().isEqual(membresia.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la fecha de inicio.");
        }
        return membresiaRepository.save(membresia);
    }

    public void eliminarMembresia(Long id) {
        membresiaRepository.deleteById(id);
    }
}