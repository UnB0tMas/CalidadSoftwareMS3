// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/response/TipoProductoAtributoResponseDto.java
package com.upsjb.ms3.dto.catalogo.atributo.response;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TipoProductoAtributoResponseDto(
        Long idTipoProductoAtributo,
        IdCodigoNombreResponseDto tipoProducto,
        IdCodigoNombreResponseDto atributo,
        TipoDatoAtributo tipoDato,
        String tipoDatoLabel,
        String unidadMedida,
        Boolean atributoRequeridoBase,
        Boolean filtrable,
        Boolean visiblePublico,
        Boolean requerido,
        Integer orden,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}