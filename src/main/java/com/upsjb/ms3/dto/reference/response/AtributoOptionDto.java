// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/AtributoOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import lombok.Builder;

@Builder
public record AtributoOptionDto(
        Long idAtributo,
        String codigo,
        String nombre,
        TipoDatoAtributo tipoDato,
        String tipoDatoLabel,
        String unidadMedida,
        Boolean requerido,
        Boolean filtrable,
        Boolean visiblePublico,
        Boolean estado
) {
}