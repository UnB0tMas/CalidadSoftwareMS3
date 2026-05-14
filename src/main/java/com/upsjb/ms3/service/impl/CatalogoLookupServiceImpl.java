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
import com.upsjb.ms3.domain.enums.TipoProveedor;
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
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final CurrentUserResolver currentUserResolver;
    private final CatalogoLookupPolicy catalogoLookupPolicy;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<TipoProductoOptionDto>> buscarTiposProducto(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<TipoProductoOptionDto> data = lookup(
                tipoProductoRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                filter
        ).stream().map(this::toTipoProductoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaOptionDto>> buscarCategorias(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<CategoriaOptionDto> data = lookup(
                categoriaRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "slug", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                filter
        ).stream().map(this::toCategoriaOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<MarcaOptionDto>> buscarMarcas(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<MarcaOptionDto> data = lookup(
                marcaRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "slug", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                filter
        ).stream().map(this::toMarcaOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoOptionDto>> buscarAtributos(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<AtributoOptionDto> data = lookup(
                atributoRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "unidadMedida"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                filter
        ).stream().map(this::toAtributoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoOptionDto>> buscarProductos(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<ProductoOptionDto> data = lookup(
                productoRepository,
                lookupSpec(
                        filter,
                        List.of("codigoProducto", "nombre", "slug", "descripcionCorta"),
                        Map.of("codigo", "codigoProducto", "nombre", "nombre", "slug", "slug")
                ),
                "nombre",
                filter
        ).stream().map(this::toProductoOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoSkuOptionDto>> buscarSkus(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<ProductoSkuOptionDto> data = lookup(
                productoSkuRepository,
                lookupSpec(
                        filter,
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
                filter
        ).stream().map(this::toProductoSkuOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProveedorOptionDto>> buscarProveedores(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<ProveedorOptionDto> data = lookup(
                proveedorRepository,
                lookupSpec(
                        filter,
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
                filter
        ).stream().map(this::toProveedorOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AlmacenOptionDto>> buscarAlmacenes(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<AlmacenOptionDto> data = lookup(
                almacenRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "direccion", "observacion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                filter
        ).stream().map(this::toAlmacenOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PromocionOptionDto>> buscarPromociones(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<PromocionOptionDto> data = lookup(
                promocionRepository,
                lookupSpec(
                        filter,
                        List.of("codigo", "nombre", "descripcion"),
                        Map.of("codigo", "codigo", "nombre", "nombre")
                ),
                "nombre",
                filter
        ).stream().map(this::toPromocionOption).toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EmpleadoInventarioOptionDto>> buscarEmpleadosInventario(ReferenceSearchFilterDto filter) {
        ensureLookupAllowed();

        List<EmpleadoInventarioOptionDto> data = lookup(
                empleadoSnapshotMs2Repository,
                lookupSpec(
                        filter,
                        List.of("codigoEmpleado", "nombresCompletos", "areaCodigo", "areaNombre"),
                        Map.of("codigo", "codigoEmpleado", "nombre", "nombresCompletos")
                ),
                "nombresCompletos",
                filter
        ).stream().map(this::toEmpleadoOption).toList();

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
                    searchPredicates.add(cb.like(
                            cb.lower(resolvePath(root, field).as(String.class)),
                            "%" + search.toLowerCase() + "%"
                    ));
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

    private <T> void addStringPredicate(
            List<Predicate> predicates,
            Path<T> root,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            Map<String, String> fieldMap,
            String filterKey,
            String value
    ) {
        String entityField = fieldMap.get(filterKey);
        String safeValue = clean(value);

        if (!StringNormalizer.hasText(entityField) || !StringNormalizer.hasText(safeValue)) {
            return;
        }

        predicates.add(cb.like(
                cb.lower(resolvePath(root, entityField).as(String.class)),
                "%" + safeValue.toLowerCase() + "%"
        ));
    }

    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        Path<?> current = root;
        String[] parts = fieldPath.split("\\.");

        for (String part : parts) {
            current = current.get(part);
        }

        return current;
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

    private TipoProductoOptionDto toTipoProductoOption(TipoProducto entity) {
        return TipoProductoOptionDto.builder()
                .idTipoProducto(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }

    private CategoriaOptionDto toCategoriaOption(Categoria entity) {
        return CategoriaOptionDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(entity.getCategoriaPadre() == null ? null : entity.getCategoriaPadre().getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .build();
    }

    private MarcaOptionDto toMarcaOption(Marca entity) {
        return MarcaOptionDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estado(entity.getEstado())
                .build();
    }

    private AtributoOptionDto toAtributoOption(Atributo entity) {
        return AtributoOptionDto.builder()
                .idAtributo(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .tipoDato(entity.getTipoDato())
                .tipoDatoLabel(entity.getTipoDato() == null ? null : entity.getTipoDato().getLabel())
                .unidadMedida(entity.getUnidadMedida())
                .requerido(entity.getRequerido())
                .filtrable(entity.getFiltrable())
                .visiblePublico(entity.getVisiblePublico())
                .estado(entity.getEstado())
                .build();
    }

    private ProductoOptionDto toProductoOption(Producto entity) {
        return ProductoOptionDto.builder()
                .idProducto(entity.getIdProducto())
                .codigoProducto(entity.getCodigoProducto())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estadoRegistro(entity.getEstadoRegistro())
                .estadoPublicacion(entity.getEstadoPublicacion())
                .estadoVenta(entity.getEstadoVenta())
                .visiblePublico(entity.getVisiblePublico())
                .vendible(entity.getVendible())
                .estado(entity.getEstado())
                .build();
    }

    private ProductoSkuOptionDto toProductoSkuOption(ProductoSku entity) {
        Producto producto = entity.getProducto();

        return ProductoSkuOptionDto.builder()
                .idSku(entity.getIdSku())
                .codigoSku(entity.getCodigoSku())
                .barcode(entity.getBarcode())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .color(entity.getColor())
                .talla(entity.getTalla())
                .material(entity.getMaterial())
                .modelo(entity.getModelo())
                .estadoSku(entity.getEstadoSku())
                .estado(entity.getEstado())
                .build();
    }

    private ProveedorOptionDto toProveedorOption(Proveedor entity) {
        return ProveedorOptionDto.builder()
                .idProveedor(entity.getIdProveedor())
                .tipoProveedor(entity.getTipoProveedor())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .ruc(entity.getRuc())
                .razonSocial(entity.getRazonSocial())
                .nombreComercial(entity.getNombreComercial())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .displayName(proveedorDisplayName(entity))
                .estado(entity.getEstado())
                .build();
    }

    private AlmacenOptionDto toAlmacenOption(Almacen entity) {
        return AlmacenOptionDto.builder()
                .idAlmacen(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .principal(entity.getPrincipal())
                .permiteVenta(entity.getPermiteVenta())
                .permiteCompra(entity.getPermiteCompra())
                .estado(entity.getEstado())
                .build();
    }

    private PromocionOptionDto toPromocionOption(Promocion entity) {
        return PromocionOptionDto.builder()
                .idPromocion(entity.getIdPromocion())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }

    private EmpleadoInventarioOptionDto toEmpleadoOption(EmpleadoSnapshotMs2 entity) {
        return EmpleadoInventarioOptionDto.builder()
                .idEmpleadoSnapshot(entity.getIdEmpleadoSnapshot())
                .idEmpleadoMs2(entity.getIdEmpleadoMs2())
                .idUsuarioMs1(entity.getIdUsuarioMs1())
                .codigoEmpleado(entity.getCodigoEmpleado())
                .nombresCompletos(entity.getNombresCompletos())
                .areaCodigo(entity.getAreaCodigo())
                .areaNombre(entity.getAreaNombre())
                .empleadoActivo(entity.getEmpleadoActivo())
                .estado(entity.getEstado())
                .build();
    }

    private String proveedorDisplayName(Proveedor entity) {
        if (entity == null) {
            return null;
        }

        if (TipoProveedor.EMPRESA.equals(entity.getTipoProveedor())) {
            if (StringNormalizer.hasText(entity.getNombreComercial())) {
                return entity.getNombreComercial();
            }
            return entity.getRazonSocial();
        }

        String nombres = entity.getNombres() == null ? "" : entity.getNombres().trim();
        String apellidos = entity.getApellidos() == null ? "" : entity.getApellidos().trim();
        String fullName = (nombres + " " + apellidos).trim();

        return StringNormalizer.hasText(fullName) ? fullName : entity.getNumeroDocumento();
    }
}