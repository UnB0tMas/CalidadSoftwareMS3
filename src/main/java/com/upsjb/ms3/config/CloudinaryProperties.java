package com.upsjb.ms3.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private boolean enabled = true;

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    private boolean secure = true;

    @NotBlank
    private String folderRoot = "ms3/catalogo-inventario";

    @NotBlank
    private String productFolder = "productos";

    @NotBlank
    private String skuFolder = "sku";

    @Min(1024)
    private Long maxFileSizeBytes = 10_485_760L;

    private List<String> allowedContentTypes = new ArrayList<>(List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    ));

    @NotBlank
    private String defaultResourceType = "image";

    private boolean invalidateOnDelete = true;

    private Duration uploadTimeout = Duration.ofSeconds(15);

    @AssertTrue(message = "Cloudinary requiere cloud-name, api-key y api-secret cuando cloudinary.enabled=true.")
    public boolean isCredentialConfigurationValid() {
        if (!enabled) {
            return true;
        }

        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }

    public String productosFolderPath() {
        return normalizeFolder(folderRoot) + "/" + normalizeFolder(productFolder);
    }

    public String skuFolderPath() {
        return normalizeFolder(folderRoot) + "/" + normalizeFolder(skuFolder);
    }

    public boolean isAllowedContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        return allowedContentTypes.stream()
                .anyMatch(value -> value.equalsIgnoreCase(contentType.trim()));
    }

    private String normalizeFolder(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
    }
}