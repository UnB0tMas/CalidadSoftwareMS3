// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoImagenUploadRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record ProductoImagenUploadRequestDto(

        @Valid
        @NotNull(message = "El producto es obligatorio.")
        EntityReferenceDto producto,

        @Valid
        EntityReferenceDto sku,

        @NotNull(message = "El archivo de imagen es obligatorio.")
        MultipartFile archivo,

        @Size(max = 250, message = "El texto alternativo no debe superar 250 caracteres.")
        String altText,

        @Size(max = 180, message = "El título no debe superar 180 caracteres.")
        String titulo,

        @Min(value = 0, message = "El orden no puede ser negativo.")
        Integer orden,

        Boolean principal
) {
}