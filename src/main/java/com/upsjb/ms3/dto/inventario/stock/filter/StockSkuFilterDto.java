// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/filter/StockSkuFilterDto.java
package com.upsjb.ms3.dto.inventario.stock.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record StockSkuFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        Long idAlmacen,
        String codigoAlmacen,
        Boolean bajoStock,
        Boolean conStockDisponible,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCreacion
) {
}