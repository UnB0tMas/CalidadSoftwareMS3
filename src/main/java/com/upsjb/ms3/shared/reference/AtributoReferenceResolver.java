package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Atributo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AtributoReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public Atributo resolve(Long idAtributo, String codigo, String nombre) {
        return displayResolver.resolveRequired(
                Atributo.class,
                "Atributo",
                "idAtributo",
                idAtributo,
                references(codigo, nombre)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        Atributo.class,
                        List.of("codigo", "nombre"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(Atributo entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                null,
                displayResolver.isActive(entity),
                displayResolver.metadata(entity, "codigo", "nombre", "tipoDato", "unidadMedida", "filtrable", "visiblePublico")
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