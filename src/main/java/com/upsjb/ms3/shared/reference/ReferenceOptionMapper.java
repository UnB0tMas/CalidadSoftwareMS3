package com.upsjb.ms3.shared.reference;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ReferenceOptionMapper {

    public ReferenceOption toOption(Object id, String value, String label) {
        return toOption(id, value, label, null, true, Map.of());
    }

    public ReferenceOption toOption(
            Object id,
            String value,
            String label,
            String description,
            Boolean active,
            Map<String, Object> metadata
    ) {
        return new ReferenceOption(
                id == null ? null : String.valueOf(id),
                clean(value, 150),
                clean(label, 250),
                clean(description, 500),
                active == null || active,
                metadata == null ? Map.of() : metadata
        );
    }

    private String clean(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");

        return cleaned.substring(0, Math.min(cleaned.length(), maxLength));
    }

    public record ReferenceOption(
            String id,
            String value,
            String label,
            String description,
            boolean active,
            Map<String, Object> metadata
    ) {
        public ReferenceOption {
            metadata = metadata == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
        }
    }
}