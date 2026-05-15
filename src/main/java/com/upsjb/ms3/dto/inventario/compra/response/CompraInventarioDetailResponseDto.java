// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/response/CompraInventarioDetailResponseDto.java
package com.upsjb.ms3.dto.inventario.compra.response;

import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CompraInventarioDetailResponseDto(
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
        List<CompraInventarioDetalleResponseDto> detalles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}