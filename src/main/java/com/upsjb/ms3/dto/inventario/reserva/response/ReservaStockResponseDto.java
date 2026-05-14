// ruta: src/main/java/com/upsjb/ms3/dto/inventario/reserva/response/ReservaStockResponseDto.java
package com.upsjb.ms3.dto.inventario.reserva.response;

import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ReservaStockResponseDto(
        Long idReservaStock,
        String codigoReserva,
        Boolean codigoGenerado,
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        TipoReferenciaStock referenciaTipo,
        String referenciaIdExterno,
        Integer cantidad,
        EstadoReservaStock estadoReserva,
        Long reservadoPorIdUsuarioMs1,
        Long confirmadoPorIdUsuarioMs1,
        Long liberadoPorIdUsuarioMs1,
        LocalDateTime reservadoAt,
        LocalDateTime confirmadoAt,
        LocalDateTime liberadoAt,
        LocalDateTime expiresAt,
        Boolean expirada,
        String motivo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}