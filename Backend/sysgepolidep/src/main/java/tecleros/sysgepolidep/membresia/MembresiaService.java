package tecleros.sysgepolidep.membresia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.categoria.Categoria;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
import tecleros.sysgepolidep.socio.SocioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SocioRepository socioRepository;


    // ==========================================================
    // LISTAR TODAS LAS MEMBRESÍAS
    // ==========================================================

    public List<Membresia> listarTodas() {
        return membresiaRepository.findAll();
    }


    // ==========================================================
    // BUSCAR MEMBRESÍA POR ID
    // ==========================================================

    public Optional<Membresia> buscarPorId(Long id) {
        return membresiaRepository.findById(id);
    }


    // ==========================================================
    // CREAR / GUARDAR MEMBRESÍA
    // ==========================================================

    public Membresia guardarMembresia(Membresia membresia) {

        if (membresia == null) {
            throw new IllegalArgumentException(
                    "La membresía no puede ser nula."
            );
        }


        // ------------------------------------------------------
        // VALIDAR CATEGORÍA
        // ------------------------------------------------------

        if (membresia.getCategoria() == null ||
                membresia.getCategoria().getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe tener una categoría."
            );
        }

        Long idCategoria =
                membresia.getCategoria().getIdCategoria();

        Categoria categoria =
                categoriaRepository.findById(idCategoria)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La categoría asociada no existe."
                                )
                        );


        // ------------------------------------------------------
        // VALIDAR SOCIO
        // ------------------------------------------------------

        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe estar asociada a un socio."
            );
        }

        Long idSocio =
                membresia.getSocio().getIdUsuario();

        if (socioRepository.findById(idSocio).isEmpty()) {

            throw new IllegalArgumentException(
                    "El socio asociado no existe."
            );
        }


        // ------------------------------------------------------
        // VALIDAR FECHAS
        // ------------------------------------------------------

        if (membresia.getFechaInicio() == null) {

            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria."
            );
        }

        if (membresia.getFechaVenc() == null) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento es obligatoria."
            );
        }

        if (!membresia.getFechaVenc()
                .isAfter(membresia.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de inicio."
            );
        }


        // ------------------------------------------------------
        // VALIDAR ESTADO
        // ------------------------------------------------------

        if (membresia.getEstado() == null ||
                membresia.getEstado().trim().isEmpty()) {

            membresia.setEstado("VIGENTE");
        }

        String estado =
                membresia.getEstado().toUpperCase();

        if (!estado.equals("VIGENTE") &&
                !estado.equals("VENCIDA") &&
                !estado.equals("CANCELADA")) {

            throw new IllegalArgumentException(
                    "El estado debe ser VIGENTE, VENCIDA o CANCELADA."
            );
        }

        membresia.setEstado(estado);


        return membresiaRepository.save(membresia);
    }


    // ==========================================================
    // RENOVAR MEMBRESÍA
    // ==========================================================

    public Membresia renovarMembresia(Long idMembresia) {

        // ------------------------------------------------------
        // BUSCAR MEMBRESÍA
        // ------------------------------------------------------

        Membresia membresia =
                membresiaRepository.findById(idMembresia)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Membresía no encontrada con ID: "
                                                + idMembresia
                                )
                        );


        // ------------------------------------------------------
        // VERIFICAR CATEGORÍA
        // ------------------------------------------------------

        if (membresia.getCategoria() == null ||
                membresia.getCategoria().getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "La membresía no tiene una categoría asociada."
            );
        }


        Categoria categoria =
                categoriaRepository.findById(
                                membresia.getCategoria()
                                        .getIdCategoria()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La categoría asociada no existe."
                                )
                        );


        // ------------------------------------------------------
        // VERIFICAR DURACIÓN
        // ------------------------------------------------------

        if (categoria.getDuracionMeses() == null ||
                categoria.getDuracionMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La categoría no tiene una duración válida."
            );
        }


        // ------------------------------------------------------
        // CALCULAR NUEVA VIGENCIA
        // ------------------------------------------------------

        LocalDate fechaInicio = LocalDate.now();

        LocalDate fechaVencimiento =
                fechaInicio.plusMonths(
                        categoria.getDuracionMeses()
                );


        // ------------------------------------------------------
        // ACTUALIZAR MEMBRESÍA
        // ------------------------------------------------------

        membresia.setFechaInicio(fechaInicio);

        membresia.setFechaVenc(fechaVencimiento);

        membresia.setEstado("VIGENTE");


        // ------------------------------------------------------
        // GUARDAR
        // ------------------------------------------------------

        return membresiaRepository.save(membresia);
    }


    // ==========================================================
    // ELIMINAR MEMBRESÍA
    // ==========================================================

    public void eliminarMembresia(Long id) {

        if (!membresiaRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Membresía no encontrada con ID: " + id
            );
        }

        membresiaRepository.deleteById(id);
    }
}