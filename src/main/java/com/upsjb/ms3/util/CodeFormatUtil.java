// ruta: src/main/java/com/upsjb/ms3/util/CodeFormatUtil.java
package com.upsjb.ms3.util;

public final class CodeFormatUtil {

    private static final int DEFAULT_LENGTH = 6;

    private CodeFormatUtil() {
    }

    public static String formatSequential(String prefix, long number) {
        return formatSequential(prefix, number, DEFAULT_LENGTH);
    }

    public static String formatSequential(String prefix, long number, int length) {
        if (number <= 0) {
            throw new IllegalArgumentException("El número correlativo debe ser mayor a cero.");
        }

        String safePrefix = normalizePrefix(prefix);
        int safeLength = length <= 0 ? DEFAULT_LENGTH : length;
        String numericPart = String.format("%0" + safeLength + "d", number);

        return safePrefix.isBlank() ? numericPart : safePrefix + "-" + numericPart;
    }

    public static String normalizePrefix(String prefix) {
        if (!StringNormalizer.hasText(prefix)) {
            return "";
        }

        return StringNormalizer.upperWithoutAccents(prefix)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

    public static String normalizeCode(String code) {
        if (!StringNormalizer.hasText(code)) {
            return "";
        }

        return StringNormalizer.upperWithoutAccents(code)
                .replaceAll("[^A-Z0-9\\-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

    public static boolean isGeneratedCode(String code, String expectedPrefix) {
        String normalizedCode = normalizeCode(code);
        String normalizedPrefix = normalizePrefix(expectedPrefix);

        if (normalizedCode.isBlank() || normalizedPrefix.isBlank()) {
            return false;
        }

        return normalizedCode.matches("^" + normalizedPrefix + "-\\d+$");
    }

    public static String joinCodeParts(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            String normalized = normalizePrefix(part);
            if (normalized.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append("-");
            }
            builder.append(normalized);
        }

        return builder.toString();
    }
}