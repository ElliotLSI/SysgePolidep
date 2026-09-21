package tecleros.sysgepolidep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.entity.Instalacion;
import tecleros.sysgepolidep.repository.InstalacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class InstalacionService {

    @Autowired
    private InstalacionRepository instalacionRepository;

    public List<Instalacion> obtenerTodas() {
        return instalacionRepository.findAll();
    }

    public Optional<Instalacion> obtenerPorId(Long id) {
        return instalacionRepository.findById(id);
    }

    public Instalacion guardar(Instalacion instalacion) {
        return instalacionRepository.save(instalacion);
    }

    public void eliminar(Long id) {
        instalacionRepository.deleteById(id);
    }
}
