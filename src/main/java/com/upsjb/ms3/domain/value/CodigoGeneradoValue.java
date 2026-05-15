package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.util.Objects;

public record CodigoGeneradoValue(String value) implements Serializable {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    private static final String VALID_PATTERN = "^[A-Z0-9][A-Z0-9_-]{2,99}$";

    public CodigoGeneradoValue {
        value = normalize(value);

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El código generado debe tener entre 3 y 100 caracteres.");
        }

        if (!value.matches(VALID_PATTERN)) {
            throw new IllegalArgumentException("El código generado solo puede contener letras, números, guion y guion bajo.");
        }
    }

    public static CodigoGeneradoValue of(String value) {
        return new CodigoGeneradoValue(value);
    }

    public static CodigoGeneradoValue fromParts(String prefix, long number, int length) {
        String normalizedPrefix = normalizePrefix(prefix);

        if (number < 0) {
            throw new IllegalArgumentException("El número correlativo no puede ser negativo.");
        }

        if (length < 3 || length > 12) {
            throw new IllegalArgumentException("La longitud del correlativo debe estar entre 3 y 12.");
        }

        String formattedNumber = String.format("%0" + length + "d", number);
        return new CodigoGeneradoValue(normalizedPrefix + "-" + formattedNumber);
    }

    public String raw() {
        return value;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El código generado es obligatorio.");
        }

        return value.trim()
                .replaceAll("\\s+", "-")
                .toUpperCase();
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("El prefijo del código es obligatorio.");
        }

        String normalized = prefix.trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();

        if (!normalized.matches("^[A-Z0-9][A-Z0-9_-]{1,29}$")) {
            throw new IllegalArgumentException("El prefijo del código no tiene un formato válido.");
        }

        return normalized;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CodigoGeneradoValue that)) {
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