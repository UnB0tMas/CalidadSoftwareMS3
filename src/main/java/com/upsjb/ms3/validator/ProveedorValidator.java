// ruta: src/main/java/com/upsjb/ms3/validator/ProveedorValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class ProveedorValidator {

    public void validateCreate(
            TipoProveedor tipoProveedor,
            String tipoDocumento,
            String numeroDocumento,
            String ruc,
            String razonSocial,
            String nombres,
            Long creadoPorIdUsuarioMs1,
            boolean duplicatedDocumento,
            boolean duplicatedRuc
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateTipoProveedor(tipoProveedor, tipoDocumento, numeroDocumento, ruc, razonSocial, nombres, errors);

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede registrar el proveedor.");

        if (duplicatedDocumento) {
            throw new ConflictException(
                    "PROVEEDOR_DOCUMENTO_DUPLICADO",
                    "Ya existe un proveedor activo con el mismo documento."
            );
        }

        if (duplicatedRuc) {
            throw new ConflictException(
                    "PROVEEDOR_RUC_DUPLICADO",
                    "Ya existe un proveedor activo con el mismo RUC."
            );
        }
    }

    public void validateUpdate(
            Proveedor proveedor,
            TipoProveedor tipoProveedor,
            String tipoDocumento,
            String numeroDocumento,
            String ruc,
            String razonSocial,
            String nombres,
            Long actualizadoPorIdUsuarioMs1,
            boolean duplicatedDocumento,
            boolean duplicatedRuc
    ) {
        requireActive(proveedor);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateTipoProveedor(tipoProveedor, tipoDocumento, numeroDocumento, ruc, razonSocial, nombres, errors);

        if (actualizadoPorIdUsuarioMs1 == null) {
            errors.add("actualizadoPorIdUsuarioMs1", "El usuario actualizador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede actualizar el proveedor.");

        if (duplicatedDocumento) {
            throw new ConflictException(
                    "PROVEEDOR_DOCUMENTO_DUPLICADO",
                    "Ya existe otro proveedor activo con el mismo documento."
            );
        }

        if (duplicatedRuc) {
            throw new ConflictException(
                    "PROVEEDOR_RUC_DUPLICADO",
                    "Ya existe otro proveedor activo con el mismo RUC."
            );
        }
    }

    public void validateCanDeactivate(Proveedor proveedor, boolean hasPendingPurchases) {
        requireActive(proveedor);

        if (hasPendingPurchases) {
            throw new ConflictException(
                    "PROVEEDOR_CON_COMPRAS_PENDIENTES",
                    "No se puede inactivar el proveedor porque tiene compras pendientes."
            );
        }
    }

    public void requireActive(Proveedor proveedor) {
        if (proveedor == null) {
            throw new NotFoundException(
                    "PROVEEDOR_NO_ENCONTRADO",
                    "Proveedor no encontrado."
            );
        }

        if (!proveedor.isActivo()) {
            throw new NotFoundException(
                    "PROVEEDOR_INACTIVO",
                    "El proveedor no está activo."
            );
        }
    }

    private void validateTipoProveedor(
            TipoProveedor tipoProveedor,
            String tipoDocumento,
            String numeroDocumento,
            String ruc,
            String razonSocial,
            String nombres,
            ValidationErrorCollector errors
    ) {
        if (tipoProveedor == null) {
            errors.add("tipoProveedor", "El tipo de proveedor es obligatorio.", "REQUIRED", null);
            return;
        }

        if (tipoProveedor.isEmpresa()) {
            if (!StringNormalizer.hasText(ruc)) {
                errors.add("ruc", "El RUC es obligatorio para proveedores empresa.", "REQUIRED", ruc);
            } else if (StringNormalizer.onlyDigits(ruc).length() != 11) {
                errors.add("ruc", "El RUC debe tener 11 dígitos.", "INVALID_FORMAT", ruc);
            }

            if (!StringNormalizer.hasText(razonSocial)) {
                errors.add("razonSocial", "La razón social es obligatoria para proveedores empresa.", "REQUIRED", razonSocial);
            }
        }

        if (tipoProveedor.isPersonaNatural()) {
            if (!StringNormalizer.hasText(tipoDocumento)) {
                errors.add("tipoDocumento", "El tipo de documento es obligatorio para persona natural.", "REQUIRED", tipoDocumento);
            }

            if (!StringNormalizer.hasText(numeroDocumento)) {
                errors.add("numeroDocumento", "El número de documento es obligatorio para persona natural.", "REQUIRED", numeroDocumento);
            }

            if (!StringNormalizer.hasText(nombres)) {
                errors.add("nombres", "Los nombres son obligatorios para persona natural.", "REQUIRED", nombres);
            }
        }
    }
}