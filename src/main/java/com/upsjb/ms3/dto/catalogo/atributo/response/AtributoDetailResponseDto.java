// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/response/AtributoDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.atributo.response;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record AtributoDetailResponseDto(
        Long idAtributo,
        String codigo,
        String nombre,
        TipoDatoAtributo tipoDato,
        String tipoDatoLabel,
        String unidadMedida,
        Boolean requerido,
        Boolean filtrable,
        Boolean visiblePublico,
        Boolean estado,
        Long cantidadValoresProducto,
        Long cantidadValoresSku,
        List<TipoProductoAtributoResponseDto> tiposProductoAsociados,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}