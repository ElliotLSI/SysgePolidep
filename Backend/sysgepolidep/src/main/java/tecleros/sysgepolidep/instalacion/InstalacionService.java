package tecleros.sysgepolidep.instalacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstalacionService {

    @Autowired
    private InstalacionRepository instalacionRepository;

    public List<Instalacion> listarTodas() {
        return instalacionRepository.findAll();
    }

    public Optional<Instalacion> buscarPorId(Long id) {
        return instalacionRepository.findById(id);
    }

    public Instalacion guardarInstalacion(Instalacion instalacion) {

        if (instalacion == null) {
            throw new IllegalArgumentException(
                    "La instalación no puede ser nula."
            );
        }

        if (instalacion.getNombre() == null ||
                instalacion.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la instalación es obligatorio."
            );
        }

        if (instalacionRepository
                .findByNombre(instalacion.getNombre())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe una instalación con ese nombre."
            );
        }

        if (instalacion.getTipo() == null ||
                instalacion.getTipo().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El tipo de instalación es obligatorio."
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

        if (instalacion.getEstado() == null ||
                instalacion.getEstado().trim().isEmpty()) {

            instalacion.setEstado("DISPONIBLE");
        }

        String estado = instalacion.getEstado().toUpperCase();

        if (!estado.equals("DISPONIBLE") &&
                !estado.equals("MANTENIMIENTO") &&
                !estado.equals("CLAUSURADA")) {

            throw new IllegalArgumentException(
                    "El estado debe ser DISPONIBLE, MANTENIMIENTO o CLAUSURADA."
            );
        }

        instalacion.setEstado(estado);

        return instalacionRepository.save(instalacion);
    }

    public void eliminarInstalacion(Long id) {

        if (!instalacionRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Instalación no encontrada con ID: " + id
            );
        }

        instalacionRepository.deleteById(id);
    }
}