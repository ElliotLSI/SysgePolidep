package tecleros.sysgepolidep.pago;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.reserva.Reserva;
import tecleros.sysgepolidep.reserva.ReservaRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    // ============================================================
    // LISTAR PAGOS
    // ============================================================

    public List<Pago> listarTodos() {
        if (esAdministrador() || esEmpleado()) {
            return pagoRepository.findAll();
        }

        return pagoRepository.findAll()
                .stream()
                .filter(this::esPagoDelUsuarioActual)
                .toList();
    }


    // ============================================================
    // BUSCAR PAGO POR ID
    // ============================================================

    public Optional<Pago> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del pago es obligatorio.");
        }

        Optional<Pago> pagoOpt = pagoRepository.findById(id);

        if (pagoOpt.isEmpty()) {
            return Optional.empty();
        }

        Pago pago = pagoOpt.get();

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(pago);
        }

        if (!esPagoDelUsuarioActual(pago)) {
            throw new IllegalArgumentException("No tenés permiso para consultar este pago.");
        }

        return Optional.of(pago);
    }


    // ============================================================
    // CREAR PAGO
    // ============================================================

    @Transactional
    public Pago crearPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        boolean tieneReserva = pago.getReserva() != null;
        boolean tieneMembresia = pago.getIdMembresia() != null;

        if (!tieneReserva && !tieneMembresia) {
            throw new IllegalArgumentException(
                    "El pago debe estar asociado a una reserva o a una membresía."
            );
        }

        if (tieneReserva && tieneMembresia) {
            throw new IllegalArgumentException(
                    "El pago no puede estar asociado simultáneamente a una reserva y a una membresía."
            );
        }

        // VALIDAR MONTO
        if (pago.getMontoTotal() == null) {
            throw new IllegalArgumentException("El monto del pago es obligatorio.");
        }

        if (pago.getMontoTotal() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a 0.");
        }

        // VALIDAR MEDIO DE PAGO
        if (pago.getMedioPago() == null || pago.getMedioPago().isBlank()) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }

        String medioPago = pago.getMedioPago().trim().toUpperCase();
        if (!medioPago.equals("MERCADOPAGO") &&
                !medioPago.equals("TRANSFERENCIA") &&
                !medioPago.equals("TARJETA") &&
                !medioPago.equals("EFECTIVO")) {
            throw new IllegalArgumentException(
                    "El medio de pago debe ser MERCADOPAGO, TRANSFERENCIA, TARJETA o EFECTIVO."
            );
        }
        pago.setMedioPago(medioPago);

        // ESTADO DEL PAGO (solo ADMIN o EMPLEADO pueden aprobar al crear)
        String estadoPago;
        if (!esAdministrador() && !esEmpleado()) {
            estadoPago = "PENDIENTE";
        } else {
            estadoPago = (pago.getEstado() == null || pago.getEstado().isBlank())
                    ? "PENDIENTE"
                    : pago.getEstado().trim().toUpperCase();
        }

        if (!estadoPago.equals("APROBADO") &&
                !estadoPago.equals("PENDIENTE") &&
                !estadoPago.equals("RECHAZADO")) {
            throw new IllegalArgumentException(
                    "El estado del pago debe ser APROBADO, PENDIENTE o RECHAZADO."
            );
        }
        pago.setEstado(estadoPago);

        // FECHA
        if (pago.getFecha() == null) {
            pago.setFecha(LocalDateTime.now());
        }

        // PAGO DE RESERVA
        if (tieneReserva) {
            Long idReserva = pago.getReserva().getIdReserva();
            if (idReserva == null) {
                throw new IllegalArgumentException("La reserva debe tener un ID.");
            }

            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));

            if (!esAdministrador() && !esEmpleado()) {
                if (reserva.getUsuario() == null ||
                        !reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
                    throw new IllegalArgumentException(
                            "No podés registrar un pago para la reserva de otro usuario."
                    );
                }
            }

            if ("CANCELADA".equalsIgnoreCase(reserva.getEstado())) {
                throw new IllegalArgumentException("No se puede pagar una reserva cancelada.");
            }

            if ("CONFIRMADA".equalsIgnoreCase(reserva.getEstado())) {
                throw new IllegalArgumentException("Esta reserva ya se encuentra confirmada y pagada.");
            }

            if (reserva.getMontoTotal() == null || reserva.getMontoTotal() < 0) {
                throw new IllegalArgumentException("La reserva no tiene un monto válido.");
            }

            if (pago.getMontoTotal() < reserva.getMontoTotal()) {
                throw new IllegalArgumentException(
                        "El monto del pago es insuficiente para cubrir la reserva."
                );
            }

            pago.setReserva(reserva);
        }

        // PAGO DE MEMBRESÍA
        if (tieneMembresia) {
            Long idMembresia = pago.getIdMembresia();
            Membresia membresia = membresiaRepository.findById(idMembresia)
                    .orElseThrow(() -> new IllegalArgumentException("La membresía no existe."));

            if (!esAdministrador() && !esEmpleado()) {
                if (membresia.getSocio() == null ||
                        membresia.getSocio().getIdUsuario() == null ||
                        !membresia.getSocio().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
                    throw new IllegalArgumentException(
                            "No podés registrar un pago para la membresía de otro usuario."
                    );
                }
            }

            if (membresia.getCategoria() == null || membresia.getCategoria().getCosto() == null) {
                throw new IllegalArgumentException("La categoría de la membresía no tiene un costo válido.");
            }

            double costoMembresia = membresia.getCategoria().getCosto();
            if (costoMembresia < 0) {
                throw new IllegalArgumentException("El costo de la membresía no puede ser negativo.");
            }

            if (pago.getMontoTotal() < costoMembresia) {
                throw new IllegalArgumentException(
                        "El monto del pago es insuficiente para cubrir la membresía."
                );
            }
        }

        Pago pagoGuardado = pagoRepository.save(pago);

        // CONFIRMAR RESERVA O MEMBRESÍA SI EL PAGO QUEDÓ APROBADO
        confirmarReservaSiCorresponde(pagoGuardado);
        confirmarMembresiaSiCorresponde(pagoGuardado);

        return pagoGuardado;
    }


    // ============================================================
    // CAMBIAR ESTADO DE PAGO (APROBAR / RECHAZAR)
    // ============================================================

    @Transactional
    public Pago cambiarEstadoPago(Long idPago, String nuevoEstado) {
        if (!esAdministrador() && !esEmpleado()) {
            throw new IllegalArgumentException(
                    "Solo un administrador o empleado puede aprobar o rechazar pagos."
            );
        }

        if (idPago == null) {
            throw new IllegalArgumentException("El ID del pago es obligatorio.");
        }

        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio.");
        }

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if (!estadoNormalizado.equals("APROBADO") &&
                !estadoNormalizado.equals("PENDIENTE") &&
                !estadoNormalizado.equals("RECHAZADO")) {
            throw new IllegalArgumentException(
                    "El estado debe ser APROBADO, PENDIENTE o RECHAZADO."
            );
        }

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));

        pago.setEstado(estadoNormalizado);
        Pago pagoActualizado = pagoRepository.save(pago);

        confirmarReservaSiCorresponde(pagoActualizado);
        confirmarMembresiaSiCorresponde(pagoActualizado);

        return pagoActualizado;
    }

    private void confirmarReservaSiCorresponde(Pago pago) {
        if (pago.getReserva() != null && "APROBADO".equalsIgnoreCase(pago.getEstado())) {
            Reserva reserva = pago.getReserva();
            if (!"CANCELADA".equalsIgnoreCase(reserva.getEstado()) &&
                    reserva.getMontoTotal() != null &&
                    pago.getMontoTotal() >= reserva.getMontoTotal()) {
                reserva.setEstado("CONFIRMADA");
                reservaRepository.save(reserva);
            }
        }
    }

    private void confirmarMembresiaSiCorresponde(Pago pago) {
        if (pago.getIdMembresia() == null || !"APROBADO".equalsIgnoreCase(pago.getEstado())) {
            return;
        }

        membresiaRepository.findById(pago.getIdMembresia()).ifPresent(membresia -> {
            double costo = (membresia.getCategoria() != null && membresia.getCategoria().getCosto() != null)
                    ? membresia.getCategoria().getCosto()
                    : 0.0;

            if (pago.getMontoTotal() == null || pago.getMontoTotal() < costo) {
                return;
            }

            int duracionMeses = (membresia.getCategoria() != null
                    && membresia.getCategoria().getDuracionMeses() != null
                    && membresia.getCategoria().getDuracionMeses() > 0)
                    ? membresia.getCategoria().getDuracionMeses()
                    : 1;

            LocalDate hoy = LocalDate.now();
            boolean estaVencida = membresia.getFechaVenc() == null || membresia.getFechaVenc().isBefore(hoy);

            if (estaVencida) {
                membresia.setFechaInicio(hoy);
                membresia.setFechaVenc(hoy.plusMonths(duracionMeses));
            } else {
                membresia.setFechaVenc(membresia.getFechaVenc().plusMonths(duracionMeses));
            }

            membresia.setEstado("VIGENTE");
            membresiaRepository.save(membresia);
        });
    }


    // ============================================================
    // ELIMINAR PAGO
    // ============================================================

    @Transactional
    public void eliminarPago(Long id) {
        if (!esAdministrador()) {
            throw new IllegalArgumentException("Solo un administrador puede eliminar pagos.");
        }

        if (!pagoRepository.existsById(id)) {
            throw new IllegalArgumentException("El pago no existe.");
        }

        pagoRepository.deleteById(id);
    }


    // ============================================================
    // HELPERS DE SEGURIDAD
    // ============================================================

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No hay un usuario autenticado.");
        }

        String nombreUsuario = authentication.getName();
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("No se pudo identificar al usuario autenticado.");
        }

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe."));
    }

    private boolean esPagoDelUsuarioActual(Pago pago) {
        Usuario usuarioActual = obtenerUsuarioActual();

        if (pago.getReserva() != null) {
            Reserva reserva = pago.getReserva();
            return reserva.getUsuario() != null &&
                    reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario());
        }

        if (pago.getIdMembresia() != null) {
            Optional<Membresia> membresiaOpt = membresiaRepository.findById(pago.getIdMembresia());
            if (membresiaOpt.isEmpty()) return false;
            Membresia membresia = membresiaOpt.get();
            return membresia.getSocio() != null &&
                    membresia.getSocio().getIdUsuario().equals(usuarioActual.getIdUsuario());
        }

        return false;
    }

    private boolean esAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
    }

    private boolean esEmpleado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_EMPLEADO".equals(a.getAuthority()));
    }
}