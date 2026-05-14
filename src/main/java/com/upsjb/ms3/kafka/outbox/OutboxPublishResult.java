// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxPublishResult.java
package com.upsjb.ms3.kafka.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxPublishResult(
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

    public static OutboxPublishResult success(
            Long idEvento,
            UUID eventId,
            String topic,
            String eventKey,
            Integer partition,
            Long offset
    ) {
        return new OutboxPublishResult(
                idEvento,
                eventId,
                true,
                false,
                "EVENTO_KAFKA_PUBLICADO",
                "Evento publicado correctamente en Kafka.",
                topic,
                eventKey,
                partition,
                offset,
                LocalDateTime.now()
        );
    }

    public static OutboxPublishResult failure(
            Long idEvento,
            UUID eventId,
            String topic,
            String eventKey,
            String code,
            String message
    ) {
        return new OutboxPublishResult(
                idEvento,
                eventId,
                false,
                false,
                code == null || code.isBlank() ? "EVENTO_KAFKA_PUBLICACION_FALLIDA" : code,
                message == null || message.isBlank() ? "No se pudo publicar el evento en Kafka." : message,
                topic,
                eventKey,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static OutboxPublishResult skipped(
            Long idEvento,
            UUID eventId,
            String code,
            String message
    ) {
        return new OutboxPublishResult(
                idEvento,
                eventId,
                false,
                true,
                code == null || code.isBlank() ? "EVENTO_KAFKA_OMITIDO" : code,
                message == null || message.isBlank() ? "La publicación del evento fue omitida." : message,
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }
}