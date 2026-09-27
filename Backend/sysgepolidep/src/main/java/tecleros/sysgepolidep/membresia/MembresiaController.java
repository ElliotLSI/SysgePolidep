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


    // ==========================================================
    // LISTAR TODAS LAS MEMBRESÍAS
    // ==========================================================

    @GetMapping
    public List<Membresia> obtenerTodas() {
        return membresiaService.listarTodas();
    }


    // ==========================================================
    // BUSCAR MEMBRESÍA POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public Optional<Membresia> obtenerPorId(@PathVariable Long id) {
        return membresiaService.buscarPorId(id);
    }


    // ==========================================================
    // RENOVAR MEMBRESÍA
    // ==========================================================

    @PutMapping("/{id}/renovar")
    public Membresia renovarMembresia(@PathVariable Long id) {
        return membresiaService.renovarMembresia(id);
    }


    // ==========================================================
    // CREAR MEMBRESÍA
    // ==========================================================

    @PostMapping
    public Membresia crearMembresia(@RequestBody Membresia membresia) {
        return membresiaService.guardarMembresia(membresia);
    }


    // ==========================================================
    // ELIMINAR MEMBRESÍA
    // ==========================================================

    @DeleteMapping("/{id}")
    public void eliminarMembresia(@PathVariable Long id) {
        membresiaService.eliminarMembresia(id);
    }
}