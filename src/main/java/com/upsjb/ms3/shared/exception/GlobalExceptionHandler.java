package com.upsjb.ms3.shared.exception;

import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.constants.HeaderNames;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                request,
                toFieldErrors(ex.getFieldErrors())
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        logBusiness(ex, request);
        return buildResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<Map<String, Object>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "La solicitud contiene datos inválidos.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(
            BindException ex,
            HttpServletRequest request
    ) {
        List<Map<String, Object>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BINDING_ERROR",
                "No se pudieron procesar los parámetros enviados.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<Map<String, Object>> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("field", String.valueOf(violation.getPropertyPath()));
                    error.put("message", violation.getMessage());
                    error.put("code", "CONSTRAINT_VIOLATION");
                    error.put("rejectedValue", safeRejectedValue(violation.getInvalidValue()));
                    return error;
                })
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "La solicitud contiene valores inválidos.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "La solicitud no tiene un formato válido.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationCredentialsNotFound(
            AuthenticationCredentialsNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Debe autenticarse para ejecutar esta operación.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "No tiene permisos suficientes para ejecutar esta operación.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "El método HTTP no está permitido para este recurso.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error(
                "Error de integridad BD. path={}, requestId={}, correlationId={}",
                request.getRequestURI(),
                resolveRequestId(request),
                resolveCorrelationId(request),
                ex
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_CONFLICT",
                "No se pudo completar la operación porque existe un conflicto de integridad de datos.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Error inesperado. path={}, requestId={}, correlationId={}",
                request.getRequestURI(),
                resolveRequestId(request),
                resolveCorrelationId(request),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error interno del sistema. Intente nuevamente o contacte al administrador.",
                request,
                List.of()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<Map<String, Object>> fieldErrors
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", code);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("requestId", resolveRequestId(request));
        body.put("correlationId", resolveCorrelationId(request));
        body.put("fieldErrors", fieldErrors == null ? List.of() : fieldErrors);

        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> toFieldError(FieldError fieldError) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("field", fieldError.getField());
        error.put("message", fieldError.getDefaultMessage());
        error.put("code", fieldError.getCode());
        error.put("rejectedValue", safeRejectedValue(fieldError.getRejectedValue()));
        return error;
    }

    private List<Map<String, Object>> toFieldErrors(
            List<ValidationException.ValidationFieldError> errors
    ) {
        if (errors == null || errors.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (ValidationException.ValidationFieldError error : errors) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("field", error.field());
            item.put("message", error.message());
            item.put("code", error.code());
            item.put("rejectedValue", safeRejectedValue(error.rejectedValue()));
            result.add(item);
        }

        return result;
    }

    private Object safeRejectedValue(Object value) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value);

        if (text.length() > 150) {
            return text.substring(0, 150) + "...";
        }

        return text;
    }

    private void logBusiness(BusinessException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error(
                    "Excepción funcional crítica. code={}, path={}, requestId={}, correlationId={}, context={}",
                    ex.getCode(),
                    request.getRequestURI(),
                    resolveRequestId(request),
                    resolveCorrelationId(request),
                    ex.getContext(),
                    ex
            );
            return;
        }

        log.warn(
                "Excepción funcional controlada. code={}, path={}, requestId={}, correlationId={}, message={}",
                ex.getCode(),
                request.getRequestURI(),
                resolveRequestId(request),
                resolveCorrelationId(request),
                ex.getMessage()
        );
    }

    private String resolveRequestId(HttpServletRequest request) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        if (StringUtils.hasText(context.requestId())) {
            return context.requestId();
        }

        return request.getHeader(HeaderNames.REQUEST_ID);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        if (StringUtils.hasText(context.correlationId())) {
            return context.correlationId();
        }

        return request.getHeader(HeaderNames.CORRELATION_ID);
    }
}