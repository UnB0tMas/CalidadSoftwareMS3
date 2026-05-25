package com.upsjb.ms3.kafka.probe;

import java.time.Instant;
import java.util.Map;

public record KafkaProbePayload(
        String probeId,
        String sourceService,
        String targetService,
        String eventType,
        String direction,
        Instant sentAt,
        String message,
        Map<String, String> metadata
) {

    public static KafkaProbePayload ms3ToMs4(
            String probeId,
            String sourceService,
            String targetService
    ) {
        return new KafkaProbePayload(
                probeId,
                sourceService,
                targetService,
                "KAFKA_PROBE",
                "MS3_TO_MS4",
                Instant.now(),
                "probe-ms3-to-ms4",
                Map.of(
                        "persistence", "DISABLED",
                        "outbox", "DISABLED",
                        "businessValidations", "DISABLED",
                        "sourceRole", "PRODUCER_AND_CONSUMER",
                        "purpose", "KAFKA_CONNECTIVITY_TEST"
                )
        );
    }

    public boolean valid() {
        return probeId != null
                && !probeId.isBlank()
                && "KAFKA_PROBE".equalsIgnoreCase(eventType)
                && direction != null
                && !direction.isBlank();
    }
}