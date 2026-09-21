package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.HistorialMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialMantenimientoRepository extends JpaRepository<HistorialMantenimiento, Long> {
}