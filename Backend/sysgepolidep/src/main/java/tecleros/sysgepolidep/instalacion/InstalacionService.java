package tecleros.sysgepolidep.instalacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstalacionService {

    @Autowired
    private InstalacionRepository instalacionRepository;

    public List<Instalacion> listarTodas() {
        return instalacionRepository.findAll();
    }

    public Optional<Instalacion> buscarPorId(Long id) {
        return instalacionRepository.findById(id);
    }

    public Instalacion guardarInstalacion(Instalacion instalacion) {
        if (instalacion.getTarifaBase() == null || instalacion.getTarifaBase() < 0) {
            throw new IllegalArgumentException("La tarifa base no puede ser negativa.");
        }
        return instalacionRepository.save(instalacion);
    }

    public void eliminarInstalacion(Long id) {
        if (!instalacionRepository.existsById(id)) {
            throw new RuntimeException("Instalación no encontrada con ID: " + id);
        }
        instalacionRepository.deleteById(id);
    }
}