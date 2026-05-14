// ruta: src/main/java/com/upsjb/ms3/dto/shared/EntityReferenceDto.java
package com.upsjb.ms3.dto.shared;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EntityReferenceDto(
        Long id,

        @Size(max = 100, message = "El código no debe superar 100 caracteres.")
        String codigo,

        @Size(max = 250, message = "El nombre no debe superar 250 caracteres.")
        String nombre,

        @Size(max = 250, message = "El slug no debe superar 250 caracteres.")
        String slug,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumento,

        @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
        String ruc
) {
}