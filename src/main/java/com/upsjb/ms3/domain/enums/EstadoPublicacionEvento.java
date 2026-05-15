package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoPublicacionEvento {

    PENDIENTE("PENDIENTE", "Pendiente", true),
    PUBLICADO("PUBLICADO", "Publicado", false),
    ERROR("ERROR", "Error", true);

    private final String code;
    private final String label;
    private final boolean reintentable;

    EstadoPublicacionEvento(String code, String label, boolean reintentable) {
        this.code = code;
        this.label = label;
        this.reintentable = reintentable;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReintentable() {
        return reintentable;
    }

    public boolean isPendiente() {
        return this == PENDIENTE;
    }

    public boolean isPublicado() {
        return this == PUBLICADO;
    }

    public boolean isError() {
        return this == ERROR;
    }

    public static EstadoPublicacionEvento fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de publicación del evento es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de publicación de evento no válido: " + code));
    }
}