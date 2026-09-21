package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    // Repositorio para administradores
    // Buscar administrador por su ID de Usuario
    Optional<Administrador> findByUsuarioId(Long usuarioId);
}
