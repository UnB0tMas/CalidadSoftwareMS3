package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.util.Objects;

public record CloudinaryPublicIdValue(String value) implements Serializable {

    private static final int MAX_LENGTH = 300;
    private static final String VALID_PATTERN = "^[A-Za-z0-9_./-]+$";

    public CloudinaryPublicIdValue {
        value = normalize(value);

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El public_id de Cloudinary no puede superar 300 caracteres.");
        }

        if (!value.matches(VALID_PATTERN)) {
            throw new IllegalArgumentException("El public_id de Cloudinary contiene caracteres no permitidos.");
        }

        if (value.startsWith("/") || value.endsWith("/")) {
            throw new IllegalArgumentException("El public_id de Cloudinary no debe iniciar ni terminar con '/'.");
        }

        if (value.contains("..")) {
            throw new IllegalArgumentException("El public_id de Cloudinary no debe contener secuencias '..'.");
        }
    }

    public static CloudinaryPublicIdValue of(String value) {
        return new CloudinaryPublicIdValue(value);
    }

    public String raw() {
        return value;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El public_id de Cloudinary es obligatorio.");
        }

        return value.trim();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CloudinaryPublicIdValue that)) {
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