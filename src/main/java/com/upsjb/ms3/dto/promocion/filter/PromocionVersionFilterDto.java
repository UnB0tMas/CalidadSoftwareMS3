// ruta: src/main/java/com/upsjb/ms3/dto/promocion/filter/PromocionVersionFilterDto.java
package com.upsjb.ms3.dto.promocion.filter;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PromocionVersionFilterDto(

        @Size(max = 180, message = "El texto de búsqueda no debe superar 180 caracteres.")
        String search,

        Long idPromocion,

        @Size(max = 80, message = "El código de promoción no debe superar 80 caracteres.")
        String codigoPromocion,

        @Size(max = 180, message = "El nombre de promoción no debe superar 180 caracteres.")
        String nombrePromocion,

        EstadoPromocion estadoPromocion,

        Boolean visiblePublico,

        Boolean vigente,

        Boolean estado,

        @Valid
        DateRangeFilterDto vigencia,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}