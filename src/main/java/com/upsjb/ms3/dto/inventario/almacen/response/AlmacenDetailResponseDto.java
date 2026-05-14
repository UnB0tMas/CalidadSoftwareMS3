// ruta: src/main/java/com/upsjb/ms3/dto/inventario/almacen/response/AlmacenDetailResponseDto.java
package com.upsjb.ms3.dto.inventario.almacen.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AlmacenDetailResponseDto(
        Long idAlmacen,
        String codigo,
        String nombre,
        String direccion,
        Boolean principal,
        Boolean permiteVenta,
        Boolean permiteCompra,
        String observacion,
        Boolean estado,
        Long cantidadSkusConStock,
        Integer stockFisicoTotal,
        Integer stockReservadoTotal,
        Integer stockDisponibleTotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}