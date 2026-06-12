package com.upsjb.ms3.dto.catalogo.categoria.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoriaUpdateRequestDto(

        @Valid
        EntityReferenceDto categoriaPadre,

        @NotBlank(
                message = "El nombre de la categoría es obligatorio."
        )
        @Size(
                max = 150,
                message = "El nombre no debe superar 150 caracteres."
        )
        String nombre,

        @Size(
                max = 500,
                message = "La descripción no debe superar 500 caracteres."
        )
        String descripcion,

        @Min(
                value = 0,
                message = "El orden no puede ser negativo."
        )
        Integer orden,

        @NotNull(
                message = "Debe indicar si la categoría admite productos."
        )
        Boolean permiteProductos

) {
}
