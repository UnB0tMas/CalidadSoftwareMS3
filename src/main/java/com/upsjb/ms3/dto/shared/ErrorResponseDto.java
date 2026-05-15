// ruta: src/main/java/com/upsjb/ms3/dto/shared/ErrorResponseDto.java
package com.upsjb.ms3.dto.shared;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ErrorResponseDto(
        String code,
        String message,
        String path,
        LocalDateTime timestamp,
        String requestId,
        String correlationId,
        List<FieldErrorDto> fieldErrors
) {
}