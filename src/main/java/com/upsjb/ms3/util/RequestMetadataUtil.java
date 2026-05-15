// ruta: src/main/java/com/upsjb/ms3/util/RequestMetadataUtil.java
package com.upsjb.ms3.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class RequestMetadataUtil {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_REAL_IP = "X-Real-IP";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_INTERNAL_SERVICE_KEY = "X-Internal-Service-Key";

    public static final String ATTR_REQUEST_ID = "MS3_REQUEST_ID";
    public static final String ATTR_CORRELATION_ID = "MS3_CORRELATION_ID";

    private static final int MAX_HEADER_LENGTH = 500;

    private RequestMetadataUtil() {
    }

    public static String resolveRequestId(HttpServletRequest request) {
        String fromAttribute = attributeAsString(request, ATTR_REQUEST_ID);
        if (StringNormalizer.hasText(fromAttribute)) {
            return fromAttribute;
        }

        String fromHeader = sanitizeHeader(header(request, HEADER_REQUEST_ID));
        return StringNormalizer.hasText(fromHeader) ? fromHeader : generateRequestId();
    }

    public static String resolveCorrelationId(HttpServletRequest request, String requestId) {
        String fromAttribute = attributeAsString(request, ATTR_CORRELATION_ID);
        if (StringNormalizer.hasText(fromAttribute)) {
            return fromAttribute;
        }

        String fromHeader = sanitizeHeader(header(request, HEADER_CORRELATION_ID));
        if (StringNormalizer.hasText(fromHeader)) {
            return fromHeader;
        }

        return StringNormalizer.hasText(requestId) ? requestId : generateRequestId();
    }

    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String forwardedFor = header(request, HEADER_FORWARDED_FOR);
        if (StringNormalizer.hasText(forwardedFor)) {
            return sanitizeHeader(forwardedFor.split(",")[0]);
        }

        String realIp = header(request, HEADER_REAL_IP);
        if (StringNormalizer.hasText(realIp)) {
            return sanitizeHeader(realIp);
        }

        return sanitizeHeader(request.getRemoteAddr());
    }

    public static String userAgent(HttpServletRequest request) {
        return sanitizeHeader(header(request, HEADER_USER_AGENT));
    }

    public static String method(HttpServletRequest request) {
        return request == null ? "" : StringNormalizer.upper(request.getMethod());
    }

    public static String path(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String uri = request.getRequestURI();
        return uri == null ? "" : uri;
    }

    public static String fullPath(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String uri = request.getRequestURI();
        String query = request.getQueryString();

        if (!StringNormalizer.hasText(query)) {
            return uri == null ? "" : uri;
        }

        return (uri == null ? "" : uri) + "?" + query;
    }

    public static String header(HttpServletRequest request, String headerName) {
        if (request == null || !StringNormalizer.hasText(headerName)) {
            return "";
        }

        return request.getHeader(headerName);
    }

    public static boolean hasInternalServiceKey(HttpServletRequest request) {
        return StringNormalizer.hasText(header(request, HEADER_INTERNAL_SERVICE_KEY));
    }

    public static String sanitizeHeader(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value
                .replace("\r", "")
                .replace("\n", "")
                .trim();

        return StringNormalizer.truncate(cleaned, MAX_HEADER_LENGTH);
    }

    public static void attachTraceAttributes(HttpServletRequest request, String requestId, String correlationId) {
        if (request == null) {
            return;
        }

        request.setAttribute(ATTR_REQUEST_ID, requestId);
        request.setAttribute(ATTR_CORRELATION_ID, correlationId);
    }

    public static String maskAuthorization(String authorizationHeader) {
        if (!StringNormalizer.hasText(authorizationHeader)) {
            return "";
        }

        String cleaned = sanitizeHeader(authorizationHeader);

        if (cleaned.length() <= 15) {
            return "***";
        }

        return cleaned.substring(0, 10) + "...***";
    }

    private static String attributeAsString(HttpServletRequest request, String attributeName) {
        if (request == null) {
            return "";
        }

        Object value = request.getAttribute(attributeName);
        return value == null ? "" : String.valueOf(value);
    }
}