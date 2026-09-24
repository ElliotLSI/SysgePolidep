package tecleros.sysgepolidep.historialMantenimiento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialMantenimientoService {

    @Autowired
    private HistorialMantenimientoRepository mantenimientoRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    public List<HistorialMantenimiento> listarTodos() {
        return mantenimientoRepository.findAll();
    }

    public Optional<HistorialMantenimiento> buscarPorId(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public HistorialMantenimiento registrarMantenimiento(
            HistorialMantenimiento mantenimiento) {

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

        if (instalacionRepository.findById(idInstalacion).isEmpty()) {
            throw new IllegalArgumentException(
                    "La instalación asociada no existe."
            );
        }

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

        mantenimiento.setInstalacion(
                instalacionRepository.findById(idInstalacion).get()
        );

        return mantenimientoRepository.save(mantenimiento);
    }

    public void eliminarMantenimiento(Long id) {

        if (!mantenimientoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Mantenimiento no encontrado con ID: " + id
            );
        }

        mantenimientoRepository.deleteById(id);
    }
}