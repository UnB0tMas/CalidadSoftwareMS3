// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/response/StockSkuDetailResponseDto.java
package com.upsjb.ms3.dto.inventario.stock.response;

import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record StockSkuDetailResponseDto(
        Long idStock,
        Long idSku,
        String codigoSku,
        String barcode,
        EstadoSku estadoSku,
        String color,
        String talla,
        String material,
        String modelo,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Boolean almacenPrincipal,
        Boolean almacenPermiteVenta,
        Boolean almacenPermiteCompra,
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