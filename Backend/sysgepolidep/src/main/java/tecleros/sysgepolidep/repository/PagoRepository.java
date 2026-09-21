package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Listar historial de pagos de un socio
    List<Pago> findBySocioId(Long socioId);
}
