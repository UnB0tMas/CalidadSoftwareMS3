// ruta: src/main/java/com/upsjb/ms3/integration/ms4/Ms4StockSyncClient.java
package com.upsjb.ms3.integration.ms4;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface Ms4StockSyncClient {

    Ms4StockSyncResponse sendStockSync(Ms4StockSyncRequest request);

    List<Ms4PendingStockEvent> fetchPendingStockEvents(Integer limit);

    default Ms4StockSyncResponse enviarSincronizacionStock(Ms4StockSyncRequest request) {
        return sendStockSync(request);
    }

    default List<Ms4PendingStockEvent> obtenerEventosPendientes(Integer limit) {
        return fetchPendingStockEvents(limit);
    }

    record Ms4StockSyncRequest(
            String eventId,
            String eventType,
            String operation,
            String referenceType,
            String referenceExternalId,
            Long idSkuMs3,
            Long idAlmacenMs3,
            String codigoSku,
            Integer cantidad,
            String status,
            String message,
            String requestId,
            String correlationId,
            LocalDateTime occurredAt,
            Map<String, Object> payload
    ) {

        public Ms4StockSyncRequest {
            eventId = clean(eventId);
            eventType = clean(eventType);
            operation = clean(operation);
            referenceType = clean(referenceType);
            referenceExternalId = clean(referenceExternalId);
            codigoSku = clean(codigoSku);
            status = clean(status);
            message = clean(message);
            requestId = clean(requestId);
            correlationId = clean(correlationId);
            payload = payload == null ? Map.of() : Map.copyOf(payload);
        }
    }

    record Ms4PendingStockEvent(
            String eventId,
            String eventType,
            String operation,
            String referenceType,
            String referenceExternalId,
            Long idSkuMs3,
            Long idAlmacenMs3,
            String codigoSku,
            Integer cantidad,
            String requestId,
            String correlationId,
            LocalDateTime occurredAt,
            Map<String, Object> payload
    ) {

        public Ms4PendingStockEvent {
            eventId = clean(eventId);
            eventType = clean(eventType);
            operation = clean(operation);
            referenceType = clean(referenceType);
            referenceExternalId = clean(referenceExternalId);
            codigoSku = clean(codigoSku);
            requestId = clean(requestId);
            correlationId = clean(correlationId);
            payload = payload == null ? Map.of() : Map.copyOf(payload);
        }
    }

    record Ms4StockSyncResponse(
            boolean accepted,
            String status,
            String message,
            String externalOperationId,
            String correlationId,
            Map<String, Object> rawResponse
    ) {

        public Ms4StockSyncResponse {
            status = clean(status);
            message = clean(message);
            externalOperationId = clean(externalOperationId);
            correlationId = clean(correlationId);
            rawResponse = rawResponse == null ? Map.of() : Map.copyOf(rawResponse);
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}