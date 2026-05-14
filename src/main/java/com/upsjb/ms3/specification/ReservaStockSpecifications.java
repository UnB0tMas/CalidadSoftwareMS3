// ruta: src/main/java/com/upsjb/ms3/specification/ReservaStockSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.inventario.reserva.filter.ReservaStockFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ReservaStockSpecifications {

    private ReservaStockSpecifications() {
    }

    public static Specification<ReservaStock> fromFilter(ReservaStockFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<ReservaStock>create()
                .textSearch(
                        SpecificationFilterSupport.text(filter, "search"),
                        "codigoReserva",
                        "referenciaIdExterno",
                        "motivo",
                        "sku.codigoSku",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "almacen.codigo",
                        "almacen.nombre"
                )
                .like("codigoReserva", SpecificationFilterSupport.text(filter, "codigoReserva"))
                .equal("sku.idSku", SpecificationFilterSupport.longValue(filter, "idSku"))
                .like("sku.codigoSku", SpecificationFilterSupport.text(filter, "codigoSku"))
                .equal("sku.producto.idProducto", SpecificationFilterSupport.longValue(filter, "idProducto"))
                .like("sku.producto.codigoProducto", SpecificationFilterSupport.text(filter, "codigoProducto"))
                .equal("almacen.idAlmacen", SpecificationFilterSupport.longValue(filter, "idAlmacen"))
                .like("almacen.codigo", SpecificationFilterSupport.text(filter, "codigoAlmacen"))
                .equal("referenciaTipo", SpecificationFilterSupport.value(filter, TipoReferenciaStock.class, "referenciaTipo"))
                .like("referenciaIdExterno", SpecificationFilterSupport.text(filter, "referenciaIdExterno"))
                .equal("estadoReserva", SpecificationFilterSupport.value(filter, EstadoReservaStock.class, "estadoReserva"))
                .equal("reservadoPorIdUsuarioMs1", SpecificationFilterSupport.longValue(filter, "reservadoPorIdUsuarioMs1"))
                .equal("confirmadoPorIdUsuarioMs1", SpecificationFilterSupport.longValue(filter, "confirmadoPorIdUsuarioMs1"))
                .equal("liberadoPorIdUsuarioMs1", SpecificationFilterSupport.longValue(filter, "liberadoPorIdUsuarioMs1"))
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("reservadoAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaReserva", "fechaReservado", "reservadoAt")
                ))
                .range("confirmadoAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaConfirmacion", "confirmadoAt")
                ))
                .range("liberadoAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaLiberacion", "liberadoAt")
                ))
                .range("expiresAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaExpiracion", "expiresAt")
                ))
                .range("createdAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaCreacion")
                ))
                .and(expirada(SpecificationFilterSupport.bool(filter, "expirada")))
                .build();
    }

    public static Specification<ReservaStock> activeOnly() {
        return SpecificationBuilder.<ReservaStock>create()
                .activeOnly()
                .build();
    }

    public static Specification<ReservaStock> pendientesOnly() {
        return SpecificationBuilder.<ReservaStock>create()
                .activeOnly()
                .equal("estadoReserva", EstadoReservaStock.RESERVADA)
                .build();
    }

    public static Specification<ReservaStock> byReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno
    ) {
        return SpecificationBuilder.<ReservaStock>create()
                .equal("referenciaTipo", referenciaTipo)
                .like("referenciaIdExterno", referenciaIdExterno)
                .build();
    }

    public static Specification<ReservaStock> expirada(Boolean expirada) {
        return (root, query, cb) -> {
            if (expirada == null) {
                return cb.conjunction();
            }

            LocalDateTime now = LocalDateTime.now();

            if (Boolean.TRUE.equals(expirada)) {
                return cb.and(
                        cb.isNotNull(root.get("expiresAt")),
                        cb.lessThan(root.get("expiresAt"), now),
                        cb.equal(root.get("estadoReserva"), EstadoReservaStock.RESERVADA)
                );
            }

            return cb.or(
                    cb.isNull(root.get("expiresAt")),
                    cb.greaterThanOrEqualTo(root.get("expiresAt"), now),
                    cb.notEqual(root.get("estadoReserva"), EstadoReservaStock.RESERVADA)
            );
        };
    }

    private static Boolean resolveEstado(ReservaStockFilterDto filter) {
        Boolean estado = SpecificationFilterSupport.bool(filter, "estado");
        return estado == null ? Boolean.TRUE : estado;
    }
}