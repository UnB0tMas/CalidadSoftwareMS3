// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoSnapshotResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoSnapshotResponseDto(
        Long idProductoMs3,
        String codigoProducto,
        String nombreProducto,
        String slug,
        String categoriaCodigo,
        String categoriaNombre,
        String marcaCodigo,
        String marcaNombre,
        EstadoProductoPublicacion estadoPublicacion,
        EstadoProductoVenta estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        String imagenPrincipalUrl,
        List<SkuSnapshotDto> skus,
        LocalDateTime updatedAt
) {

    @Builder
    public record SkuSnapshotDto(
            Long idSkuMs3,
            String codigoSku,
            String barcode,
            String color,
            String talla,
            MoneyResponseDto precioVigente,
            MoneyResponseDto precioFinalPromocion,
            Integer stockDisponible,
            Boolean vendible
    ) {
    }
}