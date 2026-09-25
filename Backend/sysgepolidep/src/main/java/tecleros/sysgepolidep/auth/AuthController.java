package tecleros.sysgepolidep.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RolService rolService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @GetMapping("/roles/{idUsuario}")
    public List<String> obtenerRoles(@PathVariable Long idUsuario) {

        return rolService.obtenerRoles(idUsuario);
    }
}