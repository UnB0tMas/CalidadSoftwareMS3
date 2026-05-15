package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

public record NombreNormalizadoValue(
        String original,
        String normalized
) implements Serializable {

    private static final int MAX_LENGTH = 250;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public NombreNormalizadoValue {
        original = normalizeOriginal(original);
        normalized = normalizeForSearch(original);

        if (original.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El nombre no puede superar 250 caracteres.");
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El nombre normalizado no puede estar vacío.");
        }
    }

    public static NombreNormalizadoValue of(String value) {
        return new NombreNormalizadoValue(value, null);
    }

    public String raw() {
        return original;
    }

    public String searchValue() {
        return normalized;
    }

    private static String normalizeOriginal(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeForSearch(String value) {
        String normalizedText = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalizedText = DIACRITICS.matcher(normalizedText).replaceAll("");
        normalizedText = normalizedText.toUpperCase();
        normalizedText = normalizedText.replaceAll("[^A-Z0-9 ]", " ");
        normalizedText = normalizedText.replaceAll("\\s+", " ").trim();
        return normalizedText;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof NombreNormalizadoValue that)) {
            return false;
        }

        return normalized.equals(that.normalized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalized);
    }

    @Override
    public String toString() {
        return original;
    }
}