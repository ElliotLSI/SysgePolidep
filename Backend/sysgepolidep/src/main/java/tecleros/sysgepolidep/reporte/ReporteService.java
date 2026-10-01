package tecleros.sysgepolidep.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.pago.Pago;
import tecleros.sysgepolidep.pago.PagoRepository;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;
import tecleros.sysgepolidep.socio.Socio;
import tecleros.sysgepolidep.socio.SocioRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReporteService {

    private final UsuarioRepository usuarioRepository;
    private final SocioRepository socioRepository;
    private final MembresiaRepository membresiaRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final InstalacionRepository instalacionRepository;

    public ReporteService(
            UsuarioRepository usuarioRepository,
            SocioRepository socioRepository,
            MembresiaRepository membresiaRepository,
            ReservaRepository reservaRepository,
            PagoRepository pagoRepository,
            InstalacionRepository instalacionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.socioRepository = socioRepository;
        this.membresiaRepository = membresiaRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.instalacionRepository = instalacionRepository;
    }

    public byte[] generarReporteUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            Document documento = new Document();

            PdfWriter.getInstance(documento, salida);

            documento.open();

            documento.add(
                    new Paragraph("SysGe PoliDep")
            );

            documento.add(
                    new Paragraph("Reporte de Usuarios")
            );

            documento.add(
                    new Paragraph(" ")
            );

            PdfPTable tabla = new PdfPTable(5);

            tabla.setWidthPercentage(100);

            tabla.addCell(new PdfPCell(new Phrase("ID")));
            tabla.addCell(new PdfPCell(new Phrase("Nombre")));
            tabla.addCell(new PdfPCell(new Phrase("Apellido")));
            tabla.addCell(new PdfPCell(new Phrase("Usuario")));
            tabla.addCell(new PdfPCell(new Phrase("Estado")));

            for (Usuario usuario : usuarios) {

                tabla.addCell(
                        String.valueOf(usuario.getIdUsuario())
                );

                tabla.addCell(
                        usuario.getNombre()
                );

                tabla.addCell(
                        usuario.getApellido()
                );

                tabla.addCell(
                        usuario.getNombreUsuario()
                );

                tabla.addCell(
                        usuario.getEstado()
                );
            }

            documento.add(tabla);

            documento.close();

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Error al generar el reporte de usuarios",
                    e
            );
        }
    }

    public byte[] generarReporteSociosMembresias() {

        List<Socio> socios = socioRepository.findAll();

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            Document documento = new Document();

            PdfWriter.getInstance(documento, salida);

            documento.open();

            documento.add(
                    new Paragraph("SysGe PoliDep")
            );

            documento.add(
                    new Paragraph("Reporte de Socios y Membresías")
            );

            documento.add(
                    new Paragraph(" ")
            );

            PdfPTable tabla = new PdfPTable(8);

            tabla.setWidthPercentage(100);

            tabla.addCell(new PdfPCell(new Phrase("N.º Socio")));
            tabla.addCell(new PdfPCell(new Phrase("Nombre")));
            tabla.addCell(new PdfPCell(new Phrase("Apellido")));
            tabla.addCell(new PdfPCell(new Phrase("DNI")));
            tabla.addCell(new PdfPCell(new Phrase("Categoría")));
            tabla.addCell(new PdfPCell(new Phrase("Inicio")));
            tabla.addCell(new PdfPCell(new Phrase("Vencimiento")));
            tabla.addCell(new PdfPCell(new Phrase("Estado")));

            for (Socio socio : socios) {

                Usuario usuario = socio.getUsuario();

                List<Membresia> membresias =
                        membresiaRepository.findBySocioIdUsuario(
                                socio.getIdUsuario()
                        );

                if (membresias.isEmpty()) {

                    tabla.addCell(
                            String.valueOf(socio.getNroSocio())
                    );

                    tabla.addCell(
                            usuario != null ? usuario.getNombre() : ""
                    );

                    tabla.addCell(
                            usuario != null ? usuario.getApellido() : ""
                    );

                    tabla.addCell(
                            usuario != null ? usuario.getDni() : ""
                    );

                    tabla.addCell("");
                    tabla.addCell("");
                    tabla.addCell("");
                    tabla.addCell("");

                } else {

                    for (Membresia membresia : membresias) {

                        tabla.addCell(
                                String.valueOf(socio.getNroSocio())
                        );

                        tabla.addCell(
                                usuario != null ? usuario.getNombre() : ""
                        );

                        tabla.addCell(
                                usuario != null ? usuario.getApellido() : ""
                        );

                        tabla.addCell(
                                usuario != null ? usuario.getDni() : ""
                        );

                        tabla.addCell(
                                membresia.getCategoria() != null
                                        ? membresia.getCategoria().getNombre()
                                        : ""
                        );

                        tabla.addCell(
                                membresia.getFechaInicio() != null
                                        ? membresia.getFechaInicio().toString()
                                        : ""
                        );

                        tabla.addCell(
                                membresia.getFechaVenc() != null
                                        ? membresia.getFechaVenc().toString()
                                        : ""
                        );

                        tabla.addCell(
                                membresia.getEstado() != null
                                        ? membresia.getEstado()
                                        : ""
                        );
                    }
                }
            }

            documento.add(tabla);

            documento.close();

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Error al generar el reporte de socios y membresías",
                    e
            );
        }
    }

    public byte[] generarReporteReservas() {

        List<Reserva> reservas = reservaRepository.findAll();

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            Document documento = new Document();

            PdfWriter.getInstance(documento, salida);

            documento.open();

            documento.add(
                    new Paragraph("SysGe PoliDep")
            );

            documento.add(
                    new Paragraph("Reporte de Reservas")
            );

            documento.add(
                    new Paragraph(" ")
            );

            PdfPTable tabla = new PdfPTable(9);

            tabla.setWidthPercentage(100);

            tabla.addCell(new PdfPCell(new Phrase("ID")));
            tabla.addCell(new PdfPCell(new Phrase("Usuario")));
            tabla.addCell(new PdfPCell(new Phrase("Instalación")));
            tabla.addCell(new PdfPCell(new Phrase("Fecha")));
            tabla.addCell(new PdfPCell(new Phrase("Inicio")));
            tabla.addCell(new PdfPCell(new Phrase("Fin")));
            tabla.addCell(new PdfPCell(new Phrase("Duración")));
            tabla.addCell(new PdfPCell(new Phrase("Estado")));
            tabla.addCell(new PdfPCell(new Phrase("Monto")));

            for (Reserva reserva : reservas) {

                LocalTime horaFin = reserva.getHoraInicio()
                        .plusHours(reserva.getDuracionHoras());

                String nombreUsuario = "";

                if (reserva.getUsuario() != null) {
                    nombreUsuario =
                            reserva.getUsuario().getNombre() + " " +
                                    reserva.getUsuario().getApellido();
                }

                String nombreInstalacion = "";

                if (reserva.getInstalacion() != null) {
                    nombreInstalacion =
                            reserva.getInstalacion().getNombre();
                }

                tabla.addCell(
                        String.valueOf(reserva.getIdReserva())
                );

                tabla.addCell(nombreUsuario);

                tabla.addCell(nombreInstalacion);

                tabla.addCell(
                        reserva.getFechaReserva() != null
                                ? reserva.getFechaReserva().toString()
                                : ""
                );

                tabla.addCell(
                        reserva.getHoraInicio() != null
                                ? reserva.getHoraInicio().toString()
                                : ""
                );

                tabla.addCell(
                        horaFin.toString()
                );

                tabla.addCell(
                        reserva.getDuracionHoras() != null
                                ? reserva.getDuracionHoras() + " h"
                                : ""
                );

                tabla.addCell(
                        reserva.getEstado() != null
                                ? reserva.getEstado()
                                : ""
                );

                tabla.addCell(
                        reserva.getMontoTotal() != null
                                ? "$ " + reserva.getMontoTotal()
                                : ""
                );
            }

            documento.add(tabla);

            documento.close();

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Error al generar el reporte de reservas",
                    e
            );
        }
    }

    public byte[] generarReportePagos() {

        List<Pago> pagos = pagoRepository.findAll();

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            Document documento = new Document();

            PdfWriter.getInstance(documento, salida);

            documento.open();

            documento.add(
                    new Paragraph("SysGe PoliDep")
            );

            documento.add(
                    new Paragraph("Reporte de Pagos")
            );

            documento.add(
                    new Paragraph(" ")
            );

            PdfPTable tabla = new PdfPTable(7);

            tabla.setWidthPercentage(100);

            tabla.addCell(new PdfPCell(new Phrase("ID")));
            tabla.addCell(new PdfPCell(new Phrase("Fecha")));
            tabla.addCell(new PdfPCell(new Phrase("Reserva")));
            tabla.addCell(new PdfPCell(new Phrase("Usuario")));
            tabla.addCell(new PdfPCell(new Phrase("Medio de Pago")));
            tabla.addCell(new PdfPCell(new Phrase("Monto")));
            tabla.addCell(new PdfPCell(new Phrase("Estado")));

            for (Pago pago : pagos) {

                String nombreUsuario = "";
                String idReserva = "";

                if (pago.getReserva() != null) {

                    idReserva = String.valueOf(
                            pago.getReserva().getIdReserva()
                    );

                    if (pago.getReserva().getUsuario() != null) {

                        nombreUsuario =
                                pago.getReserva().getUsuario().getNombre()
                                        + " "
                                        + pago.getReserva().getUsuario().getApellido();
                    }
                }

                tabla.addCell(
                        String.valueOf(pago.getIdPago())
                );

                tabla.addCell(
                        pago.getFecha() != null
                                ? pago.getFecha().toString()
                                : ""
                );

                tabla.addCell(idReserva);

                tabla.addCell(nombreUsuario);

                tabla.addCell(
                        pago.getMedioPago() != null
                                ? pago.getMedioPago()
                                : ""
                );

                tabla.addCell(
                        pago.getMontoTotal() != null
                                ? "$ " + pago.getMontoTotal()
                                : ""
                );

                tabla.addCell(
                        pago.getEstado() != null
                                ? pago.getEstado()
                                : ""
                );
            }

            documento.add(tabla);

            documento.close();

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Error al generar el reporte de pagos",
                    e
            );
        }
    }

    public byte[] generarReporteInstalaciones() {

        List<Instalacion> instalaciones =
                instalacionRepository.findAll();

        try {
            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            Document documento = new Document();

            PdfWriter.getInstance(
                    documento,
                    salida
            );

            documento.open();

            documento.add(
                    new Paragraph("SysGe PoliDep")
            );

            documento.add(
                    new Paragraph("Reporte de Instalaciones")
            );

            documento.add(
                    new Paragraph(" ")
            );

            PdfPTable tabla =
                    new PdfPTable(6);

            tabla.setWidthPercentage(100);

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("ID")
                    )
            );

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("Nombre")
                    )
            );

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("Tipo")
                    )
            );

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("Capacidad")
                    )
            );

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("Tarifa Base")
                    )
            );

            tabla.addCell(
                    new PdfPCell(
                            new Phrase("Estado")
                    )
            );

            for (Instalacion instalacion : instalaciones) {

                tabla.addCell(
                        String.valueOf(
                                instalacion.getIdInstalacion()
                        )
                );

                tabla.addCell(
                        instalacion.getNombre()
                );

                tabla.addCell(
                        instalacion.getTipo()
                );

                tabla.addCell(
                        instalacion.getCapacidad() != null
                                ? String.valueOf(
                                instalacion.getCapacidad()
                        )
                                : ""
                );

                tabla.addCell(
                        instalacion.getTarifaBase() != null
                                ? "$ " + instalacion.getTarifaBase()
                                : ""
                );

                tabla.addCell(
                        instalacion.getEstado() != null
                                ? instalacion.getEstado()
                                : ""
                );
            }

            documento.add(tabla);

            documento.close();

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Error al generar el reporte de instalaciones",
                    e
            );
        }
    }
}