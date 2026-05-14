package com.upsjb.ms3.shared.validation;

import com.upsjb.ms3.shared.exception.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationErrorCollector {

    private final List<ValidationException.ValidationFieldError> errors = new ArrayList<>();

    private ValidationErrorCollector() {
    }

    public static ValidationErrorCollector create() {
        return new ValidationErrorCollector();
    }

    public ValidationErrorCollector add(String field, String message) {
        return add(field, message, "INVALID_VALUE", null);
    }

    public ValidationErrorCollector add(String field, String message, String code, Object rejectedValue) {
        errors.add(new ValidationException.ValidationFieldError(field, message, code, rejectedValue));
        return this;
    }

    public ValidationErrorCollector addIf(boolean condition, String field, String message) {
        if (condition) {
            add(field, message);
        }

        return this;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationException.ValidationFieldError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public void throwIfAny() {
        throwIfAny("La solicitud contiene datos inválidos.");
    }

    public void throwIfAny(String message) {
        if (hasErrors()) {
            throw new ValidationException(message, errors);
        }
    }
}