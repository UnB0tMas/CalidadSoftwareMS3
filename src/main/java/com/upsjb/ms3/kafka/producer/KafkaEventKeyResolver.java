// ruta: src/main/java/com/upsjb/ms3/kafka/producer/KafkaEventKeyResolver.java
package com.upsjb.ms3.kafka.producer;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KafkaEventKeyResolver {

    public String resolve(DomainEventEnvelope<?> envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope es obligatorio para resolver la key Kafka.");
        }

        return resolve(envelope.aggregateType(), envelope.aggregateId());
    }

    public String resolve(AggregateType aggregateType, String aggregateId) {
        if (aggregateType == null) {
            throw new IllegalArgumentException("El aggregateType es obligatorio para resolver la key Kafka.");
        }

        if (!StringUtils.hasText(aggregateId)) {
            throw new IllegalArgumentException("El aggregateId es obligatorio para resolver la key Kafka.");
        }

        return aggregateType.getCode() + ":" + aggregateId.trim();
    }

    public String resolveRaw(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("La key Kafka es obligatoria.");
        }

        return key.trim();
    }
}