// ruta: src/main/java/com/upsjb/ms3/util/MoneyUtil.java
package com.upsjb.ms3.util;

import java.math.BigDecimal;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        return BigDecimalUtil.scale(amount);
    }

    public static BigDecimal normalizeNullable(BigDecimal amount) {
        return amount == null ? null : BigDecimalUtil.scale(amount);
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return BigDecimalUtil.isNonNegative(amount);
    }

    public static boolean isValidPositiveAmount(BigDecimal amount) {
        return BigDecimalUtil.isPositive(amount);
    }

    public static BigDecimal subtotal(Integer quantity, BigDecimal unitPrice) {
        int safeQuantity = quantity == null ? 0 : quantity;

        if (safeQuantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        return BigDecimalUtil.multiply(unitPrice, safeQuantity);
    }

    public static BigDecimal totalLine(Integer quantity, BigDecimal unitPrice, BigDecimal discount, BigDecimal tax) {
        BigDecimal subtotal = subtotal(quantity, unitPrice);
        BigDecimal safeDiscount = BigDecimalUtil.zeroIfNull(discount);
        BigDecimal safeTax = BigDecimalUtil.zeroIfNull(tax);

        BigDecimal total = subtotal
                .subtract(safeDiscount)
                .add(safeTax);

        return BigDecimalUtil.scale(total);
    }

    public static BigDecimal applyDiscountAmount(BigDecimal amount, BigDecimal discountAmount) {
        BigDecimal result = BigDecimalUtil.zeroIfNull(amount).subtract(BigDecimalUtil.zeroIfNull(discountAmount));
        return BigDecimalUtil.max(result, BigDecimal.ZERO);
    }

    public static BigDecimal applyTaxAmount(BigDecimal amount, BigDecimal taxAmount) {
        return BigDecimalUtil.add(amount, taxAmount);
    }

    public static BigDecimal applyPercentageDiscount(BigDecimal amount, BigDecimal percentage) {
        return PercentageUtil.applyDiscount(amount, percentage);
    }

    public static BigDecimal taxFromPercentage(BigDecimal amount, BigDecimal percentage) {
        return PercentageUtil.percentOf(amount, percentage);
    }

    public static BigDecimal unitCostFromTotal(BigDecimal total, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero para calcular costo unitario.");
        }

        return BigDecimalUtil.divide(total, BigDecimal.valueOf(quantity));
    }
}