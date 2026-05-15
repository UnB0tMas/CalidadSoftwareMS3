// ruta: src/main/java/com/upsjb/ms3/dto/shared/MoneyResponseDto.java
package com.upsjb.ms3.dto.shared;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record MoneyResponseDto(
        BigDecimal amount,
        String currency,
        String formatted
) {
}