package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    // Aquí puedes agregar consultas personalizadas para empleados si las necesitas luego
}