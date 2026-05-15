// ruta: src/main/java/com/upsjb/ms3/specification/ReservaStockSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
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
                        filter.search(),
                        "codigoReserva",
                        "referenciaIdExterno",
                        "motivo",
                        "sku.codigoSku",
                        "sku.barcode",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "almacen.codigo",
                        "almacen.nombre"
                )
                .like("codigoReserva", filter.codigoReserva())
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .like("sku.barcode", filter.barcode())
                .equal("sku.producto.idProducto", filter.idProducto())
                .like("sku.producto.codigoProducto", filter.codigoProducto())
                .like("sku.producto.nombre", filter.nombreProducto())
                .equal("almacen.idAlmacen", filter.idAlmacen())
                .like("almacen.codigo", filter.codigoAlmacen())
                .like("almacen.nombre", filter.nombreAlmacen())
                .equal("referenciaTipo", filter.referenciaTipo())
                .like("referenciaIdExterno", filter.referenciaIdExterno())
                .equal("estadoReserva", filter.estadoReserva())
                .equal("reservadoPorIdUsuarioMs1", filter.reservadoPorIdUsuarioMs1())
                .equal("confirmadoPorIdUsuarioMs1", filter.confirmadoPorIdUsuarioMs1())
                .equal("liberadoPorIdUsuarioMs1", filter.liberadoPorIdUsuarioMs1())
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("reservadoAt", SpecificationFilterSupport.dateRange(filter.fechaReserva()))
                .range("confirmadoAt", SpecificationFilterSupport.dateRange(filter.fechaConfirmacion()))
                .range("liberadoAt", SpecificationFilterSupport.dateRange(filter.fechaLiberacion()))
                .range("expiresAt", SpecificationFilterSupport.dateRange(filter.fechaExpiracion()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .range("updatedAt", SpecificationFilterSupport.dateRange(filter.fechaActualizacion()))
                .and(expirada(resolveExpirada(filter)))
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
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }

    private static Boolean resolveExpirada(ReservaStockFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return filter.expirada() == null ? filter.expiradas() : filter.expirada();
    }
}