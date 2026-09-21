package tecleros.sysgepolidep.repository;

import tecleros.sysgepolidep.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por DNI (útil para validar si ya existe antes de registrar)
    Optional<Usuario> findByDni(String dni);

    // Buscar por Nombre de Usuario
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}