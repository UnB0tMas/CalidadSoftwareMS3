// ruta: src/main/java/com/upsjb/ms3/kafka/producer/KafkaDomainEventPublisher.java
package com.upsjb.ms3.kafka.producer;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.config.OutboxProperties;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.kafka.outbox.OutboxPublishResult;
import com.upsjb.ms3.shared.exception.KafkaPublishException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties outboxProperties;
    private final AppPropertiesConfig appPropertiesConfig;

    public OutboxPublishResult publish(EventoDominioOutbox event) {
        if (event == null) {
            return OutboxPublishResult.failure(
                    null,
                    null,
                    null,
                    null,
                    "OUTBOX_EVENT_NULL",
                    "El evento outbox es obligatorio."
            );
        }

        if (!appPropertiesConfig.getKafka().isEnabled()) {
            return OutboxPublishResult.skipped(
                    event.getIdEvento(),
                    event.getEventId(),
                    "KAFKA_DISABLED",
                    "Kafka está deshabilitado por configuración."
            );
        }

        validate(event);

        try {
            SendResult<String, String> result = kafkaTemplate
                    .send(event.getTopic(), event.getEventKey(), event.getPayloadJson())
                    .get(outboxProperties.getPublishTimeout().toMillis(), TimeUnit.MILLISECONDS);

            RecordMetadata metadata = result.getRecordMetadata();

            return OutboxPublishResult.success(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    metadata == null ? null : metadata.partition(),
                    metadata == null ? null : metadata.offset()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException(
                    event.getTopic(),
                    event.getEventKey(),
                    "La publicación Kafka fue interrumpida.",
                    ex
            );
        } catch (Exception ex) {
            throw new KafkaPublishException(
                    event.getTopic(),
                    event.getEventKey(),
                    "No se pudo publicar el evento en Kafka.",
                    ex
            );
        }
    }

    private void validate(EventoDominioOutbox event) {
        if (!StringUtils.hasText(event.getTopic())) {
            throw new KafkaPublishException("El topic Kafka del evento outbox es obligatorio.");
        }

        if (!StringUtils.hasText(event.getEventKey())) {
            throw new KafkaPublishException(
                    event.getTopic(),
                    null,
                    "La key Kafka del evento outbox es obligatoria."
            );
        }

        if (!StringUtils.hasText(event.getPayloadJson())) {
            throw new KafkaPublishException(
                    event.getTopic(),
                    event.getEventKey(),
                    "El payload Kafka es obligatorio."
            );
        }
    }
}