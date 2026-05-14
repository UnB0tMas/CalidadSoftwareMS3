// ruta: src/main/java/com/upsjb/ms3/kafka/event/MovimientoInventarioEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.StockEventType;
import java.util.Map;

public record MovimientoInventarioEvent(
        DomainEventEnvelope<MovimientoInventarioPayload> envelope
) {

    public MovimientoInventarioEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del evento de movimiento es obligatorio.");
        }
    }

    public static MovimientoInventarioEvent of(
            StockEventType eventType,
            Long idMovimiento,
            MovimientoInventarioPayload payload
    ) {
        return of(eventType, idMovimiento, null, null, payload, Map.of());
    }

    public static MovimientoInventarioEvent of(
            StockEventType eventType,
            Long idMovimiento,
            String requestId,
            String correlationId,
            MovimientoInventarioPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento de movimiento es obligatorio.");
        }

        if (idMovimiento == null) {
            throw new IllegalArgumentException("El idMovimiento es obligatorio para el evento de movimiento.");
        }

        return new MovimientoInventarioEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.MOVIMIENTO_INVENTARIO,
                        String.valueOf(idMovimiento),
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public MovimientoInventarioPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}