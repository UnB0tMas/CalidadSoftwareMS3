// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/filter/ProductoAtributoValorFilterDto.java
package com.upsjb.ms3.dto.catalogo.producto.filter;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoAtributoValorFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 180, message = "El nombre del producto no debe superar 180 caracteres.")
        String nombreProducto,

        @Size(max = 240, message = "El slug del producto no debe superar 240 caracteres.")
        String slugProducto,

        Long idAtributo,

        @Size(max = 50, message = "El código de atributo no debe superar 50 caracteres.")
        String codigoAtributo,

        @Size(max = 120, message = "El nombre de atributo no debe superar 120 caracteres.")
        String nombreAtributo,

        TipoDatoAtributo tipoDato,

        @Size(max = 500, message = "El valor de texto no debe superar 500 caracteres.")
        String valorTexto,

        Boolean visiblePublico,

        Boolean filtrable,

        Boolean estado,

        Boolean incluirTodosLosEstados,

        @Valid
        DateRangeFilterDto fechaCreacion,

        @Valid
        DateRangeFilterDto fechaActualizacion
) {
}