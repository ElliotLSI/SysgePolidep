package tecleros.sysgepolidep.service;

import tecleros.sysgepolidep.entity.Membresia;
import tecleros.sysgepolidep.repository.MembresiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    // Listar todas las membresías / tarifas
    public List<Membresia> listarMembresias() {
        return membresiaRepository.findAll();
    }

    // Buscar membresía por ID
    public Optional<Membresia> buscarPorId(Long id) {
        return membresiaRepository.findById(id);
    }

    // Crear o actualizar una membresía
    public Membresia guardarMembresia(Membresia membresia) {
        return membresiaRepository.save(membresia);
    }

    // Eliminar una membresía
    public void eliminarMembresia(Long id) {
        membresiaRepository.deleteById(id);
    }
}