// ruta: src/main/java/com/upsjb/ms3/kafka/consumer/Ms4StockCommandHandler.java
package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.event.Ms4StockCommandEvent;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.mapper.Ms4StockEventMapper;
import com.upsjb.ms3.service.contract.Ms4ReconciliacionService;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.validator.Ms4StockEventValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Ms4StockCommandHandler {

    private final Ms4ReconciliacionService reconciliacionService;
    private final KafkaIdempotencyGuard idempotencyGuard;
    private final Ms4StockEventValidator validator;
    private final Ms4StockEventMapper mapper;

    public Ms4StockSyncResultDto handle(Ms4StockCommandEvent event) {
        if (event == null || event.payload() == null) {
            throw new ValidationException(
                    "MS4_STOCK_COMMAND_VACIO",
                    "El comando de stock recibido desde MS4 está vacío."
            );
        }

        Ms4StockCommandPayload payload = event.payload();

        Long idSku = idempotencyGuard.resolveSkuId(payload.sku()).orElse(null);
        Long idAlmacen = idempotencyGuard.resolveAlmacenId(payload.almacen()).orElse(null);

        validator.validateStockCommand(
                payload.safeIdempotencyKey(),
                payload.eventType(),
                payload.referenciaTipo(),
                payload.safeReferenciaIdExterno(),
                idSku,
                idAlmacen,
                payload.cantidad()
        );

        if (idempotencyGuard.isProcessed(payload)) {
            return mapper.toDuplicateResult(
                    payload.eventType(),
                    payload.safeEventId(),
                    payload.safeIdempotencyKey(),
                    payload.referenciaTipo() == null ? null : payload.referenciaTipo().getCode(),
                    payload.safeReferenciaIdExterno(),
                    payload.requestId(),
                    payload.correlationId()
            );
        }

        Ms4StockEventType eventType = payload.eventType();

        return switch (eventType) {
            case VENTA_STOCK_RESERVADO_PENDIENTE -> reconciliacionService.procesarReservaPendiente(payload);
            case VENTA_STOCK_CONFIRMADO_PENDIENTE -> reconciliacionService.procesarConfirmacionPendiente(payload);
            case VENTA_STOCK_LIBERADO_PENDIENTE -> reconciliacionService.procesarLiberacionPendiente(payload);
            case VENTA_ANULADA_STOCK_PENDIENTE -> reconciliacionService.procesarAnulacionPendiente(payload);
        };
    }
}