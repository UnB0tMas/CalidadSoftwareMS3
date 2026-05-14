// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/response/StockSkuDetailResponseDto.java
package com.upsjb.ms3.dto.inventario.stock.response;

import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record StockSkuDetailResponseDto(
        Long idStock,
        Long idSku,
        String codigoSku,
        String barcode,
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
        Boolean sobreStock,
        MoneyResponseDto costoPromedioActual,
        MoneyResponseDto ultimoCostoCompra,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}