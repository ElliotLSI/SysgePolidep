package tecleros.sysgepolidep.reserva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // GET: http://localhost:8080/api/reservas
    @GetMapping
    public List<Reserva> obtenerTodas() {
        return reservaService.listarTodas();
    }

    // GET por ID: http://localhost:8080/api/reservas/1
    @GetMapping("/{id}")
    public Optional<Reserva> obtenerPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id);
    }

    // POST: http://localhost:8080/api/reservas
    @PostMapping
    public Reserva crearReserva(@RequestBody Reserva reserva) {
        return reservaService.crearReserva(reserva);
    }

    // PUT para cancelar: http://localhost:8080/api/reservas/1/cancelar
    @PutMapping("/{id}/cancelar")
    public void cancelarReserva(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
    }
}