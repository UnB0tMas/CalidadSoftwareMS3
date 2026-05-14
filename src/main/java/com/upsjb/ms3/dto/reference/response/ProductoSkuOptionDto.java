// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/ProductoSkuOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import com.upsjb.ms3.domain.enums.EstadoSku;
import lombok.Builder;

@Builder
public record ProductoSkuOptionDto(
        Long idSku,
        String codigoSku,
        String barcode,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        String color,
        String talla,
        String material,
        String modelo,
        EstadoSku estadoSku,
        Boolean estado
) {
}