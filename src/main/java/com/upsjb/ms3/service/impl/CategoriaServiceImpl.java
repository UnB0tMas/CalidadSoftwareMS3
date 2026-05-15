// ruta: src/main/java/com/upsjb/ms3/service/impl/CategoriaServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.value.SlugValue;
import com.upsjb.ms3.dto.catalogo.categoria.filter.CategoriaFilterDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.CategoriaMapper;
import com.upsjb.ms3.policy.CategoriaPolicy;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.repository.EmpleadoInventarioPermisoHistorialRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CategoriaService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.CategoriaReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.CategoriaSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CategoriaValidator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idCategoria",
            "codigo",
            "nombre",
            "slug",
            "nivel",
            "orden",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;
    private static final int CATEGORY_SLUG_MAX_LENGTH = 180;

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final EmpleadoInventarioPermisoHistorialRepository empleadoInventarioPermisoHistorialRepository;
    private final CategoriaMapper categoriaMapper;
    private final CategoriaValidator categoriaValidator;
    private final CategoriaPolicy categoriaPolicy;
    private final CategoriaReferenceResolver categoriaReferenceResolver;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<CategoriaResponseDto> crear(CategoriaCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanCreate(actor, employeeCanCreateProductBasic(actor));

        CategoriaCreateRequestDto normalized = normalizeCreate(request);
        Categoria categoriaPadre = resolveCategoriaPadre(normalized.categoriaPadre());
        String slug = generateUniqueSlug(normalized.nombre(), null);
        Integer nivel = categoriaPadre == null ? 1 : safeNivel(categoriaPadre) + 1;

        categoriaValidator.validateCreate(
                normalized.codigo(),
                normalized.nombre(),
                slug,
                categoriaPadre,
                normalized.orden(),
                categoriaRepository.existsByCodigoIgnoreCaseAndEstadoTrue(normalized.codigo()),
                categoriaRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre()),
                categoriaRepository.existsBySlugIgnoreCaseAndEstadoTrue(slug)
        );

        Categoria entity = categoriaMapper.toEntity(normalized, categoriaPadre, slug, nivel);
        Categoria saved = categoriaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_CREADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(saved.getIdCategoria()),
                "CREAR_CATEGORIA",
                "Categoría creada correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Categoría creada. idCategoria={}, codigo={}, slug={}, actor={}",
                saved.getIdCategoria(),
                saved.getCodigo(),
                saved.getSlug(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Categoría creada correctamente.",
                categoriaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CategoriaResponseDto> actualizar(Long idCategoria, CategoriaUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));

        Categoria entity = findRequired(idCategoria);
        CategoriaUpdateRequestDto normalized = normalizeUpdate(request);
        Categoria categoriaPadre = resolveCategoriaPadre(normalized.categoriaPadre());
        String slug = generateUniqueSlug(normalized.nombre(), entity.getIdCategoria());
        Integer nivel = categoriaPadre == null ? 1 : safeNivel(categoriaPadre) + 1;
        boolean wouldCreateCycle = wouldCreateCycle(entity, categoriaPadre);

        Map<String, Object> before = beforeSnapshot(entity);

        categoriaValidator.validateUpdate(
                entity,
                normalized.codigo(),
                normalized.nombre(),
                slug,
                categoriaPadre,
                normalized.orden(),
                categoriaRepository.existsByCodigoIgnoreCaseAndEstadoTrueAndIdCategoriaNot(
                        normalized.codigo(),
                        entity.getIdCategoria()
                ),
                categoriaRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdCategoriaNot(
                        normalized.nombre(),
                        entity.getIdCategoria()
                ),
                categoriaRepository.existsBySlugIgnoreCaseAndEstadoTrueAndIdCategoriaNot(
                        slug,
                        entity.getIdCategoria()
                ),
                wouldCreateCycle
        );

        categoriaMapper.updateEntity(entity, normalized, categoriaPadre, slug, nivel);
        refreshChildrenLevels(entity);

        Categoria saved = categoriaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_ACTUALIZADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(saved.getIdCategoria()),
                "ACTUALIZAR_CATEGORIA",
                "Categoría actualizada correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        log.info(
                "Categoría actualizada. idCategoria={}, codigo={}, slug={}, actor={}",
                saved.getIdCategoria(),
                saved.getCodigo(),
                saved.getSlug(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Categoría actualizada correctamente.",
                categoriaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CategoriaResponseDto> cambiarEstado(Long idCategoria, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanChangeState(actor);

        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "CATEGORIA_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        requireMotivo(request.motivo());

        Categoria entity = findRequired(idCategoria);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    categoriaMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            boolean parentActive = entity.getCategoriaPadre() == null || entity.getCategoriaPadre().isActivo();
            categoriaValidator.validateCanActivate(entity, parentActive);
            entity.activar();

            Categoria saved = categoriaRepository.save(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.CATEGORIA_ACTIVADA,
                    EntidadAuditada.CATEGORIA,
                    String.valueOf(saved.getIdCategoria()),
                    "ACTIVAR_CATEGORIA",
                    "Categoría activada correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            return apiResponseFactory.dtoOk(
                    "Categoría activada correctamente.",
                    categoriaMapper.toResponse(saved)
            );
        }

        boolean hasChildren = categoriaRepository.existsByCategoriaPadre_IdCategoriaAndEstadoTrue(entity.getIdCategoria());
        boolean hasProducts = productoRepository.countByCategoria_IdCategoriaAndEstadoTrue(entity.getIdCategoria()) > 0;

        categoriaValidator.validateCanDeactivate(entity, hasChildren, hasProducts);

        entity.inactivar();
        Categoria saved = categoriaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_INACTIVADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(saved.getIdCategoria()),
                "INACTIVAR_CATEGORIA",
                "Categoría inactivada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        return apiResponseFactory.dtoOk(
                "Categoría inactivada correctamente.",
                categoriaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CategoriaResponseDto> obtenerPorId(Long idCategoria) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria entity = findRequired(idCategoria);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                categoriaMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CategoriaDetailResponseDto> obtenerDetalle(Long idCategoria) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria entity = findRequired(idCategoria);
        Long cantidadProductos = productoRepository.countByCategoria_IdCategoriaAndEstadoTrue(entity.getIdCategoria());
        List<Categoria> subcategorias = categoriaRepository
                .findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(entity.getIdCategoria());

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                categoriaMapper.toDetailResponse(entity, cantidadProductos, subcategorias)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<CategoriaResponseDto>> listar(
            CategoriaFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<CategoriaResponseDto> response = paginationService.toPageResponseDto(
                categoriaRepository.findAll(CategoriaSpecifications.fromFilter(normalizeFilter(filter)), pageable),
                categoriaMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaOptionDto>> lookup(String search, Integer limit) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanLookup(actor);

        Pageable pageable = paginationService.pageable(
                0,
                sanitizeLimit(limit),
                "nombre",
                "ASC",
                ALLOWED_SORT_FIELDS,
                "nombre"
        );

        CategoriaFilterDto filter = CategoriaFilterDto.builder()
                .search(StringNormalizer.cleanOrNull(search))
                .estado(Boolean.TRUE)
                .build();

        List<CategoriaOptionDto> options = categoriaRepository
                .findAll(CategoriaSpecifications.fromFilter(filter), pageable)
                .stream()
                .map(categoriaMapper::toOption)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", options);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaTreeResponseDto>> obtenerArbol(Boolean soloActivas) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanViewAdmin(actor);

        List<Categoria> categorias = Boolean.FALSE.equals(soloActivas)
                ? categoriaRepository.findAllByOrderByNivelAscOrdenAscNombreAsc()
                : categoriaRepository.findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

        List<CategoriaTreeResponseDto> tree = buildTree(categorias);

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", tree);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaResponseDto>> listarSubcategorias(Long idCategoriaPadre) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria parent = findRequired(idCategoriaPadre);

        List<CategoriaResponseDto> response = categoriaRepository
                .findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(parent.getIdCategoria())
                .stream()
                .map(categoriaMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private Categoria findRequired(Long idCategoria) {
        if (idCategoria == null) {
            throw new ValidationException(
                    "CATEGORIA_ID_REQUERIDO",
                    "Debe indicar la categoría solicitada."
            );
        }

        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new NotFoundException(
                        "CATEGORIA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));
    }

    private Categoria resolveCategoriaPadre(EntityReferenceDto reference) {
        if (!hasReference(reference)) {
            return null;
        }

        return categoriaReferenceResolver.resolve(
                reference.id(),
                cleanReference(reference.codigo()),
                cleanReference(reference.slug()),
                cleanReference(reference.nombre())
        );
    }

    private boolean hasReference(EntityReferenceDto reference) {
        return reference != null
                && (
                reference.id() != null
                        || StringNormalizer.hasText(reference.codigo())
                        || StringNormalizer.hasText(reference.nombre())
                        || StringNormalizer.hasText(reference.slug())
        );
    }

    private CategoriaCreateRequestDto normalizeCreate(CategoriaCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la categoría."
            );
        }

        return CategoriaCreateRequestDto.builder()
                .categoriaPadre(request.categoriaPadre())
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .descripcion(StringNormalizer.cleanOrNull(request.descripcion()))
                .orden(request.orden() == null ? 0 : request.orden())
                .build();
    }

    private CategoriaUpdateRequestDto normalizeUpdate(CategoriaUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la categoría."
            );
        }

        return CategoriaUpdateRequestDto.builder()
                .categoriaPadre(request.categoriaPadre())
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .descripcion(StringNormalizer.cleanOrNull(request.descripcion()))
                .orden(request.orden() == null ? 0 : request.orden())
                .build();
    }

    private CategoriaFilterDto normalizeFilter(CategoriaFilterDto filter) {
        if (filter == null) {
            return CategoriaFilterDto.builder()
                    .estado(Boolean.TRUE)
                    .build();
        }

        return CategoriaFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), 250))
                .codigo(StringNormalizer.truncateOrNull(StringNormalizer.normalizeForCode(filter.codigo()), 50))
                .nombre(StringNormalizer.truncateOrNull(filter.nombre(), 150))
                .slug(StringNormalizer.truncateOrNull(filter.slug(), 180))
                .idCategoriaPadre(filter.idCategoriaPadre())
                .nivel(filter.nivel())
                .estado(filter.estado())
                .fechaCreacion(filter.fechaCreacion())
                .build();
    }

    private String generateUniqueSlug(String nombre, Long excludingId) {
        String base = buildBaseSlug(nombre);
        String candidate = base;
        long suffix = 2L;

        while (slugExists(candidate, excludingId)) {
            candidate = appendSlugSuffix(base, suffix);
            suffix++;
        }

        return candidate;
    }

    private String buildBaseSlug(String nombre) {
        try {
            return trimSlugToColumn(SlugValue.fromName(nombre).raw());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    "CATEGORIA_SLUG_INVALIDO",
                    "El nombre de la categoría no permite generar un slug válido."
            );
        }
    }

    private String appendSlugSuffix(String base, long suffix) {
        String suffixText = "-" + suffix;
        int maxBaseLength = CATEGORY_SLUG_MAX_LENGTH - suffixText.length();

        if (maxBaseLength < 3) {
            throw new ValidationException(
                    "CATEGORIA_SLUG_INVALIDO",
                    "No se pudo generar un slug único para la categoría."
            );
        }

        String safeBase = base.length() > maxBaseLength
                ? trimTrailingHyphen(base.substring(0, maxBaseLength))
                : base;

        return safeBase + suffixText;
    }

    private String trimSlugToColumn(String slug) {
        if (!StringNormalizer.hasText(slug)) {
            throw new ValidationException(
                    "CATEGORIA_SLUG_INVALIDO",
                    "No se pudo generar un slug válido para la categoría."
            );
        }

        if (slug.length() <= CATEGORY_SLUG_MAX_LENGTH) {
            return slug;
        }

        String truncated = trimTrailingHyphen(slug.substring(0, CATEGORY_SLUG_MAX_LENGTH));

        if (truncated.length() < 3) {
            throw new ValidationException(
                    "CATEGORIA_SLUG_INVALIDO",
                    "No se pudo generar un slug válido para la categoría."
            );
        }

        return truncated;
    }

    private String trimTrailingHyphen(String value) {
        String result = value == null ? "" : value;

        while (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    private boolean slugExists(String slug, Long excludingId) {
        if (excludingId == null) {
            return categoriaRepository.existsBySlugIgnoreCaseAndEstadoTrue(slug);
        }

        return categoriaRepository.existsBySlugIgnoreCaseAndEstadoTrueAndIdCategoriaNot(slug, excludingId);
    }

    private boolean wouldCreateCycle(Categoria entity, Categoria categoriaPadre) {
        if (entity == null || categoriaPadre == null || entity.getIdCategoria() == null) {
            return false;
        }

        Categoria current = categoriaPadre;
        while (current != null) {
            if (Objects.equals(current.getIdCategoria(), entity.getIdCategoria())) {
                return true;
            }
            current = current.getCategoriaPadre();
        }

        return false;
    }

    private void refreshChildrenLevels(Categoria parent) {
        if (parent == null || parent.getIdCategoria() == null) {
            return;
        }

        List<Categoria> children = categoriaRepository
                .findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(parent.getIdCategoria());

        for (Categoria child : children) {
            child.setNivel(safeNivel(parent) + 1);
            refreshChildrenLevels(child);
        }
    }

    private List<CategoriaTreeResponseDto> buildTree(List<Categoria> categorias) {
        List<Categoria> safeCategorias = categorias == null ? List.of() : categorias;

        return safeCategorias.stream()
                .filter(categoria -> categoria.getCategoriaPadre() == null)
                .sorted(categoryComparator())
                .map(root -> buildNode(root, safeCategorias))
                .toList();
    }

    private CategoriaTreeResponseDto buildNode(Categoria categoria, List<Categoria> categorias) {
        List<CategoriaTreeResponseDto> children = categorias.stream()
                .filter(candidate -> candidate.getCategoriaPadre() != null)
                .filter(candidate -> Objects.equals(
                        candidate.getCategoriaPadre().getIdCategoria(),
                        categoria.getIdCategoria()
                ))
                .sorted(categoryComparator())
                .map(child -> buildNode(child, categorias))
                .toList();

        return categoriaMapper.toTreeResponse(categoria, children);
    }

    private Comparator<Categoria> categoryComparator() {
        return Comparator
                .comparing((Categoria categoria) -> categoria.getOrden() == null ? 0 : categoria.getOrden())
                .thenComparing(categoria -> categoria.getNombre() == null ? "" : categoria.getNombre());
    }

    private boolean employeeCanCreateProductBasic(AuthenticatedUserContext actor) {
        if (actor == null || actor.getIdUsuarioMs1() == null || !actor.isEmpleado()) {
            return false;
        }

        return empleadoInventarioPermisoHistorialRepository
                .existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeCrearProductoBasicoTrue(
                        actor.getIdUsuarioMs1()
                );
    }

    private boolean employeeCanEditProductBasic(AuthenticatedUserContext actor) {
        if (actor == null || actor.getIdUsuarioMs1() == null || !actor.isEmpleado()) {
            return false;
        }

        return empleadoInventarioPermisoHistorialRepository
                .existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeEditarProductoBasicoTrue(
                        actor.getIdUsuarioMs1()
                );
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(limit, MAX_LOOKUP_LIMIT);
    }

    private Integer safeNivel(Categoria categoria) {
        return categoria == null || categoria.getNivel() == null ? 1 : categoria.getNivel();
    }

    private void requireMotivo(String motivo) {
        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private Map<String, Object> beforeSnapshot(Categoria categoria) {
        LinkedHashMap<String, Object> before = new LinkedHashMap<>();
        before.put("codigo", categoria.getCodigo());
        before.put("nombre", categoria.getNombre());
        before.put("slug", categoria.getSlug());
        before.put("idCategoriaPadre", categoria.getCategoriaPadre() == null
                ? null
                : categoria.getCategoriaPadre().getIdCategoria());
        before.put("nivel", categoria.getNivel());
        before.put("orden", categoria.getOrden());
        return before;
    }

    private Map<String, Object> auditMetadata(
            Categoria categoria,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idCategoria", categoria.getIdCategoria());
        metadata.put("codigo", categoria.getCodigo());
        metadata.put("nombre", categoria.getNombre());
        metadata.put("slug", categoria.getSlug());
        metadata.put("nivel", categoria.getNivel());
        metadata.put("orden", categoria.getOrden());
        metadata.put("idCategoriaPadre", categoria.getCategoriaPadre() == null
                ? null
                : categoria.getCategoriaPadre().getIdCategoria());
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private String cleanReference(String value) {
        return StringNormalizer.cleanOrNull(value);
    }
}