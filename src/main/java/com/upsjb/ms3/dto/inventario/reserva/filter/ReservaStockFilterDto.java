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

        @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
        String codigoReserva,

        Long idSku,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 180, message = "El nombre de producto no debe superar 180 caracteres.")
        String nombreProducto,

        Long idAlmacen,

        @Size(max = 50, message = "El código de almacén no debe superar 50 caracteres.")
        String codigoAlmacen,

        @Size(max = 150, message = "El nombre de almacén no debe superar 150 caracteres.")
        String nombreAlmacen,

        TipoReferenciaStock referenciaTipo,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        EstadoReservaStock estadoReserva,

        Boolean expirada,

        Boolean expiradas,

        Long reservadoPorIdUsuarioMs1,

        Long confirmadoPorIdUsuarioMs1,

        Long liberadoPorIdUsuarioMs1,

        Boolean estado,

        Boolean incluirTodosLosEstados,

        @Valid
        DateRangeFilterDto fechaReserva,

        @Valid
        DateRangeFilterDto fechaConfirmacion,

        @Valid
        DateRangeFilterDto fechaLiberacion,

        @Valid
        DateRangeFilterDto fechaExpiracion,

        @Valid
        DateRangeFilterDto fechaCreacion,

        @Valid
        DateRangeFilterDto fechaActualizacion
) {
}