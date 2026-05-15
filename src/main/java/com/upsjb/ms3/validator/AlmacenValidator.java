// ruta: src/main/java/com/upsjb/ms3/validator/AlmacenValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class AlmacenValidator {

    public void validateCreate(
            String codigo,
            String nombre,
            String direccion,
            String observacion,
            Boolean permiteVenta,
            Boolean permiteCompra,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean alreadyHasPrincipal,
            Boolean principal
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateDireccion(direccion, errors);
        validateObservacion(observacion, errors);
        validateFlags(permiteVenta, permiteCompra, errors);

        errors.throwIfAny("No se puede crear el almacén.");

        if (duplicatedCodigo) {
            throw new ConflictException(
                    "ALMACEN_CODIGO_DUPLICADO",
                    "Ya existe un almacén activo con el mismo código."
            );
        }

        if (duplicatedNombre) {
            throw new ConflictException(
                    "ALMACEN_NOMBRE_DUPLICADO",
                    "Ya existe un almacén activo con el mismo nombre."
            );
        }

        validatePrincipal(alreadyHasPrincipal, principal);
    }

    public void validateUpdate(
            Almacen almacen,
            String codigo,
            String nombre,
            String direccion,
            String observacion,
            Boolean permiteVenta,
            Boolean permiteCompra,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean alreadyHasPrincipal,
            Boolean principal
    ) {
        requireActive(almacen);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateDireccion(direccion, errors);
        validateObservacion(observacion, errors);
        validateFlags(permiteVenta, permiteCompra, errors);

        errors.throwIfAny("No se puede actualizar el almacén.");

        if (duplicatedCodigo) {
            throw new ConflictException(
                    "ALMACEN_CODIGO_DUPLICADO",
                    "Ya existe otro almacén activo con el mismo código."
            );
        }

        if (duplicatedNombre) {
            throw new ConflictException(
                    "ALMACEN_NOMBRE_DUPLICADO",
                    "Ya existe otro almacén activo con el mismo nombre."
            );
        }

        if (!Boolean.TRUE.equals(almacen.getPrincipal())) {
            validatePrincipal(alreadyHasPrincipal, principal);
        }
    }

    public void validateCanDeactivate(
            Almacen almacen,
            boolean hasStock,
            boolean hasMovements,
            boolean hasPendingReservations
    ) {
        requireActive(almacen);

        if (Boolean.TRUE.equals(almacen.getPrincipal())) {
            throw new ConflictException(
                    "ALMACEN_PRINCIPAL_NO_INACTIVABLE",
                    "No se puede inactivar el almacén principal."
            );
        }

        if (hasPendingReservations) {
            throw new ConflictException(
                    "ALMACEN_CON_RESERVAS_PENDIENTES",
                    "No se puede inactivar el almacén porque tiene reservas pendientes."
            );
        }

        if (hasStock || hasMovements) {
            throw new ConflictException(
                    "ALMACEN_CON_HISTORIAL",
                    "No se puede inactivar el almacén porque tiene stock o movimientos asociados."
            );
        }
    }

    public void validateCanActivate(Almacen almacen, boolean principalDuplicado) {
        requireExists(almacen);

        if (Boolean.TRUE.equals(almacen.getPrincipal()) && principalDuplicado) {
            throw new ConflictException(
                    "ALMACEN_PRINCIPAL_DUPLICADO",
                    "No se puede activar el almacén porque ya existe un almacén principal activo."
            );
        }
    }

    public void requireActive(Almacen almacen) {
        requireExists(almacen);

        if (!almacen.isActivo()) {
            throw new NotFoundException(
                    "ALMACEN_INACTIVO",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }

    public void requireExists(Almacen almacen) {
        if (almacen == null) {
            throw new NotFoundException(
                    "ALMACEN_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validatePrincipal(boolean alreadyHasPrincipal, Boolean principal) {
        if (Boolean.TRUE.equals(principal) && alreadyHasPrincipal) {
            throw new ConflictException(
                    "ALMACEN_PRINCIPAL_DUPLICADO",
                    "Ya existe un almacén principal activo."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código del almacén es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > 50) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre del almacén es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > 150) {
            errors.add("nombre", "El nombre no debe superar 150 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateDireccion(String direccion, ValidationErrorCollector errors) {
        if (StringNormalizer.hasText(direccion) && StringNormalizer.clean(direccion).length() > 300) {
            errors.add("direccion", "La dirección no debe superar 300 caracteres.", "MAX_LENGTH", direccion);
        }
    }

    private void validateObservacion(String observacion, ValidationErrorCollector errors) {
        if (StringNormalizer.hasText(observacion) && StringNormalizer.clean(observacion).length() > 500) {
            errors.add("observacion", "La observación no debe superar 500 caracteres.", "MAX_LENGTH", observacion);
        }
    }

    private void validateFlags(Boolean permiteVenta, Boolean permiteCompra, ValidationErrorCollector errors) {
        if (permiteVenta == null) {
            errors.add("permiteVenta", "Debe indicar si el almacén permite venta.", "REQUIRED", null);
        }

        if (permiteCompra == null) {
            errors.add("permiteCompra", "Debe indicar si el almacén permite compra.", "REQUIRED", null);
        }
    }
}