package tecleros.sysgepolidep.socio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    public Optional<Socio> buscarPorId(Long id) {
        return socioRepository.findById(id);
    }

    public Optional<Socio> buscarPorNroSocio(Integer nroSocio) {
        return socioRepository.findByNroSocio(nroSocio);
    }

    public Socio guardarSocio(Socio socio) {

        // Validar que el socio no sea nulo
        if (socio == null) {
            throw new IllegalArgumentException(
                    "El socio no puede ser nulo."
            );
        }

        // Validar número de socio
        if (socio.getNroSocio() == null) {
            throw new IllegalArgumentException(
                    "El número de socio es obligatorio."
            );
        }

        // Validar que el número de socio sea positivo
        if (socio.getNroSocio() <= 0) {
            throw new IllegalArgumentException(
                    "El número de socio debe ser mayor a 0."
            );
        }

        // Validar que el número de socio no esté repetido
        if (socioRepository
                .findByNroSocio(socio.getNroSocio())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "El número de socio ya se encuentra registrado."
            );
        }

        // Validar usuario asociado
        if (socio.getUsuario() == null ||
                socio.getUsuario().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "El socio debe estar asociado a un usuario."
            );
        }

        Long idUsuario = socio.getUsuario().getIdUsuario();

        // Validar que el usuario exista
        Optional<Usuario> usuarioExistente =
                usuarioRepository.findById(idUsuario);

        if (usuarioExistente.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario asociado no existe."
            );
        }

        // Validar que el usuario no sea ya socio
        if (socioRepository.findByUsuarioIdUsuario(idUsuario).isPresent()) {
            throw new IllegalArgumentException(
                    "El usuario ya está registrado como socio."
            );
        }

        // Asociar el usuario real obtenido de la base de datos
        socio.setUsuario(usuarioExistente.get());

        return socioRepository.save(socio);
    }

    public void eliminarSocio(Long id) {

        if (!socioRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Socio no encontrado con ID: " + id
            );
        }

        socioRepository.deleteById(id);
    }
}