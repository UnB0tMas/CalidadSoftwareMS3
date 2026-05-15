// ruta: src/main/java/com/upsjb/ms3/util/TextSearchUtil.java
package com.upsjb.ms3.util;

import java.util.Arrays;
import java.util.List;

public final class TextSearchUtil {

    private TextSearchUtil() {
    }

    public static boolean hasSearchText(String value) {
        return StringNormalizer.hasText(normalizeSearchText(value));
    }

    public static String normalizeSearchText(String value) {
        return StringNormalizer.normalizeForSearch(value);
    }

    public static String containsPattern(String value) {
        String normalized = normalizeSearchText(value);
        return normalized.isBlank() ? "%" : "%" + escapeLike(normalized) + "%";
    }

    public static String startsWithPattern(String value) {
        String normalized = normalizeSearchText(value);
        return normalized.isBlank() ? "%" : escapeLike(normalized) + "%";
    }

    public static String endsWithPattern(String value) {
        String normalized = normalizeSearchText(value);
        return normalized.isBlank() ? "%" : "%" + escapeLike(normalized);
    }

    public static List<String> tokens(String value) {
        String normalized = normalizeSearchText(value);

        if (normalized.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(StringNormalizer::hasText)
                .distinct()
                .toList();
    }

    public static boolean containsLoose(String source, String search) {
        return StringNormalizer.containsNormalized(source, search);
    }

    public static String escapeLike(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }
}