// ruta: src/main/java/com/upsjb/ms3/dto/inventario/almacen/request/AlmacenCreateRequestDto.java
package com.upsjb.ms3.dto.inventario.almacen.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AlmacenCreateRequestDto(

        @NotBlank(message = "El código del almacén es obligatorio.")
        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre del almacén es obligatorio.")
        @Size(max = 150, message = "El nombre no debe superar 150 caracteres.")
        String nombre,

        @Size(max = 300, message = "La dirección no debe superar 300 caracteres.")
        String direccion,

        Boolean principal,

        Boolean permiteVenta,

        Boolean permiteCompra,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}