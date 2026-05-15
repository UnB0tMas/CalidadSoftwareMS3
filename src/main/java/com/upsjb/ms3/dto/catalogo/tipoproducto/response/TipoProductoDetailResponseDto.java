// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/tipoproducto/response/TipoProductoDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.tipoproducto.response;

import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record TipoProductoDetailResponseDto(
        Long idTipoProducto,
        String codigo,
        String nombre,
        String descripcion,
        Boolean estado,
        Long cantidadAtributos,
        Long cantidadProductos,
        List<TipoProductoAtributoResponseDto> atributos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}