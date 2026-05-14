package com.upsjb.ms3.shared.response;

import com.upsjb.ms3.shared.audit.AuditContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseFactory {

    public ResponseEntity<ErrorResponse> from(
            ApiErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        return from(errorCode, message, request, List.of());
    }

    public ResponseEntity<ErrorResponse> from(
            ApiErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<Map<String, Object>> fieldErrors
    ) {
        ApiErrorCode resolvedCode = errorCode == null
                ? ApiErrorCode.INTERNAL_SERVER_ERROR
                : errorCode;

        ErrorResponse body = new ErrorResponse(
                false,
                OffsetDateTime.now().toString(),
                resolvedCode.httpStatus().value(),
                resolvedCode.httpStatus().getReasonPhrase(),
                resolvedCode.code(),
                message,
                request == null ? null : request.getRequestURI(),
                resolveRequestId(),
                resolveCorrelationId(),
                fieldErrors == null ? List.of() : fieldErrors
        );

        return ResponseEntity.status(resolvedCode.httpStatus()).body(body);
    }

    private String resolveRequestId() {
        return AuditContextHolder.getOptional()
                .map(context -> context.requestId())
                .orElse(null);
    }

    private String resolveCorrelationId() {
        return AuditContextHolder.getOptional()
                .map(context -> context.correlationId())
                .orElse(null);
    }

    public record ErrorResponse(
            boolean success,
            String timestamp,
            int status,
            String error,
            String code,
            String message,
            String path,
            String requestId,
            String correlationId,
            List<Map<String, Object>> fieldErrors
    ) {
    }
}