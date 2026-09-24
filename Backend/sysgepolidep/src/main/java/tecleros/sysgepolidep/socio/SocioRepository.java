package tecleros.sysgepolidep.socio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {

    // Método personalizado para buscar un socio por su número de socio
    Optional<Socio> findByNroSocio(Integer nroSocio);
}