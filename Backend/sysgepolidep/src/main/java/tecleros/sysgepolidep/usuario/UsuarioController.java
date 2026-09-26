package tecleros.sysgepolidep.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioService.listarTodos()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO obtenerPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);

        return usuario.map(this::convertirADTO).orElse(null);
    }

    @PostMapping
    public UsuarioResponseDTO crearUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
        return convertirADTO(usuarioGuardado);
    }

    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }

    private UsuarioResponseDTO convertirADTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDni(),
                usuario.getFechaNacimiento(),
                usuario.getDomicilio(),
                usuario.getTelefono(),
                usuario.getEmail(),
                usuario.getNombreUsuario(),
                usuario.getPertenencia(),
                usuario.getLegajo(),
                usuario.getFechaRegistro(),
                usuario.getEstado()
        );
    }
}