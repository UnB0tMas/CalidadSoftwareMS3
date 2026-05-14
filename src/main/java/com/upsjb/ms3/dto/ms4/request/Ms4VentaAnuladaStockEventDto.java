// ruta: src/main/java/com/upsjb/ms3/dto/ms4/request/Ms4VentaAnuladaStockEventDto.java
package com.upsjb.ms3.dto.ms4.request;

import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Ms4VentaAnuladaStockEventDto(

        @NotBlank(message = "El eventId es obligatorio.")
        @Size(max = 120, message = "El eventId no debe superar 120 caracteres.")
        String eventId,

        @NotBlank(message = "La clave de idempotencia es obligatoria.")
        @Size(max = 180, message = "La clave de idempotencia no debe superar 180 caracteres.")
        String idempotencyKey,

        @NotNull(message = "El tipo de evento MS4 es obligatorio.")
        Ms4StockEventType eventType,

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @Valid
        @NotNull(message = "El almacén es obligatorio.")
        EntityReferenceDto almacen,

        @NotNull(message = "El tipo de referencia es obligatorio.")
        TipoReferenciaStock referenciaTipo,

        @NotBlank(message = "La referencia externa de MS4 es obligatoria.")
        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
        Integer cantidad,

        @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
        String codigoReserva,

        Long actorIdUsuarioMs1,

        Long actorIdEmpleadoMs2,

        RolSistema actorRol,

        LocalDateTime occurredAt,

        @NotBlank(message = "El motivo de anulación es obligatorio.")
        @Size(max = 500, message = "El motivo de anulación no debe superar 500 caracteres.")
        String motivo,

        @Size(max = 100, message = "El requestId no debe superar 100 caracteres.")
        String requestId,

        @Size(max = 100, message = "El correlationId no debe superar 100 caracteres.")
        String correlationId,

        String metadataJson
) {
}