package com.upsjb.ms3.shared.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {

    private final List<ValidationFieldError> fieldErrors;

    public ValidationException(String message) {
        this("VALIDATION_ERROR", message, List.of());
    }

    public ValidationException(String code, String message) {
        this(code, message, List.of());
    }

    public ValidationException(String message, List<ValidationFieldError> fieldErrors) {
        this("VALIDATION_ERROR", message, fieldErrors);
    }

    public ValidationException(String code, String message, List<ValidationFieldError> fieldErrors) {
        super(code, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(fieldErrors));
    }

    public static ValidationException field(String field, String message) {
        return new ValidationException(
                "VALIDATION_ERROR",
                "La solicitud contiene datos inválidos.",
                List.of(new ValidationFieldError(field, message, "INVALID_VALUE", null))
        );
    }

    public List<ValidationFieldError> getFieldErrors() {
        return fieldErrors;
    }

    public record ValidationFieldError(
            String field,
            String message,
            String code,
            Object rejectedValue
    ) {
    }
}