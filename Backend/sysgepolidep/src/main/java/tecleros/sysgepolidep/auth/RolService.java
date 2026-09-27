package tecleros.sysgepolidep.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.usuario.admin.AdministradorRepository;
import tecleros.sysgepolidep.usuario.empleado.EmpleadoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;


    public List<String> obtenerRoles(Long idUsuario) {

        List<String> roles = new ArrayList<>();

        // ---------------------------------------------
        // ADMINISTRADOR
        // ---------------------------------------------

        if (administradorRepository.existsById(idUsuario)) {
            roles.add("ADMINISTRADOR");
        }


        // ---------------------------------------------
        // EMPLEADO
        // ---------------------------------------------

        if (empleadoRepository.existsById(idUsuario)) {
            roles.add("EMPLEADO");
        }


        // ---------------------------------------------
        // SOCIO
        // ---------------------------------------------
        // Un usuario es SOCIO solamente si posee
        // una membresía vigente.

        if (!membresiaRepository
                .findBySocioIdUsuarioAndEstado(idUsuario, "VIGENTE")
                .isEmpty()) {

            roles.add("SOCIO");
        }


        // ---------------------------------------------
        // USUARIO COMÚN
        // ---------------------------------------------

        if (roles.isEmpty()) {
            roles.add("USUARIO");
        }

        return roles;
    }
}