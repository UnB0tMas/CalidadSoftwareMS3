package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoDocumentoProveedor {

    DNI("DNI", "Documento Nacional de Identidad", 8, 8, true),
    CARNET_EXTRANJERIA("CARNET_EXTRANJERIA", "Carnet de extranjería", 6, 20, false),
    PASAPORTE("PASAPORTE", "Pasaporte", 6, 20, false),
    OTRO("OTRO", "Otro documento", 3, 30, false);

    private final String code;
    private final String label;
    private final int minLength;
    private final int maxLength;
    private final boolean soloNumeros;

    TipoDocumentoProveedor(String code, String label, int minLength, int maxLength, boolean soloNumeros) {
        this.code = code;
        this.label = label;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.soloNumeros = soloNumeros;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isSoloNumeros() {
        return soloNumeros;
    }

    public static TipoDocumentoProveedor fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de documento del proveedor es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento de proveedor no válido: " + code));
    }
}