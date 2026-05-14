// ruta: src/main/java/com/upsjb/ms3/kafka/event/Ms4StockCommandEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import java.util.Map;

public record Ms4StockCommandEvent(
        DomainEventEnvelope<Ms4StockCommandPayload> envelope
) {

    public Ms4StockCommandEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del comando de stock MS4 es obligatorio.");
        }
    }

    public static Ms4StockCommandEvent of(
            Ms4StockEventType eventType,
            String referenciaIdExterno,
            Ms4StockCommandPayload payload
    ) {
        return of(eventType, referenciaIdExterno, null, null, payload, Map.of());
    }

    public static Ms4StockCommandEvent of(
            Ms4StockEventType eventType,
            String referenciaIdExterno,
            String requestId,
            String correlationId,
            Ms4StockCommandPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de comando MS4 es obligatorio.");
        }

        if (referenciaIdExterno == null || referenciaIdExterno.isBlank()) {
            throw new IllegalArgumentException("La referencia externa de MS4 es obligatoria.");
        }

        return new Ms4StockCommandEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.STOCK,
                        referenciaIdExterno,
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public Ms4StockCommandPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}