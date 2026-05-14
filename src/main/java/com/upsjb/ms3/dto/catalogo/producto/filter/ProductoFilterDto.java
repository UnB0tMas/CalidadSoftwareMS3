// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/filter/ProductoFilterDto.java
package com.upsjb.ms3.dto.catalogo.producto.filter;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        String codigoProducto,
        String nombre,
        String slug,
        Long idTipoProducto,
        Long idCategoria,
        Long idMarca,
        GeneroObjetivo generoObjetivo,
        String temporada,
        String deporte,
        EstadoProductoRegistro estadoRegistro,
        EstadoProductoPublicacion estadoPublicacion,
        EstadoProductoVenta estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCreacion
) {
}