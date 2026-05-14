// ruta: src/main/java/com/upsjb/ms3/mapper/Ms4StockEventMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockLiberarRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockMs4RequestDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaAnuladaStockEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockConfirmadoEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockLiberadoEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockReservadoEventDto;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class Ms4StockEventMapper {

    public ReservaStockMs4RequestDto toReservaStockMs4Request(Ms4VentaStockReservadoEventDto event) {
        if (event == null) {
            return null;
        }

        return ReservaStockMs4RequestDto.builder()
                .eventId(event.eventId())
                .idempotencyKey(event.idempotencyKey())
                .sku(event.sku())
                .almacen(event.almacen())
                .referenciaTipo(event.referenciaTipo())
                .referenciaIdExterno(event.referenciaIdExterno())
                .cantidad(event.cantidad())
                .actorIdUsuarioMs1(event.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(event.actorIdEmpleadoMs2())
                .actorRol(event.actorRol())
                .occurredAt(defaultDateTime(event.occurredAt()))
                .expiresAt(event.expiresAt())
                .motivo(event.motivo())
                .requestId(event.requestId())
                .correlationId(event.correlationId())
                .metadataJson(event.metadataJson())
                .build();
    }

    public ReservaStockConfirmRequestDto toConfirmRequest(Ms4VentaStockConfirmadoEventDto event) {
        if (event == null) {
            return null;
        }

        return ReservaStockConfirmRequestDto.builder()
                .motivo(defaultText(event.motivo(), "Confirmación de stock solicitada por MS4."))
                .build();
    }

    public ReservaStockLiberarRequestDto toLiberarRequest(Ms4VentaStockLiberadoEventDto event) {
        if (event == null) {
            return null;
        }

        return ReservaStockLiberarRequestDto.builder()
                .motivo(defaultText(event.motivo(), "Liberación de stock solicitada por MS4."))
                .build();
    }

    public ReservaStockLiberarRequestDto toLiberarRequest(Ms4VentaAnuladaStockEventDto event) {
        if (event == null) {
            return null;
        }

        return ReservaStockLiberarRequestDto.builder()
                .motivo(defaultText(event.motivo(), "Anulación de venta solicitada por MS4."))
                .build();
    }

    public TipoMovimientoInventario resolveTipoMovimiento(Ms4StockEventType eventType) {
        if (eventType == null) {
            return null;
        }

        return switch (eventType) {
            case VENTA_STOCK_RESERVADO_PENDIENTE -> TipoMovimientoInventario.RESERVA_VENTA;
            case VENTA_STOCK_CONFIRMADO_PENDIENTE -> TipoMovimientoInventario.CONFIRMACION_VENTA;
            case VENTA_STOCK_LIBERADO_PENDIENTE -> TipoMovimientoInventario.LIBERACION_RESERVA;
            case VENTA_ANULADA_STOCK_PENDIENTE -> TipoMovimientoInventario.ANULACION_COMPENSATORIA;
        };
    }

    public MotivoMovimientoInventario resolveMotivoMovimiento(Ms4StockEventType eventType) {
        if (eventType == null) {
            return null;
        }

        return switch (eventType) {
            case VENTA_STOCK_RESERVADO_PENDIENTE -> MotivoMovimientoInventario.RESERVA_VENTA;
            case VENTA_STOCK_CONFIRMADO_PENDIENTE -> MotivoMovimientoInventario.CONFIRMACION_VENTA;
            case VENTA_STOCK_LIBERADO_PENDIENTE -> MotivoMovimientoInventario.LIBERACION_RESERVA;
            case VENTA_ANULADA_STOCK_PENDIENTE -> MotivoMovimientoInventario.ANULACION_COMPENSATORIA;
        };
    }

    public Ms4StockSyncResultDto toSuccessResult(
            Ms4VentaStockReservadoEventDto event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock,
            String message
    ) {
        return toResult(
                StockEventView.from(event),
                reserva,
                movimiento,
                stock,
                true,
                true,
                false,
                "MS4_STOCK_SYNC_OK",
                defaultText(message, "Evento de reserva de stock procesado correctamente.")
        );
    }

    public Ms4StockSyncResultDto toSuccessResult(
            Ms4VentaStockConfirmadoEventDto event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock,
            String message
    ) {
        return toResult(
                StockEventView.from(event),
                reserva,
                movimiento,
                stock,
                true,
                true,
                false,
                "MS4_STOCK_SYNC_OK",
                defaultText(message, "Evento de confirmación de stock procesado correctamente.")
        );
    }

    public Ms4StockSyncResultDto toSuccessResult(
            Ms4VentaStockLiberadoEventDto event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock,
            String message
    ) {
        return toResult(
                StockEventView.from(event),
                reserva,
                movimiento,
                stock,
                true,
                true,
                false,
                "MS4_STOCK_SYNC_OK",
                defaultText(message, "Evento de liberación de stock procesado correctamente.")
        );
    }

    public Ms4StockSyncResultDto toSuccessResult(
            Ms4VentaAnuladaStockEventDto event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock,
            String message
    ) {
        return toResult(
                StockEventView.from(event),
                reserva,
                movimiento,
                stock,
                true,
                true,
                false,
                "MS4_STOCK_SYNC_OK",
                defaultText(message, "Evento de anulación de stock procesado correctamente.")
        );
    }

    public Ms4StockSyncResultDto toDuplicateResult(
            Ms4StockEventType eventType,
            String eventId,
            String idempotencyKey,
            String referenciaTipo,
            String referenciaIdExterno,
            String requestId,
            String correlationId
    ) {
        return Ms4StockSyncResultDto.builder()
                .eventId(eventId)
                .idempotencyKey(idempotencyKey)
                .eventType(eventType)
                .success(true)
                .processed(false)
                .duplicated(true)
                .code("MS4_STOCK_EVENT_DUPLICATED")
                .message("El evento de stock de MS4 ya fue procesado previamente.")
                .referenciaTipo(referenciaTipo)
                .referenciaIdExterno(referenciaIdExterno)
                .processedAt(LocalDateTime.now())
                .requestId(requestId)
                .correlationId(correlationId)
                .build();
    }

    public Ms4StockSyncResultDto toRejectedResult(
            Ms4StockEventType eventType,
            String eventId,
            String idempotencyKey,
            String referenciaTipo,
            String referenciaIdExterno,
            String code,
            String message,
            String requestId,
            String correlationId
    ) {
        return Ms4StockSyncResultDto.builder()
                .eventId(eventId)
                .idempotencyKey(idempotencyKey)
                .eventType(eventType)
                .success(false)
                .processed(false)
                .duplicated(false)
                .code(defaultText(code, "MS4_STOCK_EVENT_REJECTED"))
                .message(defaultText(message, "El evento de stock de MS4 fue rechazado por regla funcional."))
                .referenciaTipo(referenciaTipo)
                .referenciaIdExterno(referenciaIdExterno)
                .processedAt(LocalDateTime.now())
                .requestId(requestId)
                .correlationId(correlationId)
                .build();
    }

    private Ms4StockSyncResultDto toResult(
            StockEventView event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock,
            Boolean success,
            Boolean processed,
            Boolean duplicated,
            String code,
            String message
    ) {
        ProductoSku sku = resolveSku(event, reserva, movimiento, stock);
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = resolveAlmacen(event, reserva, movimiento, stock);

        return Ms4StockSyncResultDto.builder()
                .eventId(event.eventId())
                .idempotencyKey(event.idempotencyKey())
                .eventType(event.eventType())
                .success(success)
                .processed(processed)
                .duplicated(duplicated)
                .code(code)
                .message(message)
                .idReservaStock(reserva == null ? null : reserva.getIdReservaStock())
                .codigoReserva(resolveCodigoReserva(event, reserva))
                .idMovimiento(movimiento == null ? null : movimiento.getIdMovimiento())
                .codigoMovimiento(movimiento == null ? null : movimiento.getCodigoMovimiento())
                .idSku(sku == null ? eventSkuId(event) : sku.getIdSku())
                .codigoSku(sku == null ? eventSkuCodigo(event) : sku.getCodigoSku())
                .idAlmacen(almacen == null ? eventAlmacenId(event) : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? eventAlmacenCodigo(event) : almacen.getCodigo())
                .stockFisico(stock == null ? null : defaultInteger(stock.getStockFisico()))
                .stockReservado(stock == null ? null : defaultInteger(stock.getStockReservado()))
                .stockDisponible(stock == null ? null : resolveStockDisponible(stock))
                .referenciaTipo(event.referenciaTipo() == null ? null : event.referenciaTipo().getCode())
                .referenciaIdExterno(event.referenciaIdExterno())
                .processedAt(LocalDateTime.now())
                .requestId(event.requestId())
                .correlationId(event.correlationId())
                .build();
    }

    private ProductoSku resolveSku(
            StockEventView event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock
    ) {
        if (stock != null && stock.getSku() != null) {
            return stock.getSku();
        }

        if (movimiento != null && movimiento.getSku() != null) {
            return movimiento.getSku();
        }

        if (reserva != null && reserva.getSku() != null) {
            return reserva.getSku();
        }

        return null;
    }

    private Almacen resolveAlmacen(
            StockEventView event,
            ReservaStock reserva,
            MovimientoInventario movimiento,
            StockSku stock
    ) {
        if (stock != null && stock.getAlmacen() != null) {
            return stock.getAlmacen();
        }

        if (movimiento != null && movimiento.getAlmacen() != null) {
            return movimiento.getAlmacen();
        }

        if (reserva != null && reserva.getAlmacen() != null) {
            return reserva.getAlmacen();
        }

        return null;
    }

    private String resolveCodigoReserva(StockEventView event, ReservaStock reserva) {
        if (reserva != null && reserva.getCodigoReserva() != null) {
            return reserva.getCodigoReserva();
        }

        return event.codigoReserva();
    }

    private Long eventSkuId(StockEventView event) {
        return event.sku() == null ? null : event.sku().id();
    }

    private String eventSkuCodigo(StockEventView event) {
        return event.sku() == null ? null : event.sku().codigo();
    }

    private Long eventAlmacenId(StockEventView event) {
        return event.almacen() == null ? null : event.almacen().id();
    }

    private String eventAlmacenCodigo(StockEventView event) {
        return event.almacen() == null ? null : event.almacen().codigo();
    }

    private Integer resolveStockDisponible(StockSku stock) {
        if (stock == null) {
            return 0;
        }

        if (stock.getStockDisponible() != null) {
            return stock.getStockDisponible();
        }

        return defaultInteger(stock.getStockFisico()) - defaultInteger(stock.getStockReservado());
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private LocalDateTime defaultDateTime(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record StockEventView(
            String eventId,
            String idempotencyKey,
            Ms4StockEventType eventType,
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Integer cantidad,
            String codigoReserva,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            LocalDateTime occurredAt,
            String motivo,
            String requestId,
            String correlationId,
            String metadataJson
    ) {

        private static StockEventView from(Ms4VentaStockReservadoEventDto event) {
            return new StockEventView(
                    event.eventId(),
                    event.idempotencyKey(),
                    event.eventType(),
                    event.sku(),
                    event.almacen(),
                    event.referenciaTipo(),
                    event.referenciaIdExterno(),
                    event.cantidad(),
                    null,
                    event.actorIdUsuarioMs1(),
                    event.actorIdEmpleadoMs2(),
                    event.actorRol(),
                    event.occurredAt(),
                    event.motivo(),
                    event.requestId(),
                    event.correlationId(),
                    event.metadataJson()
            );
        }

        private static StockEventView from(Ms4VentaStockConfirmadoEventDto event) {
            return new StockEventView(
                    event.eventId(),
                    event.idempotencyKey(),
                    event.eventType(),
                    event.sku(),
                    event.almacen(),
                    event.referenciaTipo(),
                    event.referenciaIdExterno(),
                    event.cantidad(),
                    event.codigoReserva(),
                    event.actorIdUsuarioMs1(),
                    event.actorIdEmpleadoMs2(),
                    event.actorRol(),
                    event.occurredAt(),
                    event.motivo(),
                    event.requestId(),
                    event.correlationId(),
                    event.metadataJson()
            );
        }

        private static StockEventView from(Ms4VentaStockLiberadoEventDto event) {
            return new StockEventView(
                    event.eventId(),
                    event.idempotencyKey(),
                    event.eventType(),
                    event.sku(),
                    event.almacen(),
                    event.referenciaTipo(),
                    event.referenciaIdExterno(),
                    event.cantidad(),
                    event.codigoReserva(),
                    event.actorIdUsuarioMs1(),
                    event.actorIdEmpleadoMs2(),
                    event.actorRol(),
                    event.occurredAt(),
                    event.motivo(),
                    event.requestId(),
                    event.correlationId(),
                    event.metadataJson()
            );
        }

        private static StockEventView from(Ms4VentaAnuladaStockEventDto event) {
            return new StockEventView(
                    event.eventId(),
                    event.idempotencyKey(),
                    event.eventType(),
                    event.sku(),
                    event.almacen(),
                    event.referenciaTipo(),
                    event.referenciaIdExterno(),
                    event.cantidad(),
                    event.codigoReserva(),
                    event.actorIdUsuarioMs1(),
                    event.actorIdEmpleadoMs2(),
                    event.actorRol(),
                    event.occurredAt(),
                    event.motivo(),
                    event.requestId(),
                    event.correlationId(),
                    event.metadataJson()
            );
        }
    }
}