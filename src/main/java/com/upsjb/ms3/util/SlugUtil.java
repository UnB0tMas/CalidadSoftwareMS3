// ruta: src/main/java/com/upsjb/ms3/util/SlugUtil.java
package com.upsjb.ms3.util;

import java.util.function.Predicate;

public final class SlugUtil {

    private static final int DEFAULT_MAX_LENGTH = 240;
    private static final String DEFAULT_FALLBACK = "item";

    private SlugUtil() {
    }

    public static String toSlug(String value) {
        return toSlug(value, DEFAULT_MAX_LENGTH);
    }

    public static String toSlug(String value, int maxLength) {
        String base = StringNormalizer.lowerWithoutAccents(value)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (base.isBlank()) {
            base = DEFAULT_FALLBACK;
        }

        return trimSlug(base, maxLength);
    }

    public static String uniqueSlug(String value, Predicate<String> existsPredicate) {
        return uniqueSlug(value, existsPredicate, DEFAULT_MAX_LENGTH);
    }

    public static String uniqueSlug(String value, Predicate<String> existsPredicate, int maxLength) {
        String base = toSlug(value, maxLength);

        if (existsPredicate == null || !existsPredicate.test(base)) {
            return base;
        }

        int counter = 2;
        String candidate;

        do {
            String suffix = "-" + counter;
            String trimmedBase = trimSlug(base, maxLength - suffix.length());
            candidate = trimmedBase + suffix;
            counter++;
        } while (existsPredicate.test(candidate));

        return candidate;
    }

    public static boolean isValidSlug(String value) {
        if (!StringNormalizer.hasText(value)) {
            return false;
        }
        return value.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }

    private static String trimSlug(String value, int maxLength) {
        int safeMaxLength = maxLength <= 0 ? DEFAULT_MAX_LENGTH : maxLength;

        if (value.length() <= safeMaxLength) {
            return value;
        }

        return value.substring(0, safeMaxLength)
                .replaceAll("-+$", "");
    }
}