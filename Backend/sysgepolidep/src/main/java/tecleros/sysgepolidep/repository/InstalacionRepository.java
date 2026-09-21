package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstalacionRepository extends JpaRepository<Instalacion, Long> {

    // Listar instalaciones pertenecientes a una categoría
    List<Instalacion> findByCategoriaId(Long categoriaId);

    // Listar instalaciones disponibles
    List<Instalacion> findByDisponible(Boolean disponible);
}
