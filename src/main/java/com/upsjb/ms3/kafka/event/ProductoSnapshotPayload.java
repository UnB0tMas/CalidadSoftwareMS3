// ruta: src/main/java/com/upsjb/ms3/kafka/event/ProductoSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        List<ProductoAtributoSnapshotPayload> atributos,
        List<ProductoSkuSnapshotPayload> skus,
        List<ProductoImagenSnapshotPayload> imagenes
) {

    @Builder
    public record ProductoAtributoSnapshotPayload(
            Long idProductoAtributoValor,
            Long idProducto,
            String codigoProducto,
            Long idAtributo,
            String codigoAtributo,
            String nombreAtributo,
            String tipoDato,
            String unidadMedida,
            Boolean requerido,
            Boolean filtrable,
            Boolean visiblePublico,
            String valorTexto,
            BigDecimal valorNumero,
            Boolean valorBoolean,
            LocalDate valorFecha,
            String valorDisplay,
            Boolean estado,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}