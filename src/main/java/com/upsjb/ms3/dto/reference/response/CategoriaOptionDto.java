// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/CategoriaOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record CategoriaOptionDto(
        Long idCategoria,
        Long idCategoriaPadre,
        String codigo,
        String nombre,
        String slug,
        Integer nivel,
        Integer orden,
        Boolean estado
) {
}