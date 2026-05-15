// ruta: src/main/java/com/upsjb/ms3/validator/StockValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.StockMathUtil;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockValidator {

    public void validateCreate(ProductoSku sku, Almacen almacen, Integer stockFisico, Integer stockReservado) {
        validateSkuAndWarehouseForStock(sku, almacen);
        validateConsistentStock(stockFisico, stockReservado);
    }

    public void validateStockState(StockSku stock) {
        requireExists(stock);
        validateConsistentStock(stock.getStockFisico(), stock.getStockReservado());
    }

    public void validateAvailabilityTarget(ProductoSku sku, Almacen almacen) {
        validateSkuAndWarehouseForStock(sku, almacen);
    }

    public void validateAvailableQuery(Integer requestedQuantity) {
        if (requestedQuantity == null) {
            return;
        }

        if (requestedQuantity <= 0) {
            throw new ValidationException(
                    "CANTIDAD_SOLICITADA_INVALIDA",
                    "La cantidad solicitada debe ser mayor a cero."
            );
        }
    }

    public void validateRequiredAvailableQuery(Integer requestedQuantity) {
        if (requestedQuantity == null) {
            throw new ValidationException(
                    "CANTIDAD_SOLICITADA_REQUERIDA",
                    "Debe indicar la cantidad solicitada."
            );
        }

        validateAvailableQuery(requestedQuantity);
    }

    public void validateAvailableStock(StockSku stock, Integer requestedQuantity) {
        validateStockState(stock);
        validateRequiredAvailableQuery(requestedQuantity);

        if (!StockMathUtil.hasAvailable(stock.getStockFisico(), stock.getStockReservado(), requestedQuantity)) {
            throw new ConflictException(
                    "STOCK_DISPONIBLE_INSUFICIENTE",
                    "No se puede completar la operación porque el stock disponible es insuficiente."
            );
        }
    }

    public void validateEntry(Integer quantity, BigDecimal unitCost) {
        validatePositiveQuantity(quantity);

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

        int requested = validatePositiveQuantity(quantity);

        if (StockMathUtil.zeroIfNull(stock.getStockReservado()) < requested) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INSUFICIENTE",
                    "El stock reservado es insuficiente para liberar la cantidad solicitada."
            );
        }
    }

    public void validateConfirmReservation(StockSku stock, Integer quantity) {
        validateReleaseReserved(stock, quantity);

        int requested = StockMathUtil.zeroIfNull(quantity);

        if (StockMathUtil.zeroIfNull(stock.getStockFisico()) < requested) {
            throw new ConflictException(
                    "STOCK_FISICO_INSUFICIENTE",
                    "No se puede confirmar la reserva porque el stock físico es insuficiente."
            );
        }
    }

    public void requireExists(StockSku stock) {
        if (stock == null) {
            throw new NotFoundException(
                    "STOCK_SKU_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void validateSkuAndWarehouseForStock(ProductoSku sku, Almacen almacen) {
        if (sku == null || !sku.isActivo() || sku.getEstadoSku() == null || !sku.getEstadoSku().isOperativo()) {
            throw new ConflictException(
                    "SKU_INVALIDO_PARA_STOCK",
                    "El SKU debe existir, estar activo y ser operativo para controlar stock."
            );
        }

        if (sku.getProducto() == null || !sku.getProducto().isActivo()) {
            throw new ConflictException(
                    "PRODUCTO_INVALIDO_PARA_STOCK",
                    "El producto del SKU debe existir y estar activo para controlar stock."
            );
        }

        if (almacen == null || !almacen.isActivo()) {
            throw new ConflictException(
                    "ALMACEN_INVALIDO_PARA_STOCK",
                    "El almacén debe existir y estar activo para controlar stock."
            );
        }
    }

    private int validatePositiveQuantity(Integer quantity) {
        int value = StockMathUtil.zeroIfNull(quantity);

        if (value <= 0) {
            throw new ValidationException(
                    "CANTIDAD_INVALIDA",
                    "La cantidad debe ser mayor a cero."
            );
        }

        return value;
    }

    private void validateConsistentStock(Integer stockFisico, Integer stockReservado) {
        int physical = StockMathUtil.zeroIfNull(stockFisico);
        int reserved = StockMathUtil.zeroIfNull(stockReservado);

        if (physical < 0) {
            throw new ConflictException(
                    "STOCK_FISICO_NEGATIVO",
                    "El stock físico no puede ser negativo."
            );
        }

        if (reserved < 0) {
            throw new ConflictException(
                    "STOCK_RESERVADO_NEGATIVO",
                    "El stock reservado no puede ser negativo."
            );
        }

        if (reserved > physical) {
            throw new ConflictException(
                    "STOCK_RESERVADO_SUPERA_FISICO",
                    "El stock reservado no puede superar el stock físico."
            );
        }
    }
}