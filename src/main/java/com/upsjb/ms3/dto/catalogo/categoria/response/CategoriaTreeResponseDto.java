package com.upsjb.ms3.dto.catalogo.categoria.response;

import java.util.List;
import lombok.Builder;

@Builder
public record CategoriaTreeResponseDto(

        Long idCategoria,

        String codigo,

        String nombre,

        String slug,

        Integer nivel,

        Integer orden,

        Boolean permiteProductos,

        Boolean estado,

        List<CategoriaTreeResponseDto> hijos

) {
}