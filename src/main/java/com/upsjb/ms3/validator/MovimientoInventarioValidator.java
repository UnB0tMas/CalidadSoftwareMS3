// ruta: src/main/java/com/upsjb/ms3/validator/MovimientoInventarioValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioValidator {

    public void validateCreate(
            ProductoSku sku,
            Almacen almacen,
            TipoMovimientoInventario tipoMovimiento,
            MotivoMovimientoInventario motivoMovimiento,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockNuevo,
            BigDecimal costoUnitario,
            String referenciaTipo,
            String referenciaIdExterno,
            Long actorIdUsuarioMs1,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (sku == null || !sku.isActivo()) {
            errors.add("sku", "El SKU debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (almacen == null || !almacen.isActivo()) {
            errors.add("almacen", "El almacén debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (tipoMovimiento == null) {
            errors.add("tipoMovimiento", "El tipo de movimiento es obligatorio.", "REQUIRED", null);
        }

        if (motivoMovimiento == null) {
            errors.add("motivoMovimiento", "El motivo del movimiento es obligatorio.", "REQUIRED", null);
        }

        if (cantidad == null || cantidad <= 0) {
            errors.add("cantidad", "La cantidad debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        if (stockAnterior == null || stockAnterior < 0) {
            errors.add("stockAnterior", "El stock anterior no puede ser negativo.", "INVALID_VALUE", stockAnterior);
        }

        if (stockNuevo == null || stockNuevo < 0) {
            errors.add("stockNuevo", "El stock nuevo no puede ser negativo.", "INVALID_VALUE", stockNuevo);
        }

        if (costoUnitario != null && costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("costoUnitario", "El costo unitario no puede ser negativo.", "INVALID_VALUE", costoUnitario);
        }

        if (actorIdUsuarioMs1 == null) {
            errors.add("actorIdUsuarioMs1", "El usuario actor es obligatorio.", "REQUIRED", null);
        }

        if (actorRol == null) {
            errors.add("actorRol", "El rol del actor es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(requestId)) {
            errors.add("requestId", "El requestId es obligatorio para trazabilidad.", "REQUIRED", requestId);
        }

        if (!StringNormalizer.hasText(correlationId)) {
            errors.add("correlationId", "El correlationId es obligatorio para trazabilidad.", "REQUIRED", correlationId);
        }

        if (tipoMovimiento != null && tipoMovimiento.isRelacionadoVentaOCompra()) {
            if (!StringNormalizer.hasText(referenciaTipo) || !StringNormalizer.hasText(referenciaIdExterno)) {
                errors.add("referencia", "Los movimientos relacionados a venta o compra requieren referencia externa.", "REQUIRED", referenciaIdExterno);
            }
        }

        errors.throwIfAny("No se puede registrar el movimiento de inventario.");

        validateStockTransition(tipoMovimiento, cantidad, stockAnterior, stockNuevo);
    }

    public void validateCompensatoryMovement(
            MovimientoInventario originalMovement,
            String motivoCompensacion
    ) {
        requireActive(originalMovement);

        if (!StringNormalizer.hasText(motivoCompensacion)) {
            throw new ConflictException(
                    "MOTIVO_COMPENSACION_OBLIGATORIO",
                    "Debe indicar el motivo del movimiento compensatorio."
            );
        }
    }

    public void validateCanCompensate(MovimientoInventario movimiento) {
        requireActive(movimiento);

        if (!movimiento.getEstadoMovimiento().isRegistrado()) {
            throw new ConflictException(
                    "MOVIMIENTO_NO_COMPENSABLE",
                    "Solo se puede compensar un movimiento registrado."
            );
        }
    }

    public void requireActive(MovimientoInventario movimiento) {
        if (movimiento == null) {
            throw new NotFoundException(
                    "MOVIMIENTO_INVENTARIO_NO_ENCONTRADO",
                    "Movimiento de inventario no encontrado."
            );
        }

        if (!movimiento.isActivo()) {
            throw new NotFoundException(
                    "MOVIMIENTO_INVENTARIO_INACTIVO",
                    "El movimiento de inventario no está activo."
            );
        }
    }

    private void validateStockTransition(
            TipoMovimientoInventario tipoMovimiento,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockNuevo
    ) {
        int qty = StockMathUtil.requirePositiveQuantity(cantidad);

        if (tipoMovimiento.isEntradaFisica() && stockNuevo != stockAnterior + qty) {
            throw new ConflictException(
                    "KARDEX_STOCK_NUEVO_INVALIDO",
                    "El stock nuevo no coincide con la entrada registrada."
            );
        }

        if (tipoMovimiento.isSalidaFisica() && stockNuevo != stockAnterior - qty) {
            throw new ConflictException(
                    "KARDEX_STOCK_NUEVO_INVALIDO",
                    "El stock nuevo no coincide con la salida registrada."
            );
        }
    }
}