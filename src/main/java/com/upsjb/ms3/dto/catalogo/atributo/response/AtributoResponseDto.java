// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/response/AtributoResponseDto.java
package com.upsjb.ms3.dto.catalogo.atributo.response;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AtributoResponseDto(
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}