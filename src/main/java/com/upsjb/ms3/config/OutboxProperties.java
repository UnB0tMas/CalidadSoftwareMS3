package com.upsjb.ms3.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private boolean enabled = true;

    @Min(1)
    private Integer batchSize = 25;

    @Min(1)
    private Integer maxAttempts = 5;

    @NotNull
    private Duration fixedDelay = Duration.ofSeconds(10);

    @NotNull
    private Duration lockTimeout = Duration.ofSeconds(30);

    @NotNull
    private Duration publishTimeout = Duration.ofSeconds(10);

    @NotBlank
    private String publisherId = "ms3-local-outbox-publisher";

    private boolean retryErrors = true;

    private boolean deletePublished = false;

    public int safeBatchSize() {
        if (batchSize == null || batchSize <= 0) {
            return 25;
        }

        return Math.min(batchSize, 100);
    }

    public int safeMaxAttempts() {
        if (maxAttempts == null || maxAttempts <= 0) {
            return 5;
        }

        return Math.min(maxAttempts, 20);
    }

    public Duration safeFixedDelay() {
        return positiveDuration(fixedDelay, Duration.ofSeconds(10));
    }

    public long fixedDelayMillis() {
        return safeFixedDelay().toMillis();
    }

    public Duration safeLockTimeout() {
        return positiveDuration(lockTimeout, Duration.ofSeconds(30));
    }

    public long lockTimeoutMillis() {
        return safeLockTimeout().toMillis();
    }

    public Duration safePublishTimeout() {
        return positiveDuration(publishTimeout, Duration.ofSeconds(10));
    }

    public long publishTimeoutMillis() {
        return safePublishTimeout().toMillis();
    }

    public boolean canRetry(Integer currentAttempts) {
        if (!retryErrors) {
            return false;
        }

        int attempts = currentAttempts == null ? 0 : currentAttempts;
        return attempts < safeMaxAttempts();
    }

    private Duration positiveDuration(Duration value, Duration fallback) {
        if (value == null || value.isZero() || value.isNegative()) {
            return fallback;
        }

        return value;
    }
}