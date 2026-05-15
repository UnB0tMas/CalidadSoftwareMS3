package com.upsjb.ms3.shared.response;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    TOKEN_AUSENTE_O_INVALIDO("TOKEN_AUSENTE_O_INVALIDO", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN),
    ACCESO_DENEGADO("ACCESO_DENEGADO", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE),
    CLOUDINARY_INTEGRATION_ERROR("CLOUDINARY_INTEGRATION_ERROR", HttpStatus.SERVICE_UNAVAILABLE),
    KAFKA_PUBLISH_ERROR("KAFKA_PUBLISH_ERROR", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}