// ruta: src/main/java/com/upsjb/ms3/util/BigDecimalUtil.java
package com.upsjb.ms3.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BigDecimalUtil {

    public static final int DEFAULT_SCALE = 4;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);

    private BigDecimalUtil() {
    }

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    public static BigDecimal scale(BigDecimal value) {
        return scale(value, DEFAULT_SCALE);
    }

    public static BigDecimal scale(BigDecimal value, int scale) {
        return zeroIfNull(value).setScale(scale, DEFAULT_ROUNDING);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return scale(zeroIfNull(left).add(zeroIfNull(right)));
    }

    public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return scale(zeroIfNull(left).subtract(zeroIfNull(right)));
    }

    public static BigDecimal multiply(BigDecimal left, BigDecimal right) {
        return scale(zeroIfNull(left).multiply(zeroIfNull(right)));
    }

    public static BigDecimal multiply(BigDecimal left, Integer right) {
        return multiply(left, right == null ? BigDecimal.ZERO : BigDecimal.valueOf(right));
    }

    public static BigDecimal divide(BigDecimal left, BigDecimal right) {
        if (right == null || BigDecimal.ZERO.compareTo(right) == 0) {
            throw new IllegalArgumentException("No se puede dividir entre cero.");
        }

        return zeroIfNull(left).divide(right, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static boolean isZero(BigDecimal value) {
        return zeroIfNull(value).compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    public static BigDecimal min(BigDecimal left, BigDecimal right) {
        if (left == null) {
            return scale(right);
        }
        if (right == null) {
            return scale(left);
        }
        return scale(left.min(right));
    }

    public static BigDecimal max(BigDecimal left, BigDecimal right) {
        if (left == null) {
            return scale(right);
        }
        if (right == null) {
            return scale(left);
        }
        return scale(left.max(right));
    }
}