package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import tecleros.sysgepolidep.reserva.Reserva;

@RestController
@RequestMapping("/api/reservas-a-favor")
public class ReservaAFavorController {

    @Autowired
    private ReservaAFavorService reservaAFavorService;


    @GetMapping
    public List<ReservaAFavor> obtenerTodas() {
        return reservaAFavorService.listarTodas();
    }


    @GetMapping("/{id}")
    public Optional<ReservaAFavor> obtenerPorId(
            @PathVariable Long id) {

        return reservaAFavorService.buscarPorId(id);
    }


    @GetMapping("/usuario/{idUsuario}")
    public List<ReservaAFavor> obtenerPorUsuario(
            @PathVariable Long idUsuario) {

        return reservaAFavorService.buscarPorUsuario(idUsuario);
    }


    @GetMapping("/usuario/{idUsuario}/disponibles")
    public List<ReservaAFavor> obtenerDisponiblesPorUsuario(
            @PathVariable Long idUsuario) {

        return reservaAFavorService
                .buscarDisponiblesPorUsuario(idUsuario);
    }


    @PostMapping
    public ReservaAFavor crear(
            @RequestBody ReservaAFavor reservaAFavor) {

        return reservaAFavorService
                .crearReservaAFavor(reservaAFavor);
    }


    @PutMapping("/{id}/utilizar")
    public void utilizar(@PathVariable Long id) {

        reservaAFavorService.marcarComoUtilizada(id);
    }


    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {

        reservaAFavorService
                .eliminarReservaAFavor(id);
    }
    @PostMapping("/{id}/reprogramar")
    public Reserva reprogramar(
            @PathVariable Long id,
            @RequestParam LocalDate fecha,
            @RequestParam LocalTime hora,
            @RequestParam Integer duracion,
            @RequestParam Long instalacion) {

        return reservaAFavorService.reprogramarReserva(
                id,
                fecha,
                hora,
                duracion,
                instalacion
        );
    }

}