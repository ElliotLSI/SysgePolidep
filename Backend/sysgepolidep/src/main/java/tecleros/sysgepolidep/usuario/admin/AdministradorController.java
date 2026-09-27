package tecleros.sysgepolidep.usuario.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @PostMapping("/{idUsuario}")
    public Administrador crearAdministrador(
            @PathVariable Long idUsuario,
            @RequestParam(defaultValue = "1") Integer nivelAcceso) {

        return administradorService.crearAdministrador(
                idUsuario,
                nivelAcceso
        );
    }
}