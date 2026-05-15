package com.upsjb.ms3.shared.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IdempotencyKeyResolver {

    public String fromReference(
            String source,
            String eventType,
            String referenceType,
            String referenceExternalId,
            Object skuId,
            Object warehouseId,
            String operation
    ) {
        return stableKey(
                source,
                eventType,
                referenceType,
                referenceExternalId,
                skuId,
                warehouseId,
                operation
        );
    }

    public String fromEventId(String source, String eventId) {
        return stableKey(source, eventId);
    }

    public String stableKey(Object... parts) {
        StringJoiner joiner = new StringJoiner("|");

        if (parts != null) {
            for (Object part : parts) {
                if (part != null && StringUtils.hasText(String.valueOf(part))) {
                    joiner.add(normalize(String.valueOf(part)));
                }
            }
        }

        String raw = joiner.toString();

        if (!StringUtils.hasText(raw)) {
            return "";
        }

        return sha256(raw);
    }

    public boolean isValid(String key) {
        return StringUtils.hasText(key) && key.trim().length() >= 16;
    }

    private String normalize(String value) {
        return value.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte item : encoded) {
                builder.append(String.format("%02x", item));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible en el runtime.", ex);
        }
    }
}