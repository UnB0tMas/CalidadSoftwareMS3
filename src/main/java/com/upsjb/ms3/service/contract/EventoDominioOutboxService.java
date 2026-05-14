// ruta: src/main/java/com/upsjb/ms3/service/contract/EventoDominioOutboxService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.dto.outbox.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms3.dto.outbox.request.OutboxRetryRequestDto;
import com.upsjb.ms3.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import java.util.UUID;

public interface EventoDominioOutboxService {

    EventoDominioOutboxResponseDto registrarEvento(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    );

    EventoDominioOutboxResponseDto registrarEvento(DomainEventEnvelope<?> envelope);

    EventoDominioOutboxResponseDto registrarEvento(ProductoSnapshotEvent event);

    EventoDominioOutboxResponseDto registrarEvento(PrecioSnapshotEvent event);

    EventoDominioOutboxResponseDto registrarEvento(PromocionSnapshotEvent event);

    EventoDominioOutboxResponseDto registrarEvento(StockSnapshotEvent event);

    EventoDominioOutboxResponseDto registrarEvento(MovimientoInventarioEvent event);

    ApiResponseDto<PageResponseDto<EventoDominioOutboxResponseDto>> listar(
            EventoDominioOutboxFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<EventoDominioOutboxResponseDto> obtenerDetalle(Long idEvento, Boolean incluirPayload);

    ApiResponseDto<EventoDominioOutboxResponseDto> obtenerPorEventId(UUID eventId, Boolean incluirPayload);

    ApiResponseDto<OutboxPublishResultResponseDto> reintentar(
            Long idEvento,
            OutboxRetryRequestDto request
    );
}