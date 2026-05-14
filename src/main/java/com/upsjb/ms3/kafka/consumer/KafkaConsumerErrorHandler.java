// ruta: src/main/java/com/upsjb/ms3/kafka/consumer/KafkaConsumerErrorHandler.java
package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.kafka.producer.KafkaTopicResolver;
import com.upsjb.ms3.shared.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerErrorHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicResolver topicResolver;

    public boolean handle(String topic, String key, String rawMessage, Exception exception) {
        String safeTopic = StringUtils.hasText(topic) ? topic : "UNKNOWN_TOPIC";
        String safeKey = StringUtils.hasText(key) ? key : "UNKNOWN_KEY";

        if (exception instanceof BusinessException businessException) {
            log.warn(
                    "Evento Kafka rechazado funcionalmente. topic={}, key={}, code={}, message={}",
                    safeTopic,
                    safeKey,
                    businessException.getCode(),
                    businessException.getMessage()
            );

            sendToDeadLetter(safeTopic, safeKey, rawMessage, businessException.getCode(), businessException.getMessage());
            return true;
        }

        if (exception instanceof IllegalArgumentException) {
            log.warn(
                    "Evento Kafka inválido. topic={}, key={}, message={}",
                    safeTopic,
                    safeKey,
                    exception.getMessage()
            );

            sendToDeadLetter(safeTopic, safeKey, rawMessage, "KAFKA_EVENTO_INVALIDO", exception.getMessage());
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
    ) {
        String sanitizedMessage = rawMessage == null
                ? ""
                : new String(rawMessage.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");

        String sanitizedError = errorMessage == null
                ? ""
                : errorMessage.replace("\\", "\\\\").replace("\"", "\\\"");

        return """
                {
                  "sourceTopic": "%s",
                  "sourceKey": "%s",
                  "errorCode": "%s",
                  "errorMessage": "%s",
                  "failedAt": "%s",
                  "rawMessage": "%s"
                }
                """.formatted(
                sourceTopic,
                sourceKey,
                errorCode,
                sanitizedError,
                LocalDateTime.now(),
                sanitizedMessage
        );
    }
}