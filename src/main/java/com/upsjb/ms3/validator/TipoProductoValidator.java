// ruta: src/main/java/com/upsjb/ms3/validator/TipoProductoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class TipoProductoValidator {

    private static final int MAX_CODIGO_LENGTH = 50;
    private static final int MAX_NOMBRE_LENGTH = 120;
    private static final int MAX_DESCRIPCION_LENGTH = 300;

    public void validateCreate(
            String codigo,
            String nombre,
            String descripcion,
            boolean duplicatedCodigo,
            boolean duplicatedNombre
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateDescripcion(descripcion, errors);
        errors.throwIfAny("No se puede crear el tipo de producto.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe un tipo de producto activo con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe un tipo de producto activo con el mismo nombre.");
    }

    public void validateUpdate(
            TipoProducto tipoProducto,
            String codigo,
            String nombre,
            String descripcion,
            boolean duplicatedCodigo,
            boolean duplicatedNombre
    ) {
        requireActive(tipoProducto);

        ValidationErrorCollector errors = ValidationErrorCollector.create();
        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateDescripcion(descripcion, errors);
        errors.throwIfAny("No se puede actualizar el tipo de producto.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe otro tipo de producto activo con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe otro tipo de producto activo con el mismo nombre.");
    }

    public void validateCanActivate(TipoProducto tipoProducto) {
        requireExists(tipoProducto);

        if (tipoProducto.isActivo()) {
            throw new ConflictException(
                    "TIPO_PRODUCTO_YA_ACTIVO",
                    "El tipo de producto ya se encuentra activo."
            );
        }
    }

    public void validateCanDeactivate(TipoProducto tipoProducto, boolean hasActiveProducts) {
        requireActive(tipoProducto);

        if (hasActiveProducts) {
            throw new ConflictException(
                    "TIPO_PRODUCTO_CON_PRODUCTOS_ACTIVOS",
                    "No se puede inactivar el tipo de producto porque tiene productos activos asociados."
            );
        }
    }

    public void requireActive(TipoProducto tipoProducto) {
        requireExists(tipoProducto);

        if (!tipoProducto.isActivo()) {
            throw new NotFoundException(
                    "TIPO_PRODUCTO_INACTIVO",
                    "El tipo de producto no está activo."
            );
        }
    }

    public void requireExists(TipoProducto tipoProducto) {
        if (tipoProducto == null) {
            throw new NotFoundException(
                    "TIPO_PRODUCTO_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código del tipo de producto es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > MAX_CODIGO_LENGTH) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre del tipo de producto es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > MAX_NOMBRE_LENGTH) {
            errors.add("nombre", "El nombre no debe superar 120 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateDescripcion(String descripcion, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(descripcion)) {
            return;
        }

        if (StringNormalizer.clean(descripcion).length() > MAX_DESCRIPCION_LENGTH) {
            errors.add(
                    "descripcion",
                    "La descripción no debe superar 300 caracteres.",
                    "MAX_LENGTH",
                    descripcion
            );
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("TIPO_PRODUCTO_DUPLICADO", message);
        }
    }
}