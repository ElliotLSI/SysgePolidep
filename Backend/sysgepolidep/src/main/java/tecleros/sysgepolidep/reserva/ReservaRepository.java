package tecleros.sysgepolidep.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Consulta para buscar reservas activas de una instalación en una fecha determinada
    @Query("SELECT r FROM Reserva r WHERE r.instalacion.idInstalacion = :idInstalacion " +
            "AND r.fechaReserva = :fecha AND r.estado <> 'CANCELADA'")
    List<Reserva> findReservasActivasPorInstalacionYFecha(
            @Param("idInstalacion") Long idInstalacion,
            @Param("fecha") LocalDate fecha
    );
}