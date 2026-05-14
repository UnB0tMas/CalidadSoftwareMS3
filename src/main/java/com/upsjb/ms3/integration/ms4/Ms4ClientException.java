// ruta: src/main/java/com/upsjb/ms3/integration/ms4/Ms4ClientException.java
package com.upsjb.ms3.integration.ms4;

import com.upsjb.ms3.shared.exception.ExternalServiceException;

public class Ms4ClientException extends ExternalServiceException {

    private final String operation;
    private final Integer httpStatus;

    public Ms4ClientException(String code, String message, String operation) {
        this(code, message, operation, null, null);
    }

    public Ms4ClientException(String code, String message, String operation, Throwable cause) {
        this(code, message, operation, null, cause);
    }

    public Ms4ClientException(
            String code,
            String message,
            String operation,
            Integer httpStatus,
            Throwable cause
    ) {
        super("MS4", code, message, cause);
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