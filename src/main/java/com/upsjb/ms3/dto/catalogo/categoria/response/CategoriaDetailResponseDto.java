// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/categoria/response/CategoriaDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.categoria.response;

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
        Boolean estado,
        Long cantidadProductos,
        List<IdCodigoNombreResponseDto> subcategorias,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}