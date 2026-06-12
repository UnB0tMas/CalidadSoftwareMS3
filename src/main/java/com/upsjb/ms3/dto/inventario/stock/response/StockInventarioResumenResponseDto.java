// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/response/StockInventarioResumenResponseDto.java
package com.upsjb.ms3.dto.inventario.stock.response;

import lombok.Builder;

@Builder
public record StockInventarioResumenResponseDto(
        Long idAlmacen,
        Long totalRegistros,
        Long totalSku,
        Long agotados,
        Long bajoMinimo,
        Long conReservas,
        Long disponibles,
        Long stockFisico,
        Long stockReservado,
        Long stockDisponible
) {
}
