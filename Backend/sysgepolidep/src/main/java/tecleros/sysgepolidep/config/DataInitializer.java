package tecleros.sysgepolidep.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import tecleros.sysgepolidep.categoria.Categoria;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
import tecleros.sysgepolidep.instalacion.Instalacion;
import tecleros.sysgepolidep.instalacion.InstalacionRepository;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.socio.Socio;
import tecleros.sysgepolidep.socio.SocioRepository;
import tecleros.sysgepolidep.usuario.Usuario;
import tecleros.sysgepolidep.usuario.UsuarioRepository;
import tecleros.sysgepolidep.usuario.admin.Administrador;
import tecleros.sysgepolidep.usuario.admin.AdministradorRepository;
import tecleros.sysgepolidep.usuario.empleado.Empleado;
import tecleros.sysgepolidep.usuario.empleado.EmpleadoRepository;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializarDatos(
            UsuarioRepository usuarioRepository,
            AdministradorRepository administradorRepository,
            EmpleadoRepository empleadoRepository,
            SocioRepository socioRepository,
            MembresiaRepository membresiaRepository,
            CategoriaRepository categoriaRepository,
            InstalacionRepository instalacionRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            Categoria categoria = categoriaRepository
                    .findByNombre("Membresía Estándar")
                    .orElseGet(() -> {

                        Categoria nuevaCategoria = new Categoria();

                        nuevaCategoria.setNombre("Membresía Estándar");
                        nuevaCategoria.setCosto(10000.0);
                        nuevaCategoria.setPorcDescuento(10.0);
                        nuevaCategoria.setDescripBeneficios(
                                "Acceso a beneficios y descuentos para socios."
                        );
                        nuevaCategoria.setDuracionMeses(1);
                        nuevaCategoria.setActivo(true);

                        return categoriaRepository.save(nuevaCategoria);
                    });

            crearInstalacion(
                    instalacionRepository,
                    "Cancha de Fútbol 5",
                    "Cancha de Fútbol",
                    10,
                    15000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Cancha de Básquet",
                    "Cancha de Básquet",
                    12,
                    10000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Cancha de Vóley",
                    "Cancha de Vóley",
                    12,
                    8000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Pileta",
                    "Pileta",
                    30,
                    12000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Salón de Usos Múltiples",
                    "SUM",
                    50,
                    20000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Cancha de Tenis",
                    "Cancha de Tenis",
                    4,
                    7000.0,
                    "DISPONIBLE"
            );

            crearInstalacion(
                    instalacionRepository,
                    "Cancha de Fútbol 11",
                    "Cancha de Fútbol",
                    22,
                    25000.0,
                    "EN MANTENIMIENTO"
            );

            Usuario admin = usuarioRepository
                    .findByNombreUsuario("admin")
                    .orElseGet(() -> {

                        Usuario nuevo = new Usuario();

                        nuevo.setNombre("Administrador");
                        nuevo.setApellido("SysGe");
                        nuevo.setDni("99999991");
                        nuevo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
                        nuevo.setDomicilio("Polideportivo UNSE");
                        nuevo.setTelefono("3850000001");
                        nuevo.setEmail("admin@sysgepolidep.com");
                        nuevo.setNombreUsuario("admin");
                        nuevo.setPassword(
                                passwordEncoder.encode("Admin123")
                        );
                        nuevo.setPertenencia("ADMINISTRATIVO");
                        nuevo.setEstado("ACTIVO");

                        return usuarioRepository.save(nuevo);
                    });

            if (!administradorRepository.existsById(
                    admin.getIdUsuario())) {

                Administrador administrador = new Administrador();

                administrador.setIdUsuario(admin.getIdUsuario());
                administrador.setNivelAcceso(1);

                administradorRepository.save(administrador);
            }

            Usuario empleadoUsuario = usuarioRepository
                    .findByNombreUsuario("empleado")
                    .orElseGet(() -> {

                        Usuario nuevo = new Usuario();

                        nuevo.setNombre("Empleado");
                        nuevo.setApellido("Prueba");
                        nuevo.setDni("99999992");
                        nuevo.setFechaNacimiento(LocalDate.of(1995, 1, 1));
                        nuevo.setDomicilio("Polideportivo UNSE");
                        nuevo.setTelefono("3850000002");
                        nuevo.setEmail("empleado@sysgepolidep.com");
                        nuevo.setNombreUsuario("empleado");
                        nuevo.setPassword(
                                passwordEncoder.encode("Empleado123")
                        );
                        nuevo.setPertenencia("ADMINISTRATIVO");
                        nuevo.setEstado("ACTIVO");

                        return usuarioRepository.save(nuevo);
                    });

            if (!empleadoRepository.existsById(
                    empleadoUsuario.getIdUsuario())) {

                Empleado empleado = new Empleado();

                empleado.setLegajo(1001);
                empleado.setTurno("Mañana");
                empleado.setUsuario(empleadoUsuario);

                empleadoRepository.save(empleado);
            }

            Usuario socioUsuario = usuarioRepository
                    .findByNombreUsuario("socio")
                    .orElseGet(() -> {

                        Usuario nuevo = new Usuario();

                        nuevo.setNombre("Socio");
                        nuevo.setApellido("Prueba");
                        nuevo.setDni("99999993");
                        nuevo.setFechaNacimiento(LocalDate.of(2000, 1, 1));
                        nuevo.setDomicilio("Santiago del Estero");
                        nuevo.setTelefono("3850000003");
                        nuevo.setEmail("socio@sysgepolidep.com");
                        nuevo.setNombreUsuario("socio");
                        nuevo.setPassword(
                                passwordEncoder.encode("Socio123")
                        );
                        nuevo.setPertenencia("NINGUNO");
                        nuevo.setEstado("ACTIVO");

                        return usuarioRepository.save(nuevo);
                    });

            Socio socio = socioRepository
                    .findByUsuarioIdUsuario(
                            socioUsuario.getIdUsuario()
                    )
                    .orElseGet(() -> {

                        Integer nroSocio = socioRepository
                                .findTopByOrderByNroSocioDesc()
                                .map(s -> s.getNroSocio() + 1)
                                .orElse(1001);

                        Socio nuevoSocio = new Socio();

                        nuevoSocio.setIdUsuario(
                                socioUsuario.getIdUsuario()
                        );

                        nuevoSocio.setNroSocio(nroSocio);

                        return socioRepository.save(nuevoSocio);
                    });

            boolean tieneMembresia =
                    !membresiaRepository
                            .findBySocioIdUsuarioAndEstado(
                                    socioUsuario.getIdUsuario(),
                                    "VIGENTE"
                            )
                            .isEmpty();

            if (!tieneMembresia) {

                Membresia membresia = new Membresia();

                membresia.setCategoria(categoria);
                membresia.setSocio(socio);
                membresia.setFechaInicio(LocalDate.now());
                membresia.setFechaVenc(
                        LocalDate.now().plusMonths(
                                categoria.getDuracionMeses()
                        )
                );
                membresia.setEstado("VIGENTE");

                membresiaRepository.save(membresia);
            }

            usuarioRepository
                    .findByNombreUsuario("usuario")
                    .orElseGet(() -> {

                        Usuario nuevo = new Usuario();

                        nuevo.setNombre("Usuario");
                        nuevo.setApellido("Prueba");
                        nuevo.setDni("99999994");
                        nuevo.setFechaNacimiento(LocalDate.of(2002, 1, 1));
                        nuevo.setDomicilio("Santiago del Estero");
                        nuevo.setTelefono("3850000004");
                        nuevo.setEmail("usuario@sysgepolidep.com");
                        nuevo.setNombreUsuario("usuario");
                        nuevo.setPassword(
                                passwordEncoder.encode("Usuario123")
                        );
                        nuevo.setPertenencia("NINGUNO");
                        nuevo.setEstado("ACTIVO");

                        return usuarioRepository.save(nuevo);
                    });

            System.out.println();
            System.out.println(
                    "=================================================="
            );
            System.out.println(
                    " DATOS DE PRUEBA DE SYSGE POLIDEP CARGADOS"
            );
            System.out.println(
                    "=================================================="
            );
            System.out.println(" ADMIN      -> admin / Admin123");
            System.out.println(" EMPLEADO   -> empleado / Empleado123");
            System.out.println(" SOCIO      -> socio / Socio123");
            System.out.println(" USUARIO    -> usuario / Usuario123");
            System.out.println(
                    "=================================================="
            );
            System.out.println();
        };
    }

    private void crearInstalacion(
            InstalacionRepository repository,
            String nombre,
            String tipo,
            Integer capacidad,
            Double tarifaBase,
            String estado
    ) {

        if (repository.findByNombre(nombre).isEmpty()) {

            Instalacion instalacion = new Instalacion();

            instalacion.setNombre(nombre);
            instalacion.setTipo(tipo);
            instalacion.setCapacidad(capacidad);
            instalacion.setTarifaBase(tarifaBase);
            instalacion.setEstado(estado);

            repository.save(instalacion);
        }
    }
}