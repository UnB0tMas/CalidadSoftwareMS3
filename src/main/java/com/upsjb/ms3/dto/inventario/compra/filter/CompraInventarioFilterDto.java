// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/filter/CompraInventarioFilterDto.java
package com.upsjb.ms3.dto.inventario.compra.filter;

import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CompraInventarioFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        String codigoCompra,
        Long idProveedor,
        String proveedorDocumento,
        String proveedorNombre,
        Moneda moneda,
        EstadoCompraInventario estadoCompra,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCompra,
        @Valid DateRangeFilterDto fechaCreacion
) {
}