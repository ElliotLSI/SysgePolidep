package tecleros.sysgepolidep.service;

import tecleros.sysgepolidep.entity.HistorialMantenimiento;
import tecleros.sysgepolidep.repository.HistorialMantenimientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialMantenimientoService {

    private final HistorialMantenimientoRepository mantenimientoRepository;

    public HistorialMantenimientoService(HistorialMantenimientoRepository mantenimientoRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
    }

    public List<HistorialMantenimiento> listarMantenimientos() {
        return mantenimientoRepository.findAll();
    }

    public Optional<HistorialMantenimiento> buscarPorId(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public HistorialMantenimiento guardarMantenimiento(HistorialMantenimiento mantenimiento) {
        return mantenimientoRepository.save(mantenimiento);
    }

    public void eliminarMantenimiento(Long id) {
        mantenimientoRepository.deleteById(id);
    }
}