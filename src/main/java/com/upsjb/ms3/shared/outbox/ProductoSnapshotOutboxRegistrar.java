// ruta: src/main/java/com/upsjb/ms3/shared/outbox/ProductoSnapshotOutboxRegistrar.java
package com.upsjb.ms3.shared.outbox;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoSnapshotOutboxRegistrar {

    private final ProductoRepository productoRepository;
    private final EventoDominioOutboxService eventoDominioOutboxService;

    public void registrarProductosDeTipoActualizados(
            Long idTipoProducto,
            String source,
            Map<String, Object> metadata
    ) {
        if (idTipoProducto == null) {
            return;
        }

        productoRepository
                .findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByIdProductoAsc(idTipoProducto)
                .forEach(producto -> registrarProductoActualizado(producto, source, metadata));
    }

    public void registrarProductoActualizado(
            Producto producto,
            String source,
            Map<String, Object> metadata
    ) {
        if (producto == null || producto.getIdProducto() == null) {
            return;
        }

        AuditContext context = AuditContextHolder.getOrEmpty();

        ProductoSnapshotEvent event = ProductoSnapshotEvent.of(
                ProductoEventType.PRODUCTO_SNAPSHOT_ACTUALIZADO,
                producto.getIdProducto(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toPayload(producto),
                eventMetadata(source, metadata)
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private ProductoSnapshotPayload toPayload(Producto producto) {
        TipoProducto tipoProducto = producto.getTipoProducto();
        Categoria categoria = producto.getCategoria();
        Marca marca = producto.getMarca();

        return ProductoSnapshotPayload.builder()
                .idProducto(producto.getIdProducto())
                .codigoProducto(producto.getCodigoProducto())
                .nombre(producto.getNombre())
                .slug(producto.getSlug())
                .idTipoProducto(tipoProducto == null ? null : tipoProducto.getIdTipoProducto())
                .codigoTipoProducto(tipoProducto == null ? null : tipoProducto.getCodigo())
                .nombreTipoProducto(tipoProducto == null ? null : tipoProducto.getNombre())
                .idCategoria(categoria == null ? null : categoria.getIdCategoria())
                .codigoCategoria(categoria == null ? null : categoria.getCodigo())
                .nombreCategoria(categoria == null ? null : categoria.getNombre())
                .slugCategoria(categoria == null ? null : categoria.getSlug())
                .idMarca(marca == null ? null : marca.getIdMarca())
                .codigoMarca(marca == null ? null : marca.getCodigo())
                .nombreMarca(marca == null ? null : marca.getNombre())
                .slugMarca(marca == null ? null : marca.getSlug())
                .descripcionCorta(producto.getDescripcionCorta())
                .descripcionLarga(producto.getDescripcionLarga())
                .generoObjetivo(producto.getGeneroObjetivo() == null ? null : producto.getGeneroObjetivo().getCode())
                .temporada(producto.getTemporada())
                .deporte(producto.getDeporte())
                .estadoRegistro(producto.getEstadoRegistro() == null ? null : producto.getEstadoRegistro().getCode())
                .estadoPublicacion(producto.getEstadoPublicacion() == null ? null : producto.getEstadoPublicacion().getCode())
                .estadoVenta(producto.getEstadoVenta() == null ? null : producto.getEstadoVenta().getCode())
                .visiblePublico(producto.getVisiblePublico())
                .vendible(producto.getVendible())
                .fechaPublicacionInicio(producto.getFechaPublicacionInicio())
                .fechaPublicacionFin(producto.getFechaPublicacionFin())
                .motivoEstado(producto.getMotivoEstado())
                .estado(producto.getEstado())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .build();
    }

    private Map<String, Object> eventMetadata(String source, Map<String, Object> metadata) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("source", StringNormalizer.hasText(source) ? source : "MS3");

        if (metadata != null && !metadata.isEmpty()) {
            metadata.forEach((key, value) -> {
                if (key != null && value != null) {
                    values.put(key, value);
                }
            });
        }

        return values;
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }
}