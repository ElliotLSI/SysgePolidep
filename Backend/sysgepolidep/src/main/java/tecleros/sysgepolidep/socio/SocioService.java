package tecleros.sysgepolidep.socio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    public Optional<Socio> buscarPorId(Long id) {
        return socioRepository.findById(id);
    }

    public Optional<Socio> buscarPorNroSocio(Integer nroSocio) {
        return socioRepository.findByNroSocio(nroSocio);
    }

    public Socio guardarSocio(Socio socio) {
        // Validación opcional: Verificar si el número de socio ya existe
        if (socio.getNroSocio() != null && socioRepository.findByNroSocio(socio.getNroSocio()).isPresent()) {
            throw new RuntimeException("El número de socio ya se encuentra registrado.");
        }
        return socioRepository.save(socio);
    }

    public void eliminarSocio(Long id) {
        if (!socioRepository.existsById(id)) {
            throw new RuntimeException("Socio no encontrado con ID: " + id);
        }
        socioRepository.deleteById(id);
    }
}