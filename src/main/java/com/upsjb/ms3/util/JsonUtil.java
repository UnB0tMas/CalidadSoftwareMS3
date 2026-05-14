// ruta: src/main/java/com/upsjb/ms3/util/JsonUtil.java
package com.upsjb.ms3.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtil() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "{}";
        }

        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el objeto a JSON.", ex);
        }
    }

    public static String toPrettyJson(Object value) {
        if (value == null) {
            return "{}";
        }

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el objeto a JSON.", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> targetType) {
        if (!StringNormalizer.hasText(json)) {
            return null;
        }

        try {
            return MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("El JSON no tiene el formato esperado.", ex);
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> elementType) {
        if (!StringNormalizer.hasText(json)) {
            return List.of();
        }

        try {
            JavaType javaType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementType);
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("El JSON no contiene una lista válida.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        if (!StringNormalizer.hasText(json)) {
            return Map.of();
        }

        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("El JSON no contiene un objeto válido.", ex);
        }
    }

    public static boolean isValidJson(String json) {
        if (!StringNormalizer.hasText(json)) {
            return false;
        }

        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException ex) {
            return false;
        }
    }
}