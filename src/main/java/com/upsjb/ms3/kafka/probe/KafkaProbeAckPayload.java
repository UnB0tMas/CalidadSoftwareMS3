package com.upsjb.ms3.kafka.probe;

import java.time.Instant;

public record KafkaProbeAckPayload(
        String probeId,
        String sourceService,
        String targetService,
        String eventType,
        String direction,
        String receivedTopic,
        String receivedKey,
        Instant ackAt,
        String status,
        String message
) {

    public static KafkaProbeAckPayload ackToMs4(
            KafkaProbePayload received,
            String sourceService,
            String targetService,
            String receivedTopic,
            String receivedKey
    ) {
        return new KafkaProbeAckPayload(
                received.probeId(),
                sourceService,
                targetService,
                "KAFKA_PROBE_ACK",
                "MS3_TO_MS4_ACK",
                receivedTopic,
                receivedKey,
                Instant.now(),
                "OK",
                "MS3 received probe from MS4 without persistence."
        );
    }

    public boolean isOk() {
        return "KAFKA_PROBE_ACK".equalsIgnoreCase(eventType)
                && "OK".equalsIgnoreCase(status)
                && probeId != null
                && !probeId.isBlank();
    }
}