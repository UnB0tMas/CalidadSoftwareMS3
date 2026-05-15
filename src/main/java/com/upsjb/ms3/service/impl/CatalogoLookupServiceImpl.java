// ruta: src/main/java/com/upsjb/ms3/service/impl/CatalogoLookupServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.dto.reference.filter.ReferenceSearchFilterDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.reference.response.EmpleadoInventarioOptionDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoSkuOptionDto;
import com.upsjb.ms3.dto.reference.response.PromocionOptionDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.mapper.CatalogoLookupMapper;
import com.upsjb.ms3.policy.CatalogoLookupPolicy;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.AtributoRepository;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms3.repository.MarcaRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.PromocionRepository;
import com.upsjb.ms3.repository.ProveedorRepository;
import com.upsjb.ms3.repository.TipoProductoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.CatalogoLookupService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.util.StringNormalizer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoLookupServiceImpl implements CatalogoLookupService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final TipoProductoRepository tipoProductoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final AtributoRepository atributoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final ProveedorRepository proveedorRepository;
    private final AlmacenRepository almacenRepository;
    private final PromocionRepository promocionRepository;
    private final EmpleadoSnapshotMs2Repository empleadoSnapshotMs2Repository;
    private final CatalogoLookupMapper catalogoLookupMapper;
    private final CurrentUserResolver currentUserResolver;
    private final CatalogoLookupPolicy catalogoLookupPolicy;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<TipoProductoOptionDto>> buscarTiposProducto(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<TipoProductoOptionDto> data = lookup(
                tipoProductoRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toTipoProductoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaOptionDto>> buscarCategorias(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<CategoriaOptionDto> data = lookup(
                categoriaRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "slug", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toCategoriaOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<MarcaOptionDto>> buscarMarcas(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<MarcaOptionDto> data = lookup(
                marcaRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "slug", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toMarcaOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoOptionDto>> buscarAtributos(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<AtributoOptionDto> data = lookup(
                atributoRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "unidadMedida"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toAtributoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoOptionDto>> buscarProductos(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<ProductoOptionDto> data = lookup(
                productoRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigoProducto", "nombre", "slug", "descripcionCorta"),
                        Map.of("codigo", "codigoProducto", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toProductoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoSkuOptionDto>> buscarSkus(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<ProductoSkuOptionDto> data = lookup(
                productoSkuRepository,
                lookupSpec(
                        safeFilter,
                        List.of(
                                "codigoSku",
                                "barcode",
                                "color",
                                "talla",
                                "material",
                                "modelo",
                                "producto.codigoProducto",
                                "producto.nombre"
                        ),
                        Map.of(
                                "codigo", "codigoSku",
                                "barcode", "barcode",
                                "nombre", "producto.nombre",
                                "slug", "producto.slug"
                        )
                ),
                "codigoSku",
                safeFilter
        ).stream().map(catalogoLookupMapper::toProductoSkuOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProveedorOptionDto>> buscarProveedores(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<ProveedorOptionDto> data = lookup(
                proveedorRepository,
                lookupSpec(
                        safeFilter,
                        List.of(
                                "ruc",
                                "numeroDocumento",
                                "razonSocial",
                                "nombreComercial",
                                "nombres",
                                "apellidos",
                                "correo",
                                "telefono"
                        ),
                        Map.of(
                                "ruc", "ruc",
                                "numeroDocumento", "numeroDocumento",
                                "nombre", "razonSocial"
                        )
                ),
                "razonSocial",
                safeFilter
        ).stream().map(catalogoLookupMapper::toProveedorOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AlmacenOptionDto>> buscarAlmacenes(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<AlmacenOptionDto> data = lookup(
                almacenRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "direccion", "observacion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toAlmacenOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PromocionOptionDto>> buscarPromociones(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<PromocionOptionDto> data = lookup(
                promocionRepository,
                lookupSpec(
                        safeFilter,
                        List.of("codigo", "nombre", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                safeFilter
        ).stream().map(catalogoLookupMapper::toPromocionOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EmpleadoInventarioOptionDto>> buscarEmpleadosInventario(
            ReferenceSearchFilterDto filter
    ) {
        ensureLookupAllowed();
        ReferenceSearchFilterDto safeFilter = normalizeFilter(filter);

        List<EmpleadoInventarioOptionDto> data = lookup(
                empleadoSnapshotMs2Repository,
                empleadoLookupSpec(safeFilter),
                "nombresCompletos",
                safeFilter
        ).stream().map(catalogoLookupMapper::toEmpleadoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    private void ensureLookupAllowed() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        catalogoLookupPolicy.ensureCanLookup(actor);
    }

    private <T> List<T> lookup(
            JpaSpecificationExecutor<T> repository,
            Specification<T> specification,
            String sortBy,
            ReferenceSearchFilterDto filter
    ) {
        return repository.findAll(
                specification,
                PageRequest.of(
                        0,
                        sanitizeLimit(filter == null ? null : filter.limit()),
                        Sort.by(Sort.Direction.ASC, sortBy)
                )
        ).getContent();
    }

    private Specification<EmpleadoSnapshotMs2> empleadoLookupSpec(ReferenceSearchFilterDto filter) {
        Specification<EmpleadoSnapshotMs2> base = lookupSpec(
                filter,
                List.of("codigoEmpleado", "nombresCompletos", "areaCodigo", "areaNombre"),
                Map.of("codigo", "codigoEmpleado", "nombre", "nombresCompletos")
        );

        if (!activeOnly(filter)) {
            return base;
        }

        return base.and((root, query, cb) -> cb.isTrue(root.get("empleadoActivo")));
    }

    private <T> Specification<T> lookupSpec(
            ReferenceSearchFilterDto filter,
            List<String> searchFields,
            Map<String, String> filterFieldMap
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (activeOnly(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
            }

            String search = clean(filter == null ? null : filter.search());
            if (StringNormalizer.hasText(search)) {
                List<Predicate> searchPredicates = new ArrayList<>();
                for (String field : searchFields) {
                    searchPredicates.add(likePredicate(root, cb, field, search));
                }
                predicates.add(cb.or(searchPredicates.toArray(Predicate[]::new)));
            }

            Map<String, String> safeFieldMap = filterFieldMap == null ? Map.of() : filterFieldMap;
            addStringPredicate(predicates, root, cb, safeFieldMap, "codigo", filter == null ? null : filter.codigo());
            addStringPredicate(predicates, root, cb, safeFieldMap, "nombre", filter == null ? null : filter.nombre());
            addStringPredicate(predicates, root, cb, safeFieldMap, "slug", filter == null ? null : filter.slug());
            addStringPredicate(predicates, root, cb, safeFieldMap, "barcode", filter == null ? null : filter.barcode());
            addStringPredicate(
                    predicates,
                    root,
                    cb,
                    safeFieldMap,
                    "numeroDocumento",
                    filter == null ? null : filter.numeroDocumento()
            );
            addStringPredicate(predicates, root, cb, safeFieldMap, "ruc", filter == null ? null : filter.ruc());

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private <T> Predicate likePredicate(
            Path<T> root,
            CriteriaBuilder cb,
            String field,
            String value
    ) {
        return cb.like(
                cb.lower(resolvePath(root, field).as(String.class)),
                "%" + value.toLowerCase() + "%"
        );
    }

    private <T> void addStringPredicate(
            List<Predicate> predicates,
            Path<T> root,
            CriteriaBuilder cb,
            Map<String, String> fieldMap,
            String filterKey,
            String value
    ) {
        String entityField = fieldMap.get(filterKey);
        String safeValue = clean(value);

        if (!StringNormalizer.hasText(entityField) || !StringNormalizer.hasText(safeValue)) {
            return;
        }

        predicates.add(likePredicate(root, cb, entityField, safeValue));
    }

    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        Path<?> current = root;
        String[] parts = fieldPath.split("\\.");

        for (String part : parts) {
            current = current.get(part);
        }

        return current;
    }

    private ReferenceSearchFilterDto normalizeFilter(ReferenceSearchFilterDto filter) {
        if (filter == null) {
            return ReferenceSearchFilterDto.builder()
                    .soloActivos(Boolean.TRUE)
                    .limit(DEFAULT_LIMIT)
                    .build();
        }

        return ReferenceSearchFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), 250))
                .codigo(StringNormalizer.truncateOrNull(filter.codigo(), 100))
                .nombre(StringNormalizer.truncateOrNull(filter.nombre(), 250))
                .slug(StringNormalizer.truncateOrNull(filter.slug(), 250))
                .barcode(StringNormalizer.truncateOrNull(filter.barcode(), 100))
                .numeroDocumento(StringNormalizer.truncateOrNull(filter.numeroDocumento(), 30))
                .ruc(StringNormalizer.truncateOrNull(filter.ruc(), 20))
                .soloActivos(filter.soloActivos())
                .limit(sanitizeLimit(filter.limit()))
                .build();
    }

    private boolean activeOnly(ReferenceSearchFilterDto filter) {
        return filter == null || !Boolean.FALSE.equals(filter.soloActivos());
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String clean(String value) {
        return StringNormalizer.cleanOrNull(value);
    }
}