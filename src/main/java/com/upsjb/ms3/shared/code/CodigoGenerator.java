package com.upsjb.ms3.shared.code;

import org.springframework.stereotype.Component;

@Component
public class CodigoGenerator {

    public String format(String prefix, Long sequence) {
        return format(CodigoFormat.of(prefix), sequence);
    }

    public String format(String prefix, int length, Long sequence) {
        return format(CodigoFormat.of(prefix, length), sequence);
    }

    public String format(CodigoFormat format, Long sequence) {
        if (format == null) {
            throw new IllegalArgumentException("El formato de código es obligatorio.");
        }

        if (sequence == null || sequence < 1) {
            throw new IllegalArgumentException("La secuencia del código debe ser mayor a cero.");
        }

        String number = String.format("%0" + format.length() + "d", sequence);
        return format.prefix() + format.separator() + number;
    }
}