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

    public void validateCreate(
            ProductoSku sku,
            Almacen almacen,
            StockSku stock,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Integer cantidad,
            LocalDateTime expiresAt,
            Long reservadoPorIdUsuarioMs1,
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
        }

        if (cantidad == null || cantidad <= 0) {
            errors.add("cantidad", "La cantidad a reservar debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        if (reservadoPorIdUsuarioMs1 == null) {
            errors.add("reservadoPorIdUsuarioMs1", "El usuario que reserva es obligatorio.", "REQUIRED", null);
        }

        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            errors.add("expiresAt", "La fecha de expiración no puede estar en el pasado.", "INVALID_VALUE", expiresAt);
        }

        errors.throwIfAny("No se puede crear la reserva de stock.");

        if (duplicatedReservation) {
            throw new ConflictException(
                    "RESERVA_STOCK_DUPLICADA",
                    "Ya existe una reserva para la misma referencia externa, SKU y almacén."
            );
        }

        if (stock == null) {
            throw new NotFoundException(
                    "STOCK_SKU_NO_ENCONTRADO",
                    "No existe stock configurado para el SKU y almacén indicados."
            );
        }

        if (!StockMathUtil.hasAvailable(stock.getStockFisico(), stock.getStockReservado(), cantidad)) {
            throw new ConflictException(
                    "STOCK_DISPONIBLE_INSUFICIENTE",
                    "No hay stock disponible suficiente para crear la reserva."
            );
        }
    }

    public void validateCanConfirm(ReservaStock reserva, Long confirmadoPorIdUsuarioMs1) {
        requireActive(reserva);

        if (reserva.getEstadoReserva() != EstadoReservaStock.RESERVADA) {
            throw new ConflictException(
                    "RESERVA_NO_CONFIRMABLE",
                    "Solo se puede confirmar una reserva en estado RESERVADA."
            );
        }

        if (confirmadoPorIdUsuarioMs1 == null) {
            throw new ConflictException(
                    "USUARIO_CONFIRMACION_OBLIGATORIO",
                    "El usuario que confirma la reserva es obligatorio."
            );
        }
    }

    public void validateCanRelease(ReservaStock reserva, Long liberadoPorIdUsuarioMs1, String motivo) {
        requireActive(reserva);

        if (reserva.getEstadoReserva() != EstadoReservaStock.RESERVADA) {
            throw new ConflictException(
                    "RESERVA_NO_LIBERABLE",
                    "Solo se puede liberar una reserva en estado RESERVADA."
            );
        }

        if (liberadoPorIdUsuarioMs1 == null) {
            throw new ConflictException(
                    "USUARIO_LIBERACION_OBLIGATORIO",
                    "El usuario que libera la reserva es obligatorio."
            );
        }

        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(
                    "MOTIVO_LIBERACION_OBLIGATORIO",
                    "Debe indicar el motivo de liberación de la reserva."
            );
        }
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

    public void requireActive(ReservaStock reserva) {
        if (reserva == null) {
            throw new NotFoundException(
                    "RESERVA_STOCK_NO_ENCONTRADA",
                    "Reserva de stock no encontrada."
            );
        }

        if (!reserva.isActivo()) {
            throw new NotFoundException(
                    "RESERVA_STOCK_INACTIVA",
                    "La reserva de stock no está activa."
            );
        }
    }
}