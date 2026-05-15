// ruta: src/main/java/com/upsjb/ms3/dto/shared/FieldErrorDto.java
package com.upsjb.ms3.dto.shared;

import lombok.Builder;

@Builder
public record FieldErrorDto(
        String field,
        String message,
        String code,
        Object rejectedValue
) {
}