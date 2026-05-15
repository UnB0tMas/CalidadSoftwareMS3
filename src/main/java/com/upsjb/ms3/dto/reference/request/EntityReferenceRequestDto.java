// ruta: src/main/java/com/upsjb/ms3/dto/reference/request/EntityReferenceRequestDto.java
package com.upsjb.ms3.dto.reference.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EntityReferenceRequestDto(

        @NotBlank(message = "La entidad es obligatoria.")
        @Size(max = 80, message = "La entidad no debe superar 80 caracteres.")
        String entidad,

        @Valid
        @NotNull(message = "La referencia es obligatoria.")
        EntityReferenceDto referencia,

        Boolean soloActivos
) {
}