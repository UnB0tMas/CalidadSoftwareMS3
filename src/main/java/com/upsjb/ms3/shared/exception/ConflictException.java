package com.upsjb.ms3.shared.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public ConflictException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }

    public static ConflictException duplicate(String resourceName, String field, Object value) {
        return new ConflictException(
                "DUPLICATE_RESOURCE",
                "Ya existe " + resourceName + " con " + field + ": " + value + "."
        );
    }

    public static ConflictException invalidState(String message) {
        return new ConflictException("INVALID_STATE", message);
    }
}