package tecleros.sysgepolidep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.entity.Membresia;
import tecleros.sysgepolidep.repository.MembresiaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    public List<Membresia> obtenerTodas() {
        return membresiaRepository.findAll();
    }

    public Optional<Membresia> obtenerPorId(Long id) {
        return membresiaRepository.findById(id);
    }

    public Membresia guardar(Membresia membresia) {
        return membresiaRepository.save(membresia);
    }

    public void eliminar(Long id) {
        membresiaRepository.deleteById(id);
    }
}
