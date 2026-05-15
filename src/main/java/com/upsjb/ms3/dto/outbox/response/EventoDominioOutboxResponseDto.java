// ruta: src/main/java/com/upsjb/ms3/dto/outbox/response/EventoDominioOutboxResponseDto.java
package com.upsjb.ms3.dto.outbox.response;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EventoDominioOutboxResponseDto(
        Long idEvento,
        UUID eventId,
        AggregateType aggregateType,
        String aggregateId,
        String eventType,
        String topic,
        String eventKey,
        String payloadJson,
        String payloadPreview,
        EstadoPublicacionEvento estadoPublicacion,
        Integer intentosPublicacion,
        String errorPublicacion,
        Boolean reintentable,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        LocalDateTime lockedAt,
        String lockedBy,
        Boolean estado
) {
}