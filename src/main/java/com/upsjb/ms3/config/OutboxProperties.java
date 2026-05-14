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

    public boolean canRetry(Integer currentAttempts) {
        if (!retryErrors) {
            return false;
        }

        int attempts = currentAttempts == null ? 0 : currentAttempts;
        return attempts < maxAttempts;
    }
}