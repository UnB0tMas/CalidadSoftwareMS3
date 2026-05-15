package com.upsjb.ms3.shared.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static UnauthorizedException missingAuthentication() {
        return new UnauthorizedException(
                "AUTHENTICATION_REQUIRED",
                "Debe autenticarse para ejecutar esta operación."
        );
    }
}