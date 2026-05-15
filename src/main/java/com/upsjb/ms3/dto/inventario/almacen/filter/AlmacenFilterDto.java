// ruta: src/main/java/com/upsjb/ms3/dto/inventario/almacen/filter/AlmacenFilterDto.java
package com.upsjb.ms3.dto.inventario.almacen.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AlmacenFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @Size(max = 150, message = "El nombre no debe superar 150 caracteres.")
        String nombre,

        @Size(max = 300, message = "La dirección no debe superar 300 caracteres.")
        String direccion,

        Boolean principal,

        Boolean permiteVenta,

        Boolean permiteCompra,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}