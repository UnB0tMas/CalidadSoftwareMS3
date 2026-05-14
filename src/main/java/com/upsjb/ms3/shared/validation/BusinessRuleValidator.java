package com.upsjb.ms3.shared.validation;

import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class BusinessRuleValidator {

    public void requireTrue(boolean condition, String code, String message) {
        if (!condition) {
            throw new ValidationException(code, message);
        }
    }

    public void requireFalse(boolean condition, String code, String message) {
        if (condition) {
            throw new ValidationException(code, message);
        }
    }

    public void requireState(boolean condition, String code, String message) {
        if (!condition) {
            throw new ConflictException(code, message);
        }
    }

    public void requireNotDuplicated(boolean duplicated, String resourceName, String field, Object value) {
        if (duplicated) {
            throw ConflictException.duplicate(resourceName, field, value);
        }
    }

    public void requirePositive(Number value, String field) {
        if (value == null || value.doubleValue() <= 0) {
            throw ValidationException.field(field, "El valor debe ser mayor a cero.");
        }
    }

    public void requireNonNegative(Number value, String field) {
        if (value == null || value.doubleValue() < 0) {
            throw ValidationException.field(field, "El valor no puede ser negativo.");
        }
    }
}