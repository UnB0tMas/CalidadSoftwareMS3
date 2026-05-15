// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoAtributoValorRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ProductoAtributoValorRequestDto(

        @Valid
        @NotNull(message = "El atributo es obligatorio.")
        EntityReferenceDto atributo,

        @Size(max = 500, message = "El valor texto no debe superar 500 caracteres.")
        String valorTexto,

        BigDecimal valorNumero,

        Boolean valorBoolean,

        LocalDate valorFecha
) {
}