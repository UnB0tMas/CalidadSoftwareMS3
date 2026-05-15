// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoSkuCreateRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoSkuCreateRequestDto(

        @Valid
        @NotNull(message = "El producto es obligatorio.")
        EntityReferenceDto producto,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        @Size(max = 80, message = "El color no debe superar 80 caracteres.")
        String color,

        @Size(max = 50, message = "La talla no debe superar 50 caracteres.")
        String talla,

        @Size(max = 120, message = "El material no debe superar 120 caracteres.")
        String material,

        @Size(max = 120, message = "El modelo no debe superar 120 caracteres.")
        String modelo,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
        Integer stockMinimo,

        @Min(value = 0, message = "El stock máximo no puede ser negativo.")
        Integer stockMaximo,

        @DecimalMin(value = "0.000", inclusive = false, message = "El peso debe ser mayor que cero.")
        BigDecimal pesoGramos,

        @DecimalMin(value = "0.000", inclusive = false, message = "El alto debe ser mayor que cero.")
        BigDecimal altoCm,

        @DecimalMin(value = "0.000", inclusive = false, message = "El ancho debe ser mayor que cero.")
        BigDecimal anchoCm,

        @DecimalMin(value = "0.000", inclusive = false, message = "El largo debe ser mayor que cero.")
        BigDecimal largoCm,

        @Valid
        List<SkuAtributoValorRequestDto> atributos
) {
}