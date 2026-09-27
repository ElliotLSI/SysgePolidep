package tecleros.sysgepolidep.socio;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.membresia.Membresia;
import tecleros.sysgepolidep.membresia.MembresiaRepository;
import tecleros.sysgepolidep.categoria.Categoria;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
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

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private EntityManager entityManager;


    // ==========================================================
    // LISTAR TODOS LOS SOCIOS
    // ==========================================================

    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }


    // ==========================================================
    // BUSCAR SOCIO POR ID DE USUARIO
    // ==========================================================

    public Optional<Socio> buscarPorId(Long id) {
        return socioRepository.findById(id);
    }


    // ==========================================================
    // BUSCAR SOCIO POR NÚMERO DE SOCIO
    // ==========================================================

    public Optional<Socio> buscarPorNroSocio(Integer nroSocio) {
        return socioRepository.findByNroSocio(nroSocio);
    }


    // ==========================================================
    // GUARDAR SOCIO
    // ==========================================================

    public Socio guardarSocio(Socio socio) {

        if (socio == null) {
            throw new IllegalArgumentException("El socio no puede ser nulo.");
        }

        if (socio.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        Usuario usuario = usuarioRepository.findById(
                socio.getIdUsuario()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "El usuario indicado no existe."
                )
        );

        if (socioRepository.findByUsuarioIdUsuario(
                socio.getIdUsuario()
        ).isPresent()) {

            throw new IllegalArgumentException(
                    "El usuario ya está registrado como socio."
            );
        }

        if (socio.getNroSocio() == null) {
            socio.setNroSocio(generarNumeroSocio());
        }

        socio.setUsuario(usuario);

        return socioRepository.save(socio);
    }


    // ==========================================================
    // GENERAR NÚMERO DE SOCIO
    // ==========================================================

    private Integer generarNumeroSocio() {

        Optional<Socio> ultimoSocio =
                socioRepository.findTopByOrderByNroSocioDesc();

        if (ultimoSocio.isEmpty()) {
            return 1001;
        }

        return ultimoSocio.get().getNroSocio() + 1;
    }


    // ==========================================================
    // CONVERTIR USUARIO EN SOCIO
    // ==========================================================

    @Transactional
    public Membresia convertirEnSocio(AltaSocioDTO dto) {

        // ------------------------------------------------------
        // VALIDACIONES BÁSICAS
        // ------------------------------------------------------

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos de alta son obligatorios."
            );
        }

        if (dto.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (dto.getIdCategoria() == null) {
            throw new IllegalArgumentException(
                    "El ID de la categoría es obligatorio."
            );
        }


        // ------------------------------------------------------
        // BUSCAR USUARIO
        // ------------------------------------------------------

        Usuario usuario = usuarioRepository.findById(
                dto.getIdUsuario()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "El usuario indicado no existe."
                )
        );


        // ------------------------------------------------------
        // BUSCAR CATEGORÍA
        // ------------------------------------------------------

        Categoria categoria = categoriaRepository.findById(
                dto.getIdCategoria()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "La categoría indicada no existe."
                )
        );


        // ------------------------------------------------------
        // VALIDAR CATEGORÍA ACTIVA
        // ------------------------------------------------------

        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new IllegalArgumentException(
                    "La categoría seleccionada no está activa."
            );
        }


        // ------------------------------------------------------
        // VALIDAR QUE NO SEA SOCIO
        // ------------------------------------------------------

        if (socioRepository.findByUsuarioIdUsuario(
                dto.getIdUsuario()
        ).isPresent()) {

            throw new IllegalArgumentException(
                    "El usuario ya está registrado como socio."
            );
        }


        // ------------------------------------------------------
        // VALIDAR DURACIÓN DE LA MEMBRESÍA
        // ------------------------------------------------------

        if (categoria.getDuracionMeses() == null
                || categoria.getDuracionMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La categoría debe tener una duración válida."
            );
        }


        // ------------------------------------------------------
        // CREAR SOCIO
        // ------------------------------------------------------

        Socio socio = new Socio();

        socio.setIdUsuario(usuario.getIdUsuario());

        socio.setNroSocio(generarNumeroSocio());

        socio.setUsuario(usuario);


        // IMPORTANTE:
        // No usamos socioRepository.save(socio) acá.
        //
        // Como Id_Usuario es una PK asignada manualmente,
        // Spring Data puede interpretar el objeto como existente
        // y utilizar merge().
        //
        // Usamos persist() porque este socio es una entidad nueva.

        entityManager.persist(socio);


        // ------------------------------------------------------
        // CALCULAR FECHAS DE MEMBRESÍA
        // ------------------------------------------------------

        java.time.LocalDate fechaInicio =
                java.time.LocalDate.now();

        java.time.LocalDate fechaVencimiento =
                fechaInicio.plusMonths(
                        categoria.getDuracionMeses()
                );


        // ------------------------------------------------------
        // CREAR MEMBRESÍA
        // ------------------------------------------------------

        Membresia membresia = new Membresia();

        membresia.setCategoria(categoria);

        membresia.setSocio(socio);

        membresia.setFechaInicio(fechaInicio);

        membresia.setFechaVenc(fechaVencimiento);

        membresia.setEstado("VIGENTE");


        // ------------------------------------------------------
        // GUARDAR MEMBRESÍA
        // ------------------------------------------------------

        return membresiaRepository.save(membresia);
    }


    // ==========================================================
    // ELIMINAR SOCIO
    // ==========================================================

    public void eliminarSocio(Long id) {

        if (!socioRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "El socio indicado no existe."
            );
        }

        socioRepository.deleteById(id);
    }
}