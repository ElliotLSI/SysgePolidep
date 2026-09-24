package tecleros.sysgepolidep.reservaAFavor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaAFavorRepository
        extends JpaRepository<ReservaAFavor, Long> {

    List<ReservaAFavor> findByUsuarioIdUsuario(Long idUsuario);

    Optional<ReservaAFavor> findByReservaOrigenIdReserva(Long idReserva);

    List<ReservaAFavor> findByUsuarioIdUsuarioAndUtilizadaFalse(
            Long idUsuario
    );
}