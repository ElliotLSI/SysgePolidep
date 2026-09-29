
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

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * todos los pagos.
         *
         * Los demás usuarios solamente pueden consultar
         * sus propios pagos.
         */

        if (esAdministrador() || esEmpleado()) {
            return pagoRepository.findAll();
        }

        Usuario usuarioActual =
                obtenerUsuarioActual();

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
            throw new IllegalArgumentException(
                    "El ID del pago es obligatorio."
            );
        }

        Optional<Pago> pagoOpt =
                pagoRepository.findById(id);

        if (pagoOpt.isEmpty()) {
            return Optional.empty();
        }

        Pago pago = pagoOpt.get();

        /*
         * ADMINISTRADOR y EMPLEADO pueden consultar
         * cualquier pago.
         */

        if (esAdministrador() || esEmpleado()) {
            return Optional.of(pago);
        }

        /*
         * Usuario normal solamente puede consultar
         * sus propios pagos.
         */

        if (!esPagoDelUsuarioActual(pago)) {

            throw new IllegalArgumentException(
                    "No tenés permiso para consultar este pago."
            );
        }

        return Optional.of(pago);
    }


    // ============================================================
    // CREAR PAGO
    // ============================================================

    @Transactional
    public Pago crearPago(Pago pago) {

        if (pago == null) {
            throw new IllegalArgumentException(
                    "El pago no puede ser nulo."
            );
        }


        // ========================================================
        // OBTENER USUARIO AUTENTICADO
        // ========================================================

        Usuario usuarioActual =
                obtenerUsuarioActual();


        // ========================================================
        // RESERVA O MEMBRESÍA
        // ========================================================

        boolean tieneReserva =
                pago.getReserva() != null;

        boolean tieneMembresia =
                pago.getIdMembresia() != null;


        /*
         * Un pago debe pertenecer a exactamente
         * una operación.
         */

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


        // ========================================================
        // VALIDAR MONTO
        // ========================================================

        if (pago.getMontoTotal() == null) {

            throw new IllegalArgumentException(
                    "El monto del pago es obligatorio."
            );
        }

        if (pago.getMontoTotal() <= 0) {

            throw new IllegalArgumentException(
                    "El monto del pago debe ser mayor a 0."
            );
        }


        // ========================================================
        // VALIDAR MEDIO DE PAGO
        // ========================================================

        if (pago.getMedioPago() == null ||
                pago.getMedioPago().isBlank()) {

            throw new IllegalArgumentException(
                    "El medio de pago es obligatorio."
            );
        }

        String medioPago =
                pago.getMedioPago()
                        .trim()
                        .toUpperCase();


        if (!medioPago.equals("MERCADOPAGO") &&
                !medioPago.equals("TRANSFERENCIA") &&
                !medioPago.equals("TARJETA") &&
                !medioPago.equals("EFECTIVO")) {

            throw new IllegalArgumentException(
                    "El medio de pago debe ser MERCADOPAGO, TRANSFERENCIA, TARJETA o EFECTIVO."
            );
        }

        pago.setMedioPago(medioPago);


        // ========================================================
        // ESTADO DEL PAGO
        // ========================================================

        if (pago.getEstado() == null ||
                pago.getEstado().isBlank()) {

            pago.setEstado("PENDIENTE");
        }

        String estadoPago =
                pago.getEstado()
                        .trim()
                        .toUpperCase();


        if (!estadoPago.equals("APROBADO") &&
                !estadoPago.equals("PENDIENTE") &&
                !estadoPago.equals("RECHAZADO")) {

            throw new IllegalArgumentException(
                    "El estado del pago debe ser APROBADO, PENDIENTE o RECHAZADO."
            );
        }

        pago.setEstado(estadoPago);


        // ========================================================
        // FECHA
        // ========================================================

        if (pago.getFecha() == null) {

            pago.setFecha(
                    LocalDateTime.now()
            );
        }


        // ========================================================
        // PAGO DE RESERVA
        // ========================================================

        if (tieneReserva) {

            Long idReserva =
                    pago.getReserva()
                            .getIdReserva();

            if (idReserva == null) {

                throw new IllegalArgumentException(
                        "La reserva debe tener un ID."
                );
            }


            Reserva reserva =
                    reservaRepository.findById(idReserva)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "La reserva no existe."
                                    )
                            );


            // ----------------------------------------------------
            // VERIFICAR PROPIETARIO
            // ----------------------------------------------------

            if (!esAdministrador() &&
                    !esEmpleado()) {

                if (reserva.getUsuario() == null ||
                        !reserva.getUsuario()
                                .getIdUsuario()
                                .equals(
                                        usuarioActual.getIdUsuario()
                                )) {

                    throw new IllegalArgumentException(
                            "No podés registrar un pago para la reserva de otro usuario."
                    );
                }
            }


            // ----------------------------------------------------
            // VERIFICAR ESTADO DE RESERVA
            // ----------------------------------------------------

            if ("CANCELADA".equals(
                    reserva.getEstado())) {

                throw new IllegalArgumentException(
                        "No se puede pagar una reserva cancelada."
                );
            }


            // ----------------------------------------------------
            // VALIDAR MONTO
            // ----------------------------------------------------

            if (reserva.getMontoTotal() == null ||
                    reserva.getMontoTotal() < 0) {

                throw new IllegalArgumentException(
                        "La reserva no tiene un monto válido."
                );
            }


            /*
             * El pago debe cubrir el monto de la reserva.
             *
             * No permitimos pagar menos y confirmar.
             */

            if (pago.getMontoTotal()
                    < reserva.getMontoTotal()) {

                throw new IllegalArgumentException(
                        "El monto del pago es insuficiente para cubrir la reserva."
                );
            }


            pago.setReserva(reserva);
        }


        // ========================================================
        // PAGO DE MEMBRESÍA
        // ========================================================

        if (tieneMembresia) {

            Long idMembresia =
                    pago.getIdMembresia();


            Membresia membresia =
                    membresiaRepository.findById(
                            idMembresia
                    ).orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La membresía no existe."
                            )
                    );


            // ----------------------------------------------------
            // VERIFICAR PROPIETARIO
            // ----------------------------------------------------

            if (!esAdministrador() &&
                    !esEmpleado()) {

                if (membresia.getSocio() == null ||
                        membresia.getSocio()
                                .getIdUsuario() == null ||
                        !membresia.getSocio()
                                .getIdUsuario()
                                .equals(
                                        usuarioActual.getIdUsuario()
                                )) {

                    throw new IllegalArgumentException(
                            "No podés registrar un pago para la membresía de otro usuario."
                    );
                }
            }


            // ----------------------------------------------------
            // VALIDAR CATEGORÍA
            // ----------------------------------------------------

            if (membresia.getCategoria() == null) {

                throw new IllegalArgumentException(
                        "La membresía no tiene una categoría asociada."
                );
            }


            if (membresia.getCategoria()
                    .getCosto() == null) {

                throw new IllegalArgumentException(
                        "La categoría de la membresía no tiene un costo válido."
                );
            }


            double costoMembresia =
                    membresia.getCategoria()
                            .getCosto();


            if (costoMembresia < 0) {

                throw new IllegalArgumentException(
                        "El costo de la membresía no puede ser negativo."
                );
            }


            /*
             * El pago debe cubrir el costo de la categoría.
             */

            if (pago.getMontoTotal()
                    < costoMembresia) {

                throw new IllegalArgumentException(
                        "El monto del pago es insuficiente para cubrir la membresía."
                );
            }
        }


        // ========================================================
        // GUARDAR PAGO
        // ========================================================

        Pago pagoGuardado =
                pagoRepository.save(pago);


        // ========================================================
        // CONFIRMAR RESERVA
        // ========================================================

        /*
         * Solamente un pago APROBADO puede confirmar
         * una reserva.
         */

        if (pagoGuardado.getReserva() != null &&
                "APROBADO".equals(
                        pagoGuardado.getEstado()
                )) {

            Reserva reserva =
                    pagoGuardado.getReserva();


            if (reserva.getMontoTotal() != null &&
                    pagoGuardado.getMontoTotal()
                            >= reserva.getMontoTotal()) {

                reserva.setEstado(
                        "CONFIRMADA"
                );

                reservaRepository.save(
                        reserva
                );
            }
        }


        return pagoGuardado;
    }


    // ============================================================
    // ELIMINAR PAGO
    // ============================================================

    @Transactional
    public void eliminarPago(Long id) {

        /*
         * SecurityConfig ya restringe DELETE a ADMINISTRADOR.
         * Repetimos la validación en Service como segunda capa.
         */

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar pagos."
            );
        }


        if (!pagoRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "El pago no existe."
            );
        }


        pagoRepository.deleteById(id);
    }


    // ============================================================
    // OBTENER USUARIO AUTENTICADO
    // ============================================================

    private Usuario obtenerUsuarioActual() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "No hay un usuario autenticado."
            );
        }


        String nombreUsuario =
                authentication.getName();


        if (nombreUsuario == null ||
                nombreUsuario.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario autenticado."
            );
        }


        return usuarioRepository
                .findByNombreUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario autenticado no existe."
                        )
                );
    }


    // ============================================================
    // VERIFICAR SI EL PAGO PERTENECE AL USUARIO
    // ============================================================

    private boolean esPagoDelUsuarioActual(
            Pago pago) {

        Usuario usuarioActual =
                obtenerUsuarioActual();


        // --------------------------------------------------------
        // Pago de reserva
        // --------------------------------------------------------

        if (pago.getReserva() != null) {

            Reserva reserva =
                    pago.getReserva();

            return reserva.getUsuario() != null &&
                    reserva.getUsuario()
                            .getIdUsuario()
                            .equals(
                                    usuarioActual.getIdUsuario()
                            );
        }


        // --------------------------------------------------------
        // Pago de membresía
        // --------------------------------------------------------

        if (pago.getIdMembresia() != null) {

            Optional<Membresia> membresiaOpt =
                    membresiaRepository.findById(
                            pago.getIdMembresia()
                    );

            if (membresiaOpt.isEmpty()) {
                return false;
            }

            Membresia membresia =
                    membresiaOpt.get();

            return membresia.getSocio() != null &&
                    membresia.getSocio()
                            .getIdUsuario()
                            .equals(
                                    usuarioActual.getIdUsuario()
                            );
        }


        return false;
    }


    // ============================================================
    // VERIFICAR ADMINISTRADOR
    // ============================================================

    private boolean esAdministrador() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null) {
            return false;
        }


        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMINISTRADOR"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }


    // ============================================================
    // VERIFICAR EMPLEADO
    // ============================================================

    private boolean esEmpleado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null) {
            return false;
        }


        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_EMPLEADO"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }
}

