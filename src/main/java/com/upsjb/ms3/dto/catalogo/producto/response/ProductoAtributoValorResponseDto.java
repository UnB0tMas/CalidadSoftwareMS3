// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoAtributoValorResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoAtributoValorResponseDto(
        Long idProductoAtributoValor,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        IdCodigoNombreResponseDto atributo,
        TipoDatoAtributo tipoDato,
        String tipoDatoLabel,
        String unidadMedida,
        String valorTexto,
        BigDecimal valorNumero,
        Boolean valorBoolean,
        LocalDate valorFecha,
        String valorDisplay,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}