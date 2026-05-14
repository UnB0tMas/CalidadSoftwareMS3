// ruta: src/main/java/com/upsjb/ms3/service/contract/KafkaPublisherService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.outbox.request.OutboxRetryRequestDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import java.util.List;

public interface KafkaPublisherService {

    ApiResponseDto<OutboxPublishResultResponseDto> publicar(Long idEvento);

    ApiResponseDto<OutboxPublishResultResponseDto> reintentar(
            Long idEvento,
            OutboxRetryRequestDto request
    );

    ApiResponseDto<List<OutboxPublishResultResponseDto>> publicarPendientes();

    OutboxPublishResultResponseDto publicarInterno(Long idEvento);

    OutboxPublishResultResponseDto reintentarInterno(Long idEvento, boolean forzarReintento);

    List<OutboxPublishResultResponseDto> publicarPendientesInterno();
}