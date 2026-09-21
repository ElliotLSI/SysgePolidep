package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Buscar empleado vinculado a un Usuario
    Optional<Empleado> findByUsuarioId(Long usuarioId);

    // Listar empleados por cargo
    List<Empleado> findByCargo(String cargo);
}
