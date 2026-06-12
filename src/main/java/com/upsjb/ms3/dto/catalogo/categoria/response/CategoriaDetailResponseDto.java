package com.upsjb.ms3.dto.catalogo.categoria.response;

import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoriaDetailResponseDto(
        Long idCategoria,
        IdCodigoNombreResponseDto categoriaPadre,
        String codigo,
        String nombre,
        String slug,
        Boolean slugGenerado,
        String descripcion,
        Integer nivel,
        Integer orden,
        Boolean permiteProductos,
        Boolean estado,
        Long cantidadProductos,
        List<IdCodigoNombreResponseDto> subcategorias,
        List<CategoriaAtributoResponseDto> atributos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}