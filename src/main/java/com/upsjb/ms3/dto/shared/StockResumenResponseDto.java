// ruta: src/main/java/com/upsjb/ms3/dto/shared/StockResumenResponseDto.java
package com.upsjb.ms3.dto.shared;

import lombok.Builder;

@Builder
public record StockResumenResponseDto(
        Long idSku,
        String codigoSku,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        Integer stockMinimo,
        Integer stockMaximo,
        Boolean bajoStock
) {
}