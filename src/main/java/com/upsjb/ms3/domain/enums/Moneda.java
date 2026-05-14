package com.upsjb.ms3.domain.enums;

import java.util.Arrays;
import java.util.Currency;

public enum Moneda {

    PEN("PEN", "Sol peruano", "S/"),
    USD("USD", "Dólar estadounidense", "$");

    private final String code;
    private final String label;
    private final String symbol;

    Moneda(String code, String label, String symbol) {
        this.code = code;
        this.label = label;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public Currency toCurrency() {
        return Currency.getInstance(code);
    }

    public static Moneda fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Moneda no válida: " + code));
    }
}