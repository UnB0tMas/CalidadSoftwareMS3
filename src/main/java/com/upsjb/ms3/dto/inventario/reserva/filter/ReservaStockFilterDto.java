// ruta: src/main/java/com/upsjb/ms3/dto/inventario/reserva/filter/ReservaStockFilterDto.java
package com.upsjb.ms3.dto.inventario.reserva.filter;

import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ReservaStockFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        String codigoReserva,

        Long idSku,

        String codigoSku,

        Long idProducto,

        String codigoProducto,

        Long idAlmacen,

        String codigoAlmacen,

        TipoReferenciaStock referenciaTipo,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        EstadoReservaStock estadoReserva,

        Boolean expiradas,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaReserva,

        @Valid
        DateRangeFilterDto fechaExpiracion,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}