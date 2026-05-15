package com.upsjb.ms3.shared.validation;

import com.upsjb.ms3.shared.exception.ValidationException;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RequiredFieldValidator {

    public void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw ValidationException.field(fieldName, "El campo es obligatorio.");
        }
    }

    public void requireObject(Object value, String fieldName) {
        if (value == null) {
            throw ValidationException.field(fieldName, "El campo es obligatorio.");
        }
    }

    public void requireNotEmpty(Collection<?> values, String fieldName) {
        if (values == null || values.isEmpty()) {
            throw ValidationException.field(fieldName, "Debe enviar al menos un elemento.");
        }
    }

    public void requireNotEmpty(Map<?, ?> values, String fieldName) {
        if (values == null || values.isEmpty()) {
            throw ValidationException.field(fieldName, "Debe enviar al menos un valor.");
        }
    }

    public ValidationErrorCollector collectText(
            ValidationErrorCollector collector,
            String value,
            String fieldName
    ) {
        ValidationErrorCollector safeCollector = collector == null
                ? ValidationErrorCollector.create()
                : collector;

        if (!StringUtils.hasText(value)) {
            safeCollector.add(fieldName, "El campo es obligatorio.", "REQUIRED", value);
        }

        return safeCollector;
    }

    public ValidationErrorCollector collectObject(
            ValidationErrorCollector collector,
            Object value,
            String fieldName
    ) {
        ValidationErrorCollector safeCollector = collector == null
                ? ValidationErrorCollector.create()
                : collector;

        if (value == null) {
            safeCollector.add(fieldName, "El campo es obligatorio.", "REQUIRED", null);
        }

        return safeCollector;
    }
}