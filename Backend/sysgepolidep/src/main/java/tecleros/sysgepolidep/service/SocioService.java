package tecleros.sysgepolidep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.entity.Socio;
import tecleros.sysgepolidep.repository.SocioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    public List<Socio> obtenerTodos() {
        return socioRepository.findAll();
    }

    public Optional<Socio> obtenerPorId(Long id) {
        return socioRepository.findById(id);
    }

    public Socio guardar(Socio socio) {
        return socioRepository.save(socio);
    }

    public void eliminar(Long id) {
        socioRepository.deleteById(id);
    }
}

 
