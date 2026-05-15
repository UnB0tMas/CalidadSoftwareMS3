// ruta: src/main/java/com/upsjb/ms3/dto/shared/DateRangeFilterDto.java
package com.upsjb.ms3.dto.shared;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record DateRangeFilterDto(
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
}