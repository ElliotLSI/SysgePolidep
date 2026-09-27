package tecleros.sysgepolidep.usuario.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Administrador crearAdministrador(Long idUsuario, Integer nivelAcceso) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un usuario con el ID: " + idUsuario
                        )
                );

        if (administradorRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException(
                    "El usuario ya es administrador."
            );
        }

        Administrador administrador = new Administrador();

        administrador.setIdUsuario(usuario.getIdUsuario());
        administrador.setNivelAcceso(nivelAcceso);

        return administradorRepository.save(administrador);
    }
}