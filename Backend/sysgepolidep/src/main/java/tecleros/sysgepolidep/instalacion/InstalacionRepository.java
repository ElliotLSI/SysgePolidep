package tecleros.sysgepolidep.instalacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstalacionRepository extends JpaRepository<Instalacion, Long> {
    List<Instalacion> findByEstado(String estado);
    List<Instalacion> findByTipo(String tipo);
}