package tecleros.sysgepolidep.controller;

import tecleros.sysgepolidep.entity.Instalacion;
import tecleros.sysgepolidep.service.InstalacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instalaciones")
public class InstalacionController {

    private final InstalacionService instalacionService;

    public InstalacionController(InstalacionService instalacionService) {
        this.instalacionService = instalacionService;
    }

    @GetMapping
    public List<Instalacion> listar() {
        return instalacionService.listarInstalaciones();
    }

    @PostMapping
    public Instalacion guardar(@RequestBody Instalacion instalacion) {
        return instalacionService.guardarInstalacion(instalacion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instalacion> buscarPorId(@PathVariable Long id) {
        return instalacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        instalacionService.eliminarInstalacion(id);
    }
}