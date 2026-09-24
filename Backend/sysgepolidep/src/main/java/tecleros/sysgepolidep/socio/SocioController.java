package tecleros.sysgepolidep.socio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    @Autowired
    private SocioService socioService;

    // GET: http://localhost:8080/api/socios
    @GetMapping
    public List<Socio> obtenerTodos() {
        return socioService.listarTodos();
    }

    // GET por ID: http://localhost:8080/api/socios/1
    @GetMapping("/{id}")
    public Optional<Socio> obtenerPorId(@PathVariable Long id) {
        return socioService.buscarPorId(id);
    }

    // GET por Número de Socio: http://localhost:8080/api/socios/numero/1002
    @GetMapping("/numero/{nroSocio}")
    public Optional<Socio> obtenerPorNroSocio(@PathVariable Integer nroSocio) {
        return socioService.buscarPorNroSocio(nroSocio);
    }

    // POST: http://localhost:8080/api/socios
    @PostMapping
    public Socio crearSocio(@RequestBody Socio socio) {
        return socioService.guardarSocio(socio);
    }

    // DELETE: http://localhost:8080/api/socios/1
    @DeleteMapping("/{id}")
    public void eliminarSocio(@PathVariable Long id) {
        socioService.eliminarSocio(id);
    }
}