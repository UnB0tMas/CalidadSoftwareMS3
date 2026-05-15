// ruta: src/main/java/com/upsjb/ms3/validator/ReservaStockValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ReservaStockValidator {

    private static final int MAX_REFERENCIA_LENGTH = 100;
    private static final int MAX_MOTIVO_LENGTH = 500;

    public void validateCreate(
            ProductoSku sku,
            Almacen almacen,
            StockSku stock,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Integer cantidad,
            LocalDateTime expiresAt,
            Long reservadoPorIdUsuarioMs1,
            String motivo,
            boolean duplicatedReservation
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (sku == null || !sku.isActivo()) {
            errors.add("sku", "El SKU debe existir y estar activo.", "INVALID_REFERENCE", null);
        }

        if (almacen == null || !almacen.isActivo()) {
            errors.add("almacen", "El almacén debe existir y estar activo.", "INVALID_REFERENCE", null);
        } else if (!Boolean.TRUE.equals(almacen.getPermiteVenta())) {
            errors.add("almacen", "El almacén seleccionado no permite venta.", "INVALID_STATE", almacen.getIdAlmacen());
        }

        if (referenciaTipo == null) {
            errors.add("referenciaTipo", "El tipo de referencia es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(referenciaIdExterno)) {
            errors.add("referenciaIdExterno", "La referencia externa es obligatoria.", "REQUIRED", referenciaIdExterno);
        } else if (StringNormalizer.clean(referenciaIdExterno).length() > MAX_REFERENCIA_LENGTH) {
            errors.add(
                    "referenciaIdExterno",
                    "La referencia externa no debe superar 100 caracteres.",
                    "MAX_LENGTH",
                    referenciaIdExterno
            );
        }

        if (cantidad == null || cantidad <= 0) {
            errors.add("cantidad", "La cantidad a reservar debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        if (reservadoPorIdUsuarioMs1 == null) {
            errors.add("reservadoPorIdUsuarioMs1", "El usuario que reserva es obligatorio.", "REQUIRED", null);
        }

        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            errors.add("expiresAt", "La fecha de expiración debe ser futura.", "INVALID_VALUE", expiresAt);
        }

        if (!StringNormalizer.hasText(motivo)) {
            errors.add("motivo", "Debe indicar el motivo de la reserva.", "REQUIRED", motivo);
        } else if (StringNormalizer.clean(motivo).length() > MAX_MOTIVO_LENGTH) {
            errors.add("motivo", "El motivo no debe superar 500 caracteres.", "MAX_LENGTH", motivo);
        }

        errors.throwIfAny("No se puede crear la reserva de stock.");

        if (duplicatedReservation) {
            throw new ConflictException(
                    "RESERVA_STOCK_DUPLICADA",
                    "Ya existe una reserva para la misma referencia externa, SKU y almacén."
            );
        }

        if (stock == null || !stock.isActivo()) {
            throw new NotFoundException(
                    "STOCK_SKU_NO_ENCONTRADO",
                    "No existe stock configurado para el SKU y almacén indicados."
            );
        }

        StockMathUtil.requireConsistentStock(stock.getStockFisico(), stock.getStockReservado());

        if (!StockMathUtil.hasAvailable(stock.getStockFisico(), stock.getStockReservado(), cantidad)) {
            throw new ConflictException(
                    "STOCK_DISPONIBLE_INSUFICIENTE",
                    "No se puede registrar la salida porque el stock disponible es insuficiente."
            );
        }
    }

    public void validateReferenceLookup(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (referenciaTipo == null) {
            errors.add("referenciaTipo", "El tipo de referencia es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(referenciaIdExterno)) {
            errors.add("referenciaIdExterno", "La referencia externa es obligatoria.", "REQUIRED", referenciaIdExterno);
        }

        if (idSku == null) {
            errors.add("sku", "Debe indicar el SKU.", "REQUIRED", null);
        }

        if (idAlmacen == null) {
            errors.add("almacen", "Debe indicar el almacén.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede ubicar la reserva solicitada.");
    }

    public void validateCanConfirm(
            ReservaStock reserva,
            Long confirmadoPorIdUsuarioMs1,
            LocalDateTime now,
            String motivo
    ) {
        requireActive(reserva);

        if (reserva.getEstadoReserva() == EstadoReservaStock.CONFIRMADA) {
            return;
        }

        if (reserva.getEstadoReserva() != EstadoReservaStock.RESERVADA) {
            throw new ConflictException(
                    "RESERVA_NO_CONFIRMABLE",
                    "No se puede confirmar la reserva porque ya fue liberada, vencida o anulada."
            );
        }

        if (reserva.getExpiresAt() != null && !reserva.getExpiresAt().isAfter(now)) {
            throw new ConflictException(
                    "RESERVA_VENCIDA_NO_CONFIRMABLE",
                    "No se puede confirmar la reserva porque ya venció."
            );
        }

        if (confirmadoPorIdUsuarioMs1 == null) {
            throw new ConflictException(
                    "USUARIO_CONFIRMACION_OBLIGATORIO",
                    "El usuario que confirma la reserva es obligatorio."
            );
        }

        validateMotivo(motivo, "MOTIVO_CONFIRMACION_OBLIGATORIO", "Debe indicar el motivo de confirmación.");
    }

    public void validateCanRelease(
            ReservaStock reserva,
            Long liberadoPorIdUsuarioMs1,
            String motivo
    ) {
        requireActive(reserva);

        if (reserva.getEstadoReserva() == EstadoReservaStock.LIBERADA
                || reserva.getEstadoReserva() == EstadoReservaStock.VENCIDA) {
            return;
        }

        if (reserva.getEstadoReserva() != EstadoReservaStock.RESERVADA) {
            throw new ConflictException(
                    "RESERVA_NO_LIBERABLE",
                    "No se puede confirmar la reserva porque ya fue liberada, vencida o anulada."
            );
        }

        if (liberadoPorIdUsuarioMs1 == null) {
            throw new ConflictException(
                    "USUARIO_LIBERACION_OBLIGATORIO",
                    "El usuario que libera la reserva es obligatorio."
            );
        }

        validateMotivo(motivo, "MOTIVO_LIBERACION_OBLIGATORIO", "Debe indicar el motivo de liberación.");
    }

    public void validateCanExpire(ReservaStock reserva, LocalDateTime now) {
        requireActive(reserva);

        if (reserva.getEstadoReserva() != EstadoReservaStock.RESERVADA) {
            throw new ConflictException(
                    "RESERVA_NO_EXPIRABLE",
                    "Solo se puede vencer una reserva en estado RESERVADA."
            );
        }

        if (reserva.getExpiresAt() == null || reserva.getExpiresAt().isAfter(now)) {
            throw new ConflictException(
                    "RESERVA_AUN_VIGENTE",
                    "La reserva aún no ha vencido."
            );
        }
    }

    public void validateReservedStockEnough(StockSku stock, ReservaStock reserva) {
        if (stock == null || reserva == null) {
            throw new NotFoundException(
                    "STOCK_SKU_NO_ENCONTRADO",
                    "No existe stock configurado para el SKU y almacén indicados."
            );
        }

        StockMathUtil.requireConsistentStock(stock.getStockFisico(), stock.getStockReservado());

        if (StockMathUtil.zeroIfNull(stock.getStockReservado()) < StockMathUtil.zeroIfNull(reserva.getCantidad())) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INSUFICIENTE",
                    "No se puede completar la operación porque el stock reservado es insuficiente."
            );
        }
    }

    public void requireActive(ReservaStock reserva) {
        if (reserva == null) {
            throw new NotFoundException(
                    "RESERVA_STOCK_NO_ENCONTRADA",
                    "No se encontró el registro solicitado."
            );
        }

        if (!reserva.isActivo()) {
            throw new NotFoundException(
                    "RESERVA_STOCK_INACTIVA",
                    "La reserva de stock no está activa."
            );
        }
    }

    private void validateMotivo(String motivo, String code, String message) {
        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(code, message);
        }

        if (StringNormalizer.clean(motivo).length() > MAX_MOTIVO_LENGTH) {
            throw new ConflictException(
                    "MOTIVO_RESERVA_INVALIDO",
                    "El motivo no debe superar 500 caracteres."
            );
        }
    }
}