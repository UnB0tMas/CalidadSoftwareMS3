package com.upsjb.ms3.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.security.filter.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final ObjectMapper objectMapper;

    public void writeUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws IOException {
        writeError(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "TOKEN_AUSENTE_O_INVALIDO",
                message == null || message.isBlank()
                        ? "Token ausente, inválido o expirado."
                        : message
        );
    }

    public void writeForbidden(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws IOException {
        writeError(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN",
                "ACCESO_DENEGADO",
                message == null || message.isBlank()
                        ? "No tiene permisos suficientes para ejecutar esta operación."
                        : message
        );
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String code,
            String message
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("code", code);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("requestId", resolveTraceValue(request, RequestTraceFilter.REQUEST_ID_ATTRIBUTE, RequestTraceFilter.REQUEST_ID_HEADER));
        body.put("correlationId", resolveTraceValue(request, RequestTraceFilter.CORRELATION_ID_ATTRIBUTE, RequestTraceFilter.CORRELATION_ID_HEADER));

        objectMapper.writeValue(response.getWriter(), body);
    }

    private String resolveTraceValue(HttpServletRequest request, String attributeName, String headerName) {
        Object attributeValue = request.getAttribute(attributeName);

        if (attributeValue != null) {
            return String.valueOf(attributeValue);
        }

        return request.getHeader(headerName);
    }
}