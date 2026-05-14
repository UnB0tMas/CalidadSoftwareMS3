// ruta: src/main/java/com/upsjb/ms3/dto/outbox/request/OutboxRetryRequestDto.java
package com.upsjb.ms3.dto.outbox.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OutboxRetryRequestDto(

        @NotBlank(message = "El motivo del reintento es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo,

        Boolean forzarReintento
) {
}