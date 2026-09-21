package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    Optional<Socio> findByNroSocio(Integer nroSocio);
}