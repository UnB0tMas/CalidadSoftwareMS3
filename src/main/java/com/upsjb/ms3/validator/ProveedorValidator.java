// ruta: src/main/java/com/upsjb/ms3/validator/ProveedorValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
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
            TipoDocumentoProveedor tipoDocumento,
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

        validateDuplicados(duplicatedDocumento, duplicatedRuc, false);
    }

    public void validateUpdate(
            Proveedor proveedor,
            TipoProveedor tipoProveedor,
            TipoDocumentoProveedor tipoDocumento,
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

        validateDuplicados(duplicatedDocumento, duplicatedRuc, true);
    }

    public void validateCanActivate(Proveedor proveedor) {
        requireExists(proveedor);
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

    public void requireExists(Proveedor proveedor) {
        if (proveedor == null) {
            throw new NotFoundException(
                    "PROVEEDOR_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    public void requireActive(Proveedor proveedor) {
        requireExists(proveedor);

        if (!proveedor.isActivo()) {
            throw new NotFoundException(
                    "PROVEEDOR_INACTIVO",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }

    private void validateTipoProveedor(
            TipoProveedor tipoProveedor,
            TipoDocumentoProveedor tipoDocumento,
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
            validateEmpresa(ruc, razonSocial, errors);
            return;
        }

        if (tipoProveedor.isPersonaNatural()) {
            validatePersonaNatural(tipoDocumento, numeroDocumento, nombres, errors);
        }
    }

    private void validateEmpresa(
            String ruc,
            String razonSocial,
            ValidationErrorCollector errors
    ) {
        if (!StringNormalizer.hasText(ruc)) {
            errors.add("ruc", "El RUC es obligatorio para proveedores empresa.", "REQUIRED", ruc);
        } else if (StringNormalizer.onlyDigits(ruc).length() != 11) {
            errors.add("ruc", "El RUC debe tener 11 dígitos.", "INVALID_FORMAT", ruc);
        }

        if (!StringNormalizer.hasText(razonSocial)) {
            errors.add("razonSocial", "La razón social es obligatoria para proveedores empresa.", "REQUIRED", razonSocial);
        }
    }

    private void validatePersonaNatural(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento,
            String nombres,
            ValidationErrorCollector errors
    ) {
        if (tipoDocumento == null) {
            errors.add("tipoDocumento", "El tipo de documento es obligatorio para persona natural.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(numeroDocumento)) {
            errors.add("numeroDocumento", "El número de documento es obligatorio para persona natural.", "REQUIRED", numeroDocumento);
        }

        if (!StringNormalizer.hasText(nombres)) {
            errors.add("nombres", "Los nombres son obligatorios para persona natural.", "REQUIRED", nombres);
        }

        if (tipoDocumento == null || !StringNormalizer.hasText(numeroDocumento)) {
            return;
        }

        String normalizedDocument = tipoDocumento.isSoloNumeros()
                ? StringNormalizer.onlyDigits(numeroDocumento)
                : StringNormalizer.clean(numeroDocumento);

        if (normalizedDocument.length() < tipoDocumento.getMinLength()
                || normalizedDocument.length() > tipoDocumento.getMaxLength()) {
            errors.add(
                    "numeroDocumento",
                    "El número de documento no cumple la longitud permitida para " + tipoDocumento.getCode() + ".",
                    "INVALID_LENGTH",
                    numeroDocumento
            );
        }

        if (tipoDocumento.isSoloNumeros()
                && !normalizedDocument.equals(numeroDocumento.trim())) {
            errors.add(
                    "numeroDocumento",
                    "El número de documento solo debe contener dígitos.",
                    "INVALID_FORMAT",
                    numeroDocumento
            );
        }
    }

    private void validateDuplicados(
            boolean duplicatedDocumento,
            boolean duplicatedRuc,
            boolean update
    ) {
        if (duplicatedDocumento) {
            throw new ConflictException(
                    "PROVEEDOR_DOCUMENTO_DUPLICADO",
                    update
                            ? "Ya existe otro proveedor activo con el mismo documento."
                            : "Ya existe un proveedor activo con el mismo documento."
            );
        }

        if (duplicatedRuc) {
            throw new ConflictException(
                    "PROVEEDOR_RUC_DUPLICADO",
                    update
                            ? "Ya existe otro proveedor activo con el mismo RUC."
                            : "Ya existe un proveedor activo con el mismo RUC."
            );
        }
    }
}