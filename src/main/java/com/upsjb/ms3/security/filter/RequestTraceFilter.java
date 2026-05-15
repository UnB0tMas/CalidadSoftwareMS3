package com.upsjb.ms3.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String REQUEST_ID_ATTRIBUTE = "ms3.requestId";
    public static final String CORRELATION_ID_ATTRIBUTE = "ms3.correlationId";

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveOrGenerate(request.getHeader(REQUEST_ID_HEADER));
        String correlationId = resolveCorrelationId(request.getHeader(CORRELATION_ID_HEADER), requestId);

        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(CORRELATION_ID_ATTRIBUTE, correlationId);

        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_CORRELATION_ID);
        }
    }

    private String resolveOrGenerate(String value) {
        if (StringUtils.hasText(value)) {
            return sanitize(value);
        }

        return UUID.randomUUID().toString();
    }

    private String resolveCorrelationId(String correlationId, String requestId) {
        if (StringUtils.hasText(correlationId)) {
            return sanitize(correlationId);
        }

        return requestId;
    }

    private String sanitize(String value) {
        return value.trim()
                .replaceAll("[\\r\\n\\t]", "")
                .substring(0, Math.min(value.trim().length(), 100));
    }
}