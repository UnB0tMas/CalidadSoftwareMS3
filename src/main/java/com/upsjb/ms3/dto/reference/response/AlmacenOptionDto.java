// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/AlmacenOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record AlmacenOptionDto(
        Long idAlmacen,
        String codigo,
        String nombre,
        String direccion,
        Boolean principal,
        Boolean permiteVenta,
        Boolean permiteCompra,
        Boolean estado
) {
}