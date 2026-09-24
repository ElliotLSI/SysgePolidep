package tecleros.sysgepolidep.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Buscar pagos asociados a una reserva específica
    Optional<Pago> findByReservaIdReserva(Long idReserva);

    // Listar pagos según su estado ('APROBADO', 'PENDIENTE', 'RECHAZADO')
    List<Pago> findByEstado(String estado);
}