// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/response/CompraInventarioResponseDto.java
package com.upsjb.ms3.dto.inventario.compra.response;

import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CompraInventarioResponseDto(
        Long idCompra,
        String codigoCompra,
        Boolean codigoGenerado,
        Long idProveedor,
        String proveedorDisplay,
        LocalDateTime fechaCompra,
        Moneda moneda,
        MoneyResponseDto subtotal,
        MoneyResponseDto descuentoTotal,
        MoneyResponseDto impuestoTotal,
        MoneyResponseDto total,
        EstadoCompraInventario estadoCompra,
        String observacion,
        Long creadoPorIdUsuarioMs1,
        Long confirmadoPorIdUsuarioMs1,
        LocalDateTime confirmadoAt,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}