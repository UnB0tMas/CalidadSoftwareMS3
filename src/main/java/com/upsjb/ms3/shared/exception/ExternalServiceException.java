package com.upsjb.ms3.shared.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {

    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        this(serviceName, "EXTERNAL_SERVICE_ERROR", message, null);
    }

    public ExternalServiceException(String serviceName, String code, String message) {
        this(serviceName, code, message, null);
    }

    public ExternalServiceException(String serviceName, String code, String message, Throwable cause) {
        super(
                code,
                message,
                HttpStatus.SERVICE_UNAVAILABLE,
                cause,
                Map.of("externalService", serviceName == null ? "UNKNOWN" : serviceName)
        );
        this.serviceName = serviceName == null || serviceName.isBlank() ? "UNKNOWN" : serviceName.trim();
    }

    public String getServiceName() {
        return serviceName;
    }
}