package com.upsjb.ms3.shared.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }

    public static NotFoundException resource(String resourceName, Object reference) {
        return new NotFoundException(
                "RESOURCE_NOT_FOUND",
                resourceName + " no encontrado para la referencia: " + reference + "."
        );
    }

    public static NotFoundException activeResource(String resourceName, Object reference) {
        return new NotFoundException(
                "ACTIVE_RESOURCE_NOT_FOUND",
                resourceName + " activo no encontrado para la referencia: " + reference + "."
        );
    }
}