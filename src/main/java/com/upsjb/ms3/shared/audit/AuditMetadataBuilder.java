package com.upsjb.ms3.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

public final class AuditMetadataBuilder {

    private static final String MASK = "********";
    private static final int MAX_KEY_LENGTH = 80;
    private static final int MAX_VALUE_LENGTH = 1000;

    private final Map<String, Object> values = new LinkedHashMap<>();

    private AuditMetadataBuilder() {
    }

    public static AuditMetadataBuilder create() {
        return new AuditMetadataBuilder();
    }

    public AuditMetadataBuilder put(String key, Object value) {
        if (!StringUtils.hasText(key) || value == null) {
            return this;
        }

        values.put(normalizeKey(key), sanitizeValue(value));
        return this;
    }

    public AuditMetadataBuilder putIfPresent(String key, Object value) {
        return value == null ? this : put(key, value);
    }

    public AuditMetadataBuilder putMasked(String key) {
        if (StringUtils.hasText(key)) {
            values.put(normalizeKey(key), MASK);
        }

        return this;
    }

    public AuditMetadataBuilder entity(String entityName, Object id) {
        put("entity", entityName);
        put("entityId", id);
        return this;
    }

    public AuditMetadataBuilder action(String action) {
        put("action", action);
        return this;
    }

    public AuditMetadataBuilder beforeAfter(String field, Object before, Object after) {
        if (!StringUtils.hasText(field)) {
            return this;
        }

        Map<String, Object> change = new LinkedHashMap<>();
        change.put("before", sanitizeValue(before));
        change.put("after", sanitizeValue(after));

        values.put("change." + normalizeKey(field), change);
        return this;
    }

    public AuditMetadataBuilder request(AuditContext context) {
        if (context == null) {
            return this;
        }

        put("requestId", context.requestId());
        put("correlationId", context.correlationId());
        put("path", context.requestPath());
        put("method", context.httpMethod());
        put("ipAddress", context.ipAddress());
        put("actor", context.actorLabel());
        put("rolActor", context.rolActor().getCode());
        return this;
    }

    public Map<String, Object> build() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public String toJson(ObjectMapper objectMapper) {
        if (objectMapper == null || values.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            return "{\"metadataSerializationError\":\"No se pudo serializar metadata de auditoría.\"}";
        }
    }

    private String normalizeKey(String key) {
        String normalized = key.trim()
                .replaceAll("[^A-Za-z0-9_.-]", "_");

        return normalized.substring(0, Math.min(normalized.length(), MAX_KEY_LENGTH));
    }

    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof LocalDate
                || value instanceof LocalDateTime
                || value instanceof BigDecimal) {
            return value;
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitizedMap = new LinkedHashMap<>();
            map.forEach((mapKey, mapValue) -> {
                if (mapKey != null) {
                    sanitizedMap.put(normalizeKey(String.valueOf(mapKey)), sanitizeValue(mapValue));
                }
            });
            return sanitizedMap;
        }

        String sanitized = String.valueOf(value)
                .trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");

        return sanitized.substring(0, Math.min(sanitized.length(), MAX_VALUE_LENGTH));
    }
}