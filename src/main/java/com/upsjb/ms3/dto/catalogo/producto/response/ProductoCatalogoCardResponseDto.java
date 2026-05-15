// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoCatalogoCardResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import lombok.Builder;

@Builder
public record ProductoCatalogoCardResponseDto(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        String categoriaNombre,
        String marcaNombre,
        String imagenPrincipalUrl,
        MoneyResponseDto precio,
        MoneyResponseDto precioFinal,
        Boolean tienePromocion,
        Boolean vendible,
        Integer stockDisponibleTotal
) {
}