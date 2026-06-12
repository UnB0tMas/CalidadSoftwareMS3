package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductoSnapshotPayload(

        Boolean snapshotCompleto,
        LocalDateTime snapshotGeneradoAt,

        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,

        Long idCategoria,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Integer nivelCategoria,
        Integer ordenCategoria,
        Boolean categoriaPermiteProductos,
        Boolean categoriaEstado,

        Long idCategoriaPadre,
        String codigoCategoriaPadre,
        String nombreCategoriaPadre,
        String slugCategoriaPadre,

        String categoriaRutaCodigo,
        String categoriaRutaNombre,
        List<CategoriaRutaSnapshotPayload> categoriaRuta,

        Long idMarca,
        String codigoMarca,
        String nombreMarca,
        String slugMarca,
        Boolean marcaEstado,

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

        String imagenPrincipalUrl,

        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        List<CategoriaAtributoSnapshotPayload> plantillaAtributos,
        List<ProductoAtributoSnapshotPayload> atributos,
        List<ProductoSkuSnapshotPayload> skus,
        List<ProductoImagenSnapshotPayload> imagenes

) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaRutaSnapshotPayload(
            Long idCategoria,
            String codigo,
            String nombre,
            String slug,
            Integer nivel,
            Integer orden,
            Boolean permiteProductos,
            Boolean estado
    ) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaAtributoSnapshotPayload(
            Long idCategoriaAtributo,
            Long idAtributo,
            String codigoAtributo,
            String nombreAtributo,
            String tipoDato,
            String unidadMedida,
            Boolean requeridoBase,
            Boolean requeridoCategoria,
            Boolean requerido,
            Boolean filtrable,
            Boolean visiblePublico,
            Integer orden,
            Boolean estado
    ) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProductoAtributoSnapshotPayload(
            Long idProductoAtributoValor,
            Long idProducto,
            String codigoProducto,
            Long idAtributo,
            String codigoAtributo,
            String nombreAtributo,
            String tipoDato,
            String unidadMedida,
            Boolean requeridoBase,
            Boolean requeridoCategoria,
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