// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryUploadResponse.java
package com.upsjb.ms3.integration.cloudinary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record CloudinaryUploadResponse(
        String assetId,
        String publicId,
        Long version,
        String secureUrl,
        String url,
        String resourceType,
        String format,
        Long bytes,
        Integer width,
        Integer height,
        String folder,
        String originalFilename,
        Map<String, Object> rawResponse
) {

    public CloudinaryUploadResponse {
        rawResponse = rawResponse == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(rawResponse));
    }

    public static CloudinaryUploadResponse fromRaw(Map<?, ?> raw) {
        Map<String, Object> normalized = normalize(raw);

        return new CloudinaryUploadResponse(
                asString(normalized.get("asset_id")),
                asString(normalized.get("public_id")),
                asLong(normalized.get("version")),
                asString(normalized.get("secure_url")),
                asString(normalized.get("url")),
                asString(normalized.get("resource_type")),
                asString(normalized.get("format")),
                asLong(normalized.get("bytes")),
                asInteger(normalized.get("width")),
                asInteger(normalized.get("height")),
                asString(normalized.get("folder")),
                asString(normalized.get("original_filename")),
                normalized
        );
    }

    private static Map<String, Object> normalize(Map<?, ?> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        raw.forEach((key, value) -> {
            if (key != null) {
                normalized.put(String.valueOf(key), value);
            }
        });
        return normalized;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer asInteger(Object value) {
        Long parsed = asLong(value);
        return parsed == null ? null : parsed.intValue();
    }
}