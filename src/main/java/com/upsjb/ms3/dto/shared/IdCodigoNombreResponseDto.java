// ruta: src/main/java/com/upsjb/ms3/dto/shared/IdCodigoNombreResponseDto.java
package com.upsjb.ms3.dto.shared;

import lombok.Builder;

@Builder
public record IdCodigoNombreResponseDto(
        Long id,
        String codigo,
        String nombre,
        Boolean estado
) {
}