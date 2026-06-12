package com.upsjb.ms3.shared.outbox;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductoSnapshotOutboxRegistrar {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    private final EventoDominioOutboxService
            eventoDominioOutboxService;

    @Transactional
    public void registrarProductoActualizado(
            Long idProducto,
            String source,
            Map<String, Object> metadata
    ) {
        if (idProducto == null) {
            return;
        }

        productoRepository
                .findById(
                        idProducto
                )
                .ifPresent(
                        producto ->
                                registrarProductoActualizado(
                                        producto,
                                        source,
                                        metadata
                                )
                );
    }

    @Transactional
    public void registrarProductoActualizado(
            Producto producto,
            String source,
            Map<String, Object> metadata
    ) {
        registrarProducto(
                producto,
                ProductoEventType
                        .PRODUCTO_SNAPSHOT_ACTUALIZADO,
                source,
                metadata
        );
    }

    @Transactional
    public void registrarProductosDeCategoriaActualizados(
            Long idCategoria,
            String source,
            Map<String, Object> metadata
    ) {
        if (idCategoria == null) {
            return;
        }

        List<Producto> productos =
                productoRepository
                        .findByCategoria_IdCategoriaOrderByIdProductoAsc(
                                idCategoria
                        );

        Map<String, Object> batchMetadata =
                mergeMetadata(
                        metadata,
                        Map.of(
                                "idCategoriaOrigen",
                                idCategoria,
                                "incluyeSubcategorias",
                                Boolean.FALSE,
                                "totalProductosAfectados",
                                productos.size()
                        )
                );

        registrarProductos(
                productos,
                source,
                batchMetadata
        );
    }

    @Transactional
    public void registrarProductosDeSubarbolCategoriaActualizados(
            Long idCategoriaRaiz,
            String source,
            Map<String, Object> metadata
    ) {
        if (idCategoriaRaiz == null) {
            return;
        }

        Set<Long> categoryIds =
                resolveSubtreeCategoryIds(
                        idCategoriaRaiz
                );

        if (categoryIds.isEmpty()) {
            return;
        }

        List<Producto> productos =
                productoRepository
                        .findByCategoria_IdCategoriaInOrderByIdProductoAsc(
                                categoryIds
                        );

        Map<String, Object> batchMetadata =
                mergeMetadata(
                        metadata,
                        Map.of(
                                "idCategoriaRaiz",
                                idCategoriaRaiz,
                                "incluyeSubcategorias",
                                Boolean.TRUE,
                                "totalCategoriasAfectadas",
                                categoryIds.size(),
                                "totalProductosAfectados",
                                productos.size()
                        )
                );

        registrarProductos(
                productos,
                source,
                batchMetadata
        );
    }

    @Transactional
    public void registrarProductosDeMarcaActualizados(
            Long idMarca,
            String source,
            Map<String, Object> metadata
    ) {
        if (idMarca == null) {
            return;
        }

        List<Producto> productos =
                productoRepository
                        .findByMarca_IdMarcaOrderByIdProductoAsc(
                                idMarca
                        );

        Map<String, Object> batchMetadata =
                mergeMetadata(
                        metadata,
                        Map.of(
                                "idMarcaOrigen",
                                idMarca,
                                "totalProductosAfectados",
                                productos.size()
                        )
                );

        registrarProductos(
                productos,
                source,
                batchMetadata
        );
    }

    private void registrarProductos(
            Collection<Producto> productos,
            String source,
            Map<String, Object> metadata
    ) {
        if (
                productos == null
                        || productos.isEmpty()
        ) {
            return;
        }

        for (
                Producto producto
                : productos
        ) {
            registrarProducto(
                    producto,
                    ProductoEventType
                            .PRODUCTO_SNAPSHOT_ACTUALIZADO,
                    source,
                    metadata
            );
        }
    }

    private void registrarProducto(
            Producto producto,
            ProductoEventType eventType,
            String source,
            Map<String, Object> metadata
    ) {
        if (
                producto == null
                        || producto.getIdProducto() == null
        ) {
            return;
        }

        AuditContext context =
                AuditContextHolder
                        .getOrEmpty();

        /*
         * Este payload es intencionalmente mínimo.
         * OutboxEventFactory lo reemplaza por el snapshot
         * canónico completo antes de persistirlo.
         */
        ProductoSnapshotPayload placeholder =
                ProductoSnapshotPayload.builder()
                        .idProducto(
                                producto.getIdProducto()
                        )
                        .codigoProducto(
                                producto.getCodigoProducto()
                        )
                        .build();

        ProductoSnapshotEvent event =
                ProductoSnapshotEvent.of(
                        eventType,
                        producto.getIdProducto(),
                        traceValue(
                                context.requestId()
                        ),
                        traceValue(
                                context.correlationId()
                        ),
                        placeholder,
                        eventMetadata(
                                source,
                                metadata
                        )
                );

        eventoDominioOutboxService
                .registrarEvento(
                        event
                );
    }

    private Set<Long> resolveSubtreeCategoryIds(
            Long rootCategoryId
    ) {
        List<Categoria> categories =
                categoriaRepository
                        .findAll();

        Map<Long, List<Long>> childrenByParent =
                new LinkedHashMap<>();

        boolean rootExists =
                false;

        for (
                Categoria category
                : categories
        ) {
            if (
                    category == null
                            || category.getIdCategoria() == null
            ) {
                continue;
            }

            if (
                    rootCategoryId.equals(
                            category.getIdCategoria()
                    )
            ) {
                rootExists =
                        true;
            }

            Categoria parent =
                    category.getCategoriaPadre();

            if (
                    parent == null
                            || parent.getIdCategoria() == null
            ) {
                continue;
            }

            childrenByParent
                    .computeIfAbsent(
                            parent.getIdCategoria(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            category.getIdCategoria()
                    );
        }

        if (!rootExists) {
            return Set.of();
        }

        Set<Long> result =
                new LinkedHashSet<>();

        Deque<Long> pending =
                new ArrayDeque<>();

        pending.add(
                rootCategoryId
        );

        while (!pending.isEmpty()) {
            Long currentId =
                    pending.removeFirst();

            if (!result.add(
                    currentId
            )) {
                continue;
            }

            pending.addAll(
                    childrenByParent
                            .getOrDefault(
                                    currentId,
                                    List.of()
                            )
            );
        }

        return result;
    }

    private Map<String, Object> eventMetadata(
            String source,
            Map<String, Object> metadata
    ) {
        Map<String, Object> values =
                new LinkedHashMap<>();

        values.put(
                "source",
                StringNormalizer.hasText(
                        source
                )
                        ? source.trim()
                        : "MS3"
        );

        values.put(
                "snapshotCompleto",
                Boolean.TRUE
        );

        if (metadata != null) {
            metadata.forEach(
                    (key, value) -> {
                        if (
                                StringNormalizer.hasText(
                                        key
                                )
                                        && value != null
                        ) {
                            values.put(
                                    key.trim(),
                                    value
                            );
                        }
                    }
            );
        }

        return values;
    }

    private Map<String, Object> mergeMetadata(
            Map<String, Object> original,
            Map<String, Object> additional
    ) {
        Map<String, Object> result =
                new LinkedHashMap<>();

        if (original != null) {
            result.putAll(
                    original
            );
        }

        if (additional != null) {
            result.putAll(
                    additional
            );
        }

        return result;
    }

    private String traceValue(
            String value
    ) {
        return StringNormalizer.hasText(
                value
        )
                ? value.trim()
                : UUID.randomUUID()
                .toString();
    }
}