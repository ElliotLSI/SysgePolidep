package tecleros.sysgepolidep.membresia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

    @Autowired
    private MembresiaService membresiaService;

    @GetMapping
    public List<Membresia> obtenerTodas() {
        return membresiaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Optional<Membresia> obtenerPorId(@PathVariable Long id) {
        return membresiaService.buscarPorId(id);
    }

    @PostMapping
    public Membresia crearMembresia(@RequestBody Membresia membresia) {
        return membresiaService.guardarMembresia(membresia);
    }

    @DeleteMapping("/{id}")
    public void eliminarMembresia(@PathVariable Long id) {
        membresiaService.eliminarMembresia(id);
    }
}