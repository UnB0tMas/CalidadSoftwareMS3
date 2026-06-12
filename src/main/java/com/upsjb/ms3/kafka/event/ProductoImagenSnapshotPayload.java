package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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