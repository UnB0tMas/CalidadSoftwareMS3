// ruta: src/main/java/com/upsjb/ms3/util/PercentageUtil.java
package com.upsjb.ms3.util;

import java.math.BigDecimal;

public final class PercentageUtil {

    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private PercentageUtil() {
    }

    public static BigDecimal normalize(BigDecimal percentage) {
        return BigDecimalUtil.scale(percentage);
    }

    public static boolean isValidPercentage(BigDecimal percentage) {
        return percentage != null
                && percentage.compareTo(BigDecimal.ZERO) >= 0
                && percentage.compareTo(ONE_HUNDRED) <= 0;
    }

    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            return BigDecimalUtil.ZERO;
        }

        return BigDecimalUtil.divide(amount.multiply(percentage), ONE_HUNDRED);
    }

    public static BigDecimal applyDiscount(BigDecimal amount, BigDecimal percentage) {
        if (!isValidPercentage(percentage)) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }

        BigDecimal discount = percentOf(amount, percentage);
        BigDecimal result = BigDecimalUtil.zeroIfNull(amount).subtract(discount);

        return BigDecimalUtil.max(result, BigDecimal.ZERO);
    }

    public static BigDecimal addPercentage(BigDecimal amount, BigDecimal percentage) {
        if (!isValidPercentage(percentage)) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }

        BigDecimal increment = percentOf(amount, percentage);
        return BigDecimalUtil.add(amount, increment);
    }

    public static BigDecimal rate(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimalUtil.ZERO;
        }

        return BigDecimalUtil.divide(part.multiply(ONE_HUNDRED), total);
    }
}