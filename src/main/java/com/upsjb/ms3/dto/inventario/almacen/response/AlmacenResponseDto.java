// ruta: src/main/java/com/upsjb/ms3/dto/inventario/almacen/response/AlmacenResponseDto.java
package com.upsjb.ms3.dto.inventario.almacen.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AlmacenResponseDto(
        Long idAlmacen,
        String codigo,
        String nombre,
        String direccion,
        Boolean principal,
        Boolean permiteVenta,
        Boolean permiteCompra,
        String observacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}