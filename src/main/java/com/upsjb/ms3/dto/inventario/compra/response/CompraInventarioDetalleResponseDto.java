// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/response/CompraInventarioDetalleResponseDto.java
package com.upsjb.ms3.dto.inventario.compra.response;

import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CompraInventarioDetalleResponseDto(
        Long idCompraDetalle,
        Long idCompra,
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer cantidad,
        MoneyResponseDto costoUnitario,
        MoneyResponseDto descuento,
        MoneyResponseDto impuesto,
        MoneyResponseDto costoTotal,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}