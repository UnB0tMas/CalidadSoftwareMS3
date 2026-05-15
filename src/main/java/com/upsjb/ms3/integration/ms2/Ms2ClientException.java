// ruta: src/main/java/com/upsjb/ms3/integration/ms2/Ms2ClientException.java
package com.upsjb.ms3.integration.ms2;

import com.upsjb.ms3.shared.exception.ExternalServiceException;

public class Ms2ClientException extends ExternalServiceException {

    private final String operation;
    private final Integer httpStatus;

    public Ms2ClientException(String code, String message, String operation) {
        this(code, message, operation, null, null);
    }

    public Ms2ClientException(String code, String message, String operation, Throwable cause) {
        this(code, message, operation, null, cause);
    }

    public Ms2ClientException(
            String code,
            String message,
            String operation,
            Integer httpStatus,
            Throwable cause
    ) {
        super("MS2", code, message, cause);
        this.operation = operation == null || operation.isBlank() ? "UNKNOWN" : operation.trim();
        this.httpStatus = httpStatus;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}