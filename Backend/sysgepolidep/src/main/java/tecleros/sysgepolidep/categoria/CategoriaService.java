package tecleros.sysgepolidep.categoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    public Categoria guardarCategoria(Categoria categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría no puede ser nula."
            );
        }

        if (categoria.getNombre() == null ||
                categoria.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio."
            );
        }

        if (categoriaRepository
                .findByNombre(categoria.getNombre())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe una categoría con ese nombre."
            );
        }

        if (categoria.getCosto() == null) {
            throw new IllegalArgumentException(
                    "El costo es obligatorio."
            );
        }

        if (categoria.getCosto() < 0) {
            throw new IllegalArgumentException(
                    "El costo no puede ser negativo."
            );
        }

        if (categoria.getPorcDescuento() == null) {
            throw new IllegalArgumentException(
                    "El porcentaje de descuento es obligatorio."
            );
        }

        if (categoria.getPorcDescuento() < 0 ||
                categoria.getPorcDescuento() > 100) {

            throw new IllegalArgumentException(
                    "El porcentaje de descuento debe estar entre 0 y 100."
            );
        }

        if (categoria.getDuracionMeses() == null) {
            throw new IllegalArgumentException(
                    "La duración de la categoría es obligatoria."
            );
        }

        if (categoria.getDuracionMeses() <= 0) {
            throw new IllegalArgumentException(
                    "La duración debe ser mayor a 0 meses."
            );
        }

        if (categoria.getActivo() == null) {
            categoria.setActivo(true);
        }

        return categoriaRepository.save(categoria);
    }

    public void eliminarCategoria(Long id) {

        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Categoría no encontrada con ID: " + id
            );
        }

        categoriaRepository.deleteById(id);
    }

    public Categoria actualizarCategoria(Long id, Categoria datos) {

        Categoria categoriaExistente =
                categoriaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe una categoría con el ID indicado."
                                )
                        );

        if (datos.getNombre() == null ||
                datos.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio."
            );
        }

        Optional<Categoria> categoriaPorNombre =
                categoriaRepository.findByNombre(datos.getNombre());

        if (categoriaPorNombre.isPresent() &&
                !categoriaPorNombre.get().getIdCategoria().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otra categoría con ese nombre."
            );
        }

        if (datos.getCosto() == null) {
            throw new IllegalArgumentException(
                    "El costo es obligatorio."
            );
        }

        if (datos.getCosto() < 0) {
            throw new IllegalArgumentException(
                    "El costo no puede ser negativo."
            );
        }

        if (datos.getPorcDescuento() == null) {
            throw new IllegalArgumentException(
                    "El porcentaje de descuento es obligatorio."
            );
        }

        if (datos.getPorcDescuento() < 0 ||
                datos.getPorcDescuento() > 100) {

            throw new IllegalArgumentException(
                    "El porcentaje de descuento debe estar entre 0 y 100."
            );
        }

        if (datos.getDuracionMeses() == null) {
            throw new IllegalArgumentException(
                    "La duración de la categoría es obligatoria."
            );
        }

        if (datos.getDuracionMeses() <= 0) {
            throw new IllegalArgumentException(
                    "La duración debe ser mayor a 0 meses."
            );
        }

        if (datos.getActivo() == null) {
            datos.setActivo(true);
        }

        categoriaExistente.setNombre(datos.getNombre());
        categoriaExistente.setCosto(datos.getCosto());
        categoriaExistente.setPorcDescuento(datos.getPorcDescuento());
        categoriaExistente.setDescripBeneficios(datos.getDescripBeneficios());
        categoriaExistente.setDuracionMeses(datos.getDuracionMeses());
        categoriaExistente.setActivo(datos.getActivo());

        return categoriaRepository.save(categoriaExistente);
    }
}