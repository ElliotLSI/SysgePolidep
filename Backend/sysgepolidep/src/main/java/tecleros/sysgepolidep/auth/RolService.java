package tecleros.sysgepolidep.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.usuario.admin.AdministradorRepository;
import tecleros.sysgepolidep.usuario.empleado.EmpleadoRepository;
import tecleros.sysgepolidep.socio.SocioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    public List<String> obtenerRoles(Long idUsuario) {

        List<String> roles = new ArrayList<>();

        if (administradorRepository.existsById(idUsuario)) {
            roles.add("ADMINISTRADOR");
        }

        if (empleadoRepository.existsById(idUsuario)) {
            roles.add("EMPLEADO");
        }

        if (socioRepository.existsById(idUsuario)) {
            roles.add("SOCIO");
        }

        if (!membresiaRepository
                .findBySocioIdUsuarioAndEstado(idUsuario, "VIGENTE")
                .isEmpty()) {

            roles.add("SOCIO");
        }

        // Si no pertenece a ningún grupo especial,
        // es un usuario común.
        if (roles.isEmpty()) {
            roles.add("USUARIO");
        }

        return roles;
    }
}