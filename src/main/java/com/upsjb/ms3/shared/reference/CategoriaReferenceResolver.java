package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CategoriaReferenceResolver {

    private final CategoriaRepository categoriaRepository;
    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    @Transactional(readOnly = true)
    public Categoria resolve(
            Long idCategoria,
            String codigo,
            String slug,
            String nombre
    ) {
        Optional<Categoria> resolved = resolveByStableReference(
                idCategoria,
                codigo,
                slug
        );

        if (resolved.isPresent()) {
            return resolved.get();
        }

        if (StringUtils.hasText(nombre)) {
            List<Categoria> matches = categoriaRepository
                    .findByNombreIgnoreCaseAndEstadoTrueOrderByNivelAscOrdenAscIdCategoriaAsc(
                            nombre.trim()
                    );

            if (matches.size() == 1) {
                return matches.getFirst();
            }

            if (matches.size() > 1) {
                throw new ConflictException(
                        "CATEGORIA_REFERENCIA_AMBIGUA",
                        "Existen varias categorías activas con el nombre indicado. Use el id, código o slug de la categoría."
                );
            }
        }

        throw NotFoundException.activeResource(
                "Categoría",
                referenceLabel(
                        idCategoria,
                        codigo,
                        slug,
                        nombre
                )
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(
            String search,
            Integer limit
    ) {
        return displayResolver.searchActive(
                        Categoria.class,
                        List.of(
                                "codigo",
                                "slug",
                                "nombre"
                        ),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(
            Categoria entity
    ) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity)
                        .orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                null,
                displayResolver.isActive(entity),
                displayResolver.metadata(
                        entity,
                        "codigo",
                        "slug",
                        "nombre",
                        "nivel",
                        "orden"
                )
        );
    }

    private Optional<Categoria> resolveByStableReference(
            Long idCategoria,
            String codigo,
            String slug
    ) {
        if (idCategoria != null) {
            Optional<Categoria> byId = categoriaRepository
                    .findByIdCategoriaAndEstadoTrue(
                            idCategoria
                    );

            if (byId.isPresent()) {
                return byId;
            }
        }

        if (StringUtils.hasText(codigo)) {
            Optional<Categoria> byCodigo = categoriaRepository
                    .findByCodigoIgnoreCaseAndEstadoTrue(
                            codigo.trim()
                    );

            if (byCodigo.isPresent()) {
                return byCodigo;
            }
        }

        if (StringUtils.hasText(slug)) {
            return categoriaRepository
                    .findBySlugIgnoreCaseAndEstadoTrue(
                            slug.trim()
                    );
        }

        return Optional.empty();
    }

    private Map<String, Object> referenceLabel(
            Long idCategoria,
            String codigo,
            String slug,
            String nombre
    ) {
        Map<String, Object> references =
                new LinkedHashMap<>();

        if (idCategoria != null) {
            references.put(
                    "idCategoria",
                    idCategoria
            );
        }

        if (StringUtils.hasText(codigo)) {
            references.put(
                    "codigo",
                    codigo.trim()
            );
        }

        if (StringUtils.hasText(slug)) {
            references.put(
                    "slug",
                    slug.trim()
            );
        }

        if (StringUtils.hasText(nombre)) {
            references.put(
                    "nombre",
                    nombre.trim()
            );
        }

        if (references.isEmpty()) {
            references.put(
                    "referencia",
                    "sin referencia"
            );
        }

        return references;
    }
}
