package tecleros.sysgepolidep.membresia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    List<Membresia> findByEstado(String estado);
    List<Membresia> findBySocioIdUsuario(Long idUsuario);
}