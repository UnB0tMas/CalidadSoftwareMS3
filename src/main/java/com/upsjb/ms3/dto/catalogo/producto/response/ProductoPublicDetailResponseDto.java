// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoPublicDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoPublicDetailResponseDto(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        String descripcionCorta,
        String descripcionLarga,
        String categoriaNombre,
        String categoriaSlug,
        String marcaNombre,
        String generoObjetivo,
        String temporada,
        String deporte,
        EstadoProductoVenta estadoVenta,
        Boolean vendible,
        MoneyResponseDto precioDesde,
        MoneyResponseDto precioFinalDesde,
        Boolean tienePromocion,
        List<ProductoSkuResponseDto> skus,
        List<ProductoAtributoValorResponseDto> atributos,
        List<ProductoImagenResponseDto> imagenes
) {
}