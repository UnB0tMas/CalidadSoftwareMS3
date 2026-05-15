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

    public void validateCreate(
            String codigo,
            String nombre,
            String slug,
            Categoria categoriaPadre,
            Integer orden,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateSlug(slug, errors);
        validateOrden(orden, errors);
        validateParent(categoriaPadre, errors);

        errors.throwIfAny("No se puede crear la categoría.");

        validateDuplicates(duplicatedCodigo, duplicatedNombre, duplicatedSlug);
    }

    public void validateUpdate(
            Categoria categoria,
            String codigo,
            String nombre,
            String slug,
            Categoria categoriaPadre,
            Integer orden,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug,
            boolean wouldCreateCycle
    ) {
        requireActive(categoria);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateSlug(slug, errors);
        validateOrden(orden, errors);
        validateParent(categoriaPadre, errors);

        if (wouldCreateCycle) {
            errors.add(
                    "categoriaPadre",
                    "La categoría padre seleccionada genera una jerarquía circular.",
                    "CYCLE_DETECTED",
                    categoriaPadre == null ? null : categoriaPadre.getIdCategoria()
            );
        }

        errors.throwIfAny("No se puede actualizar la categoría.");

        validateDuplicates(duplicatedCodigo, duplicatedNombre, duplicatedSlug);
    }

    public void validateCanDeactivate(
            Categoria categoria,
            boolean hasActiveChildren,
            boolean hasActiveProducts
    ) {
        requireActive(categoria);

        if (hasActiveChildren) {
            throw new ConflictException(
                    "CATEGORIA_CON_SUBCATEGORIAS_ACTIVAS",
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

    public void validateCanActivate(Categoria categoria, boolean parentActive) {
        requireExists(categoria);

        if (categoria.isActivo()) {
            throw new ConflictException(
                    "CATEGORIA_YA_ACTIVA",
                    "La categoría ya se encuentra activa."
            );
        }

        if (!parentActive) {
            throw new ConflictException(
                    "CATEGORIA_PADRE_INACTIVA",
                    "No se puede activar la categoría porque su categoría padre está inactiva."
            );
        }
    }

    public void requireActive(Categoria categoria) {
        requireExists(categoria);

        if (!categoria.isActivo()) {
            throw new ConflictException(
                    "CATEGORIA_INACTIVA",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }

    public void requireExists(Categoria categoria) {
        if (categoria == null) {
            throw new NotFoundException(
                    "CATEGORIA_NO_ENCONTRADA",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código de la categoría es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (codigo.length() > 50) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre de la categoría es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (nombre.length() > 150) {
            errors.add("nombre", "El nombre no debe superar 150 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateSlug(String slug, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(slug)) {
            errors.add("slug", "El slug de la categoría es obligatorio.", "REQUIRED", slug);
            return;
        }

        if (slug.length() > 180) {
            errors.add("slug", "El slug no debe superar 180 caracteres.", "MAX_LENGTH", slug);
        }
    }

    private void validateOrden(Integer orden, ValidationErrorCollector errors) {
        if (orden != null && orden < 0) {
            errors.add("orden", "El orden no puede ser negativo.", "MIN_VALUE", orden);
        }
    }

    private void validateParent(Categoria categoriaPadre, ValidationErrorCollector errors) {
        if (categoriaPadre != null && !categoriaPadre.isActivo()) {
            errors.add(
                    "categoriaPadre",
                    "La categoría padre debe estar activa.",
                    "INACTIVE_REFERENCE",
                    categoriaPadre.getIdCategoria()
            );
        }
    }

    private void validateDuplicates(
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        if (duplicatedCodigo) {
            throw new ConflictException(
                    "CATEGORIA_CODIGO_DUPLICADO",
                    "Ya existe un registro activo con los mismos datos."
            );
        }

        if (duplicatedNombre) {
            throw new ConflictException(
                    "CATEGORIA_NOMBRE_DUPLICADO",
                    "Ya existe un registro activo con los mismos datos."
            );
        }

        if (duplicatedSlug) {
            throw new ConflictException(
                    "CATEGORIA_SLUG_DUPLICADO",
                    "Ya existe un registro activo con los mismos datos."
            );
        }
    }
}