package tecleros.sysgepolidep.service;

import tecleros.sysgepolidep.entity.Instalacion;
import tecleros.sysgepolidep.repository.InstalacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstalacionService {

    private final InstalacionRepository instalacionRepository;

    public InstalacionService(InstalacionRepository instalacionRepository) {
        this.instalacionRepository = instalacionRepository;
    }

    public List<Instalacion> listarInstalaciones() {
        return instalacionRepository.findAll();
    }

    public Optional<Instalacion> buscarPorId(Long id) {
        return instalacionRepository.findById(id);
    }

    public Instalacion guardarInstalacion(Instalacion instalacion) {
        return instalacionRepository.save(instalacion);
    }

    public void eliminarInstalacion(Long id) {
        instalacionRepository.deleteById(id);
    }
}