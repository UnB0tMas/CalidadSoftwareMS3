// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/EntityReferenceResolvedDto.java
package com.upsjb.ms3.dto.reference.response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;

@Builder
public record EntityReferenceResolvedDto(
        String entidad,
        String id,
        String value,
        String label,
        String description,
        Boolean active,
        Map<String, Object> metadata
) {
    public EntityReferenceResolvedDto {
        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}