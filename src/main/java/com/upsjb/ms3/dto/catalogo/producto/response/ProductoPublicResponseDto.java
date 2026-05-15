// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoPublicResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoPublicResponseDto(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        String descripcionCorta,
        String categoriaNombre,
        String categoriaSlug,
        String marcaNombre,
        String imagenPrincipalUrl,
        MoneyResponseDto precioDesde,
        MoneyResponseDto precioFinalDesde,
        Boolean tienePromocion,
        EstadoProductoVenta estadoVenta,
        Boolean vendible,
        List<String> coloresDisponibles,
        List<String> tallasDisponibles
) {
}