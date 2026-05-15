// ruta: src/main/java/com/upsjb/ms3/validator/CompraInventarioValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class CompraInventarioValidator {

    public void validateCreate(
            Proveedor proveedor,
            LocalDateTime fechaCompra,
            Moneda moneda,
            Integer detallesCount,
            Long creadoPorIdUsuarioMs1
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (proveedor == null || !proveedor.isActivo()) {
            errors.add("proveedor", "El proveedor debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (fechaCompra == null) {
            errors.add("fechaCompra", "La fecha de compra es obligatoria.", "REQUIRED", null);
        }

        if (moneda == null) {
            errors.add("moneda", "La moneda es obligatoria.", "REQUIRED", null);
        }

        if (detallesCount == null || detallesCount <= 0) {
            errors.add("detalles", "La compra debe tener al menos un detalle.", "REQUIRED", detallesCount);
        }

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede registrar la compra de inventario.");
    }

    public void validateDetail(
            ProductoSku sku,
            Almacen almacen,
            Integer cantidad,
            BigDecimal costoUnitario,
            BigDecimal descuento,
            BigDecimal impuesto
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (sku == null || !sku.isActivo()) {
            errors.add("sku", "El SKU debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (sku != null && (sku.getEstadoSku() == null || !sku.getEstadoSku().isOperativo())) {
            errors.add("sku", "El SKU debe estar operativo para registrar compra.", "INVALID_STATE", sku.getIdSku());
        }

        if (sku != null && (sku.getProducto() == null || !sku.getProducto().isActivo())) {
            errors.add("producto", "El producto del SKU debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (almacen == null || !almacen.isActivo()) {
            errors.add("almacen", "El almacén debe existir y estar activo.", "INVALID_REFERENCE", null);
        } else if (!Boolean.TRUE.equals(almacen.getPermiteCompra())) {
            errors.add("almacen", "El almacén seleccionado no permite compras.", "INVALID_STATE", almacen.getIdAlmacen());
        }

        if (cantidad == null || cantidad <= 0) {
            errors.add("cantidad", "La cantidad debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        if (!MoneyUtil.isValidPositiveAmount(costoUnitario)) {
            errors.add("costoUnitario", "El costo unitario debe ser mayor a cero.", "INVALID_VALUE", costoUnitario);
        }

        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("descuento", "El descuento no puede ser negativo.", "INVALID_VALUE", descuento);
        }

        if (impuesto != null && impuesto.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("impuesto", "El impuesto no puede ser negativo.", "INVALID_VALUE", impuesto);
        }

        if (cantidad != null
                && cantidad > 0
                && costoUnitario != null
                && descuento != null
                && descuento.compareTo(costoUnitario.multiply(BigDecimal.valueOf(cantidad))) > 0) {
            errors.add("descuento", "El descuento no puede superar el subtotal del detalle.", "INVALID_VALUE", descuento);
        }

        errors.throwIfAny("El detalle de compra no es válido.");
    }

    public void validateCanConfirm(
            CompraInventario compra,
            boolean hasDetails,
            String motivoConfirmacion
    ) {
        requireActive(compra);

        if (compra.getEstadoCompra() != EstadoCompraInventario.BORRADOR) {
            throw new ConflictException(
                    "COMPRA_NO_CONFIRMABLE",
                    "Solo se puede confirmar una compra en estado BORRADOR."
            );
        }

        if (!hasDetails) {
            throw new ConflictException(
                    "COMPRA_SIN_DETALLE",
                    "No se puede confirmar la compra porque no tiene detalles."
            );
        }

        if (!StringNormalizer.hasText(motivoConfirmacion)) {
            throw new ConflictException(
                    "MOTIVO_CONFIRMACION_COMPRA_REQUERIDO",
                    "Debe indicar el motivo de confirmación de la compra."
            );
        }
    }

    public void validateCanAnular(CompraInventario compra, String motivo) {
        requireActive(compra);

        if (compra.getEstadoCompra() == EstadoCompraInventario.ANULADA) {
            throw new ConflictException(
                    "COMPRA_YA_ANULADA",
                    "La compra ya se encuentra anulada."
            );
        }

        if (compra.getEstadoCompra() == EstadoCompraInventario.CONFIRMADA) {
            throw new ConflictException(
                    "COMPRA_CONFIRMADA_NO_ANULABLE",
                    "No se puede anular directamente una compra confirmada. Debe realizarse un movimiento compensatorio."
            );
        }

        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(
                    "MOTIVO_ANULACION_OBLIGATORIO",
                    "Debe indicar el motivo de anulación de la compra."
            );
        }
    }

    public void requireActive(CompraInventario compra) {
        if (compra == null) {
            throw new NotFoundException(
                    "COMPRA_INVENTARIO_NO_ENCONTRADA",
                    "No se encontró el registro solicitado."
            );
        }

        if (!compra.isActivo()) {
            throw new NotFoundException(
                    "COMPRA_INVENTARIO_INACTIVA",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }
}