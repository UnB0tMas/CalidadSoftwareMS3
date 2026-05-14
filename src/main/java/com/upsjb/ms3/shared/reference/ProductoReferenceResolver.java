package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Producto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProductoReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public Producto resolve(Long idProducto, String codigoProducto, String slug, String nombre) {
        return displayResolver.resolveRequired(
                Producto.class,
                "Producto",
                "idProducto",
                idProducto,
                references(codigoProducto, slug, nombre)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        Producto.class,
                        List.of("codigoProducto", "slug", "nombre"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(Producto entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                null,
                displayResolver.isActive(entity),
                displayResolver.metadata(entity,
                        "codigoProducto",
                        "slug",
                        "nombre",
                        "estadoRegistro",
                        "estadoPublicacion",
                        "estadoVenta",
                        "visiblePublico",
                        "vendible"
                )
        );
    }

    private Map<String, Object> references(String codigoProducto, String slug, String nombre) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (StringUtils.hasText(codigoProducto)) {
            references.put("codigoProducto", codigoProducto.trim());
        }

        if (StringUtils.hasText(slug)) {
            references.put("slug", slug.trim());
        }

        if (StringUtils.hasText(nombre)) {
            references.put("nombre", nombre.trim());
        }

        return references;
    }
}