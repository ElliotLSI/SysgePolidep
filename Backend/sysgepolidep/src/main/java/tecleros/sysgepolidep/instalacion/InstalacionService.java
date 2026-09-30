package tecleros.sysgepolidep.instalacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InstalacionService {

    @Autowired
    private InstalacionRepository instalacionRepository;

    // Listar todas las instalaciones
    public List<Instalacion> listarTodas() {
        return instalacionRepository.findAll();
    }

    // Buscar instalación por ID
    public Optional<Instalacion> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la instalación es obligatorio."
            );
        }

        return instalacionRepository.findById(id);
    }

    // Registrar una instalación
    @Transactional
    public Instalacion guardarInstalacion(Instalacion instalacion) {

        verificarAdministrador();

        if (instalacion == null) {
            throw new IllegalArgumentException(
                    "La instalación no puede ser nula."
            );
        }

        validarDatos(instalacion);

        String nombre = instalacion.getNombre().trim();

        if (instalacionRepository.findByNombre(nombre).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una instalación con ese nombre."
            );
        }

        instalacion.setNombre(nombre);
        instalacion.setTipo(instalacion.getTipo().trim());

        if (instalacion.getEstado() == null ||
                instalacion.getEstado().trim().isEmpty()) {
            instalacion.setEstado("DISPONIBLE");
        } else {
            instalacion.setEstado(
                    instalacion.getEstado().trim().toUpperCase()
            );
        }

        return instalacionRepository.save(instalacion);
    }

    // Actualizar una instalación
    @Transactional
    public Instalacion actualizarInstalacion(Long id, Instalacion datos) {

        verificarAdministrador();

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la instalación es obligatorio."
            );
        }

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la instalación son obligatorios."
            );
        }

        Instalacion instalacionExistente =
                instalacionRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No existe una instalación con el ID indicado."
                        ));

        validarDatos(datos);

        String nombre = datos.getNombre().trim();

        Optional<Instalacion> instalacionPorNombre =
                instalacionRepository.findByNombre(nombre);

        if (instalacionPorNombre.isPresent() &&
                !instalacionPorNombre.get()
                        .getIdInstalacion().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otra instalación con ese nombre."
            );
        }

        String estado;

        if (datos.getEstado() == null ||
                datos.getEstado().trim().isEmpty()) {
            estado = instalacionExistente.getEstado();
        } else {
            estado = datos.getEstado().trim().toUpperCase();
        }

        validarEstado(estado);

        instalacionExistente.setNombre(nombre);
        instalacionExistente.setTipo(datos.getTipo().trim());
        instalacionExistente.setCapacidad(datos.getCapacidad());
        instalacionExistente.setTarifaBase(datos.getTarifaBase());
        instalacionExistente.setEstado(estado);

        return instalacionRepository.save(instalacionExistente);
    }

    // Eliminar una instalación
    @Transactional
    public void eliminarInstalacion(Long id) {

        verificarAdministrador();

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la instalación es obligatorio."
            );
        }

        if (!instalacionRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "No existe una instalación con el ID indicado."
            );
        }

        instalacionRepository.deleteById(id);
    }

    // Validar los datos de la instalación
    private void validarDatos(Instalacion instalacion) {

        if (instalacion.getNombre() == null ||
                instalacion.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la instalación es obligatorio."
            );
        }

        if (instalacion.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres."
            );
        }

        if (instalacion.getTipo() == null ||
                instalacion.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El tipo de instalación es obligatorio."
            );
        }

        if (instalacion.getTipo().trim().length() > 50) {
            throw new IllegalArgumentException(
                    "El tipo no puede superar los 50 caracteres."
            );
        }

        if (instalacion.getCapacidad() != null &&
                instalacion.getCapacidad() <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor a 0."
            );
        }

        if (instalacion.getTarifaBase() == null) {
            throw new IllegalArgumentException(
                    "La tarifa base es obligatoria."
            );
        }

        if (instalacion.getTarifaBase() < 0) {
            throw new IllegalArgumentException(
                    "La tarifa base no puede ser negativa."
            );
        }

        if (instalacion.getEstado() != null &&
                !instalacion.getEstado().trim().isEmpty()) {
            validarEstado(instalacion.getEstado().trim().toUpperCase());
        }
    }

    // Validar los estados permitidos
    private void validarEstado(String estado) {

        if (!estado.equals("DISPONIBLE") &&
                !estado.equals("MANTENIMIENTO") &&
                !estado.equals("CLAUSURADA")) {
            throw new IllegalArgumentException(
                    "El estado debe ser DISPONIBLE, MANTENIMIENTO o CLAUSURADA."
            );
        }
    }

    // Verificar que el usuario sea administrador
    private void verificarAdministrador() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(authority ->
                                "ROLE_ADMINISTRADOR".equals(
                                        authority.getAuthority()
                                ))) {
            throw new IllegalArgumentException(
                    "Solo un administrador puede realizar esta operación."
            );
        }
    }
}