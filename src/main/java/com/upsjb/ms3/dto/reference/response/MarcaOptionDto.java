// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/MarcaOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record MarcaOptionDto(
        Long idMarca,
        String codigo,
        String nombre,
        String slug,
        Boolean estado
) {
}