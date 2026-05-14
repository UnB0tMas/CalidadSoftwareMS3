// ruta: src/main/java/com/upsjb/ms3/dto/promocion/filter/PromocionVersionFilterDto.java
package com.upsjb.ms3.dto.promocion.filter;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
public record PromocionVersionFilterDto(
        Long idPromocion,
        String codigoPromocion,
        EstadoPromocion estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        Boolean estado,
        @Valid DateRangeFilterDto vigencia,
        @Valid DateRangeFilterDto fechaCreacion
) {
}