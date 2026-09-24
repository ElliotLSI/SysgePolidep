package tecleros.sysgepolidep.instalacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instalaciones")
public class InstalacionController {

    @Autowired
    private InstalacionService instalacionService;

    @GetMapping
    public List<Instalacion> obtenerTodas() {
        return instalacionService.listarTodas();
    }

    @GetMapping("/{id}")
    public Optional<Instalacion> obtenerPorId(@PathVariable Long id) {
        return instalacionService.buscarPorId(id);
    }

    @PostMapping
    public Instalacion crearInstalacion(@RequestBody Instalacion instalacion) {
        return instalacionService.guardarInstalacion(instalacion);
    }

    @DeleteMapping("/{id}")
    public void eliminarInstalacion(@PathVariable Long id) {
        instalacionService.eliminarInstalacion(id);
    }
}