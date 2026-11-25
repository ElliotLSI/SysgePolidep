package tecleros.sysgepolidep.reporte;

import com.lowagie.text.*;
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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ReporteService {

    private final UsuarioRepository usuarioRepository;
    private final SocioRepository socioRepository;
    private final MembresiaRepository membresiaRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final InstalacionRepository instalacionRepository;

    // Paleta institucional SysGe PoliDep
    private static final Color COLOR_PRIMARIO = new Color(92, 6, 19);      // #5C0613
    private static final Color COLOR_FILA_PAR = new Color(248, 245, 246);
    private static final Color COLOR_BORDE = new Color(215, 215, 215);
    private static final Color COLOR_TEXTO = new Color(33, 37, 41);
    private static final Color COLOR_SUBTEXTO = new Color(108, 117, 125);
    private static final Color COLOR_FONDO_FILTRO = new Color(244, 244, 244);

    // Fuentes
    private static final Font FONT_MARCA = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FONT_TITULO = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_TEXTO);
    private static final Font FONT_META = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SUBTEXTO);
    private static final Font FONT_FILTRO = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_TEXTO);
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font FONT_DATO = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, COLOR_TEXTO);
    private static final Font FONT_TOTAL = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_PRIMARIO);

    // Formateadores
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat FMT_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

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

    // ==================== 1. USUARIOS ====================

    public byte[] generarReporteUsuarios(String estado) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> coincideTexto(u.getEstado(), estado))
                .toList();

        List<String> filtros = new ArrayList<>();
        if (tieneValor(estado)) filtros.add("Estado: " + estado);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 30, 30, 35, 35);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, "Reporte de Usuarios", filtros);

            if (usuarios.isEmpty()) {
                agregarMensajeVacio(documento);
            } else {
                PdfPTable tabla = crearTabla(new float[]{1f, 2.5f, 2.5f, 2.5f, 2f});
                agregarHeaders(tabla, "ID", "Nombre", "Apellido", "Usuario", "Estado");

                for (int i = 0; i < usuarios.size(); i++) {
                    Usuario u = usuarios.get(i);
                    boolean par = i % 2 == 1;
                    tabla.addCell(celdaDato(String.valueOf(u.getIdUsuario()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(textoSeguro(u.getNombre()), par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(textoSeguro(u.getApellido()), par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(textoSeguro(u.getNombreUsuario()), par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(textoSeguro(u.getEstado()), par, Element.ALIGN_CENTER));
                }
                documento.add(tabla);
                agregarResumenFinal(documento, usuarios.size(), null);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte de usuarios", e);
        }
    }

    // ==================== 2. SOCIOS Y MEMBRESÍAS ====================

    public byte[] generarReporteSociosMembresias(String estado) {
        List<Socio> socios = socioRepository.findAll();

        List<String> filtros = new ArrayList<>();
        if (tieneValor(estado)) filtros.add("Estado membresía: " + estado);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4.rotate(), 30, 30, 35, 35);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, "Reporte de Socios y Membresías", filtros);

            PdfPTable tabla = crearTabla(new float[]{1.2f, 2f, 2f, 1.8f, 2f, 1.6f, 1.6f, 1.6f});
            agregarHeaders(tabla, "N.º Socio", "Nombre", "Apellido", "DNI", "Categoría", "Inicio", "Vencimiento", "Estado");

            int filasAgregadas = 0;

            for (Socio socio : socios) {
                Usuario usuario = socio.getUsuario();
                List<Membresia> membresias = membresiaRepository.findBySocioIdUsuario(socio.getIdUsuario());

                if (membresias.isEmpty()) {
                    if (tieneValor(estado)) continue;
                    boolean par = filasAgregadas % 2 == 1;
                    tabla.addCell(celdaDato(String.valueOf(socio.getNroSocio()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getNombre()) : "-", par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getApellido()) : "-", par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getDni()) : "-", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato("Sin membresía", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato("-", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato("-", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato("-", par, Element.ALIGN_CENTER));
                    filasAgregadas++;
                } else {
                    for (Membresia m : membresias) {
                        if (!coincideTexto(m.getEstado(), estado)) continue;
                        boolean par = filasAgregadas % 2 == 1;
                        tabla.addCell(celdaDato(String.valueOf(socio.getNroSocio()), par, Element.ALIGN_CENTER));
                        tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getNombre()) : "-", par, Element.ALIGN_LEFT));
                        tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getApellido()) : "-", par, Element.ALIGN_LEFT));
                        tabla.addCell(celdaDato(usuario != null ? textoSeguro(usuario.getDni()) : "-", par, Element.ALIGN_CENTER));
                        tabla.addCell(celdaDato(m.getCategoria() != null ? textoSeguro(m.getCategoria().getNombre()) : "-", par, Element.ALIGN_LEFT));
                        tabla.addCell(celdaDato(formatearFecha(m.getFechaInicio()), par, Element.ALIGN_CENTER));
                        tabla.addCell(celdaDato(formatearFecha(m.getFechaVenc()), par, Element.ALIGN_CENTER));
                        tabla.addCell(celdaDato(textoSeguro(m.getEstado()), par, Element.ALIGN_CENTER));
                        filasAgregadas++;
                    }
                }
            }

            if (filasAgregadas == 0) {
                agregarMensajeVacio(documento);
            } else {
                documento.add(tabla);
                agregarResumenFinal(documento, filasAgregadas, null);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte de socios y membresías", e);
        }
    }

    // ==================== 3. RESERVAS ====================

    public byte[] generarReporteReservas(
            Integer anio, Integer mes, Integer dia,
            Long idUsuario, Long idInstalacion, String estado
    ) {
        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> coincideFecha(r.getFechaReserva(), anio, mes, dia))
                .filter(r -> idUsuario == null || (r.getUsuario() != null && idUsuario.equals(r.getUsuario().getIdUsuario())))
                .filter(r -> idInstalacion == null || (r.getInstalacion() != null && idInstalacion.equals(r.getInstalacion().getIdInstalacion())))
                .filter(r -> coincideTexto(r.getEstado(), estado))
                .toList();

        List<String> filtros = construirResumenFiltros(anio, mes, dia, idUsuario, idInstalacion, estado);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4.rotate(), 30, 30, 35, 35);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, "Reporte de Reservas", filtros);

            if (reservas.isEmpty()) {
                agregarMensajeVacio(documento);
            } else {
                PdfPTable tabla = crearTabla(new float[]{0.8f, 2.5f, 2.5f, 1.5f, 1.1f, 1.1f, 1.1f, 1.6f, 1.7f});
                agregarHeaders(tabla, "ID", "Cliente", "Instalación", "Fecha", "Inicio", "Fin", "Duración", "Estado", "Monto");

                double totalMonto = 0.0;

                for (int i = 0; i < reservas.size(); i++) {
                    Reserva r = reservas.get(i);
                    boolean par = i % 2 == 1;

                    String nombreUsuario = r.getUsuario() != null
                            ? r.getUsuario().getNombre() + " " + r.getUsuario().getApellido()
                            : "-";
                    String nombreInstalacion = r.getInstalacion() != null
                            ? textoSeguro(r.getInstalacion().getNombre())
                            : "-";

                    String horaInicioStr = r.getHoraInicio() != null ? r.getHoraInicio().format(FMT_HORA) : "-";
                    String horaFinStr = "-";
                    if (r.getHoraInicio() != null && r.getDuracionHoras() != null) {
                        LocalTime horaFin = r.getHoraInicio().plusHours(r.getDuracionHoras());
                        horaFinStr = horaFin.format(FMT_HORA);
                    }

                    if (r.getMontoTotal() != null) {
                        totalMonto += r.getMontoTotal();
                    }

                    tabla.addCell(celdaDato(String.valueOf(r.getIdReserva()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(nombreUsuario, par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(nombreInstalacion, par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(formatearFecha(r.getFechaReserva()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(horaInicioStr, par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(horaFinStr, par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(r.getDuracionHoras() != null ? r.getDuracionHoras() + " h" : "-", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(textoSeguro(r.getEstado()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(formatearMoneda(r.getMontoTotal()), par, Element.ALIGN_RIGHT));
                }

                documento.add(tabla);
                agregarResumenFinal(documento, reservas.size(), totalMonto);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte de reservas", e);
        }
    }

    // ==================== 4. PAGOS ====================

    public byte[] generarReportePagos(
            Integer anio, Integer mes, Integer dia,
            Long idUsuario, Long idInstalacion, String estado
    ) {
        List<Pago> pagos = pagoRepository.findAll().stream()
                .filter(p -> p.getFecha() != null && coincideFecha(p.getFecha().toLocalDate(), anio, mes, dia))
                .filter(p -> {
                    if (idUsuario == null) return true;
                    if (p.getReserva() != null && p.getReserva().getUsuario() != null) {
                        return idUsuario.equals(p.getReserva().getUsuario().getIdUsuario());
                    }
                    if (p.getIdMembresia() != null) {
                        return membresiaRepository.findById(p.getIdMembresia())
                                .map(m -> m.getSocio() != null && idUsuario.equals(m.getSocio().getIdUsuario()))
                                .orElse(false);
                    }
                    return false;
                })
                .filter(p -> idInstalacion == null || (
                        p.getReserva() != null &&
                                p.getReserva().getInstalacion() != null &&
                                idInstalacion.equals(p.getReserva().getInstalacion().getIdInstalacion())
                ))
                .filter(p -> coincideTexto(p.getEstado(), estado))
                .toList();

        List<String> filtros = construirResumenFiltros(anio, mes, dia, idUsuario, idInstalacion, estado);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4.rotate(), 30, 30, 35, 35);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, "Reporte de Pagos", filtros);

            if (pagos.isEmpty()) {
                agregarMensajeVacio(documento);
            } else {
                PdfPTable tabla = crearTabla(new float[]{0.8f, 2f, 1.5f, 2.4f, 2.2f, 1.7f, 1.6f, 1.5f});
                agregarHeaders(tabla, "ID", "Fecha y Hora", "Concepto", "Cliente / Socio", "Detalle", "Medio de Pago", "Monto", "Estado");

                double totalMonto = 0.0;

                for (int i = 0; i < pagos.size(); i++) {
                    Pago p = pagos.get(i);
                    boolean par = i % 2 == 1;

                    String concepto = "-";
                    String nombreUsuario = "-";
                    String detalle = "-";

                    if (p.getReserva() != null) {
                        concepto = "Reserva #" + p.getReserva().getIdReserva();
                        if (p.getReserva().getUsuario() != null) {
                            nombreUsuario = p.getReserva().getUsuario().getNombre() + " " + p.getReserva().getUsuario().getApellido();
                        }
                        if (p.getReserva().getInstalacion() != null) {
                            detalle = textoSeguro(p.getReserva().getInstalacion().getNombre());
                        }
                    } else if (p.getIdMembresia() != null) {
                        concepto = "Membresía #" + p.getIdMembresia();
                        Membresia m = membresiaRepository.findById(p.getIdMembresia()).orElse(null);
                        if (m != null) {
                            if (m.getSocio() != null && m.getSocio().getUsuario() != null) {
                                nombreUsuario = m.getSocio().getUsuario().getNombre() + " " + m.getSocio().getUsuario().getApellido();
                            }
                            if (m.getCategoria() != null) {
                                detalle = textoSeguro(m.getCategoria().getNombre());
                            }
                        }
                    }

                    if (p.getMontoTotal() != null) {
                        totalMonto += p.getMontoTotal();
                    }

                    tabla.addCell(celdaDato(String.valueOf(p.getIdPago()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(formatearFechaHora(p.getFecha()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(concepto, par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(nombreUsuario, par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(detalle, par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(textoSeguro(p.getMedioPago()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(formatearMoneda(p.getMontoTotal()), par, Element.ALIGN_RIGHT));
                    tabla.addCell(celdaDato(textoSeguro(p.getEstado()), par, Element.ALIGN_CENTER));
                }

                documento.add(tabla);
                agregarResumenFinal(documento, pagos.size(), totalMonto);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte de pagos", e);
        }
    }

    // ==================== 5. INSTALACIONES ====================

    public byte[] generarReporteInstalaciones(String estado) {
        List<Instalacion> instalaciones = instalacionRepository.findAll().stream()
                .filter(inst -> coincideTexto(inst.getEstado(), estado))
                .toList();

        List<String> filtros = new ArrayList<>();
        if (tieneValor(estado)) filtros.add("Estado: " + estado);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 30, 30, 35, 35);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, "Reporte de Instalaciones", filtros);

            if (instalaciones.isEmpty()) {
                agregarMensajeVacio(documento);
            } else {
                PdfPTable tabla = crearTabla(new float[]{0.8f, 2.5f, 2.2f, 1.3f, 1.7f, 1.8f});
                agregarHeaders(tabla, "ID", "Nombre", "Tipo", "Capacidad", "Tarifa Base", "Estado");

                for (int i = 0; i < instalaciones.size(); i++) {
                    Instalacion inst = instalaciones.get(i);
                    boolean par = i % 2 == 1;
                    tabla.addCell(celdaDato(String.valueOf(inst.getIdInstalacion()), par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(textoSeguro(inst.getNombre()), par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(textoSeguro(inst.getTipo()), par, Element.ALIGN_LEFT));
                    tabla.addCell(celdaDato(inst.getCapacidad() != null ? inst.getCapacidad() + " pers." : "-", par, Element.ALIGN_CENTER));
                    tabla.addCell(celdaDato(formatearMoneda(inst.getTarifaBase()), par, Element.ALIGN_RIGHT));
                    tabla.addCell(celdaDato(textoSeguro(inst.getEstado()), par, Element.ALIGN_CENTER));
                }

                documento.add(tabla);
                agregarResumenFinal(documento, instalaciones.size(), null);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte de instalaciones", e);
        }
    }

    // ==================== HELPERS DE DISEÑO PDF ====================

    private void agregarEncabezado(Document documento, String titulo, List<String> filtros) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3f, 2f});

        PdfPCell celdaIzq = new PdfPCell();
        celdaIzq.setBorder(Rectangle.BOTTOM);
        celdaIzq.setBorderColorBottom(COLOR_PRIMARIO);
        celdaIzq.setBorderWidthBottom(2f);
        celdaIzq.setPaddingBottom(8f);
        celdaIzq.addElement(new Paragraph("SysGe PoliDep", FONT_MARCA));
        celdaIzq.addElement(new Paragraph(titulo, FONT_TITULO));

        PdfPCell celdaDer = new PdfPCell();
        celdaDer.setBorder(Rectangle.BOTTOM);
        celdaDer.setBorderColorBottom(COLOR_PRIMARIO);
        celdaDer.setBorderWidthBottom(2f);
        celdaDer.setPaddingBottom(8f);
        celdaDer.setVerticalAlignment(Element.ALIGN_BOTTOM);
        Paragraph fechaEmision = new Paragraph(
                "Emitido: " + LocalDateTime.now().format(FMT_FECHA_HORA),
                FONT_META
        );
        fechaEmision.setAlignment(Element.ALIGN_RIGHT);
        celdaDer.addElement(fechaEmision);

        headerTable.addCell(celdaIzq);
        headerTable.addCell(celdaDer);
        documento.add(headerTable);

        String textoFiltros = filtros.isEmpty()
                ? "Filtros aplicados: Ninguno (mostrando todos los registros)"
                : "Filtros aplicados: " + String.join("  |  ", filtros);

        PdfPTable filtroBox = new PdfPTable(1);
        filtroBox.setWidthPercentage(100);
        filtroBox.setSpacingBefore(8f);
        filtroBox.setSpacingAfter(12f);

        PdfPCell celdaFiltro = new PdfPCell(new Phrase(textoFiltros, FONT_FILTRO));
        celdaFiltro.setBackgroundColor(COLOR_FONDO_FILTRO);
        celdaFiltro.setBorderColor(COLOR_BORDE);
        celdaFiltro.setPadding(6f);
        filtroBox.addCell(celdaFiltro);

        documento.add(filtroBox);
    }

    private PdfPTable crearTabla(float[] anchos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(anchos.length);
        tabla.setWidthPercentage(100);
        tabla.setWidths(anchos);
        tabla.setHeaderRows(1);
        return tabla;
    }

    private void agregarHeaders(PdfPTable tabla, String... titulos) {
        for (String titulo : titulos) {
            PdfPCell celda = new PdfPCell(new Phrase(titulo, FONT_HEADER));
            celda.setBackgroundColor(COLOR_PRIMARIO);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celda.setPaddingTop(7f);
            celda.setPaddingBottom(7f);
            celda.setBorderColor(COLOR_PRIMARIO);
            tabla.addCell(celda);
        }
    }

    private PdfPCell celdaDato(String texto, boolean filaPar, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FONT_DATO));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(5.5f);
        celda.setBorderColor(COLOR_BORDE);
        if (filaPar) {
            celda.setBackgroundColor(COLOR_FILA_PAR);
        }
        return celda;
    }

    private void agregarResumenFinal(Document documento, int cantidad, Double totalMonto) throws DocumentException {
        PdfPTable resumen = new PdfPTable(1);
        resumen.setWidthPercentage(100);
        resumen.setSpacingBefore(10f);

        StringBuilder sb = new StringBuilder("Total de registros: ").append(cantidad);
        if (totalMonto != null) {
            sb.append("     |     Monto total acumulado: ").append(formatearMoneda(totalMonto));
        }

        PdfPCell celda = new PdfPCell(new Phrase(sb.toString(), FONT_TOTAL));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setBorder(Rectangle.TOP);
        celda.setBorderColorTop(COLOR_PRIMARIO);
        celda.setBorderWidthTop(1.5f);
        celda.setPaddingTop(8f);
        resumen.addCell(celda);

        documento.add(resumen);
    }

    private void agregarMensajeVacio(Document documento) throws DocumentException {
        Paragraph p = new Paragraph("No se encontraron registros con los filtros seleccionados.", FONT_META);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(25f);
        documento.add(p);
    }

    // ==================== HELPERS DE FILTRO Y FORMATO ====================

    private List<String> construirResumenFiltros(
            Integer anio, Integer mes, Integer dia,
            Long idUsuario, Long idInstalacion, String estado
    ) {
        List<String> filtros = new ArrayList<>();
        if (anio != null) filtros.add("Año: " + anio);
        if (mes != null) filtros.add("Mes: " + mes);
        if (dia != null) filtros.add("Día: " + dia);

        if (idUsuario != null) {
            String nombre = usuarioRepository.findById(idUsuario)
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("ID " + idUsuario);
            filtros.add("Cliente: " + nombre);
        }

        if (idInstalacion != null) {
            String nombre = instalacionRepository.findById(idInstalacion)
                    .map(Instalacion::getNombre)
                    .orElse("ID " + idInstalacion);
            filtros.add("Instalación: " + nombre);
        }

        if (tieneValor(estado)) filtros.add("Estado: " + estado);
        return filtros;
    }

    private boolean coincideFecha(LocalDate fecha, Integer anio, Integer mes, Integer dia) {
        if (anio == null && mes == null && dia == null) return true;
        if (fecha == null) return false;
        if (anio != null && fecha.getYear() != anio) return false;
        if (mes != null && fecha.getMonthValue() != mes) return false;
        if (dia != null && fecha.getDayOfMonth() != dia) return false;
        return true;
    }

    private boolean coincideTexto(String valorEntidad, String filtro) {
        if (!tieneValor(filtro)) return true;
        return valorEntidad != null && valorEntidad.equalsIgnoreCase(filtro.trim());
    }

    private boolean tieneValor(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String textoSeguro(String s) {
        return tieneValor(s) ? s : "-";
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FMT_FECHA) : "-";
    }

    private String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(FMT_FECHA_HORA) : "-";
    }

    private String formatearMoneda(Double monto) {
        return monto != null ? FMT_MONEDA.format(monto) : "-";
    }
}