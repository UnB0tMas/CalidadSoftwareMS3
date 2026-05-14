// ruta: src/main/java/com/upsjb/ms3/kafka/event/ProductoSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductoSnapshotPayload(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        Long idTipoProducto,
        String codigoTipoProducto,
        String nombreTipoProducto,
        Long idCategoria,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Long idMarca,
        String codigoMarca,
        String nombreMarca,
        String slugMarca,
        String descripcionCorta,
        String descripcionLarga,
        String generoObjetivo,
        String temporada,
        String deporte,
        String estadoRegistro,
        String estadoPublicacion,
        String estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        LocalDateTime fechaPublicacionInicio,
        LocalDateTime fechaPublicacionFin,
        String motivoEstado,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductoSkuSnapshotPayload> skus,
        List<ProductoImagenSnapshotPayload> imagenes
) {
}