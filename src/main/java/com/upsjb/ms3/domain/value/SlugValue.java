package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

public record SlugValue(String value) implements Serializable {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 240;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public SlugValue {
        value = normalizeSlug(value);

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El slug debe tener entre 3 y 240 caracteres.");
        }

        if (!value.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("El slug no tiene un formato válido.");
        }
    }

    public static SlugValue of(String value) {
        return new SlugValue(value);
    }

    public static SlugValue fromName(String name) {
        return new SlugValue(toSlug(name));
    }

    public SlugValue withSuffix(long suffix) {
        if (suffix <= 1) {
            return this;
        }

        String suffixText = "-" + suffix;
        String base = value;

        if (base.length() + suffixText.length() > MAX_LENGTH) {
            base = base.substring(0, MAX_LENGTH - suffixText.length());
            base = trimTrailingHyphen(base);
        }

        return new SlugValue(base + suffixText);
    }

    public String raw() {
        return value;
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("El texto para generar slug es obligatorio.");
        }

        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("-{2,}", "-");
        normalized = trimHyphens(normalized);

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("No se pudo generar un slug válido.");
        }

        if (normalized.length() > MAX_LENGTH) {
            normalized = normalized.substring(0, MAX_LENGTH);
            normalized = trimTrailingHyphen(normalized);
        }

        return normalized;
    }

    private static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("El slug es obligatorio.");
        }

        return toSlug(slug);
    }

    private static String trimHyphens(String value) {
        String result = value;
        while (result.startsWith("-")) {
            result = result.substring(1);
        }
        while (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String trimTrailingHyphen(String value) {
        String result = value;
        while (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SlugValue that)) {
            return false;
        }

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}