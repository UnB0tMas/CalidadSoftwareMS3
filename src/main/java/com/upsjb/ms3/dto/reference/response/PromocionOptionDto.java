// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/PromocionOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record PromocionOptionDto(
        Long idPromocion,
        String codigo,
        String nombre,
        String descripcion,
        Boolean estado
) {
}