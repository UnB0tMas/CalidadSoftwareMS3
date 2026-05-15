// ruta: src/main/java/com/upsjb/ms3/dto/promocion/request/PromocionVersionCreateRequestDto.java
package com.upsjb.ms3.dto.promocion.request;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionVersionCreateRequestDto(

        @Valid
        @NotNull(message = "La promoción es obligatoria.")
        EntityReferenceDto promocion,

        @NotNull(message = "La fecha de inicio es obligatoria.")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha fin es obligatoria.")
        LocalDateTime fechaFin,

        EstadoPromocion estadoPromocion,

        Boolean visiblePublico,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo,

        @Valid
        List<PromocionSkuDescuentoCreateRequestDto> descuentos
) {
}