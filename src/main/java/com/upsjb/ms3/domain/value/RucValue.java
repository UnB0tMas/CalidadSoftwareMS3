package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.util.Objects;

public record RucValue(String value) implements Serializable {

    private static final int LENGTH = 11;

    public RucValue {
        value = normalize(value);

        if (!value.matches("^\\d{11}$")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos.");
        }

        if (!startsWithValidPrefix(value)) {
            throw new IllegalArgumentException("El RUC no tiene un prefijo válido.");
        }
    }

    public static RucValue of(String value) {
        return new RucValue(value);
    }

    public String raw() {
        return value;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El RUC es obligatorio.");
        }

        return value.trim().replaceAll("\\s+", "");
    }

    private static boolean startsWithValidPrefix(String value) {
        if (value.length() != LENGTH) {
            return false;
        }

        return value.startsWith("10")
                || value.startsWith("15")
                || value.startsWith("17")
                || value.startsWith("20");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof RucValue that)) {
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