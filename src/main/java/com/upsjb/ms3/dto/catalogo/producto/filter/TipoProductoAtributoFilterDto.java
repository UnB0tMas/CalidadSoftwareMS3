// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/filter/TipoProductoAtributoFilterDto.java
package com.upsjb.ms3.dto.catalogo.atributo.filter;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoProductoAtributoFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Valid
        EntityReferenceDto tipoProducto,

        @Valid
        EntityReferenceDto atributo,

        Long idTipoProducto,

        Long idAtributo,

        @Size(max = 50, message = "El código del tipo de producto no debe superar 50 caracteres.")
        String codigoTipoProducto,

        @Size(max = 120, message = "El nombre del tipo de producto no debe superar 120 caracteres.")
        String nombreTipoProducto,

        @Size(max = 50, message = "El código del atributo no debe superar 50 caracteres.")
        String codigoAtributo,

        @Size(max = 120, message = "El nombre del atributo no debe superar 120 caracteres.")
        String nombreAtributo,

        TipoDatoAtributo tipoDato,

        Boolean requerido,

        Boolean atributoRequeridoBase,

        Boolean filtrable,

        Boolean visiblePublico,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}