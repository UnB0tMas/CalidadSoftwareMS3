package com.upsjb.ms3.shared.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class KafkaPublishException extends BusinessException {

    private final String topic;
    private final String eventKey;

    public KafkaPublishException(String message) {
        this(null, null, message, null);
    }

    public KafkaPublishException(String topic, String eventKey, String message) {
        this(topic, eventKey, message, null);
    }

    public KafkaPublishException(String topic, String eventKey, String message, Throwable cause) {
        super(
                "KAFKA_PUBLISH_ERROR",
                message,
                HttpStatus.SERVICE_UNAVAILABLE,
                cause,
                Map.of(
                        "topic", topic == null ? "" : topic,
                        "eventKey", eventKey == null ? "" : eventKey
                )
        );
        this.topic = topic;
        this.eventKey = eventKey;
    }

    public String getTopic() {
        return topic;
    }

    public String getEventKey() {
        return eventKey;
    }
}