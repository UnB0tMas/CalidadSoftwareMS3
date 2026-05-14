// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/ProductoOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import lombok.Builder;

@Builder
public record ProductoOptionDto(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        EstadoProductoRegistro estadoRegistro,
        EstadoProductoPublicacion estadoPublicacion,
        EstadoProductoVenta estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        Boolean estado
) {
}