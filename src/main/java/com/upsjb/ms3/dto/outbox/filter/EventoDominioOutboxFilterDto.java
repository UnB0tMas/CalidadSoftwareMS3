// ruta: src/main/java/com/upsjb/ms3/dto/outbox/filter/EventoDominioOutboxFilterDto.java
package com.upsjb.ms3.dto.outbox.filter;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EventoDominioOutboxFilterDto(

        Long idEvento,

        UUID eventId,

        @Size(max = 200, message = "El texto de búsqueda no debe superar 200 caracteres.")
        String search,

        AggregateType aggregateType,

        @Size(max = 100, message = "El aggregateId no debe superar 100 caracteres.")
        String aggregateId,

        @Size(max = 120, message = "El eventType no debe superar 120 caracteres.")
        String eventType,

        @Size(max = 200, message = "El topic no debe superar 200 caracteres.")
        String topic,

        @Size(max = 200, message = "El eventKey no debe superar 200 caracteres.")
        String eventKey,

        EstadoPublicacionEvento estadoPublicacion,

        Boolean soloReintentables,

        Boolean conError,

        Boolean bloqueado,

        Boolean bloqueados,

        Boolean locked,

        @Size(max = 100, message = "El lockedBy no debe superar 100 caracteres.")
        String lockedBy,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion,

        @Valid
        DateRangeFilterDto fechaPublicacion,

        @Valid
        DateRangeFilterDto fechaBloqueo
) {
}