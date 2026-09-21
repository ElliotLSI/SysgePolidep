package tecleros.sysgepolidep.controller;

import tecleros.sysgepolidep.entity.Usuario;
import tecleros.sysgepolidep.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Endpoint para ver todos: GET http://localhost:8080/api/usuarios
    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioService.listarUsuarios();
    }

    // Endpoint para crear: POST http://localhost:8080/api/usuarios
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.registrarUsuario(usuario);
    }
}