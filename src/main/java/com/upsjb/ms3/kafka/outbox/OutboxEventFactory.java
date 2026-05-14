// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxEventFactory.java
package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.producer.KafkaEventKeyResolver;
import com.upsjb.ms3.kafka.producer.KafkaTopicResolver;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.validator.EventoDominioOutboxValidator;
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

    public EventoDominioOutbox create(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    ) {
        String topic = topicResolver.resolve(aggregateType, eventType);
        String eventKey = keyResolver.resolve(aggregateType, aggregateId);
        String payloadJson = serializer.toJson(payload);

        validator.validateCreate(aggregateType, aggregateId, eventType, topic, eventKey, payloadJson);

        return mapper.toEntity(
                aggregateType,
                aggregateId,
                eventType,
                topic,
                eventKey,
                payloadJson
        );
    }

    public EventoDominioOutbox createEnvelope(DomainEventEnvelope<?> envelope) {
        String topic = topicResolver.resolve(envelope.aggregateType(), envelope.eventType());
        String eventKey = keyResolver.resolve(envelope);
        String payloadJson = serializer.toEnvelopeJson(envelope);

        validator.validateCreate(
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.eventType(),
                topic,
                eventKey,
                payloadJson
        );

        return mapper.toEntity(
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.eventType(),
                topic,
                eventKey,
                payloadJson
        );
    }

    public EventoDominioOutbox create(ProductoSnapshotEvent event) {
        return createEnvelope(event.envelope());
    }

    public EventoDominioOutbox create(PrecioSnapshotEvent event) {
        return createEnvelope(event.envelope());
    }

    public EventoDominioOutbox create(PromocionSnapshotEvent event) {
        return createEnvelope(event.envelope());
    }

    public EventoDominioOutbox create(StockSnapshotEvent event) {
        return createEnvelope(event.envelope());
    }

    public EventoDominioOutbox create(MovimientoInventarioEvent event) {
        return createEnvelope(event.envelope());
    }
}