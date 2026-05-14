// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/filter/ProductoSkuFilterDto.java
package com.upsjb.ms3.dto.catalogo.producto.filter;

import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoSkuFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        Long idProducto,
        String codigoProducto,
        String codigoSku,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        EstadoSku estadoSku,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCreacion
) {
}