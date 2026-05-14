// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoImagenPrincipalRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
public record ProductoImagenPrincipalRequestDto(

        @Valid
        EntityReferenceDto sku,

        Boolean principalProducto,

        Boolean principalSku
) {
}