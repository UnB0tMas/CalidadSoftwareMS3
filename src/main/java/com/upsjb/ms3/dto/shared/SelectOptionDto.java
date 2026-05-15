// ruta: src/main/java/com/upsjb/ms3/dto/shared/SelectOptionDto.java
package com.upsjb.ms3.dto.shared;

import lombok.Builder;

@Builder
public record SelectOptionDto(
        String value,
        String label,
        String code,
        Boolean disabled
) {
}