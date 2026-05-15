// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoUpdateRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoUpdateRequestDto(

        @Valid
        @NotNull(message = "El tipo de producto es obligatorio.")
        EntityReferenceDto tipoProducto,

        @Valid
        @NotNull(message = "La categoría es obligatoria.")
        EntityReferenceDto categoria,

        @Valid
        EntityReferenceDto marca,

        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 180, message = "El nombre no debe superar 180 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripción corta no debe superar 500 caracteres.")
        String descripcionCorta,

        String descripcionLarga,

        GeneroObjetivo generoObjetivo,

        @Size(max = 80, message = "La temporada no debe superar 80 caracteres.")
        String temporada,

        @Size(max = 80, message = "El deporte no debe superar 80 caracteres.")
        String deporte,

        @Valid
        List<ProductoAtributoValorRequestDto> atributos
) {
}