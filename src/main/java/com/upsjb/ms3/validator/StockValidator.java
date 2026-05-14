// ruta: src/main/java/com/upsjb/ms3/validator/StockValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.util.StockMathUtil;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockValidator {

    public void validateCreate(ProductoSku sku, Almacen almacen, Integer stockFisico, Integer stockReservado) {
        if (sku == null || !sku.isActivo()) {
            throw new ConflictException(
                    "SKU_INVALIDO_PARA_STOCK",
                    "El SKU debe existir y estar activo para controlar stock."
            );
        }

        if (almacen == null || !almacen.isActivo()) {
            throw new ConflictException(
                    "ALMACEN_INVALIDO_PARA_STOCK",
                    "El almacén debe existir y estar activo para controlar stock."
            );
        }

        StockMathUtil.requireConsistentStock(stockFisico, stockReservado);
    }

    public void validateStockState(StockSku stock) {
        requireExists(stock);
        StockMathUtil.requireConsistentStock(stock.getStockFisico(), stock.getStockReservado());
    }

    public void validateAvailableStock(StockSku stock, Integer requestedQuantity) {
        validateStockState(stock);

        if (!StockMathUtil.hasAvailable(stock.getStockFisico(), stock.getStockReservado(), requestedQuantity)) {
            throw new ConflictException(
                    "STOCK_DISPONIBLE_INSUFICIENTE",
                    "El stock disponible es insuficiente para completar la operación."
            );
        }
    }

    public void validateEntry(Integer quantity, BigDecimal unitCost) {
        StockMathUtil.requirePositiveQuantity(quantity);

        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException(
                    "COSTO_UNITARIO_INVALIDO",
                    "El costo unitario no puede ser negativo."
            );
        }
    }

    public void validateOutput(StockSku stock, Integer quantity) {
        validateAvailableStock(stock, quantity);
    }

    public void validateReserve(StockSku stock, Integer quantity) {
        validateAvailableStock(stock, quantity);
    }

    public void validateReleaseReserved(StockSku stock, Integer quantity) {
        validateStockState(stock);

        int requested = StockMathUtil.requirePositiveQuantity(quantity);

        if (StockMathUtil.zeroIfNull(stock.getStockReservado()) < requested) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INSUFICIENTE",
                    "El stock reservado es insuficiente para liberar la cantidad solicitada."
            );
        }
    }

    public void validateConfirmReservation(StockSku stock, Integer quantity) {
        validateReleaseReserved(stock, quantity);
    }

    public void requireExists(StockSku stock) {
        if (stock == null) {
            throw new NotFoundException(
                    "STOCK_SKU_NO_ENCONTRADO",
                    "Stock de SKU no encontrado."
            );
        }
    }
}