package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoProductoPublicacion {

    NO_PUBLICADO("NO_PUBLICADO", "No publicado", false),
    PUBLICADO("PUBLICADO", "Publicado", true),
    PROGRAMADO("PROGRAMADO", "Programado", true),
    OCULTO("OCULTO", "Oculto", false);

    private final String code;
    private final String label;
    private final boolean visibleEnFlujoPublico;

    EstadoProductoPublicacion(String code, String label, boolean visibleEnFlujoPublico) {
        this.code = code;
        this.label = label;
        this.visibleEnFlujoPublico = visibleEnFlujoPublico;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisibleEnFlujoPublico() {
        return visibleEnFlujoPublico;
    }

    public boolean isPublicado() {
        return this == PUBLICADO;
    }

    public boolean isProgramado() {
        return this == PROGRAMADO;
    }

    public boolean isOculto() {
        return this == OCULTO;
    }

    public boolean isNoPublicado() {
        return this == NO_PUBLICADO;
    }

    public static EstadoProductoPublicacion fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de publicación del producto es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de publicación del producto no válido: " + code));
    }
}