package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.value.SlugValue;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
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
import com.upsjb.ms3.mapper.CategoriaAtributoMapper;
import com.upsjb.ms3.mapper.CategoriaMapper;
import com.upsjb.ms3.policy.CategoriaPolicy;
import com.upsjb.ms3.repository.CategoriaAtributoRepository;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.repository.EmpleadoInventarioPermisoHistorialRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CategoriaService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.SlugGeneratorService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.outbox.ProductoSnapshotOutboxRegistrar;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.CategoriaReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.CategoriaSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CategoriaValidator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
            "permiteProductos",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;

    private final CategoriaRepository categoriaRepository;
    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final ProductoRepository productoRepository;
    private final EmpleadoInventarioPermisoHistorialRepository
            empleadoInventarioPermisoHistorialRepository;
    private final CategoriaMapper categoriaMapper;
    private final CategoriaAtributoMapper categoriaAtributoMapper;
    private final CategoriaValidator categoriaValidator;
    private final CategoriaPolicy categoriaPolicy;
    private final CategoriaReferenceResolver categoriaReferenceResolver;
    private final CodigoGeneradorService codigoGeneradorService;
    private final SlugGeneratorService slugGeneratorService;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final ProductoSnapshotOutboxRegistrar
            productoSnapshotOutboxRegistrar;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<CategoriaResponseDto> crear(
            CategoriaCreateRequestDto request
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanCreate(
                actor,
                employeeCanCreateProductBasic(actor)
        );

        CategoriaCreateRequestDto normalized =
                normalizeCreate(request);

        Categoria categoriaPadre =
                resolveCategoriaPadre(
                        normalized.categoriaPadre()
                );

        String codigo =
                codigoGeneradorService.generarCodigoCategoria();

        String slug =
                slugGeneratorService.generarSlugUnico(
                        normalized.nombre(),
                        categoriaRepository::existsBySlugIgnoreCase
                );

        Integer nivel =
                categoriaPadre == null
                        ? 1
                        : safeNivel(categoriaPadre) + 1;

        categoriaValidator.validateCreate(
                codigo,
                normalized.nombre(),
                slug,
                categoriaPadre,
                normalized.orden(),
                normalized.permiteProductos(),
                categoriaRepository
                        .existsByCodigoIgnoreCase(
                                codigo
                        ),
                categoriaRepository
                        .existsActiveByNombreAndParent(
                                normalized.nombre(),
                                categoriaPadre == null
                                        ? null
                                        : categoriaPadre.getIdCategoria()
                        ),
                categoriaRepository
                        .existsBySlugIgnoreCase(
                                slug
                        )
        );

        Categoria entity =
                categoriaMapper.toEntity(
                        normalized,
                        categoriaPadre,
                        codigo,
                        slug,
                        nivel
                );

        Categoria saved =
                categoriaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_CREADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(
                        saved.getIdCategoria()
                ),
                "CREAR_CATEGORIA",
                "Categoría creada correctamente.",
                auditMetadata(
                        saved,
                        actor,
                        null
                )
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
    public ApiResponseDto<CategoriaResponseDto> actualizar(
            Long idCategoria,
            CategoriaUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanUpdate(
                actor,
                employeeCanEditProductBasic(actor)
        );

        Categoria entity =
                findRequired(idCategoria);

        CategoriaUpdateRequestDto normalized =
                normalizeUpdate(request);

        Categoria categoriaPadre =
                resolveCategoriaPadre(
                        normalized.categoriaPadre()
                );

        Integer nivel =
                categoriaPadre == null
                        ? 1
                        : safeNivel(categoriaPadre) + 1;

        boolean wouldCreateCycle =
                wouldCreateCycle(
                        entity,
                        categoriaPadre
                );

        Map<String, Object> before =
                beforeSnapshot(entity);

        categoriaValidator.validateUpdate(
                entity,
                normalized.nombre(),
                categoriaPadre,
                normalized.orden(),
                normalized.permiteProductos(),
                categoriaRepository
                        .existsActiveByNombreAndParentExcludingId(
                                normalized.nombre(),
                                categoriaPadre == null
                                        ? null
                                        : categoriaPadre.getIdCategoria(),
                                entity.getIdCategoria()
                        ),
                wouldCreateCycle,
                productoRepository
                        .existsByCategoria_IdCategoriaAndEstadoTrue(
                                entity.getIdCategoria()
                        ),
                categoriaRepository
                        .existsByCategoriaPadre_IdCategoriaAndEstadoTrue(
                                entity.getIdCategoria()
                        )
        );

        categoriaMapper.updateEntity(
                entity,
                normalized,
                categoriaPadre,
                nivel
        );

        refreshChildrenLevels(entity);

        Categoria saved =
                categoriaRepository.save(entity);

        registrarSnapshotsSubarbolCategoria(
                saved,
                actor,
                "ACTUALIZAR_CATEGORIA",
                null
        );

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_ACTUALIZADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(
                        saved.getIdCategoria()
                ),
                "ACTUALIZAR_CATEGORIA",
                "Categoría actualizada correctamente.",
                auditMetadata(
                        saved,
                        actor,
                        Map.of(
                                "before",
                                before
                        )
                )
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
    public ApiResponseDto<CategoriaResponseDto> cambiarEstado(
            Long idCategoria,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanChangeState(actor);

        if (
                request == null
                        || request.estado() == null
        ) {
            throw new ValidationException(
                    "CATEGORIA_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        requireMotivo(
                request.motivo()
        );

        Categoria entity =
                findRequired(idCategoria);

        if (
                Objects.equals(
                        entity.getEstado(),
                        request.estado()
                )
        ) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    categoriaMapper.toResponse(entity)
            );
        }

        if (
                Boolean.TRUE.equals(
                        request.estado()
                )
        ) {
            boolean parentActive =
                    entity.getCategoriaPadre() == null
                            || entity.getCategoriaPadre()
                            .isActivo();

            categoriaValidator.validateCanActivate(
                    entity,
                    parentActive,
                    categoriaRepository
                            .existsActiveByNombreAndParentExcludingId(
                                    entity.getNombre(),
                                    entity.getCategoriaPadre() == null
                                            ? null
                                            : entity.getCategoriaPadre()
                                            .getIdCategoria(),
                                    entity.getIdCategoria()
                            )
            );

            entity.activar();

            Categoria saved =
                    categoriaRepository.save(entity);

            registrarSnapshotsSubarbolCategoria(
                    saved,
                    actor,
                    "ACTIVAR_CATEGORIA",
                    request.motivo()
            );

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.CATEGORIA_ACTIVADA,
                    EntidadAuditada.CATEGORIA,
                    String.valueOf(
                            saved.getIdCategoria()
                    ),
                    "ACTIVAR_CATEGORIA",
                    "Categoría activada correctamente.",
                    auditMetadata(
                            saved,
                            actor,
                            Map.of(
                                    "motivo",
                                    request.motivo()
                            )
                    )
            );

            return apiResponseFactory.dtoOk(
                    "Categoría activada correctamente.",
                    categoriaMapper.toResponse(saved)
            );
        }

        boolean hasChildren =
                categoriaRepository
                        .existsByCategoriaPadre_IdCategoriaAndEstadoTrue(
                                entity.getIdCategoria()
                        );

        boolean hasProducts =
                productoRepository
                        .countByCategoria_IdCategoriaAndEstadoTrue(
                                entity.getIdCategoria()
                        ) > 0;

        categoriaValidator.validateCanDeactivate(
                entity,
                hasChildren,
                hasProducts
        );

        entity.inactivar();

        Categoria saved =
                categoriaRepository.save(entity);

        registrarSnapshotsSubarbolCategoria(
                saved,
                actor,
                "INACTIVAR_CATEGORIA",
                request.motivo()
        );

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_INACTIVADA,
                EntidadAuditada.CATEGORIA,
                String.valueOf(
                        saved.getIdCategoria()
                ),
                "INACTIVAR_CATEGORIA",
                "Categoría inactivada correctamente.",
                auditMetadata(
                        saved,
                        actor,
                        Map.of(
                                "motivo",
                                request.motivo()
                        )
                )
        );

        return apiResponseFactory.dtoOk(
                "Categoría inactivada correctamente.",
                categoriaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CategoriaResponseDto> obtenerPorId(
            Long idCategoria
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria entity =
                findRequired(idCategoria);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                categoriaMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CategoriaDetailResponseDto> obtenerDetalle(
            Long idCategoria
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria entity =
                findRequired(idCategoria);

        Long cantidadProductos =
                productoRepository
                        .countByCategoria_IdCategoriaAndEstadoTrue(
                                entity.getIdCategoria()
                        );

        List<Categoria> subcategorias =
                categoriaRepository
                        .findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(
                                entity.getIdCategoria()
                        );

        List<CategoriaAtributoResponseDto> atributos =
                categoriaAtributoRepository
                        .findByCategoria_IdCategoriaAndEstadoTrueOrderByOrdenAscIdCategoriaAtributoAsc(
                                entity.getIdCategoria()
                        )
                        .stream()
                        .map(
                                categoriaAtributoMapper::toResponse
                        )
                        .toList();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                categoriaMapper.toDetailResponse(
                        entity,
                        cantidadProductos,
                        subcategorias,
                        atributos
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<CategoriaResponseDto>> listar(
            CategoriaFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage =
                safePageRequest(
                        pageRequest,
                        "createdAt"
                );

        Pageable pageable =
                paginationService.pageable(
                        safePage.page(),
                        safePage.size(),
                        safePage.sortBy(),
                        safePage.sortDirection(),
                        ALLOWED_SORT_FIELDS,
                        "createdAt"
                );

        PageResponseDto<CategoriaResponseDto> response =
                paginationService.toPageResponseDto(
                        categoriaRepository.findAll(
                                CategoriaSpecifications.fromFilter(
                                        normalizeFilter(filter)
                                ),
                                pageable
                        ),
                        categoriaMapper::toResponse
                );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaOptionDto>> lookup(
            String search,
            Integer limit
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanLookup(actor);

        Pageable pageable =
                paginationService.pageable(
                        0,
                        sanitizeLimit(limit),
                        "nombre",
                        "ASC",
                        ALLOWED_SORT_FIELDS,
                        "nombre"
                );

        CategoriaFilterDto filter =
                CategoriaFilterDto.builder()
                        .search(
                                StringNormalizer.cleanOrNull(
                                        search
                                )
                        )
                        .estado(Boolean.TRUE)
                        .build();

        List<CategoriaOptionDto> options =
                categoriaRepository
                        .findAll(
                                CategoriaSpecifications.fromFilter(
                                        filter
                                ),
                                pageable
                        )
                        .stream()
                        .map(
                                categoriaMapper::toOption
                        )
                        .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                options
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaTreeResponseDto>> obtenerArbol(
            Boolean soloActivas
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanViewAdmin(actor);

        List<Categoria> categorias =
                Boolean.FALSE.equals(soloActivas)
                        ? categoriaRepository
                        .findAllByOrderByNivelAscOrdenAscNombreAsc()
                        : categoriaRepository
                        .findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

        List<CategoriaTreeResponseDto> tree =
                buildTree(categorias);

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                tree
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaTreeResponseDto>> obtenerArbolPublico() {
        List<Categoria> categorias =
                categoriaRepository
                        .findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

        List<CategoriaTreeResponseDto> tree =
                buildTree(categorias);

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                tree
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaResponseDto>> listarSubcategorias(
            Long idCategoriaPadre
    ) {
        AuthenticatedUserContext actor =
                currentUserResolver.resolveRequired();

        categoriaPolicy.ensureCanViewAdmin(actor);

        Categoria parent =
                findRequired(idCategoriaPadre);

        List<CategoriaResponseDto> response =
                categoriaRepository
                        .findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(
                                parent.getIdCategoria()
                        )
                        .stream()
                        .map(
                                categoriaMapper::toResponse
                        )
                        .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private Categoria findRequired(
            Long idCategoria
    ) {
        if (idCategoria == null) {
            throw new ValidationException(
                    "CATEGORIA_ID_REQUERIDO",
                    "Debe indicar la categoría solicitada."
            );
        }

        return categoriaRepository
                .findById(idCategoria)
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "CATEGORIA_NO_ENCONTRADA",
                                        "No se encontró el registro solicitado."
                                )
                );
    }

    private Categoria resolveCategoriaPadre(
            EntityReferenceDto reference
    ) {
        if (!hasReference(reference)) {
            return null;
        }

        return categoriaReferenceResolver.resolve(
                reference.id(),
                cleanReference(
                        reference.codigo()
                ),
                cleanReference(
                        reference.slug()
                ),
                cleanReference(
                        reference.nombre()
                )
        );
    }

    private boolean hasReference(
            EntityReferenceDto reference
    ) {
        return reference != null
                && (
                reference.id() != null
                        || StringNormalizer.hasText(
                        reference.codigo()
                )
                        || StringNormalizer.hasText(
                        reference.nombre()
                )
                        || StringNormalizer.hasText(
                        reference.slug()
                )
        );
    }

    private CategoriaCreateRequestDto normalizeCreate(
            CategoriaCreateRequestDto request
    ) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la categoría."
            );
        }

        return CategoriaCreateRequestDto.builder()
                .categoriaPadre(
                        request.categoriaPadre()
                )
                .nombre(
                        StringNormalizer.clean(
                                request.nombre()
                        )
                )
                .descripcion(
                        StringNormalizer.cleanOrNull(
                                request.descripcion()
                        )
                )
                .orden(
                        request.orden() == null
                                ? 0
                                : request.orden()
                )
                .permiteProductos(
                        Boolean.TRUE.equals(
                                request.permiteProductos()
                        )
                )
                .build();
    }

    private CategoriaUpdateRequestDto normalizeUpdate(
            CategoriaUpdateRequestDto request
    ) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la categoría."
            );
        }

        return CategoriaUpdateRequestDto.builder()
                .categoriaPadre(
                        request.categoriaPadre()
                )
                .nombre(
                        StringNormalizer.clean(
                                request.nombre()
                        )
                )
                .descripcion(
                        StringNormalizer.cleanOrNull(
                                request.descripcion()
                        )
                )
                .orden(
                        request.orden() == null
                                ? 0
                                : request.orden()
                )
                .permiteProductos(
                        Boolean.TRUE.equals(
                                request.permiteProductos()
                        )
                )
                .build();
    }

    private CategoriaFilterDto normalizeFilter(
            CategoriaFilterDto filter
    ) {
        if (filter == null) {
            return CategoriaFilterDto.builder()
                    .estado(Boolean.TRUE)
                    .build();
        }

        return CategoriaFilterDto.builder()
                .search(
                        StringNormalizer.truncateOrNull(
                                filter.search(),
                                250
                        )
                )
                .codigo(
                        StringNormalizer.truncateOrNull(
                                StringNormalizer.normalizeForCode(
                                        filter.codigo()
                                ),
                                50
                        )
                )
                .nombre(
                        StringNormalizer.truncateOrNull(
                                filter.nombre(),
                                150
                        )
                )
                .slug(
                        StringNormalizer.truncateOrNull(
                                filter.slug(),
                                SlugValue.MAX_LENGTH
                        )
                )
                .idCategoriaPadre(
                        filter.idCategoriaPadre()
                )
                .nivel(
                        filter.nivel()
                )
                .permiteProductos(
                        filter.permiteProductos()
                )
                .estado(
                        filter.estado()
                )
                .fechaCreacion(
                        filter.fechaCreacion()
                )
                .build();
    }

    private boolean wouldCreateCycle(
            Categoria entity,
            Categoria categoriaPadre
    ) {
        if (
                entity == null
                        || categoriaPadre == null
                        || entity.getIdCategoria() == null
        ) {
            return false;
        }

        Categoria current =
                categoriaPadre;

        while (current != null) {
            if (
                    Objects.equals(
                            current.getIdCategoria(),
                            entity.getIdCategoria()
                    )
            ) {
                return true;
            }

            current =
                    current.getCategoriaPadre();
        }

        return false;
    }

    private void refreshChildrenLevels(
            Categoria parent
    ) {
        if (
                parent == null
                        || parent.getIdCategoria() == null
        ) {
            return;
        }

        List<Categoria> children =
                categoriaRepository
                        .findByCategoriaPadre_IdCategoriaOrderByOrdenAscNombreAsc(
                                parent.getIdCategoria()
                        );

        for (Categoria child : children) {
            child.setNivel(
                    safeNivel(parent) + 1
            );

            refreshChildrenLevels(child);
        }
    }

    private List<CategoriaTreeResponseDto> buildTree(
            List<Categoria> categorias
    ) {
        List<Categoria> safeCategorias =
                categorias == null
                        ? List.of()
                        : categorias.stream()
                        .filter(Objects::nonNull)
                        .toList();

        Map<Long, List<Categoria>> childrenByParent =
                new HashMap<>();

        List<Categoria> roots =
                new ArrayList<>();

        for (Categoria categoria : safeCategorias) {
            if (categoria.getCategoriaPadre() == null) {
                roots.add(categoria);
                continue;
            }

            Long parentId = categoria
                    .getCategoriaPadre()
                    .getIdCategoria();

            childrenByParent
                    .computeIfAbsent(
                            parentId,
                            ignored -> new ArrayList<>()
                    )
                    .add(categoria);
        }

        roots.sort(categoryComparator());
        childrenByParent.values()
                .forEach(
                        children ->
                                children.sort(
                                        categoryComparator()
                                )
                );

        return roots.stream()
                .map(
                        root -> buildNode(
                                root,
                                childrenByParent,
                                new HashSet<>()
                        )
                )
                .toList();
    }

    private CategoriaTreeResponseDto buildNode(
            Categoria categoria,
            Map<Long, List<Categoria>> childrenByParent,
            Set<Long> path
    ) {
        if (
                categoria == null
                        || categoria.getIdCategoria() == null
        ) {
            return categoriaMapper.toTreeResponse(
                    categoria,
                    List.of()
            );
        }

        Long idCategoria = categoria.getIdCategoria();

        if (!path.add(idCategoria)) {
            log.warn(
                    "Se detectó una referencia circular al construir el árbol de categorías. idCategoria={}",
                    idCategoria
            );

            return categoriaMapper.toTreeResponse(
                    categoria,
                    List.of()
            );
        }

        List<CategoriaTreeResponseDto> children =
                childrenByParent
                        .getOrDefault(
                                idCategoria,
                                List.of()
                        )
                        .stream()
                        .map(
                                child -> buildNode(
                                        child,
                                        childrenByParent,
                                        new HashSet<>(path)
                                )
                        )
                        .toList();

        return categoriaMapper.toTreeResponse(
                categoria,
                children
        );
    }

    private Comparator<Categoria> categoryComparator() {
        return Comparator
                .comparing(
                        (Categoria categoria) ->
                                categoria.getOrden() == null
                                        ? 0
                                        : categoria.getOrden()
                )
                .thenComparing(
                        categoria ->
                                categoria.getNombre() == null
                                        ? ""
                                        : categoria.getNombre()
                );
    }

    private void registrarSnapshotsSubarbolCategoria(
            Categoria categoria,
            AuthenticatedUserContext actor,
            String accion,
            String motivo
    ) {
        if (
                categoria == null
                        || categoria.getIdCategoria() == null
        ) {
            return;
        }

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "accion",
                accion
        );

        metadata.put(
                "idCategoria",
                categoria.getIdCategoria()
        );

        metadata.put(
                "codigoCategoria",
                categoria.getCodigo()
        );

        metadata.put(
                "nombreCategoria",
                categoria.getNombre()
        );

        metadata.put(
                "slugCategoria",
                categoria.getSlug()
        );

        metadata.put(
                "nivelCategoria",
                categoria.getNivel()
        );

        metadata.put(
                "ordenCategoria",
                categoria.getOrden()
        );

        metadata.put(
                "categoriaPermiteProductos",
                categoria.getPermiteProductos()
        );

        metadata.put(
                "categoriaEstado",
                categoria.getEstado()
        );

        metadata.put(
                "idCategoriaPadre",
                categoria.getCategoriaPadre() == null
                        ? null
                        : categoria.getCategoriaPadre()
                        .getIdCategoria()
        );

        metadata.put(
                "actorIdUsuarioMs1",
                actor == null
                        ? null
                        : actor.getIdUsuarioMs1()
        );

        metadata.put(
                "actor",
                actor == null
                        ? null
                        : actor.actorLabel()
        );

        if (StringNormalizer.hasText(motivo)) {
            metadata.put(
                    "motivo",
                    StringNormalizer.clean(motivo)
            );
        }

        productoSnapshotOutboxRegistrar
                .registrarProductosDeSubarbolCategoriaActualizados(
                        categoria.getIdCategoria(),
                        "CategoriaService",
                        metadata
                );
    }

    private boolean employeeCanCreateProductBasic(
            AuthenticatedUserContext actor
    ) {
        if (
                actor == null
                        || actor.getIdUsuarioMs1() == null
                        || !actor.isEmpleado()
        ) {
            return false;
        }

        return empleadoInventarioPermisoHistorialRepository
                .existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeCrearProductoBasicoTrue(
                        actor.getIdUsuarioMs1()
                );
    }

    private boolean employeeCanEditProductBasic(
            AuthenticatedUserContext actor
    ) {
        if (
                actor == null
                        || actor.getIdUsuarioMs1() == null
                        || !actor.isEmpleado()
        ) {
            return false;
        }

        return empleadoInventarioPermisoHistorialRepository
                .existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeEditarProductoBasicoTrue(
                        actor.getIdUsuarioMs1()
                );
    }

    private PageRequestDto safePageRequest(
            PageRequestDto pageRequest,
            String defaultSortBy
    ) {
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

    private int sanitizeLimit(
            Integer limit
    ) {
        if (
                limit == null
                        || limit < 1
        ) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(
                limit,
                MAX_LOOKUP_LIMIT
        );
    }

    private Integer safeNivel(
            Categoria categoria
    ) {
        return categoria == null
                || categoria.getNivel() == null
                ? 1
                : categoria.getNivel();
    }

    private void requireMotivo(
            String motivo
    ) {
        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private Map<String, Object> beforeSnapshot(
            Categoria categoria
    ) {
        LinkedHashMap<String, Object> before =
                new LinkedHashMap<>();

        before.put(
                "codigo",
                categoria.getCodigo()
        );

        before.put(
                "nombre",
                categoria.getNombre()
        );

        before.put(
                "slug",
                categoria.getSlug()
        );

        before.put(
                "idCategoriaPadre",
                categoria.getCategoriaPadre() == null
                        ? null
                        : categoria.getCategoriaPadre()
                        .getIdCategoria()
        );

        before.put(
                "nivel",
                categoria.getNivel()
        );

        before.put(
                "orden",
                categoria.getOrden()
        );

        before.put(
                "permiteProductos",
                categoria.getPermiteProductos()
        );

        return before;
    }

    private Map<String, Object> auditMetadata(
            Categoria categoria,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        LinkedHashMap<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "idCategoria",
                categoria.getIdCategoria()
        );

        metadata.put(
                "codigo",
                categoria.getCodigo()
        );

        metadata.put(
                "nombre",
                categoria.getNombre()
        );

        metadata.put(
                "slug",
                categoria.getSlug()
        );

        metadata.put(
                "nivel",
                categoria.getNivel()
        );

        metadata.put(
                "orden",
                categoria.getOrden()
        );

        metadata.put(
                "permiteProductos",
                categoria.getPermiteProductos()
        );

        metadata.put(
                "idCategoriaPadre",
                categoria.getCategoriaPadre() == null
                        ? null
                        : categoria.getCategoriaPadre()
                        .getIdCategoria()
        );

        metadata.put(
                "actor",
                actor.actorLabel()
        );

        metadata.put(
                "idUsuarioMs1",
                actor.getIdUsuarioMs1()
        );

        if (extra != null) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private String cleanReference(
            String value
    ) {
        return StringNormalizer.cleanOrNull(
                value
        );
    }
}
