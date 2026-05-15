// ruta: src/main/java/com/upsjb/ms3/kafka/event/PromocionSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionSnapshotPayload(
        Long idPromocion,
        String codigo,
        String nombre,
        String descripcion,
        Long creadoPorIdUsuarioMs1,
        Long idPromocionVersion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        String motivo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PromocionSkuDescuentoPayload> descuentos
) {
}