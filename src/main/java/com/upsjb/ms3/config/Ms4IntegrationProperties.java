package com.upsjb.ms3.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
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
@ConfigurationProperties(prefix = "ms4")
public class Ms4IntegrationProperties {

    private boolean enabled = true;

    @NotNull
    private URI baseUrl = URI.create("http://localhost:8084");

    @NotNull
    private Duration timeout = Duration.ofSeconds(5);

    @NotBlank
    private String stockSyncPath = "/api/internal/ms4/stock-sync";

    @NotBlank
    private String pendingStockEventsPath = "/api/internal/ms4/stock-events/pending";

    @NotBlank
    private String internalServiceKeyHeader = "X-Internal-Service-Key";

    @NotBlank
    private String internalServiceKey = "local-ms3-ms4-internal-key-change-me";

    public URI stockSyncUri() {
        return resolve(stockSyncPath);
    }

    public URI pendingStockEventsUri() {
        return resolve(pendingStockEventsPath);
    }

    private URI resolve(String path) {
        String base = baseUrl.toString().replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(base + normalizedPath);
    }
}