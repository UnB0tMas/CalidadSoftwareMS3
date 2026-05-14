// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/response/ProductoImagenResponseDto.java
package com.upsjb.ms3.dto.catalogo.producto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoImagenResponseDto(
        Long idImagen,
        Long idProducto,
        Long idSku,
        String codigoProducto,
        String nombreProducto,
        String codigoSku,
        String cloudinaryAssetId,
        String cloudinaryPublicId,
        Long cloudinaryVersion,
        String secureUrl,
        String url,
        String resourceType,
        String format,
        Long bytes,
        Integer width,
        Integer height,
        String folder,
        String originalFilename,
        String altText,
        String titulo,
        Integer orden,
        Boolean principal,
        Long creadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}