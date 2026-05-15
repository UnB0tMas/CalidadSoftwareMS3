// ruta: src/main/java/com/upsjb/ms3/service/impl/EntityReferenceServiceImpl.java
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
import com.upsjb.ms3.dto.reference.request.EntityReferenceRequestDto;
import com.upsjb.ms3.dto.reference.response.EntityReferenceResolvedDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.policy.CatalogoLookupPolicy;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.EntityReferenceService;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.reference.AlmacenReferenceResolver;
import com.upsjb.ms3.shared.reference.AtributoReferenceResolver;
import com.upsjb.ms3.shared.reference.CategoriaReferenceResolver;
import com.upsjb.ms3.shared.reference.EmpleadoInventarioReferenceResolver;
import com.upsjb.ms3.shared.reference.MarcaReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.reference.PromocionReferenceResolver;
import com.upsjb.ms3.shared.reference.ProveedorReferenceResolver;
import com.upsjb.ms3.shared.reference.ReferenceOptionMapper;
import com.upsjb.ms3.shared.reference.TipoProductoReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntityReferenceServiceImpl implements EntityReferenceService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final TipoProductoReferenceResolver tipoProductoReferenceResolver;
    private final CategoriaReferenceResolver categoriaReferenceResolver;
    private final MarcaReferenceResolver marcaReferenceResolver;
    private final AtributoReferenceResolver atributoReferenceResolver;
    private final ProductoReferenceResolver productoReferenceResolver;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final ProveedorReferenceResolver proveedorReferenceResolver;
    private final AlmacenReferenceResolver almacenReferenceResolver;
    private final PromocionReferenceResolver promocionReferenceResolver;
    private final EmpleadoInventarioReferenceResolver empleadoInventarioReferenceResolver;
    private final CurrentUserResolver currentUserResolver;
    private final CatalogoLookupPolicy catalogoLookupPolicy;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceResolvedDto> resolver(EntityReferenceRequestDto request) {
        ensureCanResolve();

        if (request == null || request.referencia() == null) {
            throw new ValidationException(
                    "REFERENCIA_OBLIGATORIA",
                    "Debe indicar la entidad y la referencia a resolver."
            );
        }

        validarSoloActivos(request.soloActivos());

        String entidad = normalizeEntity(request.entidad());
        ReferenceOptionMapper.ReferenceOption option = resolveAsOption(entidad, request.referencia());

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toResolved(entidad, option)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscar(String entidad, ReferenceSearchFilterDto filter) {
        ensureCanResolve();

        validarSoloActivos(filter == null ? null : filter.soloActivos());

        String normalizedEntity = normalizeEntity(entidad);
        List<EntityReferenceResolvedDto> data = searchOptions(normalizedEntity, filter)
                .stream()
                .map(option -> toResolved(normalizedEntity, option))
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarTipoProducto(ReferenceSearchFilterDto filter) {
        return buscar("TIPO_PRODUCTO", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarCategoria(ReferenceSearchFilterDto filter) {
        return buscar("CATEGORIA", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarMarca(ReferenceSearchFilterDto filter) {
        return buscar("MARCA", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarAtributo(ReferenceSearchFilterDto filter) {
        return buscar("ATRIBUTO", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarProducto(ReferenceSearchFilterDto filter) {
        return buscar("PRODUCTO", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarSku(ReferenceSearchFilterDto filter) {
        return buscar("PRODUCTO_SKU", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarProveedor(ReferenceSearchFilterDto filter) {
        return buscar("PROVEEDOR", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarAlmacen(ReferenceSearchFilterDto filter) {
        return buscar("ALMACEN", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarPromocion(ReferenceSearchFilterDto filter) {
        return buscar("PROMOCION", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntityReferenceResolvedDto>> buscarEmpleadoInventario(ReferenceSearchFilterDto filter) {
        return buscar("EMPLEADO_SNAPSHOT_MS2", filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverTipoProducto(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        TipoProducto entity = tipoProductoReferenceResolver.resolve(
                safeReference.id(),
                safeReference.codigo(),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverCategoria(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Categoria entity = categoriaReferenceResolver.resolve(
                safeReference.id(),
                safeReference.codigo(),
                safeReference.slug(),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverMarca(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Marca entity = marcaReferenceResolver.resolve(
                safeReference.id(),
                safeReference.codigo(),
                safeReference.slug(),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverAtributo(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Atributo entity = atributoReferenceResolver.resolve(
                safeReference.id(),
                safeReference.codigo(),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverProducto(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Producto entity = productoReferenceResolver.resolve(
                safeReference.id(),
                firstText(safeReference.codigoProducto(), safeReference.codigo()),
                safeReference.slug(),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverSku(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        ProductoSku entity = productoSkuReferenceResolver.resolve(
                safeReference.id(),
                firstText(safeReference.codigoSku(), safeReference.codigo()),
                safeReference.barcode()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverProveedor(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Proveedor entity = proveedorReferenceResolver.resolve(
                safeReference.id(),
                safeReference.ruc(),
                safeReference.numeroDocumento(),
                firstText(safeReference.nombre(), safeReference.codigoProveedor(), safeReference.codigo())
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverAlmacen(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Almacen entity = almacenReferenceResolver.resolve(
                safeReference.id(),
                firstText(safeReference.codigoAlmacen(), safeReference.codigo()),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverPromocion(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        Promocion entity = promocionReferenceResolver.resolve(
                safeReference.id(),
                firstText(safeReference.codigoPromocion(), safeReference.codigo()),
                safeReference.nombre()
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EntityReferenceDto> resolverEmpleadoInventario(EntityReferenceDto reference) {
        ensureCanResolve();
        EntityReferenceDto safeReference = requireReference(reference);

        EmpleadoSnapshotMs2 entity = empleadoInventarioReferenceResolver.resolve(
                firstNonNull(safeReference.idEmpleadoSnapshot(), safeReference.id()),
                safeReference.idEmpleadoMs2(),
                safeReference.idUsuarioMs1(),
                firstText(safeReference.codigoEmpleado(), safeReference.codigo())
        );

        return apiResponseFactory.dtoOk("Detalle obtenido correctamente.", toReference(entity));
    }

    private ReferenceOptionMapper.ReferenceOption resolveAsOption(String entidad, EntityReferenceDto reference) {
        EntityReferenceDto safeReference = requireReference(reference);

        return switch (entidad) {
            case "TIPO_PRODUCTO" -> tipoProductoReferenceResolver.toOption(tipoProductoReferenceResolver.resolve(
                    safeReference.id(),
                    safeReference.codigo(),
                    safeReference.nombre()
            ));
            case "CATEGORIA" -> categoriaReferenceResolver.toOption(categoriaReferenceResolver.resolve(
                    safeReference.id(),
                    safeReference.codigo(),
                    safeReference.slug(),
                    safeReference.nombre()
            ));
            case "MARCA" -> marcaReferenceResolver.toOption(marcaReferenceResolver.resolve(
                    safeReference.id(),
                    safeReference.codigo(),
                    safeReference.slug(),
                    safeReference.nombre()
            ));
            case "ATRIBUTO" -> atributoReferenceResolver.toOption(atributoReferenceResolver.resolve(
                    safeReference.id(),
                    safeReference.codigo(),
                    safeReference.nombre()
            ));
            case "PRODUCTO" -> productoReferenceResolver.toOption(productoReferenceResolver.resolve(
                    safeReference.id(),
                    firstText(safeReference.codigoProducto(), safeReference.codigo()),
                    safeReference.slug(),
                    safeReference.nombre()
            ));
            case "SKU", "PRODUCTO_SKU" -> productoSkuReferenceResolver.toOption(productoSkuReferenceResolver.resolve(
                    safeReference.id(),
                    firstText(safeReference.codigoSku(), safeReference.codigo()),
                    safeReference.barcode()
            ));
            case "PROVEEDOR" -> proveedorReferenceResolver.toOption(proveedorReferenceResolver.resolve(
                    safeReference.id(),
                    safeReference.ruc(),
                    safeReference.numeroDocumento(),
                    firstText(safeReference.nombre(), safeReference.codigoProveedor(), safeReference.codigo())
            ));
            case "ALMACEN" -> almacenReferenceResolver.toOption(almacenReferenceResolver.resolve(
                    safeReference.id(),
                    firstText(safeReference.codigoAlmacen(), safeReference.codigo()),
                    safeReference.nombre()
            ));
            case "PROMOCION" -> promocionReferenceResolver.toOption(promocionReferenceResolver.resolve(
                    safeReference.id(),
                    firstText(safeReference.codigoPromocion(), safeReference.codigo()),
                    safeReference.nombre()
            ));
            case "EMPLEADO_INVENTARIO", "EMPLEADO_SNAPSHOT_MS2" -> empleadoInventarioReferenceResolver.toOption(
                    empleadoInventarioReferenceResolver.resolve(
                            firstNonNull(safeReference.idEmpleadoSnapshot(), safeReference.id()),
                            safeReference.idEmpleadoMs2(),
                            safeReference.idUsuarioMs1(),
                            firstText(safeReference.codigoEmpleado(), safeReference.codigo())
                    )
            );
            default -> throw unsupportedEntity(entidad);
        };
    }

    private List<ReferenceOptionMapper.ReferenceOption> searchOptions(String entidad, ReferenceSearchFilterDto filter) {
        String search = resolveSearchTerm(entidad, filter);
        Integer limit = sanitizeLimit(filter == null ? null : filter.limit());

        if (!StringNormalizer.hasText(search)) {
            return List.of();
        }

        return switch (entidad) {
            case "TIPO_PRODUCTO" -> tipoProductoReferenceResolver.search(search, limit);
            case "CATEGORIA" -> categoriaReferenceResolver.search(search, limit);
            case "MARCA" -> marcaReferenceResolver.search(search, limit);
            case "ATRIBUTO" -> atributoReferenceResolver.search(search, limit);
            case "PRODUCTO" -> productoReferenceResolver.search(search, limit);
            case "SKU", "PRODUCTO_SKU" -> productoSkuReferenceResolver.search(search, limit);
            case "PROVEEDOR" -> proveedorReferenceResolver.search(search, limit);
            case "ALMACEN" -> almacenReferenceResolver.search(search, limit);
            case "PROMOCION" -> promocionReferenceResolver.search(search, limit);
            case "EMPLEADO_INVENTARIO", "EMPLEADO_SNAPSHOT_MS2" -> empleadoInventarioReferenceResolver.search(search, limit);
            default -> throw unsupportedEntity(entidad);
        };
    }

    private String resolveSearchTerm(String entidad, ReferenceSearchFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return switch (entidad) {
            case "CATEGORIA", "MARCA", "PRODUCTO" -> firstText(
                    filter.search(),
                    filter.codigo(),
                    filter.slug(),
                    filter.nombre()
            );
            case "SKU", "PRODUCTO_SKU" -> firstText(
                    filter.search(),
                    filter.codigo(),
                    filter.barcode(),
                    filter.nombre()
            );
            case "PROVEEDOR" -> firstText(
                    filter.search(),
                    filter.ruc(),
                    filter.numeroDocumento(),
                    filter.nombre(),
                    filter.codigo()
            );
            default -> firstText(
                    filter.search(),
                    filter.codigo(),
                    filter.nombre(),
                    filter.slug(),
                    filter.barcode(),
                    filter.ruc(),
                    filter.numeroDocumento()
            );
        };
    }

    private void ensureCanResolve() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        catalogoLookupPolicy.ensureCanLookup(actor);
    }

    private void validarSoloActivos(Boolean soloActivos) {
        if (Boolean.FALSE.equals(soloActivos)) {
            throw new ValidationException(
                    "REFERENCIA_SOLO_ACTIVOS_REQUERIDO",
                    "La resolución funcional de referencias solo permite registros activos. Use el listado administrativo correspondiente para revisar inactivos."
            );
        }
    }

    private EntityReferenceDto requireReference(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "REFERENCIA_OBLIGATORIA",
                    "Debe indicar la referencia a resolver."
            );
        }

        return reference;
    }

    private EntityReferenceResolvedDto toResolved(String entidad, ReferenceOptionMapper.ReferenceOption option) {
        return EntityReferenceResolvedDto.builder()
                .entidad(entidad)
                .id(option.id())
                .value(option.value())
                .label(option.label())
                .description(option.description())
                .active(option.active())
                .metadata(option.metadata())
                .build();
    }

    private EntityReferenceDto toReference(TipoProducto entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    private EntityReferenceDto toReference(Categoria entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .build();
    }

    private EntityReferenceDto toReference(Marca entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .build();
    }

    private EntityReferenceDto toReference(Atributo entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    private EntityReferenceDto toReference(Producto entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdProducto())
                .codigo(entity.getCodigoProducto())
                .codigoProducto(entity.getCodigoProducto())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .build();
    }

    private EntityReferenceDto toReference(ProductoSku entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdSku())
                .codigo(entity.getCodigoSku())
                .codigoSku(entity.getCodigoSku())
                .barcode(entity.getBarcode())
                .build();
    }

    private EntityReferenceDto toReference(Proveedor entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdProveedor())
                .nombre(firstText(entity.getRazonSocial(), entity.getNombreComercial(), entity.getNombres()))
                .ruc(entity.getRuc())
                .numeroDocumento(entity.getNumeroDocumento())
                .build();
    }

    private EntityReferenceDto toReference(Almacen entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .codigoAlmacen(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    private EntityReferenceDto toReference(Promocion entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdPromocion())
                .codigo(entity.getCodigo())
                .codigoPromocion(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    private EntityReferenceDto toReference(EmpleadoSnapshotMs2 entity) {
        return EntityReferenceDto.builder()
                .id(entity.getIdEmpleadoSnapshot())
                .idEmpleadoSnapshot(entity.getIdEmpleadoSnapshot())
                .idEmpleadoMs2(entity.getIdEmpleadoMs2())
                .idUsuarioMs1(entity.getIdUsuarioMs1())
                .codigo(entity.getCodigoEmpleado())
                .codigoEmpleado(entity.getCodigoEmpleado())
                .nombre(entity.getNombresCompletos())
                .build();
    }

    private String normalizeEntity(String entidad) {
        if (!StringNormalizer.hasText(entidad)) {
            throw new ValidationException(
                    "ENTIDAD_REFERENCIA_OBLIGATORIA",
                    "La entidad de referencia es obligatoria."
            );
        }

        String normalized = StringNormalizer.normalizeForCode(entidad)
                .replace("-", "_")
                .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "TIPOPRODUCTO", "TIPO_PRODUCTO" -> "TIPO_PRODUCTO";
            case "CATEGORIA" -> "CATEGORIA";
            case "MARCA" -> "MARCA";
            case "ATRIBUTO" -> "ATRIBUTO";
            case "PRODUCTO" -> "PRODUCTO";
            case "SKU", "PRODUCTO_SKU", "PRODUCTOSKU" -> "PRODUCTO_SKU";
            case "PROVEEDOR" -> "PROVEEDOR";
            case "ALMACEN" -> "ALMACEN";
            case "PROMOCION" -> "PROMOCION";
            case "EMPLEADO", "EMPLEADO_INVENTARIO", "EMPLEADO_SNAPSHOT", "EMPLEADO_SNAPSHOT_MS2" -> "EMPLEADO_SNAPSHOT_MS2";
            default -> normalized;
        };
    }

    private ValidationException unsupportedEntity(String entidad) {
        return new ValidationException(
                "ENTIDAD_REFERENCIA_NO_SOPORTADA",
                "La entidad de referencia no está soportada: " + entidad + "."
        );
    }

    private Integer sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private Long firstNonNull(Long first, Long second) {
        return first != null ? first : second;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringNormalizer.hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }
}