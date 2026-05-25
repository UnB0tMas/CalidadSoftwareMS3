package com.upsjb.ms3.kafka.probe;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaProbeProperties properties;

    public KafkaProbePublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            KafkaProbeProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public RecordMetadata publishProbe(KafkaProbePayload payload, String topic, String key) {
        validate(topic, key, payload);

        try {
            String json = objectMapper.writeValueAsString(payload);

            SendResult<String, String> result = kafkaTemplate
                    .send(topic.trim(), key.trim(), json)
                    .get(properties.safeSendTimeoutMs(), TimeUnit.MILLISECONDS);

            return result.getRecordMetadata();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación Kafka Probe MS3 interrumpida.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo publicar Kafka Probe MS3 hacia topic: " + topic, ex);
        }
    }

    public RecordMetadata publishAck(KafkaProbeAckPayload payload, String topic, String key) {
        validateAck(topic, key, payload);

        try {
            String json = objectMapper.writeValueAsString(payload);

            SendResult<String, String> result = kafkaTemplate
                    .send(topic.trim(), key.trim(), json)
                    .get(properties.safeSendTimeoutMs(), TimeUnit.MILLISECONDS);

            return result.getRecordMetadata();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación Kafka Probe ACK MS3 interrumpida.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo publicar Kafka Probe ACK MS3 hacia topic: " + topic, ex);
        }
    }

    private void validate(String topic, String key, KafkaProbePayload payload) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("El topic Kafka Probe es obligatorio.");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La key Kafka Probe es obligatoria.");
        }

        if (payload == null) {
            throw new IllegalArgumentException("El payload Kafka Probe es obligatorio.");
        }

        if (!payload.valid()) {
            throw new IllegalArgumentException("El payload Kafka Probe es inválido.");
        }
    }

    private void validateAck(String topic, String key, KafkaProbeAckPayload payload) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("El topic Kafka Probe ACK es obligatorio.");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La key Kafka Probe ACK es obligatoria.");
        }

        if (payload == null || !payload.isOk()) {
            throw new IllegalArgumentException("El payload Kafka Probe ACK es inválido.");
        }
    }
}