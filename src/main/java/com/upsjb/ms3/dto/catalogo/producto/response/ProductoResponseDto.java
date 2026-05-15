// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoResponseDto(
        Long idProducto,
        IdCodigoNombreResponseDto tipoProducto,
        IdCodigoNombreResponseDto categoria,
        IdCodigoNombreResponseDto marca,
        String codigoProducto,
        Boolean codigoGenerado,
        String nombre,
        String slug,
        Boolean slugGenerado,
        String descripcionCorta,
        String descripcionLarga,
        GeneroObjetivo generoObjetivo,
        String temporada,
        String deporte,
        EstadoProductoRegistro estadoRegistro,
        EstadoProductoPublicacion estadoPublicacion,
        EstadoProductoVenta estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        LocalDateTime fechaPublicacionInicio,
        LocalDateTime fechaPublicacionFin,
        String motivoEstado,
        Long creadoPorIdUsuarioMs1,
        Long actualizadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}