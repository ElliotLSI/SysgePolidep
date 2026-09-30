package tecleros.sysgepolidep.categoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Listar todas las categorías
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // Buscar categoría por ID
    public Optional<Categoria> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la categoría es obligatorio."
            );
        }

        return categoriaRepository.findById(id);
    }

    // Buscar categoría por nombre
    public Optional<Categoria> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio."
            );
        }

        return categoriaRepository.findByNombre(nombre.trim());
    }

    // Registrar una categoría
    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {

        verificarAdministrador();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría no puede ser nula."
            );
        }

        validarDatos(categoria);

        String nombre = categoria.getNombre().trim();

        if (categoriaRepository.findByNombre(nombre).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con ese nombre."
            );
        }

        categoria.setNombre(nombre);

        if (categoria.getActivo() == null) {
            categoria.setActivo(true);
        }

        return categoriaRepository.save(categoria);
    }

    // Actualizar una categoría
    @Transactional
    public Categoria actualizarCategoria(Long id, Categoria datos) {

        verificarAdministrador();

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la categoría es obligatorio."
            );
        }

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la categoría son obligatorios."
            );
        }

        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una categoría con el ID indicado."
                ));

        validarDatos(datos);

        String nombre = datos.getNombre().trim();

        Optional<Categoria> categoriaPorNombre =
                categoriaRepository.findByNombre(nombre);

        if (categoriaPorNombre.isPresent() &&
                !categoriaPorNombre.get().getIdCategoria().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otra categoría con ese nombre."
            );
        }

        categoriaExistente.setNombre(nombre);
        categoriaExistente.setCosto(datos.getCosto());
        categoriaExistente.setPorcDescuento(datos.getPorcDescuento());
        categoriaExistente.setDescripBeneficios(datos.getDescripBeneficios());
        categoriaExistente.setDuracionMeses(datos.getDuracionMeses());

        if (datos.getActivo() != null) {
            categoriaExistente.setActivo(datos.getActivo());
        }

        return categoriaRepository.save(categoriaExistente);
    }

    // Eliminar una categoría
    @Transactional
    public void eliminarCategoria(Long id) {

        verificarAdministrador();

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la categoría es obligatorio."
            );
        }

        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "No existe una categoría con el ID indicado."
            );
        }

        categoriaRepository.deleteById(id);
    }

    // Validar los datos de una categoría
    private void validarDatos(Categoria categoria) {

        if (categoria.getNombre() == null ||
                categoria.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio."
            );
        }

        if (categoria.getNombre().trim().length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 50 caracteres."
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
    }

    // Verificar que el usuario autenticado sea administrador
    private void verificarAdministrador() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(authority ->
                                "ROLE_ADMINISTRADOR".equals(
                                        authority.getAuthority()
                                ))) {

            throw new IllegalArgumentException(
                    "Solo un administrador puede realizar esta operación."
            );
        }
    }
}