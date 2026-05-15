// ruta: src/main/java/com/upsjb/ms3/validator/ProveedorValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ProveedorValidator {

    private static final int MAX_DOCUMENTO_LENGTH = 30;
    private static final int MAX_RUC_LENGTH = 20;
    private static final int MAX_RAZON_SOCIAL_LENGTH = 200;
    private static final int MAX_NOMBRE_COMERCIAL_LENGTH = 200;
    private static final int MAX_NOMBRES_LENGTH = 150;
    private static final int MAX_APELLIDOS_LENGTH = 150;
    private static final int MAX_CORREO_LENGTH = 180;
    private static final int MAX_TELEFONO_LENGTH = 30;
    private static final int MAX_DIRECCION_LENGTH = 300;
    private static final int MAX_OBSERVACION_LENGTH = 500;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public void validateCreate(
            TipoProveedor tipoProveedor,
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento,
            String ruc,
            String razonSocial,
            String nombreComercial,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String direccion,
            String observacion,
            Long creadoPorIdUsuarioMs1,
            boolean duplicatedDocumento,
            boolean duplicatedRuc
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateTipoProveedor(tipoProveedor, tipoDocumento, numeroDocumento, ruc, razonSocial, nombres, errors);
        validateCommonLengths(
                numeroDocumento,
                ruc,
                razonSocial,
                nombreComercial,
                nombres,
                apellidos,
                correo,
                telefono,
                direccion,
                observacion,
                errors
        );

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
            String nombreComercial,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String direccion,
            String observacion,
            Long actualizadoPorIdUsuarioMs1,
            boolean duplicatedDocumento,
            boolean duplicatedRuc
    ) {
        requireActive(proveedor);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateTipoProveedor(tipoProveedor, tipoDocumento, numeroDocumento, ruc, razonSocial, nombres, errors);
        validateCommonLengths(
                numeroDocumento,
                ruc,
                razonSocial,
                nombreComercial,
                nombres,
                apellidos,
                correo,
                telefono,
                direccion,
                observacion,
                errors
        );

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
    }

    private void validateCommonLengths(
            String numeroDocumento,
            String ruc,
            String razonSocial,
            String nombreComercial,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String direccion,
            String observacion,
            ValidationErrorCollector errors
    ) {
        validateMax("numeroDocumento", numeroDocumento, MAX_DOCUMENTO_LENGTH, "El número de documento no debe superar 30 caracteres.", errors);
        validateMax("ruc", ruc, MAX_RUC_LENGTH, "El RUC no debe superar 20 caracteres.", errors);
        validateMax("razonSocial", razonSocial, MAX_RAZON_SOCIAL_LENGTH, "La razón social no debe superar 200 caracteres.", errors);
        validateMax("nombreComercial", nombreComercial, MAX_NOMBRE_COMERCIAL_LENGTH, "El nombre comercial no debe superar 200 caracteres.", errors);
        validateMax("nombres", nombres, MAX_NOMBRES_LENGTH, "Los nombres no deben superar 150 caracteres.", errors);
        validateMax("apellidos", apellidos, MAX_APELLIDOS_LENGTH, "Los apellidos no deben superar 150 caracteres.", errors);
        validateMax("correo", correo, MAX_CORREO_LENGTH, "El correo no debe superar 180 caracteres.", errors);
        validateMax("telefono", telefono, MAX_TELEFONO_LENGTH, "El teléfono no debe superar 30 caracteres.", errors);
        validateMax("direccion", direccion, MAX_DIRECCION_LENGTH, "La dirección no debe superar 300 caracteres.", errors);
        validateMax("observacion", observacion, MAX_OBSERVACION_LENGTH, "La observación no debe superar 500 caracteres.", errors);

        if (StringNormalizer.hasText(correo) && !EMAIL_PATTERN.matcher(correo).matches()) {
            errors.add("correo", "El correo no tiene formato válido.", "INVALID_FORMAT", correo);
        }
    }

    private void validateMax(
            String field,
            String value,
            int maxLength,
            String message,
            ValidationErrorCollector errors
    ) {
        if (StringNormalizer.hasText(value) && StringNormalizer.clean(value).length() > maxLength) {
            errors.add(field, message, "MAX_LENGTH", value);
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