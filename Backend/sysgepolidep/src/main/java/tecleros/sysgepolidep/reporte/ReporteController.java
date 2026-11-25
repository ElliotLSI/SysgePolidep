package tecleros.sysgepolidep.reporte;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> generarReporteUsuarios(
            @RequestParam(required = false) String estado
    ) {
        byte[] pdf = reporteService.generarReporteUsuarios(estado);
        return construirRespuestaPdf(pdf, "reporte-usuarios.pdf");
    }

    @GetMapping("/socios-membresias")
    public ResponseEntity<byte[]> generarReporteSociosMembresias(
            @RequestParam(required = false) String estado
    ) {
        byte[] pdf = reporteService.generarReporteSociosMembresias(estado);
        return construirRespuestaPdf(pdf, "reporte-socios-membresias.pdf");
    }

    @GetMapping("/reservas")
    public ResponseEntity<byte[]> generarReporteReservas(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idInstalacion,
            @RequestParam(required = false) String estado
    ) {
        byte[] pdf = reporteService.generarReporteReservas(
                anio, mes, dia, idUsuario, idInstalacion, estado
        );
        return construirRespuestaPdf(pdf, "reporte-reservas.pdf");
    }

    @GetMapping("/pagos")
    public ResponseEntity<byte[]> generarReportePagos(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idInstalacion,
            @RequestParam(required = false) String estado
    ) {
        byte[] pdf = reporteService.generarReportePagos(
                anio, mes, dia, idUsuario, idInstalacion, estado
        );
        return construirRespuestaPdf(pdf, "reporte-pagos.pdf");
    }

    @GetMapping("/instalaciones")
    public ResponseEntity<byte[]> generarReporteInstalaciones(
            @RequestParam(required = false) String estado
    ) {
        byte[] pdf = reporteService.generarReporteInstalaciones(estado);
        return construirRespuestaPdf(pdf, "reporte-instalaciones.pdf");
    }

    private ResponseEntity<byte[]> construirRespuestaPdf(byte[] pdf, String nombreArchivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}