package tecleros.sysgepolidep.reporte;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> generarReporteUsuarios() {

        byte[] pdf = reporteService.generarReporteUsuarios();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=reporte-usuarios.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/socios-membresias")
    public ResponseEntity<byte[]> generarReporteSociosMembresias() {

        byte[] pdf = reporteService.generarReporteSociosMembresias();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=reporte-socios-membresias.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reservas")
    public ResponseEntity<byte[]> generarReporteReservas() {

        byte[] pdf = reporteService.generarReporteReservas();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=reporte-reservas.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/pagos")
    public ResponseEntity<byte[]> generarReportePagos() {

        byte[] pdf = reporteService.generarReportePagos();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=reporte-pagos.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/instalaciones")
    public ResponseEntity<byte[]> generarReporteInstalaciones() {

        byte[] pdf = reporteService.generarReporteInstalaciones();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=reporte-instalaciones.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}