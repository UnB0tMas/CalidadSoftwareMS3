package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record PorcentajeValue(BigDecimal value) implements Serializable {

    private static final int SCALE = 4;
    private static final BigDecimal CIEN = new BigDecimal("100.0000");

    public PorcentajeValue {
        if (value == null) {
            throw new IllegalArgumentException("El porcentaje es obligatorio.");
        }

        value = value.setScale(SCALE, RoundingMode.HALF_UP);

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El porcentaje debe ser mayor a cero.");
        }

        if (value.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("El porcentaje no puede ser mayor a 100.");
        }
    }

    public static PorcentajeValue of(BigDecimal value) {
        return new PorcentajeValue(value);
    }

    public static PorcentajeValue of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El porcentaje es obligatorio.");
        }

        return new PorcentajeValue(new BigDecimal(value.trim()));
    }

    public BigDecimal asDecimalFactor() {
        return value.divide(CIEN, SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularDescuento(BigDecimal base) {
        validarBase(base);
        return base.multiply(asDecimalFactor()).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal aplicarDescuento(BigDecimal base) {
        validarBase(base);

        BigDecimal descuento = calcularDescuento(base);
        BigDecimal result = base.subtract(descuento);

        if (result.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }

        return result.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public String formatted() {
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    private static void validarBase(BigDecimal base) {
        if (base == null) {
            throw new IllegalArgumentException("El valor base es obligatorio.");
        }

        if (base.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor base no puede ser negativo.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PorcentajeValue that)) {
            return false;
        }

        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return formatted();
    }
}