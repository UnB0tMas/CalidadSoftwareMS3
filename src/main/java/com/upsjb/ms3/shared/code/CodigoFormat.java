package com.upsjb.ms3.shared.code;

import org.springframework.util.StringUtils;

public record CodigoFormat(
        String prefix,
        int length,
        String separator
) {

    private static final int DEFAULT_LENGTH = 6;
    private static final String DEFAULT_SEPARATOR = "-";

    public CodigoFormat {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalArgumentException("El prefijo del código es obligatorio.");
        }

        prefix = prefix.trim().toUpperCase();

        if (length < 1) {
            length = DEFAULT_LENGTH;
        }

        separator = separator == null ? DEFAULT_SEPARATOR : separator;
    }

    public static CodigoFormat of(String prefix) {
        return new CodigoFormat(prefix, DEFAULT_LENGTH, DEFAULT_SEPARATOR);
    }

    public static CodigoFormat of(String prefix, int length) {
        return new CodigoFormat(prefix, length, DEFAULT_SEPARATOR);
    }
}