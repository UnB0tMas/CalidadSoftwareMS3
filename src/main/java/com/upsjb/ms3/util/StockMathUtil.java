// ruta: src/main/java/com/upsjb/ms3/util/StockMathUtil.java
package com.upsjb.ms3.util;

import java.math.BigDecimal;

public final class StockMathUtil {

    private StockMathUtil() {
    }

    public static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    public static int available(Integer physicalStock, Integer reservedStock) {
        return zeroIfNull(physicalStock) - zeroIfNull(reservedStock);
    }

    public static boolean hasAvailable(Integer physicalStock, Integer reservedStock, Integer requestedQuantity) {
        int requested = zeroIfNull(requestedQuantity);

        if (requested <= 0) {
            return false;
        }

        return available(physicalStock, reservedStock) >= requested;
    }

    public static int reserve(Integer currentReservedStock, Integer quantity) {
        int quantityValue = requirePositiveQuantity(quantity);
        return zeroIfNull(currentReservedStock) + quantityValue;
    }

    public static int releaseReserved(Integer currentReservedStock, Integer quantity) {
        int quantityValue = requirePositiveQuantity(quantity);
        int result = zeroIfNull(currentReservedStock) - quantityValue;

        if (result < 0) {
            throw new IllegalArgumentException("El stock reservado no puede quedar negativo.");
        }

        return result;
    }

    public static int confirmReservationPhysical(Integer currentPhysicalStock, Integer quantity) {
        int quantityValue = requirePositiveQuantity(quantity);
        int result = zeroIfNull(currentPhysicalStock) - quantityValue;

        if (result < 0) {
            throw new IllegalArgumentException("El stock físico no puede quedar negativo.");
        }

        return result;
    }

    public static int applyEntry(Integer currentPhysicalStock, Integer quantity) {
        int quantityValue = requirePositiveQuantity(quantity);
        return zeroIfNull(currentPhysicalStock) + quantityValue;
    }

    public static int applyOutput(Integer currentPhysicalStock, Integer quantity) {
        int quantityValue = requirePositiveQuantity(quantity);
        int result = zeroIfNull(currentPhysicalStock) - quantityValue;

        if (result < 0) {
            throw new IllegalArgumentException("El stock físico no puede quedar negativo.");
        }

        return result;
    }

    public static boolean isLowStock(Integer availableStock, Integer minimumStock) {
        int minimum = zeroIfNull(minimumStock);

        if (minimum <= 0) {
            return false;
        }

        return zeroIfNull(availableStock) <= minimum;
    }

    public static BigDecimal weightedAverageCost(
            Integer currentQuantity,
            BigDecimal currentAverageCost,
            Integer incomingQuantity,
            BigDecimal incomingUnitCost
    ) {
        int currentQty = zeroIfNull(currentQuantity);
        int incomingQty = requirePositiveQuantity(incomingQuantity);

        BigDecimal currentCost = BigDecimalUtil.zeroIfNull(currentAverageCost);
        BigDecimal incomingCost = BigDecimalUtil.zeroIfNull(incomingUnitCost);

        if (currentQty <= 0) {
            return BigDecimalUtil.scale(incomingCost);
        }

        BigDecimal currentTotal = currentCost.multiply(BigDecimal.valueOf(currentQty));
        BigDecimal incomingTotal = incomingCost.multiply(BigDecimal.valueOf(incomingQty));
        BigDecimal totalQuantity = BigDecimal.valueOf(currentQty + incomingQty);

        return BigDecimalUtil.divide(currentTotal.add(incomingTotal), totalQuantity);
    }

    public static int requirePositiveQuantity(Integer quantity) {
        int value = zeroIfNull(quantity);

        if (value <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        return value;
    }

    public static void requireConsistentStock(Integer physicalStock, Integer reservedStock) {
        int physical = zeroIfNull(physicalStock);
        int reserved = zeroIfNull(reservedStock);

        if (physical < 0) {
            throw new IllegalArgumentException("El stock físico no puede ser negativo.");
        }

        if (reserved < 0) {
            throw new IllegalArgumentException("El stock reservado no puede ser negativo.");
        }

        if (reserved > physical) {
            throw new IllegalArgumentException("El stock reservado no puede superar el stock físico.");
        }
    }
}