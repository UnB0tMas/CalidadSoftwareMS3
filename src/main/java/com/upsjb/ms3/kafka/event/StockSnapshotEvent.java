// ruta: src/main/java/com/upsjb/ms3/kafka/event/StockSnapshotEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.StockEventType;
import java.util.Map;

public record StockSnapshotEvent(
        DomainEventEnvelope<StockSnapshotPayload> envelope
) {

    public StockSnapshotEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del evento de stock es obligatorio.");
        }
    }

    public static StockSnapshotEvent of(
            StockEventType eventType,
            Long idStock,
            StockSnapshotPayload payload
    ) {
        return of(eventType, idStock, null, null, payload, Map.of());
    }

    public static StockSnapshotEvent of(
            StockEventType eventType,
            Long idStock,
            String requestId,
            String correlationId,
            StockSnapshotPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento de stock es obligatorio.");
        }

        if (idStock == null) {
            throw new IllegalArgumentException("El idStock es obligatorio para el evento de stock.");
        }

        return new StockSnapshotEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.STOCK,
                        String.valueOf(idStock),
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public StockSnapshotPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}