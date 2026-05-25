package com.upsjb.ms3.kafka.producer;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.config.OutboxProperties;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.kafka.outbox.OutboxPublishResult;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_DISABLED",
                    "Kafka está deshabilitado por configuración."
            );
        }

        OutboxPublishResult validationFailure = validate(event);
        if (validationFailure != null) {
            return validationFailure;
        }

        try {
            SendResult<String, String> result = kafkaTemplate
                    .send(event.getTopic().trim(), event.getEventKey().trim(), event.getPayloadJson())
                    .get(outboxProperties.publishTimeoutMillis(), TimeUnit.MILLISECONDS);

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

            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_PUBLICACION_INTERRUPTED",
                    "La publicación Kafka fue interrumpida."
            );
        } catch (TimeoutException ex) {
            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_PUBLICACION_TIMEOUT",
                    "Timeout al publicar evento en Kafka."
            );
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();

            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_PUBLICACION_ERROR",
                    "Error al publicar en Kafka: " + cause.getMessage()
            );
        } catch (Exception ex) {
            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_PUBLICACION_ERROR",
                    "Error inesperado al publicar en Kafka: " + ex.getMessage()
            );
        }
    }

    private OutboxPublishResult validate(EventoDominioOutbox event) {
        if (!StringUtils.hasText(event.getTopic())) {
            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_TOPIC_REQUERIDO",
                    "El topic Kafka del evento outbox es obligatorio."
            );
        }

        if (!StringUtils.hasText(event.getEventKey())) {
            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_KEY_REQUERIDA",
                    "La key Kafka del evento outbox es obligatoria."
            );
        }

        if (!StringUtils.hasText(event.getPayloadJson())) {
            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "KAFKA_PAYLOAD_REQUERIDO",
                    "El payload Kafka es obligatorio."
            );
        }

        return null;
    }
}