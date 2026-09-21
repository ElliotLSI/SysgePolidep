package tecleros.sysgepolidep.controller;

import tecleros.sysgepolidep.entity.HistorialMantenimiento;
import tecleros.sysgepolidep.service.HistorialMantenimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mantenimiento")
public class HistorialMantenimientoController {

    private final HistorialMantenimientoService mantenimientoService;

    public HistorialMantenimientoController(HistorialMantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    @GetMapping
    public List<HistorialMantenimiento> listar() {
        return mantenimientoService.listarMantenimientos();
    }

    @PostMapping
    public HistorialMantenimiento guardar(@RequestBody HistorialMantenimiento mantenimiento) {
        return mantenimientoService.guardarMantenimiento(mantenimiento);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialMantenimiento> buscarPorId(@PathVariable Long id) {
        return mantenimientoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        mantenimientoService.eliminarMantenimiento(id);
    }
}