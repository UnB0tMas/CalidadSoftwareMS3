package com.upsjb.ms3.domain.value;

import com.upsjb.ms3.domain.enums.Moneda;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record MoneyValue(BigDecimal amount, Moneda moneda) implements Serializable {

    private static final int SCALE = 4;

    public MoneyValue {
        if (amount == null) {
            throw new IllegalArgumentException("El monto es obligatorio.");
        }

        if (moneda == null) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }

        amount = amount.setScale(SCALE, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo.");
        }
    }

    public static MoneyValue of(BigDecimal amount, Moneda moneda) {
        return new MoneyValue(amount, moneda);
    }

    public static MoneyValue of(String amount, Moneda moneda) {
        if (amount == null || amount.isBlank()) {
            throw new IllegalArgumentException("El monto es obligatorio.");
        }

        return new MoneyValue(new BigDecimal(amount.trim()), moneda);
    }

    public static MoneyValue zero(Moneda moneda) {
        return new MoneyValue(BigDecimal.ZERO, moneda);
    }

    public MoneyValue requirePositive() {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }

        return this;
    }

    public MoneyValue add(MoneyValue other) {
        ensureSameCurrency(other);
        return new MoneyValue(amount.add(other.amount), moneda);
    }

    public MoneyValue subtract(MoneyValue other) {
        ensureSameCurrency(other);

        BigDecimal result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El resultado monetario no puede ser negativo.");
        }

        return new MoneyValue(result, moneda);
    }

    public MoneyValue multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("El multiplicador es obligatorio.");
        }

        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El multiplicador no puede ser negativo.");
        }

        return new MoneyValue(amount.multiply(multiplier), moneda);
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String formatted() {
        return moneda.getSymbol() + " " + amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureSameCurrency(MoneyValue other) {
        if (other == null) {
            throw new IllegalArgumentException("El valor monetario comparado es obligatorio.");
        }

        if (moneda != other.moneda) {
            throw new IllegalArgumentException("No se pueden operar montos con monedas diferentes.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof MoneyValue that)) {
            return false;
        }

        return amount.compareTo(that.amount) == 0 && moneda == that.moneda;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), moneda);
    }

    @Override
    public String toString() {
        return formatted();
    }
}