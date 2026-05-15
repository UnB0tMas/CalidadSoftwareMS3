package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoDatoAtributo {

    TEXTO("TEXTO", "Texto"),
    NUMERO("NUMERO", "Número entero"),
    DECIMAL("DECIMAL", "Número decimal"),
    BOOLEANO("BOOLEANO", "Verdadero/Falso"),
    FECHA("FECHA", "Fecha");

    private final String code;
    private final String label;

    TipoDatoAtributo(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTexto() {
        return this == TEXTO;
    }

    public boolean isNumero() {
        return this == NUMERO;
    }

    public boolean isDecimal() {
        return this == DECIMAL;
    }

    public boolean isBooleano() {
        return this == BOOLEANO;
    }

    public boolean isFecha() {
        return this == FECHA;
    }

    public static TipoDatoAtributo fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de dato del atributo es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de dato de atributo no válido: " + code));
    }
}