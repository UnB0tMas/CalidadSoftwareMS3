package com.upsjb.ms3.config;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CloudinaryClientConfig {

    private static final String LOG_PREFIX =
            "[CLOUDINARY-CONFIG][MS3]";

    private final CloudinaryProperties properties;

    @Bean
    @ConditionalOnMissingBean(Cloudinary.class)
    @ConditionalOnProperty(
            prefix = "cloudinary",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public Cloudinary cloudinary() {
        properties.validateForClientCreation();

        String cloudName =
                properties.normalizedCloudName();

        String apiKey =
                properties.normalizedApiKey();

        String apiSecret =
                properties.normalizedApiSecret();

        log.info(
                "{} Inicializando cliente Cloudinary. "
                        + "cloudName={}, apiKeyMasked={}, secure={}, "
                        + "folderRoot={}",
                LOG_PREFIX,
                cloudName,
                maskApiKey(apiKey),
                properties.isSecure(),
                properties.getFolderRoot()
        );

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                "cloud_name",
                cloudName
        );

        config.put(
                "api_key",
                apiKey
        );

        config.put(
                "api_secret",
                apiSecret
        );

        config.put(
                "secure",
                properties.isSecure()
        );

        Cloudinary cloudinary =
                new Cloudinary(config);

        log.info(
                "{} Cliente Cloudinary configurado correctamente. "
                        + "cloudName={}, secure={}",
                LOG_PREFIX,
                cloudName,
                properties.isSecure()
        );

        return cloudinary;
    }

    private String maskApiKey(
            String apiKey
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            return "NOT_CONFIGURED";
        }

        String normalized =
                apiKey.trim();

        if (normalized.length() <= 4) {
            return "*".repeat(
                    normalized.length()
            );
        }

        return "*".repeat(
                normalized.length() - 4
        ) + normalized.substring(
                normalized.length() - 4
        );
    }
}