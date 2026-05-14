// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/response/StockSkuResponseDto.java
package com.upsjb.ms3.dto.inventario.stock.response;

import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record StockSkuResponseDto(
        Long idStock,
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
        Integer stockMinimo,
        Integer stockMaximo,
        Boolean bajoStock,
        MoneyResponseDto costoPromedioActual,
        MoneyResponseDto ultimoCostoCompra,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}