package com.upsjb.ms3.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppPropertiesConfig {

    @NotBlank
    private String name = "MS Catalogo Inventario";

    @NotBlank
    private String version = "1.0.0";

    private String description;

    @NotBlank
    private String environment = "local";

    @NotBlank
    private String gatewayUrl = "http://localhost:8080";

    @NotBlank
    private String frontendUrl = "http://localhost:4200";

    @NotBlank
    private String locale = "es-PE";

    @NotBlank
    private String timezone = "America/Lima";

    @NotBlank
    private String zoneId = "UTC";

    private boolean ms1Enabled = true;
    private boolean ms2Enabled = true;
    private boolean ms4Enabled = true;

    @Valid
    @NotNull
    private Kafka kafka = new Kafka();

    @Valid
    @NotNull
    private Inventory inventory = new Inventory();

    @Valid
    @NotNull
    private Catalog catalog = new Catalog();

    @Valid
    @NotNull
    private Pricing pricing = new Pricing();

    @Valid
    @NotNull
    private Promotion promotion = new Promotion();

    @Valid
    @NotNull
    private EmployeeInventoryPermissions employeeInventoryPermissions = new EmployeeInventoryPermissions();

    @Valid
    @NotNull
    private Audit audit = new Audit();

    @Valid
    @NotNull
    private Pagination pagination = new Pagination();

    @Valid
    @NotNull
    private Cache cache = new Cache();

    @Valid
    @NotNull
    private Cors cors = new Cors();

    @Valid
    @NotNull
    private OpenApi openApi = new OpenApi();

    @Valid
    @NotNull
    private Jpa jpa = new Jpa();

    @Getter
    @Setter
    public static class Kafka {

        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Inventory {

        @Min(1)
        private Integer defaultReservationMinutes = 15;

        private boolean allowNegativeStock = false;

        private boolean lowStockEnabled = true;

        private boolean kardexRequired = true;

        private boolean requireMovementReason = true;

        private boolean allowManualStockUpdate = false;
    }

    @Getter
    @Setter
    public static class Catalog {

        private boolean autoGenerateProductCode = true;

        private boolean autoGenerateSkuCode = true;

        private boolean autoGenerateSlug = true;

        private boolean requireMainImageToPublish = true;

        private boolean requireActiveSkuToPublish = true;

        private boolean requireCurrentPriceToPublish = true;

        private boolean publicShowProgrammedProducts = true;

        private boolean programmedProductsSelectable = false;
    }

    @Getter
    @Setter
    public static class Pricing {

        @NotBlank
        private String defaultCurrency = "PEN";

        private boolean priceHistoryRequired = true;

        private boolean requirePriceChangeReason = true;
    }

    @Getter
    @Setter
    public static class Promotion {

        private boolean requireDateRange = true;

        private boolean allowGlobalDiscount = false;

        private boolean discountPerSkuRequired = true;

        private boolean allowNegativeMargin = false;
    }

    @Getter
    @Setter
    public static class EmployeeInventoryPermissions {

        private boolean enabled = true;

        private boolean requireActiveEmployee = true;

        private boolean versioned = true;
    }

    @Getter
    @Setter
    public static class Audit {

        private boolean enabled = true;

        private boolean includeRequestMetadata = true;

        private boolean includeResponseSummary = false;

        private boolean maskSensitiveData = true;
    }

    @Getter
    @Setter
    public static class Pagination {

        @Min(1)
        private Integer defaultSize = 20;

        @Min(1)
        @Max(500)
        private Integer maxSize = 100;

        @NotBlank
        private String defaultSortDirection = "DESC";
    }

    @Getter
    @Setter
    public static class Cache {

        private List<String> names = new ArrayList<>();

        @Min(1)
        private Integer expireAfterWriteMinutes = 10;

        @Min(1)
        private Long maximumSize = 10000L;

        private boolean recordStats = true;

        private boolean allowNullValues = false;
    }

    @Getter
    @Setter
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:8080"
        ));

        private List<String> allowedMethods = new ArrayList<>(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        private List<String> allowedHeaders = new ArrayList<>(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Request-Id",
                "X-Correlation-Id",
                "X-Forwarded-For",
                "X-Forwarded-Proto",
                "X-Forwarded-Host",
                "X-Forwarded-Port",
                "X-Real-IP",
                "X-Gateway-Source",
                "X-Internal-Service-Key"
        ));

        private List<String> exposedHeaders = new ArrayList<>(List.of(
                "Authorization",
                "X-Request-Id",
                "X-Correlation-Id"
        ));

        private boolean allowCredentials = false;

        @Min(0)
        private Long maxAgeSeconds = 3600L;

        @NotBlank
        private String pathPattern = "/**";
    }

    @Getter
    @Setter
    public static class OpenApi {

        @NotBlank
        private String title = "MS Catalogo Inventario API";

        private String description;

        private String contactName;

        @Email
        private String contactEmail;

        private String contactUrl;

        @Valid
        private List<OpenApiServer> servers = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class OpenApiServer {

        @NotBlank
        private String url;

        private String description;
    }

    @Getter
    @Setter
    public static class Jpa {

        @NotBlank
        private String systemAuditor = "SYSTEM";

        @Min(20)
        @Max(250)
        private Integer auditorMaxLength = 120;
    }

    public Duration reservationDuration() {
        return Duration.ofMinutes(inventory.defaultReservationMinutes);
    }
}