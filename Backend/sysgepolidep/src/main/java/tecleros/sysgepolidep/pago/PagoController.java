package tecleros.sysgepolidep.pago;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    // GET: http://localhost:8080/api/pagos
    @GetMapping
    public List<Pago> obtenerTodos() {
        return pagoService.listarTodos();
    }

    // GET por ID: http://localhost:8080/api/pagos/1
    @GetMapping("/{id}")
    public Optional<Pago> obtenerPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id);
    }

    // POST para registrar un pago: http://localhost:8080/api/pagos
    @PostMapping
    public Pago crearPago(@RequestBody Pago pago) {
        return pagoService.registrarPago(pago);
    }

    // DELETE: http://localhost:8080/api/pagos/1
    @DeleteMapping("/{id}")
    public void eliminarPago(@PathVariable Long id) {
        pagoService.eliminarPago(id);
    }
}