package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductoSkuSnapshotPayload(

        Long idSku,
        Long idProducto,
        String codigoProducto,
        String codigoSku,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal pesoGramos,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        String estadoSku,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SkuAtributoSnapshotPayload> atributos

) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SkuAtributoSnapshotPayload(
            Long idSkuAtributoValor,
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