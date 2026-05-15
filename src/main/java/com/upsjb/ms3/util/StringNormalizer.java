// ruta: src/main/java/com/upsjb/ms3/util/StringNormalizer.java
package com.upsjb.ms3.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class StringNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]+");

    private StringNormalizer() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String clean(String value) {
        if (value == null) {
            return "";
        }
        return normalizeSpaces(value);
    }

    public static String cleanOrNull(String value) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? null : cleaned;
    }

    public static String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return MULTIPLE_SPACES.matcher(value.trim()).replaceAll(" ");
    }

    public static String removeAccents(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("");
    }

    public static String lower(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    public static String upper(String value) {
        return clean(value).toUpperCase(Locale.ROOT);
    }

    public static String lowerWithoutAccents(String value) {
        return removeAccents(clean(value)).toLowerCase(Locale.ROOT);
    }

    public static String upperWithoutAccents(String value) {
        return removeAccents(clean(value)).toUpperCase(Locale.ROOT);
    }

    public static String normalizeForSearch(String value) {
        return lowerWithoutAccents(value);
    }

    public static String normalizeForCode(String value) {
        String normalized = upperWithoutAccents(value);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        normalized = normalized.replaceAll("^-+", "").replaceAll("-+$", "");
        return normalized;
    }

    public static String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D+", "");
    }

    public static String onlyAlphanumericUpper(String value) {
        if (value == null) {
            return "";
        }
        return removeAccents(value)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "");
    }

    public static boolean equalsNormalized(String left, String right) {
        return Objects.equals(normalizeForSearch(left), normalizeForSearch(right));
    }

    public static boolean containsNormalized(String source, String fragment) {
        if (!hasText(source) || !hasText(fragment)) {
            return false;
        }
        return normalizeForSearch(source).contains(normalizeForSearch(fragment));
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (maxLength <= 0) {
            return "";
        }

        String cleaned = clean(value);
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    public static String truncateOrNull(String value, int maxLength) {
        String truncated = truncate(value, maxLength);
        return truncated.isBlank() ? null : truncated;
    }
}