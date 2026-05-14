// ruta: src/main/java/com/upsjb/ms3/validator/MarcaValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class MarcaValidator {

    public void validateCreate(
            String codigo,
            String nombre,
            String slug,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateSlug(slug, errors);

        errors.throwIfAny("No se puede crear la marca.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe una marca activa con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe una marca activa con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe una marca activa con el mismo slug.");
    }

    public void validateUpdate(
            Marca marca,
            String codigo,
            String nombre,
            String slug,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        requireActive(marca);

        ValidationErrorCollector errors = ValidationErrorCollector.create();
        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateSlug(slug, errors);

        errors.throwIfAny("No se puede actualizar la marca.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe otra marca activa con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe otra marca activa con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe otra marca activa con el mismo slug.");
    }

    public void validateCanActivate(Marca marca) {
        requireExists(marca);

        if (marca.isActivo()) {
            throw new ConflictException(
                    "MARCA_YA_ACTIVA",
                    "La marca ya se encuentra activa."
            );
        }
    }

    public void validateCanDeactivate(Marca marca, boolean hasActiveProducts) {
        requireActive(marca);

        if (hasActiveProducts) {
            throw new ConflictException(
                    "MARCA_CON_PRODUCTOS_ACTIVOS",
                    "No se puede inactivar la marca porque tiene productos activos asociados."
            );
        }
    }

    public void requireActive(Marca marca) {
        requireExists(marca);

        if (!marca.isActivo()) {
            throw new NotFoundException(
                    "MARCA_INACTIVA",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }

    public void requireExists(Marca marca) {
        if (marca == null) {
            throw new NotFoundException(
                    "MARCA_NO_ENCONTRADA",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código de la marca es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > 50) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre de la marca es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > 120) {
            errors.add("nombre", "El nombre no debe superar 120 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateSlug(String slug, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(slug)) {
            errors.add("slug", "El slug de la marca es obligatorio.", "REQUIRED", slug);
            return;
        }

        if (StringNormalizer.clean(slug).length() > 150) {
            errors.add("slug", "El slug no debe superar 150 caracteres.", "MAX_LENGTH", slug);
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("MARCA_DUPLICADA", message);
        }
    }
}