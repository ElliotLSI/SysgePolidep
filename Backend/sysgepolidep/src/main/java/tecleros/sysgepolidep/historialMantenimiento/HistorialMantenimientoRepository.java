package tecleros.sysgepolidep.historialMantenimiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialMantenimientoRepository extends JpaRepository<HistorialMantenimiento, Long> {
    List<HistorialMantenimiento> findByInstalacionIdInstalacion(Long idInstalacion);
}