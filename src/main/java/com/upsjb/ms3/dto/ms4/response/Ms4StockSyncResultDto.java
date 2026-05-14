// ruta: src/main/java/com/upsjb/ms3/dto/ms4/response/Ms4StockSyncResultDto.java
package com.upsjb.ms3.dto.ms4.response;

import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Ms4StockSyncResultDto(
        String eventId,
        String idempotencyKey,
        Ms4StockEventType eventType,
        Boolean success,
        Boolean processed,
        Boolean duplicated,
        String code,
        String message,
        Long idReservaStock,
        String codigoReserva,
        Long idMovimiento,
        String codigoMovimiento,
        Long idSku,
        String codigoSku,
        Long idAlmacen,
        String codigoAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        String referenciaTipo,
        String referenciaIdExterno,
        LocalDateTime processedAt,
        String requestId,
        String correlationId
) {
}