package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Listar reservas asociadas a un socio
    List<Reserva> findBySocioId(Long socioId);

    // Listar reservas para una instalación en particular
    List<Reserva> findByInstalacionId(Long instalacionId);
}
