// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/request/AtributoCreateRequestDto.java
package com.upsjb.ms3.dto.catalogo.atributo.request;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AtributoCreateRequestDto(

        @NotBlank(message = "El código del atributo es obligatorio.")
        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre del atributo es obligatorio.")
        @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
        String nombre,

        @NotNull(message = "El tipo de dato del atributo es obligatorio.")
        TipoDatoAtributo tipoDato,

        @Size(max = 30, message = "La unidad de medida no debe superar 30 caracteres.")
        String unidadMedida,

        Boolean requerido,
        Boolean filtrable,
        Boolean visiblePublico
) {
}