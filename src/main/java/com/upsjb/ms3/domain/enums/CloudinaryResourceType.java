package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum CloudinaryResourceType {

    IMAGE("image", "Imagen"),
    VIDEO("video", "Video"),
    RAW("raw", "Archivo raw");

    private final String code;
    private final String label;

    CloudinaryResourceType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public static CloudinaryResourceType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El resource type de Cloudinary es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource type de Cloudinary no válido: " + code));
    }
}