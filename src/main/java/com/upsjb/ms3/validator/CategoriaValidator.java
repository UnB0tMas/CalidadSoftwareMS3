// ruta: src/main/java/com/upsjb/ms3/validator/CategoriaValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class CategoriaValidator {

    private static final int MAX_NIVEL = 6;

    public void validateCreate(
            String codigo,
            String nombre,
            Categoria categoriaPadre,
            Integer orden,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateOrden(orden, errors);

        if (categoriaPadre != null && !categoriaPadre.isActivo()) {
            errors.add("categoriaPadre", "La categoría padre debe estar activa.", "INACTIVE_PARENT", categoriaPadre.getIdCategoria());
        }

        if (categoriaPadre != null && categoriaPadre.getNivel() != null && categoriaPadre.getNivel() >= MAX_NIVEL) {
            errors.add("categoriaPadre", "La categoría padre supera el nivel máximo permitido.", "MAX_LEVEL", categoriaPadre.getIdCategoria());
        }

        errors.throwIfAny("No se puede crear la categoría.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe una categoría activa con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe una categoría activa con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe una categoría activa con el mismo slug.");
    }

    public void validateUpdate(
            Categoria categoria,
            String nombre,
            Integer orden,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        requireActive(categoria);

        ValidationErrorCollector errors = ValidationErrorCollector.create();
        validateNombre(nombre, errors);
        validateOrden(orden, errors);
        errors.throwIfAny("No se puede actualizar la categoría.");

        requireNotDuplicated(duplicatedNombre, "Ya existe otra categoría activa con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe otra categoría activa con el mismo slug.");
    }

    public void validateChangeParent(Categoria categoria, Categoria nuevaPadre) {
        requireActive(categoria);

        if (nuevaPadre == null) {
            return;
        }

        requireActive(nuevaPadre);

        if (categoria.getIdCategoria() != null
                && categoria.getIdCategoria().equals(nuevaPadre.getIdCategoria())) {
            throw new ConflictException(
                    "CATEGORIA_PADRE_INVALIDA",
                    "Una categoría no puede ser padre de sí misma."
            );
        }

        Categoria cursor = nuevaPadre;
        while (cursor != null) {
            if (cursor.getIdCategoria() != null
                    && cursor.getIdCategoria().equals(categoria.getIdCategoria())) {
                throw new ConflictException(
                        "CATEGORIA_CICLO_DETECTADO",
                        "No se puede asignar esa categoría padre porque genera un ciclo jerárquico."
                );
            }
            cursor = cursor.getCategoriaPadre();
        }

        if (nuevaPadre.getNivel() != null && nuevaPadre.getNivel() >= MAX_NIVEL) {
            throw new ConflictException(
                    "CATEGORIA_NIVEL_MAXIMO",
                    "No se puede asignar la categoría padre porque supera el nivel máximo permitido."
            );
        }
    }

    public void validateCanDeactivate(
            Categoria categoria,
            boolean hasActiveChildren,
            boolean hasActiveProducts
    ) {
        requireActive(categoria);

        if (hasActiveChildren) {
            throw new ConflictException(
                    "CATEGORIA_CON_HIJAS_ACTIVAS",
                    "No se puede inactivar la categoría porque tiene subcategorías activas."
            );
        }

        if (hasActiveProducts) {
            throw new ConflictException(
                    "CATEGORIA_CON_PRODUCTOS_ACTIVOS",
                    "No se puede inactivar la categoría porque tiene productos activos asociados."
            );
        }
    }

    public void requireActive(Categoria categoria) {
        requireExists(categoria);

        if (!categoria.isActivo()) {
            throw new NotFoundException(
                    "CATEGORIA_INACTIVA",
                    "La categoría no está activa."
            );
        }
    }

    private void requireExists(Categoria categoria) {
        if (categoria == null) {
            throw new NotFoundException(
                    "CATEGORIA_NO_ENCONTRADA",
                    "Categoría no encontrada."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código de la categoría es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > 50) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre de la categoría es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > 150) {
            errors.add("nombre", "El nombre no debe superar 150 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateOrden(Integer orden, ValidationErrorCollector errors) {
        if (orden != null && orden < 0) {
            errors.add("orden", "El orden no puede ser negativo.", "INVALID_VALUE", orden);
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("CATEGORIA_DUPLICADA", message);
        }
    }
}