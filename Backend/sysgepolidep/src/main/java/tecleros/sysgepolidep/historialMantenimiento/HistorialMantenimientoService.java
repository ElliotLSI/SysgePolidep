package tecleros.sysgepolidep.historialMantenimiento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialMantenimientoService {

    @Autowired
    private HistorialMantenimientoRepository mantenimientoRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    // Listar todos los mantenimientos
    public List<HistorialMantenimiento> listarTodos() {
        return mantenimientoRepository.findAll();
    }

    // Buscar mantenimiento por ID
    public Optional<HistorialMantenimiento> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del mantenimiento es obligatorio."
            );
        }

        return mantenimientoRepository.findById(id);
    }

    // Registrar mantenimiento
    @Transactional
    public HistorialMantenimiento registrarMantenimiento(
            HistorialMantenimiento mantenimiento) {

        verificarAdministradorOEmpleado();

        if (mantenimiento == null) {
            throw new IllegalArgumentException(
                    "El mantenimiento no puede ser nulo."
            );
        }

        if (mantenimiento.getInstalacion() == null ||
                mantenimiento.getInstalacion().getIdInstalacion() == null) {

            throw new IllegalArgumentException(
                    "El mantenimiento debe estar asociado a una instalación."
            );
        }

        Long idInstalacion =
                mantenimiento.getInstalacion().getIdInstalacion();

        Instalacion instalacion = instalacionRepository.findById(idInstalacion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La instalación asociada no existe."
                ));

        if (mantenimiento.getFechaInicio() == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria."
            );
        }

        if (mantenimiento.getFechaFin() != null &&
                mantenimiento.getFechaFin()
                        .isBefore(mantenimiento.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio."
            );
        }

        if (mantenimiento.getMotivo() == null ||
                mantenimiento.getMotivo().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El motivo del mantenimiento es obligatorio."
            );
        }

        if (mantenimiento.getMotivo().trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "El motivo del mantenimiento no puede superar los 1000 caracteres."
            );
        }

        mantenimiento.setInstalacion(instalacion);
        mantenimiento.setMotivo(mantenimiento.getMotivo().trim());


        instalacion.setEstado("MANTENIMIENTO");

        instalacionRepository.save(instalacion);

        return mantenimientoRepository.save(mantenimiento);
    }

    // Eliminar mantenimiento
    @Transactional
    public void eliminarMantenimiento(Long id) {

        verificarAdministrador();

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del mantenimiento es obligatorio."
            );
        }

        if (!mantenimientoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "No existe un mantenimiento con el ID indicado."
            );
        }

        mantenimientoRepository.deleteById(id);
    }

    // Verificar Administrador o Empleado
    private void verificarAdministradorOEmpleado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "No hay un usuario autenticado."
            );
        }

        boolean tienePermiso = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMINISTRADOR".equals(
                                authority.getAuthority()
                        ) ||
                                "ROLE_EMPLEADO".equals(
                                        authority.getAuthority()
                                )
                );

        if (!tienePermiso) {
            throw new IllegalArgumentException(
                    "Solo un administrador o empleado puede registrar mantenimientos."
            );
        }
    }

    // Verificar solamente Administrador
    private void verificarAdministrador() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "No hay un usuario autenticado."
            );
        }

        boolean esAdministrador = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMINISTRADOR".equals(
                                authority.getAuthority()
                        )
                );

        if (!esAdministrador) {
            throw new IllegalArgumentException(
                    "Solo un administrador puede eliminar mantenimientos."
            );
        }
    }
}