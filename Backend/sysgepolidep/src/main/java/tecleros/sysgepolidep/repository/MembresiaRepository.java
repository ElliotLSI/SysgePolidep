package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // <-- Importa esto

@Repository // <-- Y agrégale esta anotación acá
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
}