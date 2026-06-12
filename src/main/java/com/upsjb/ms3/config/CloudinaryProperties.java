package com.upsjb.ms3.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    private static final Set<String> PLACEHOLDER_VALUES = Set.of(
            "MS_IMG",
            "CLOUDINARY",
            "CLOUDINARY_CLOUD_NAME",
            "YOUR_CLOUD_NAME",
            "YOUR_API_KEY",
            "YOUR_API_SECRET",
            "CHANGE_ME",
            "CHANGEME",
            "REPLACE_ME",
            "PENDING",
            "UNDEFINED",
            "NULL"
    );

    private boolean enabled = true;

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    private boolean secure = true;

    @NotBlank
    private String folderRoot =
            "ms3/catalogo-inventario";

    @NotBlank
    private String productFolder =
            "productos";

    @NotBlank
    private String skuFolder =
            "sku";

    @Min(1024)
    private Long maxFileSizeBytes =
            10_485_760L;

    private List<String> allowedContentTypes =
            new ArrayList<>(
                    List.of(
                            "image/jpeg",
                            "image/png",
                            "image/webp",
                            "image/jpg"
                    )
            );

    @NotBlank
    private String defaultResourceType =
            "image";

    private boolean invalidateOnDelete = true;

    private Duration uploadTimeout =
            Duration.ofSeconds(15);

    @AssertTrue(
            message =
                    "Cloudinary requiere cloud-name, api-key y api-secret "
                            + "cuando cloudinary.enabled=true."
    )
    public boolean isRequiredCredentialConfigurationPresent() {
        if (!enabled) {
            return true;
        }

        return isConfiguredValue(cloudName)
                && isConfiguredValue(apiKey)
                && isConfiguredValue(apiSecret);
    }

    @AssertTrue(
            message =
                    "cloudinary.cloud-name no puede ser un valor de ejemplo. "
                            + "Debe contener el Cloud name real mostrado "
                            + "en el Dashboard de Cloudinary."
    )
    public boolean isCloudNameNotPlaceholder() {
        if (!enabled || !StringUtils.hasText(cloudName)) {
            return true;
        }

        return !isPlaceholderValue(cloudName);
    }

    @AssertTrue(
            message =
                    "cloudinary.cloud-name tiene un formato inválido. "
                            + "Use únicamente letras minúsculas, números "
                            + "y guiones, sin espacios ni guion bajo."
    )
    public boolean isCloudNameFormatValid() {
        if (!enabled || !StringUtils.hasText(cloudName)) {
            return true;
        }

        String normalized =
                cloudName.trim();

        return normalized.matches(
                "^[a-z0-9][a-z0-9-]*$"
        );
    }

    @AssertTrue(
            message =
                    "cloudinary.api-key y cloudinary.api-secret "
                            + "no pueden contener valores de ejemplo."
    )
    public boolean areApiCredentialsNotPlaceholders() {
        if (!enabled) {
            return true;
        }

        if (!StringUtils.hasText(apiKey)
                || !StringUtils.hasText(apiSecret)) {
            return true;
        }

        return !isPlaceholderValue(apiKey)
                && !isPlaceholderValue(apiSecret);
    }

    public void validateForClientCreation() {
        if (!enabled) {
            return;
        }

        if (!isConfiguredValue(cloudName)) {
            throw new IllegalStateException(
                    "No se configuró cloudinary.cloud-name. "
                            + "Defina el Cloud name real de Cloudinary."
            );
        }

        if (isPlaceholderValue(cloudName)) {
            throw new IllegalStateException(
                    "El valor configurado en cloudinary.cloud-name es "
                            + "un valor de ejemplo y no identifica un entorno "
                            + "real de Cloudinary. Valor recibido: "
                            + cloudName.trim()
            );
        }

        if (!isCloudNameFormatValid()) {
            throw new IllegalStateException(
                    "El Cloud name configurado tiene un formato inválido. "
                            + "Valor recibido: "
                            + cloudName.trim()
            );
        }

        if (!isConfiguredValue(apiKey)) {
            throw new IllegalStateException(
                    "No se configuró cloudinary.api-key."
            );
        }

        if (!isConfiguredValue(apiSecret)) {
            throw new IllegalStateException(
                    "No se configuró cloudinary.api-secret."
            );
        }

        if (isPlaceholderValue(apiKey)) {
            throw new IllegalStateException(
                    "cloudinary.api-key contiene un valor de ejemplo."
            );
        }

        if (isPlaceholderValue(apiSecret)) {
            throw new IllegalStateException(
                    "cloudinary.api-secret contiene un valor de ejemplo."
            );
        }
    }

    public String normalizedCloudName() {
        return requireConfiguredValue(
                cloudName,
                "cloudinary.cloud-name"
        );
    }

    public String normalizedApiKey() {
        return requireConfiguredValue(
                apiKey,
                "cloudinary.api-key"
        );
    }

    public String normalizedApiSecret() {
        return requireConfiguredValue(
                apiSecret,
                "cloudinary.api-secret"
        );
    }

    public String productosFolderPath() {
        return normalizeFolder(folderRoot)
                + "/"
                + normalizeFolder(productFolder);
    }

    public String skuFolderPath() {
        return normalizeFolder(folderRoot)
                + "/"
                + normalizeFolder(skuFolder);
    }

    /**
     * Comprueba si un MIME type está permitido para subirlo a Cloudinary.
     *
     * La comparación ignora espacios laterales y diferencias entre
     * mayúsculas y minúsculas.
     */
    public boolean isAllowedContentType(
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        if (allowedContentTypes == null
                || allowedContentTypes.isEmpty()) {
            return false;
        }

        String normalizedContentType =
                contentType.trim()
                        .toLowerCase(Locale.ROOT);

        return allowedContentTypes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(value ->
                        value.toLowerCase(Locale.ROOT)
                )
                .anyMatch(normalizedContentType::equals);
    }

    private boolean isConfiguredValue(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized =
                value.trim();

        boolean unresolvedPlaceholder =
                normalized.startsWith("${")
                        && normalized.endsWith("}");

        return !unresolvedPlaceholder;
    }

    private boolean isPlaceholderValue(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized =
                value.trim()
                        .toUpperCase(Locale.ROOT);

        return PLACEHOLDER_VALUES.contains(normalized)
                || normalized.contains("YOUR_")
                || normalized.contains("CHANGE_ME")
                || normalized.contains("REPLACE_ME")
                || normalized.contains("PLACEHOLDER");
    }

    private String requireConfiguredValue(
            String value,
            String propertyName
    ) {
        if (!isConfiguredValue(value)) {
            throw new IllegalStateException(
                    "La propiedad "
                            + propertyName
                            + " no está configurada correctamente."
            );
        }

        return value.trim();
    }

    private String normalizeFolder(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    "La carpeta de Cloudinary no puede estar vacía."
            );
        }

        String normalized =
                value.trim()
                        .replace('\\', '/')
                        .replaceAll("/+", "/")
                        .replaceAll("^/+", "")
                        .replaceAll("/+$", "");

        if (!StringUtils.hasText(normalized)) {
            throw new IllegalStateException(
                    "La carpeta de Cloudinary no tiene un formato válido."
            );
        }

        return normalized;
    }
}