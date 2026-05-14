package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Marca;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MarcaReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public Marca resolve(Long idMarca, String codigo, String slug, String nombre) {
        return displayResolver.resolveRequired(
                Marca.class,
                "Marca",
                "idMarca",
                idMarca,
                references(codigo, slug, nombre)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        Marca.class,
                        List.of("codigo", "slug", "nombre"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(Marca entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                null,
                displayResolver.isActive(entity),
                displayResolver.metadata(entity, "codigo", "slug", "nombre")
        );
    }

    private Map<String, Object> references(String codigo, String slug, String nombre) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (StringUtils.hasText(codigo)) {
            references.put("codigo", codigo.trim());
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