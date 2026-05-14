// ruta: src/main/java/com/upsjb/ms3/kafka/event/ProductoImagenSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoImagenSnapshotPayload(
        Long idImagen,
        Long idProducto,
        Long idSku,
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
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}