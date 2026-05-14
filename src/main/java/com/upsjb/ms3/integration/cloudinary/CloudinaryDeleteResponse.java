// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryDeleteResponse.java
package com.upsjb.ms3.integration.cloudinary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record CloudinaryDeleteResponse(
        String publicId,
        String result,
        boolean deleted,
        Map<String, Object> rawResponse
) {

    public CloudinaryDeleteResponse {
        rawResponse = rawResponse == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(rawResponse));
    }

    public static CloudinaryDeleteResponse fromRaw(String publicId, Map<?, ?> raw) {
        Map<String, Object> normalized = normalize(raw);
        String result = normalized.get("result") == null ? null : String.valueOf(normalized.get("result"));

        return new CloudinaryDeleteResponse(
                publicId,
                result,
                "ok".equalsIgnoreCase(result),
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
}