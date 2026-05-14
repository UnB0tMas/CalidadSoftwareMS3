package com.upsjb.ms3.shared.response;

import com.upsjb.ms3.shared.audit.AuditContextHolder;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseFactory {

    public <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return status(HttpStatus.OK, message, data);
    }

    public <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return status(HttpStatus.CREATED, message, data);
    }

    public <T> ResponseEntity<ApiResponse<T>> accepted(String message, T data) {
        return status(HttpStatus.ACCEPTED, message, data);
    }

    public ResponseEntity<ApiResponse<Void>> noContentMessage(String message) {
        return ResponseEntity.ok(success(message, null));
    }

    public <T> ResponseEntity<ApiResponse<T>> status(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(success(message, data));
    }

    public <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                OffsetDateTime.now().toString(),
                message,
                data,
                trace()
        );
    }

    private Map<String, Object> trace() {
        Map<String, Object> trace = new LinkedHashMap<>();
        AuditContextHolder.getOptional().ifPresent(context -> {
            trace.put("requestId", context.requestId());
            trace.put("correlationId", context.correlationId());
        });
        return trace;
    }

    public record ApiResponse<T>(
            boolean success,
            String timestamp,
            String message,
            T data,
            Map<String, Object> trace
    ) {
    }
}