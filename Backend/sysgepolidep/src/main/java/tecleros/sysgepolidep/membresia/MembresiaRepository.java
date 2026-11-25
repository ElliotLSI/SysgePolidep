package tecleros.sysgepolidep.membresia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

    List<Membresia> findByEstado(String estado);

    List<Membresia> findBySocioIdUsuario(Long idUsuario);

    List<Membresia> findBySocioIdUsuarioAndEstado(
            Long idUsuario,
            String estado
    );

    List<Membresia> findBySocioIdUsuarioAndEstadoAndFechaInicioLessThanEqualAndFechaVencAfter(
            Long idUsuario,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaVenc
    );
}