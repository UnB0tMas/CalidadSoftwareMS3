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
                cleanCode(code, "EVENTO_KAFKA_PUBLICACION_FALLIDA"),
                cleanMessage(message, "No se pudo publicar el evento en Kafka."),
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
        return skipped(idEvento, eventId, null, null, code, message);
    }

    public static OutboxPublishResult skipped(
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
                true,
                cleanCode(code, "EVENTO_KAFKA_OMITIDO"),
                cleanMessage(message, "La publicación del evento fue omitida."),
                topic,
                eventKey,
                null,
                null,
                LocalDateTime.now()
        );
    }

    private static String cleanCode(String code, String fallback) {
        return code == null || code.isBlank() ? fallback : code.trim();
    }

    private static String cleanMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message.trim();
    }
}