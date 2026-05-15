// ruta: src/main/java/com/upsjb/ms3/dto/inventario/stock/filter/StockSkuFilterDto.java
package com.upsjb.ms3.dto.inventario.stock.filter;

import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record StockSkuFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idSku,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        EstadoSku estadoSku,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 180, message = "El nombre del producto no debe superar 180 caracteres.")
        String nombreProducto,

        Long idAlmacen,

        @Size(max = 50, message = "El código de almacén no debe superar 50 caracteres.")
        String codigoAlmacen,

        @Size(max = 150, message = "El nombre del almacén no debe superar 150 caracteres.")
        String nombreAlmacen,

        Boolean bajoStock,

        Boolean sobreStock,

        Boolean conStockDisponible,

        @Min(value = 0, message = "El stock físico mínimo no puede ser negativo.")
        Integer stockFisicoMin,

        @Min(value = 0, message = "El stock físico máximo no puede ser negativo.")
        Integer stockFisicoMax,

        @Min(value = 0, message = "El stock reservado mínimo no puede ser negativo.")
        Integer stockReservadoMin,

        @Min(value = 0, message = "El stock reservado máximo no puede ser negativo.")
        Integer stockReservadoMax,

        @Min(value = 0, message = "El stock disponible mínimo no puede ser negativo.")
        Integer stockDisponibleMin,

        @Min(value = 0, message = "El stock disponible máximo no puede ser negativo.")
        Integer stockDisponibleMax,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
        Integer stockMinimoMin,

        @Min(value = 0, message = "El stock mínimo máximo no puede ser negativo.")
        Integer stockMinimoMax,

        @Min(value = 0, message = "El stock máximo mínimo no puede ser negativo.")
        Integer stockMaximoMin,

        @Min(value = 0, message = "El stock máximo no puede ser negativo.")
        Integer stockMaximoMax,

        Boolean estado,

        Boolean incluirTodosLosEstados,

        @Valid
        DateRangeFilterDto fechaCreacion,

        @Valid
        DateRangeFilterDto fechaActualizacion
) {
}