package com.upsjb.ms3.shared.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, Object> context;

    public BusinessException(String code, String message, HttpStatus status) {
        this(code, message, status, null, null);
    }

    public BusinessException(String code, String message, HttpStatus status, Throwable cause) {
        this(code, message, status, cause, null);
    }

    public BusinessException(
            String code,
            String message,
            HttpStatus status,
            Throwable cause,
            Map<String, Object> context
    ) {
        super(message, cause);
        this.code = code == null || code.isBlank() ? "BUSINESS_ERROR" : code.trim();
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
        this.context = context == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(context));
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}