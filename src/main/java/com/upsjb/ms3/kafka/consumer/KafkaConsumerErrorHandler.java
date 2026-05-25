package com.upsjb.ms3.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.kafka.producer.KafkaTopicResolver;
import com.upsjb.ms3.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerErrorHandler {

    private static final int MAX_RAW_MESSAGE_LENGTH = 10_000;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicResolver topicResolver;
    private final ObjectMapper objectMapper;

    public boolean handle(String topic, String key, String rawMessage, Exception exception) {
        String safeTopic = StringUtils.hasText(topic) ? topic.trim() : "UNKNOWN_TOPIC";
        String safeKey = StringUtils.hasText(key) ? key.trim() : "UNKNOWN_KEY";

        if (exception instanceof BusinessException businessException) {
            log.warn(
                    "Evento Kafka rechazado funcionalmente. topic={}, key={}, code={}, message={}",
                    safeTopic,
                    safeKey,
                    businessException.getCode(),
                    businessException.getMessage()
            );

            sendToDeadLetter(
                    safeTopic,
                    safeKey,
                    rawMessage,
                    businessException.getCode(),
                    businessException.getMessage()
            );

            return true;
        }

        if (exception instanceof IllegalArgumentException) {
            log.warn(
                    "Evento Kafka inválido. topic={}, key={}, message={}",
                    safeTopic,
                    safeKey,
                    exception.getMessage()
            );

            sendToDeadLetter(
                    safeTopic,
                    safeKey,
                    rawMessage,
                    "KAFKA_EVENTO_INVALIDO",
                    exception.getMessage()
            );

            return true;
        }

        log.error(
                "Error técnico al consumir evento Kafka. topic={}, key={}",
                safeTopic,
                safeKey,
                exception
        );

        return false;
    }

    private void sendToDeadLetter(
            String sourceTopic,
            String sourceKey,
            String rawMessage,
            String errorCode,
            String errorMessage
    ) {
        try {
            String dlqTopic = topicResolver.resolveDeadLetterTopic();
            String payload = buildDeadLetterPayload(sourceTopic, sourceKey, rawMessage, errorCode, errorMessage);
            kafkaTemplate.send(dlqTopic, sourceKey, payload);
        } catch (Exception ex) {
            log.error("No se pudo enviar evento a Dead Letter Topic.", ex);
        }
    }

    private String buildDeadLetterPayload(
            String sourceTopic,
            String sourceKey,
            String rawMessage,
            String errorCode,
            String errorMessage
    ) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceTopic", sourceTopic);
        payload.put("sourceKey", sourceKey);
        payload.put("errorCode", clean(errorCode, "KAFKA_CONSUMER_ERROR"));
        payload.put("errorMessage", clean(errorMessage, "Error consumiendo evento Kafka."));
        payload.put("failedAt", LocalDateTime.now());
        payload.put("rawMessage", truncate(rawMessage, MAX_RAW_MESSAGE_LENGTH));

        return objectMapper.writeValueAsString(payload);
    }

    private String clean(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }

        return value.trim()
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();

        if (clean.length() <= maxLength) {
            return clean;
        }

        return clean.substring(0, maxLength);
    }
}