// ruta: src/main/java/com/upsjb/ms3/validator/AtributoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AtributoValidator {

    public void validateCreate(
            String codigo,
            String nombre,
            TipoDatoAtributo tipoDato,
            String unidadMedida,
            boolean duplicatedCodigo,
            boolean duplicatedNombre
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateTipoDato(tipoDato, errors);
        validateUnidadMedida(unidadMedida, errors);

        errors.throwIfAny("No se puede crear el atributo.");

        if (duplicatedCodigo) {
            throw new ConflictException(
                    "ATRIBUTO_CODIGO_DUPLICADO",
                    "Ya existe un atributo activo con el mismo código."
            );
        }

        if (duplicatedNombre) {
            throw new ConflictException(
                    "ATRIBUTO_NOMBRE_DUPLICADO",
                    "Ya existe un atributo activo con el mismo nombre."
            );
        }
    }

    public void validateUpdate(
            Atributo atributo,
            String codigo,
            String nombre,
            TipoDatoAtributo tipoDato,
            String unidadMedida,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean hasExistingValues
    ) {
        requireActive(atributo);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateTipoDato(tipoDato, errors);
        validateUnidadMedida(unidadMedida, errors);

        errors.throwIfAny("No se puede actualizar el atributo.");

        if (hasExistingValues
                && atributo.getTipoDato() != null
                && tipoDato != null
                && !atributo.getTipoDato().equals(tipoDato)) {
            throw new ConflictException(
                    "ATRIBUTO_TIPO_DATO_NO_EDITABLE",
                    "No se puede cambiar el tipo de dato porque el atributo ya tiene valores registrados."
            );
        }

        if (duplicatedCodigo) {
            throw new ConflictException(
                    "ATRIBUTO_CODIGO_DUPLICADO",
                    "Ya existe otro atributo activo con el mismo código."
            );
        }

        if (duplicatedNombre) {
            throw new ConflictException(
                    "ATRIBUTO_NOMBRE_DUPLICADO",
                    "Ya existe otro atributo activo con el mismo nombre."
            );
        }
    }

    public void validateValueByType(
            Atributo atributo,
            String valorTexto,
            BigDecimal valorNumero,
            Boolean valorBoolean,
            LocalDate valorFecha
    ) {
        requireActive(atributo);

        TipoDatoAtributo tipoDato = atributo.getTipoDato();
        if (tipoDato == null) {
            throw new ConflictException(
                    "ATRIBUTO_SIN_TIPO_DATO",
                    "El atributo no tiene tipo de dato configurado."
            );
        }

        boolean hasText = StringNormalizer.hasText(valorTexto);
        boolean hasNumber = valorNumero != null;
        boolean hasBoolean = valorBoolean != null;
        boolean hasDate = valorFecha != null;

        int filled = (hasText ? 1 : 0)
                + (hasNumber ? 1 : 0)
                + (hasBoolean ? 1 : 0)
                + (hasDate ? 1 : 0);

        if (filled == 0 && Boolean.TRUE.equals(atributo.getRequerido())) {
            throw new ConflictException(
                    "ATRIBUTO_VALOR_REQUERIDO",
                    "El atributo " + atributo.getNombre() + " requiere un valor."
            );
        }

        if (filled > 1) {
            throw new ConflictException(
                    "ATRIBUTO_VALOR_MULTIPLE",
                    "Solo se debe registrar un valor según el tipo de dato del atributo."
            );
        }

        if (tipoDato.isTexto() && filled > 0 && !hasText) {
            throwInvalidType("valorTexto");
        }

        if ((tipoDato.isNumero() || tipoDato.isDecimal()) && filled > 0 && !hasNumber) {
            throwInvalidType("valorNumero");
        }

        if (tipoDato.isBooleano() && filled > 0 && !hasBoolean) {
            throwInvalidType("valorBoolean");
        }

        if (tipoDato.isFecha() && filled > 0 && !hasDate) {
            throwInvalidType("valorFecha");
        }
    }

    public void validateCanDeactivate(
            Atributo atributo,
            boolean hasActiveAssociations,
            boolean hasExistingValues
    ) {
        requireActive(atributo);

        if (hasActiveAssociations || hasExistingValues) {
            throw new ConflictException(
                    "ATRIBUTO_EN_USO",
                    "No se puede inactivar el atributo porque está asociado o tiene valores registrados."
            );
        }
    }

    public void validateCanActivate(Atributo atributo) {
        requireExists(atributo);

        if (atributo.isActivo()) {
            throw new ConflictException(
                    "ATRIBUTO_YA_ACTIVO",
                    "El atributo ya se encuentra activo."
            );
        }
    }

    public void requireActive(Atributo atributo) {
        requireExists(atributo);

        if (!atributo.isActivo()) {
            throw new NotFoundException(
                    "ATRIBUTO_INACTIVO",
                    "El atributo no está activo."
            );
        }
    }

    public void requireExists(Atributo atributo) {
        if (atributo == null) {
            throw new NotFoundException(
                    "ATRIBUTO_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigo", "El código del atributo es obligatorio.", "REQUIRED", codigo);
            return;
        }

        String normalized = StringNormalizer.clean(codigo);
        if (normalized.length() > 50) {
            errors.add("codigo", "El código no debe superar 50 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre del atributo es obligatorio.", "REQUIRED", nombre);
            return;
        }

        String normalized = StringNormalizer.clean(nombre);
        if (normalized.length() > 120) {
            errors.add("nombre", "El nombre no debe superar 120 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void validateTipoDato(TipoDatoAtributo tipoDato, ValidationErrorCollector errors) {
        if (tipoDato == null) {
            errors.add("tipoDato", "El tipo de dato del atributo es obligatorio.", "REQUIRED", null);
        }
    }

    private void validateUnidadMedida(String unidadMedida, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(unidadMedida)) {
            return;
        }

        String normalized = StringNormalizer.clean(unidadMedida);
        if (normalized.length() > 30) {
            errors.add(
                    "unidadMedida",
                    "La unidad de medida no debe superar 30 caracteres.",
                    "MAX_LENGTH",
                    unidadMedida
            );
        }
    }

    private void throwInvalidType(String expectedField) {
        throw new ConflictException(
                "ATRIBUTO_TIPO_VALOR_INVALIDO",
                "El valor enviado no corresponde al tipo de dato esperado. Campo esperado: " + expectedField + "."
        );
    }
}