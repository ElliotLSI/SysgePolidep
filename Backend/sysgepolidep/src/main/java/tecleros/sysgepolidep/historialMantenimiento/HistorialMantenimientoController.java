package tecleros.sysgepolidep.historialMantenimiento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mantenimientos")
public class HistorialMantenimientoController {

    @Autowired
    private HistorialMantenimientoService mantenimientoService;

    @GetMapping
    public List<HistorialMantenimiento> obtenerTodos() {
        return mantenimientoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Optional<HistorialMantenimiento> obtenerPorId(@PathVariable Long id) {
        return mantenimientoService.buscarPorId(id);
    }

    @PostMapping
    public HistorialMantenimiento crearMantenimiento(@RequestBody HistorialMantenimiento mantenimiento) {
        return mantenimientoService.registrarMantenimiento(mantenimiento);
    }

    @DeleteMapping("/{id}")
    public void eliminarMantenimiento(@PathVariable Long id) {
        mantenimientoService.eliminarMantenimiento(id);
    }
}