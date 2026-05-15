// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryException.java
package com.upsjb.ms3.integration.cloudinary;

import com.upsjb.ms3.shared.exception.ExternalServiceException;

public class CloudinaryException extends ExternalServiceException {

    private final String operation;

    public CloudinaryException(String code, String message, String operation) {
        this(code, message, operation, null);
    }

    public CloudinaryException(String code, String message, String operation, Throwable cause) {
        super("Cloudinary", code, message, cause);
        this.operation = operation == null || operation.isBlank() ? "UNKNOWN" : operation.trim();
    }

    public String getOperation() {
        return operation;
    }
}