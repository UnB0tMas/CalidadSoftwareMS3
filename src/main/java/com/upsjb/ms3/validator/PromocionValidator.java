// ruta: src/main/java/com/upsjb/ms3/validator/PromocionValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class PromocionValidator {

    public void validateCreate(
            String codigo,
            String nombre,
            Long creadoPorIdUsuarioMs1,
            boolean duplicatedCodigo,
            boolean duplicatedNombre
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede crear la promoción.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe una promoción activa con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe una promoción activa con el mismo nombre.");
    }

    public void validateUpdate(
            Promocion promocion,
            String nombre,
            boolean duplicatedNombre
    ) {
        requireActive(promocion);

        ValidationErrorCollector errors = ValidationErrorCollector.create();
        validateNombre(nombre, errors);
        errors.throwIfAny("No se puede actualizar la promoción.");

        requireNotDuplicated(duplicatedNombre, "Ya existe otra promoción activa con el mismo nombre.");
    }

    public void validateCanDeactivate(Promocion promocion, boolean hasActiveVersion) {
        requireActive(promocion);

        if (hasActiveVersion) {
            throw new ConflictException(
                    "PROMOCION_CON_VERSION_ACTIVA",
                    "No se puede inactivar la promoción porque tiene una versión activa o vigente."
            );
        }
    }

    public void requireActive(Promocion promocion) {
        if (promocion == null) {
            throw new NotFoundException(
                    "PROMOCION_NO_ENCONTRADA",
                    "Promoción no encontrada."
            );
        }

        if (!promocion.isActivo()) {
            throw new NotFoundException(
                    "PROMOCION_INACTIVA",
                    "La promoción no está activa."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código de la promoción es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > 80) {
            errors.add("codigo", "El código no debe superar 80 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre de la promoción es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > 180) {
            errors.add("nombre", "El nombre no debe superar 180 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("PROMOCION_DUPLICADA", message);
        }
    }
}