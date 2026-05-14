package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Promocion;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PromocionReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public Promocion resolve(Long idPromocion, String codigo, String nombre) {
        return displayResolver.resolveRequired(
                Promocion.class,
                "Promoción",
                "idPromocion",
                idPromocion,
                references(codigo, nombre)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        Promocion.class,
                        List.of("codigo", "nombre", "descripcion"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(Promocion entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                entity == null ? null : entity.getDescripcion(),
                displayResolver.isActive(entity),
                displayResolver.metadata(entity, "codigo", "nombre")
        );
    }

    private Map<String, Object> references(String codigo, String nombre) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (StringUtils.hasText(codigo)) {
            references.put("codigo", codigo.trim());
        }

        if (StringUtils.hasText(nombre)) {
            references.put("nombre", nombre.trim());
        }

        return references;
    }
}