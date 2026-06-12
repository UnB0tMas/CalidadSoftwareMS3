package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.producer.KafkaEventKeyResolver;
import com.upsjb.ms3.kafka.producer.KafkaTopicResolver;
import com.upsjb.ms3.kafka.support.ProductoSnapshotAssembler;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.validator.EventoDominioOutboxValidator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final KafkaTopicResolver topicResolver;
    private final KafkaEventKeyResolver keyResolver;
    private final OutboxEventSerializer serializer;
    private final EventoDominioOutboxMapper mapper;
    private final EventoDominioOutboxValidator validator;
    private final ProductoSnapshotAssembler productoSnapshotAssembler;

    public EventoDominioOutbox create(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    ) {
        String topic =
                topicResolver.resolve(
                        aggregateType,
                        eventType
                );

        String eventKey =
                keyResolver.resolve(
                        aggregateType,
                        aggregateId
                );

        String payloadJson =
                serializer.toJson(
                        payload
                );

        validator.validateCreate(
                aggregateType,
                aggregateId,
                eventType,
                topic,
                eventKey,
                payloadJson
        );

        return mapper.toEntity(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                eventType,
                topic,
                eventKey,
                payloadJson
        );
    }

    public EventoDominioOutbox createEnvelope(
            DomainEventEnvelope<?> envelope
    ) {
        if (envelope == null) {
            throw new IllegalArgumentException(
                    "El envelope del evento outbox es obligatorio."
            );
        }

        DomainEventEnvelope<?> normalizedEnvelope =
                normalizeEnvelope(
                        envelope
                );

        String topic =
                topicResolver.resolve(
                        normalizedEnvelope.aggregateType(),
                        normalizedEnvelope.eventType()
                );

        String eventKey =
                keyResolver.resolve(
                        normalizedEnvelope
                );

        String payloadJson =
                serializer.toEnvelopeJson(
                        normalizedEnvelope
                );

        validator.validateCreate(
                normalizedEnvelope.aggregateType(),
                normalizedEnvelope.aggregateId(),
                normalizedEnvelope.eventType(),
                topic,
                eventKey,
                payloadJson
        );

        return mapper.toEntity(
                normalizedEnvelope.eventId(),
                normalizedEnvelope.aggregateType(),
                normalizedEnvelope.aggregateId(),
                normalizedEnvelope.eventType(),
                topic,
                eventKey,
                payloadJson
        );
    }

    public EventoDominioOutbox create(
            ProductoSnapshotEvent event
    ) {
        requireEvent(
                event,
                "El evento de producto es obligatorio."
        );

        return createEnvelope(
                event.envelope()
        );
    }

    public EventoDominioOutbox create(
            PrecioSnapshotEvent event
    ) {
        requireEvent(
                event,
                "El evento de precio es obligatorio."
        );

        return createEnvelope(
                event.envelope()
        );
    }

    public EventoDominioOutbox create(
            PromocionSnapshotEvent event
    ) {
        requireEvent(
                event,
                "El evento de promoción es obligatorio."
        );

        return createEnvelope(
                event.envelope()
        );
    }

    public EventoDominioOutbox create(
            StockSnapshotEvent event
    ) {
        requireEvent(
                event,
                "El evento de stock es obligatorio."
        );

        return createEnvelope(
                event.envelope()
        );
    }

    public EventoDominioOutbox create(
            MovimientoInventarioEvent event
    ) {
        requireEvent(
                event,
                "El evento de movimiento de inventario es obligatorio."
        );

        return createEnvelope(
                event.envelope()
        );
    }

    private DomainEventEnvelope<?> normalizeEnvelope(
            DomainEventEnvelope<?> envelope
    ) {
        if (
                envelope.aggregateType()
                        != AggregateType.PRODUCTO
                        || !(
                        envelope.payload()
                                instanceof ProductoSnapshotPayload
                )
        ) {
            return envelope;
        }

        Long idProducto =
                parseProductId(
                        envelope.aggregateId()
                );

        ProductoSnapshotPayload canonicalPayload =
                productoSnapshotAssembler
                        .assemble(
                                idProducto
                        );

        Map<String, Object> canonicalMetadata =
                new LinkedHashMap<>();

        canonicalMetadata.put(
                "snapshotCompleto",
                Boolean.TRUE
        );

        canonicalMetadata.put(
                "snapshotSchemaVersion",
                ProductoSnapshotEvent.SCHEMA_VERSION
        );

        canonicalMetadata.put(
                "snapshotAssembler",
                ProductoSnapshotAssembler.class
                        .getSimpleName()
        );

        @SuppressWarnings("unchecked")
        DomainEventEnvelope<ProductoSnapshotPayload>
                productEnvelope =
                (DomainEventEnvelope<ProductoSnapshotPayload>)
                        envelope;

        return productEnvelope.withPayload(
                canonicalPayload,
                ProductoSnapshotEvent.SCHEMA_VERSION,
                canonicalMetadata
        );
    }

    private Long parseProductId(
            String aggregateId
    ) {
        try {
            return Long.valueOf(
                    aggregateId
            );
        } catch (
                NumberFormatException
                | NullPointerException ex
        ) {
            throw new ValidationException(
                    "PRODUCTO_SNAPSHOT_AGGREGATE_ID_INVALIDO",
                    "El aggregateId del snapshot de producto debe contener un identificador numérico válido."
            );
        }
    }

    private void requireEvent(
            Object event,
            String message
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }
}