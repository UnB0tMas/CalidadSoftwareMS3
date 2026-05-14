// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/TipoProductoOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record TipoProductoOptionDto(
        Long idTipoProducto,
        String codigo,
        String nombre,
        String descripcion,
        Boolean estado
) {
}