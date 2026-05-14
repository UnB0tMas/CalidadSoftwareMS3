package com.upsjb.ms3.shared.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String code, String message) {
        super(code, message, HttpStatus.FORBIDDEN);
    }

    public static ForbiddenException operation(String operation) {
        return new ForbiddenException(
                "OPERATION_FORBIDDEN",
                "No tiene permisos suficientes para ejecutar la operación: " + operation + "."
        );
    }
}