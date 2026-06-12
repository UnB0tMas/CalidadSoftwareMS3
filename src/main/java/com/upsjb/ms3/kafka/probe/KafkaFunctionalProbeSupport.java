package com.upsjb.ms3.kafka.probe;

public final class KafkaFunctionalProbeSupport {

    public static final String CORRELATION_PREFIX =
            "KAFKA-FUNCTIONAL-E2E:";

    private KafkaFunctionalProbeSupport() {
    }

    public static String correlationId(
            String probeId
    ) {
        return CORRELATION_PREFIX
                + requireProbeId(probeId);
    }

    public static boolean isFunctionalProbe(
            String correlationId
    ) {
        return correlationId != null
                && correlationId.startsWith(
                CORRELATION_PREFIX
        )
                && correlationId.length()
                > CORRELATION_PREFIX.length();
    }

    public static String extractProbeId(
            String correlationId
    ) {
        if (!isFunctionalProbe(correlationId)) {
            throw new IllegalArgumentException(
                    "El correlationId no pertenece a una prueba funcional Kafka."
            );
        }

        return requireProbeId(
                correlationId.substring(
                        CORRELATION_PREFIX.length()
                )
        );
    }

    private static String requireProbeId(
            String probeId
    ) {
        if (
                probeId == null
                        || probeId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El probeId funcional Kafka es obligatorio."
            );
        }

        return probeId.trim();
    }
}