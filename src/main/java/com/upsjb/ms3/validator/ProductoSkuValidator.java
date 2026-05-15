// ruta: src/main/java/com/upsjb/ms3/validator/ProductoSkuValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ProductoSkuValidator {

    public void validateCreate(
            Producto producto,
            String codigoSku,
            String barcode,
            Integer stockMinimo,
            Integer stockMaximo,
            BigDecimal pesoGramos,
            BigDecimal altoCm,
            BigDecimal anchoCm,
            BigDecimal largoCm,
            boolean duplicatedCodigoSku,
            boolean duplicatedBarcode
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateProducto(producto, errors);
        validateCodigoSku(codigoSku, errors);
        validateStockRange(stockMinimo, stockMaximo, errors);
        validateDimensions(pesoGramos, altoCm, anchoCm, largoCm, errors);

        if (StringNormalizer.hasText(barcode) && StringNormalizer.clean(barcode).length() > 100) {
            errors.add("barcode", "El barcode no debe superar 100 caracteres.", "MAX_LENGTH", barcode);
        }

        errors.throwIfAny("No se puede crear el SKU.");

        requireNotDuplicated(duplicatedCodigoSku, "Ya existe un SKU activo con el mismo código.");
        requireNotDuplicated(duplicatedBarcode, "Ya existe un SKU activo con el mismo barcode.");
    }

    public void validateUpdate(
            ProductoSku sku,
            EstadoSku requestedEstadoSku,
            String barcode,
            Integer stockMinimo,
            Integer stockMaximo,
            BigDecimal pesoGramos,
            BigDecimal altoCm,
            BigDecimal anchoCm,
            BigDecimal largoCm,
            boolean duplicatedBarcode
    ) {
        requireActive(sku);

        validateStateUpdate(sku, requestedEstadoSku);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateStockRange(stockMinimo, stockMaximo, errors);
        validateDimensions(pesoGramos, altoCm, anchoCm, largoCm, errors);

        if (StringNormalizer.hasText(barcode) && StringNormalizer.clean(barcode).length() > 100) {
            errors.add("barcode", "El barcode no debe superar 100 caracteres.", "MAX_LENGTH", barcode);
        }

        errors.throwIfAny("No se puede actualizar el SKU.");

        requireNotDuplicated(duplicatedBarcode, "Ya existe otro SKU activo con el mismo barcode.");
    }

    public void validateUpdate(
            ProductoSku sku,
            String barcode,
            Integer stockMinimo,
            Integer stockMaximo,
            BigDecimal pesoGramos,
            BigDecimal altoCm,
            BigDecimal anchoCm,
            BigDecimal largoCm,
            boolean duplicatedBarcode
    ) {
        validateUpdate(
                sku,
                null,
                barcode,
                stockMinimo,
                stockMaximo,
                pesoGramos,
                altoCm,
                anchoCm,
                largoCm,
                duplicatedBarcode
        );
    }

    private void validateStateUpdate(ProductoSku sku, EstadoSku requestedEstadoSku) {
        EstadoSku currentEstadoSku = sku.getEstadoSku();

        if (currentEstadoSku == EstadoSku.DESCONTINUADO && requestedEstadoSku != EstadoSku.DESCONTINUADO) {
            throw new ConflictException(
                    "SKU_DESCONTINUADO_NO_EDITABLE",
                    "No se puede actualizar porque el estado actual no lo permite."
            );
        }

        if (requestedEstadoSku == null) {
            return;
        }

        if (requestedEstadoSku == EstadoSku.INACTIVO) {
            throw new ConflictException(
                    "SKU_INACTIVACION_REQUIERE_FLUJO",
                    "No se puede inactivar el SKU desde actualización. Use la operación de inactivación indicando motivo."
            );
        }
    }

    public void validateCanDeactivate(ProductoSku sku, boolean hasStock, boolean hasPendingReservations) {
        requireActive(sku);

        if (hasPendingReservations) {
            throw new ConflictException(
                    "SKU_CON_RESERVAS_PENDIENTES",
                    "No se puede inactivar el SKU porque tiene reservas pendientes."
            );
        }

        if (hasStock) {
            throw new ConflictException(
                    "SKU_CON_STOCK",
                    "No se puede inactivar el SKU porque todavía tiene stock registrado."
            );
        }
    }

    public void validateCanDiscontinue(ProductoSku sku, boolean hasPendingReservations) {
        requireActive(sku);

        if (hasPendingReservations) {
            throw new ConflictException(
                    "SKU_CON_RESERVAS_PENDIENTES",
                    "No se puede descontinuar el SKU porque tiene reservas pendientes."
            );
        }
    }

    public void validateCanSell(ProductoSku sku) {
        requireActive(sku);

        if (sku.getEstadoSku() != EstadoSku.ACTIVO) {
            throw new ConflictException(
                    "SKU_NO_VENDIBLE",
                    "El SKU no se encuentra activo para venta."
            );
        }
    }

    public void requireActive(ProductoSku sku) {
        requireExists(sku);

        if (!sku.isActivo()) {
            throw new NotFoundException(
                    "SKU_INACTIVO",
                    "El SKU no está activo."
            );
        }
    }

    private void requireExists(ProductoSku sku) {
        if (sku == null) {
            throw new NotFoundException(
                    "SKU_NO_ENCONTRADO",
                    "SKU no encontrado."
            );
        }
    }

    private void validateProducto(Producto producto, ValidationErrorCollector errors) {
        if (producto == null) {
            errors.add("producto", "El producto es obligatorio.", "REQUIRED", null);
            return;
        }

        if (!producto.isActivo()) {
            errors.add("producto", "El producto debe estar activo.", "INACTIVE", producto.getIdProducto());
        }

        if (producto.getEstadoRegistro() == EstadoProductoRegistro.DESCONTINUADO) {
            errors.add(
                    "producto",
                    "No se puede crear SKU para un producto descontinuado.",
                    "INVALID_STATE",
                    producto.getIdProducto()
            );
        }
    }

    private void validateCodigoSku(String codigoSku, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigoSku)) {
            errors.add("codigoSku", "El código SKU es obligatorio.", "REQUIRED", codigoSku);
            return;
        }

        if (StringNormalizer.clean(codigoSku).length() > 100) {
            errors.add("codigoSku", "El código SKU no debe superar 100 caracteres.", "MAX_LENGTH", codigoSku);
        }
    }

    private void validateStockRange(Integer stockMinimo, Integer stockMaximo, ValidationErrorCollector errors) {
        if (stockMinimo != null && stockMinimo < 0) {
            errors.add("stockMinimo", "El stock mínimo no puede ser negativo.", "INVALID_VALUE", stockMinimo);
        }

        if (stockMaximo != null && stockMaximo < 0) {
            errors.add("stockMaximo", "El stock máximo no puede ser negativo.", "INVALID_VALUE", stockMaximo);
        }

        if (stockMinimo != null && stockMaximo != null && stockMaximo < stockMinimo) {
            errors.add(
                    "stockMaximo",
                    "El stock máximo no puede ser menor que el stock mínimo.",
                    "INVALID_RANGE",
                    stockMaximo
            );
        }
    }

    private void validateDimensions(
            BigDecimal pesoGramos,
            BigDecimal altoCm,
            BigDecimal anchoCm,
            BigDecimal largoCm,
            ValidationErrorCollector errors
    ) {
        validatePositiveIfPresent("pesoGramos", pesoGramos, errors);
        validatePositiveIfPresent("altoCm", altoCm, errors);
        validatePositiveIfPresent("anchoCm", anchoCm, errors);
        validatePositiveIfPresent("largoCm", largoCm, errors);
    }

    private void validatePositiveIfPresent(String field, BigDecimal value, ValidationErrorCollector errors) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(field, "El valor debe ser mayor a cero.", "INVALID_VALUE", value);
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("SKU_DUPLICADO", message);
        }
    }
}