package com.upsjb.ms3.kafka.probe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.kafka.probe")
public class KafkaProbeProperties {

    private boolean enabled = true;

    private boolean runOnStartup = true;

    @Min(0)
    private long initialDelayMs = 10_000L;

    @Min(1_000)
    private long retryDelayMs = 10_000L;

    @Min(1)
    private int maxAttempts = 6;

    private boolean failOnTimeout = false;

    @Min(1_000)
    private long sendTimeoutMs = 10_000L;

    @NotBlank
    private String consumerGroup = "ms3-probe-consumer";

    @NotBlank
    private String serviceName = "ms-catalogo-inventario";

    @NotBlank
    private String targetMs4 = "ms-ventas-facturacion";

    @Valid
    private Topics topics = new Topics();

    public long safeInitialDelayMs() {
        return Math.max(initialDelayMs, 0L);
    }

    public long safeRetryDelayMs() {
        return Math.max(retryDelayMs, 1_000L);
    }

    public int safeMaxAttempts() {
        return Math.max(maxAttempts, 1);
    }

    public long safeSendTimeoutMs() {
        return Math.max(sendTimeoutMs, 1_000L);
    }

    public String ms3ToMs4Topic() {
        return topics.normalize(topics.ms3ToMs4, "dev.ms3.ms4.probe.v1");
    }

    public String ms4ToMs3AckTopic() {
        return topics.normalize(topics.ms4ToMs3Ack, "dev.ms4.ms3.probe-ack.v1");
    }

    public String ms4ToMs3Topic() {
        return topics.normalize(topics.ms4ToMs3, "dev.ms4.ms3.probe.v1");
    }

    public String ms3ToMs4AckTopic() {
        return topics.normalize(topics.ms3ToMs4Ack, "dev.ms3.ms4.probe-ack.v1");
    }

    @Getter
    @Setter
    public static class Topics {

        @NotBlank
        private String ms3ToMs4 = "dev.ms3.ms4.probe.v1";

        @NotBlank
        private String ms4ToMs3Ack = "dev.ms4.ms3.probe-ack.v1";

        @NotBlank
        private String ms4ToMs3 = "dev.ms4.ms3.probe.v1";

        @NotBlank
        private String ms3ToMs4Ack = "dev.ms3.ms4.probe-ack.v1";

        private String normalize(String value, String fallback) {
            if (value == null || value.isBlank()) {
                return fallback;
            }

            return value.trim();
        }
    }
}