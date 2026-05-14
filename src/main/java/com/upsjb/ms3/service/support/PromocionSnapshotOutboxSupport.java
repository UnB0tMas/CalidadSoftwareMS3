// ruta: src/main/java/com/upsjb/ms3/service/support/PromocionSnapshotOutboxSupport.java
package com.upsjb.ms3.service.support;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import com.upsjb.ms3.kafka.event.PromocionSkuDescuentoPayload;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.PromocionSnapshotPayload;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.PromocionVersionRepository;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromocionSnapshotOutboxSupport {

    private final PromocionVersionRepository promocionVersionRepository;
    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;
    private final EventoDominioOutboxService eventoDominioOutboxService;

    public void registrarSnapshot(
            Promocion promocion,
            PromocionVersion version,
            PromocionEventType eventType,
            String source
    ) {
        if (promocion == null || promocion.getIdPromocion() == null) {
            return;
        }

        AuditContext context = AuditContextHolder.getOrEmpty();

        PromocionSnapshotEvent event = PromocionSnapshotEvent.of(
                eventType,
                promocion.getIdPromocion(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toPayload(promocion, version),
                Map.of("source", source == null ? "PromocionService" : source)
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private PromocionSnapshotPayload toPayload(Promocion promocion, PromocionVersion version) {
        PromocionVersion effectiveVersion = version == null
                ? currentVersion(promocion)
                : version;

        List<PromocionSkuDescuentoPayload> descuentos = effectiveVersion == null
                ? List.of()
                : promocionSkuDescuentoRepository
                .findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
                        effectiveVersion.getIdPromocionVersion()
                )
                .stream()
                .map(this::toDescuentoPayload)
                .toList();

        return PromocionSnapshotPayload.builder()
                .idPromocion(promocion.getIdPromocion())
                .codigo(promocion.getCodigo())
                .nombre(promocion.getNombre())
                .descripcion(promocion.getDescripcion())
                .creadoPorIdUsuarioMs1(promocion.getCreadoPorIdUsuarioMs1())
                .idPromocionVersion(effectiveVersion == null ? null : effectiveVersion.getIdPromocionVersion())
                .fechaInicio(effectiveVersion == null ? null : effectiveVersion.getFechaInicio())
                .fechaFin(effectiveVersion == null ? null : effectiveVersion.getFechaFin())
                .estadoPromocion(effectiveVersion == null || effectiveVersion.getEstadoPromocion() == null
                        ? null
                        : effectiveVersion.getEstadoPromocion().getCode())
                .visiblePublico(effectiveVersion == null ? null : effectiveVersion.getVisiblePublico())
                .vigente(effectiveVersion == null ? null : effectiveVersion.getVigente())
                .motivo(effectiveVersion == null ? null : effectiveVersion.getMotivo())
                .estado(promocion.getEstado())
                .createdAt(promocion.getCreatedAt())
                .updatedAt(promocion.getUpdatedAt())
                .descuentos(descuentos)
                .build();
    }

    private PromocionSkuDescuentoPayload toDescuentoPayload(PromocionSkuDescuentoVersion descuento) {
        ProductoSku sku = descuento.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        PromocionVersion version = descuento.getPromocionVersion();
        Promocion promocion = version == null ? null : version.getPromocion();

        return PromocionSkuDescuentoPayload.builder()
                .idPromocionSkuDescuentoVersion(descuento.getIdPromocionSkuDescuentoVersion())
                .idPromocionVersion(version == null ? null : version.getIdPromocionVersion())
                .idPromocion(promocion == null ? null : promocion.getIdPromocion())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .tipoDescuento(descuento.getTipoDescuento() == null ? null : descuento.getTipoDescuento().getCode())
                .valorDescuento(descuento.getValorDescuento())
                .precioFinalEstimado(descuento.getPrecioFinalEstimado())
                .margenEstimado(descuento.getMargenEstimado())
                .limiteUnidades(descuento.getLimiteUnidades())
                .prioridad(descuento.getPrioridad())
                .estado(descuento.getEstado())
                .createdAt(descuento.getCreatedAt())
                .updatedAt(descuento.getUpdatedAt())
                .build();
    }

    private PromocionVersion currentVersion(Promocion promocion) {
        if (promocion == null || promocion.getIdPromocion() == null) {
            return null;
        }

        return promocionVersionRepository
                .findFirstByPromocion_IdPromocionAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
                        promocion.getIdPromocion()
                )
                .orElse(null);
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }
}