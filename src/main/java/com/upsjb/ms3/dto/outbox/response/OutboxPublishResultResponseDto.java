// ruta: src/main/java/com/upsjb/ms3/dto/outbox/response/OutboxPublishResultResponseDto.java
package com.upsjb.ms3.dto.outbox.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OutboxPublishResultResponseDto(
        Long idEvento,
        UUID eventId,
        Boolean success,
        Boolean skipped,
        String code,
        String message,
        String topic,
        String eventKey,
        Integer partition,
        Long offset,
        LocalDateTime processedAt
) {
}