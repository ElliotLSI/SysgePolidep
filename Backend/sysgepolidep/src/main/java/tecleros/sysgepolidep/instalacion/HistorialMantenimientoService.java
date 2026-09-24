package tecleros.sysgepolidep.instalacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialMantenimientoService {

    @Autowired
    private HistorialMantenimientoRepository mantenimientoRepository;

    public List<HistorialMantenimiento> listarTodos() {
        return mantenimientoRepository.findAll();
    }

    public Optional<HistorialMantenimiento> buscarPorId(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public HistorialMantenimiento registrarMantenimiento(HistorialMantenimiento mantenimiento) {
        return mantenimientoRepository.save(mantenimiento);
    }

    public void eliminarMantenimiento(Long id) {
        mantenimientoRepository.deleteById(id);
    }
}