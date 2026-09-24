package tecleros.sysgepolidep.membresia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tecleros.sysgepolidep.categoria.CategoriaRepository;
import tecleros.sysgepolidep.socio.SocioRepository;

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

    public List<Membresia> listarTodas() {
        return membresiaRepository.findAll();
    }

    public Optional<Membresia> buscarPorId(Long id) {
        return membresiaRepository.findById(id);
    }

    public Membresia guardarMembresia(Membresia membresia) {

        if (membresia == null) {
            throw new IllegalArgumentException(
                    "La membresía no puede ser nula."
            );
        }

        if (membresia.getCategoria() == null ||
                membresia.getCategoria().getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe tener una categoría."
            );
        }

        Long idCategoria = membresia.getCategoria().getIdCategoria();

        if (categoriaRepository.findById(idCategoria).isEmpty()) {
            throw new IllegalArgumentException(
                    "La categoría asociada no existe."
            );
        }

        if (membresia.getSocio() == null ||
                membresia.getSocio().getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "La membresía debe estar asociada a un socio."
            );
        }

        Long idSocio = membresia.getSocio().getIdUsuario();

        if (socioRepository.findById(idSocio).isEmpty()) {
            throw new IllegalArgumentException(
                    "El socio asociado no existe."
            );
        }

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

        if (!membresia.getFechaVenc().isAfter(membresia.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a la fecha de inicio."
            );
        }

        if (membresia.getEstado() == null ||
                membresia.getEstado().trim().isEmpty()) {

            membresia.setEstado("VIGENTE");
        }

        String estado = membresia.getEstado().toUpperCase();

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

    public void eliminarMembresia(Long id) {

        if (!membresiaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Membresía no encontrada con ID: " + id
            );
        }

        membresiaRepository.deleteById(id);
    }
}