// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/response/StockDisponibleResponseDto.java
package com.upsjb.ms3.dto.inventario.stock.response;

import lombok.Builder;

@Builder
public record StockDisponibleResponseDto(
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        Boolean disponible,
        Integer cantidadSolicitada,
        Boolean cantidadDisponible
) {
}