// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoSkuDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.StockResumenResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoSkuDetailResponseDto(
        Long idSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        String codigoSku,
        Boolean codigoGenerado,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal pesoGramos,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        EstadoSku estadoSku,
        Boolean estado,
        MoneyResponseDto precioVigente,
        MoneyResponseDto precioFinalPromocion,
        List<StockResumenResponseDto> stocks,
        List<SkuAtributoValorResponseDto> atributos,
        List<ProductoImagenResponseDto> imagenes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}