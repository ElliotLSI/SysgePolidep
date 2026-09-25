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


    @GetMapping
    public List<Pago> obtenerTodos() {
        return pagoService.listarTodos();
    }


    @GetMapping("/{id}")
    public Optional<Pago> obtenerPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id);
    }


    @PostMapping
    public Pago crearPago(@RequestBody Pago pago) {
        return pagoService.crearPago(pago);
    }


    @DeleteMapping("/{id}")
    public void eliminarPago(@PathVariable Long id) {
        pagoService.eliminarPago(id);
    }
}