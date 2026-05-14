// ruta: src/main/java/com/upsjb/ms3/kafka/event/PromocionSkuDescuentoPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoPayload(
        Long idPromocionSkuDescuentoVersion,
        Long idPromocionVersion,
        Long idPromocion,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        String tipoDescuento,
        BigDecimal valorDescuento,
        BigDecimal precioFinalEstimado,
        BigDecimal margenEstimado,
        Integer limiteUnidades,
        Integer prioridad,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}