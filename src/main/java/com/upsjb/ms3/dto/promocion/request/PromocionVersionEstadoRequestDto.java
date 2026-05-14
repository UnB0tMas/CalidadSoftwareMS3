// ruta: src/main/java/com/upsjb/ms3/dto/promocion/request/PromocionVersionEstadoRequestDto.java
package com.upsjb.ms3.dto.promocion.request;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PromocionVersionEstadoRequestDto(

        @NotNull(message = "El estado de promoción es obligatorio.")
        EstadoPromocion estadoPromocion,

        Boolean visiblePublico,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}