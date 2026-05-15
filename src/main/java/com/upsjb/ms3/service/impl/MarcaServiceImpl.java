// ruta: src/main/java/com/upsjb/ms3/service/impl/MarcaServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.marca.filter.MarcaFilterDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaResponseDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.MarcaMapper;
import com.upsjb.ms3.policy.MarcaPolicy;
import com.upsjb.ms3.repository.MarcaRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.MarcaService;
import com.upsjb.ms3.service.contract.SlugGeneratorService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.MarcaSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.MarcaValidator;
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
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private static final String EVENTO_PRODUCTO_MARCA_ACTUALIZADA = "ProductoSnapshotMarcaActualizada";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idMarca",
            "codigo",
            "nombre",
            "slug",
            "slugGenerado",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;
    private final MarcaMapper marcaMapper;
    private final MarcaValidator marcaValidator;
    private final MarcaPolicy marcaPolicy;
    private final SlugGeneratorService slugGeneratorService;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<MarcaResponseDto> crear(MarcaCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanCreate(actor, employeeCanCreateProductBasic(actor));

        MarcaCreateRequestDto normalized = normalizeCreate(request);
        String slug = slugGeneratorService.generarSlugUnico(
                normalized.nombre(),
                marcaRepository::existsBySlugIgnoreCaseAndEstadoTrue
        );

        marcaValidator.validateCreate(
                normalized.codigo(),
                normalized.nombre(),
                slug,
                marcaRepository.existsByCodigoIgnoreCaseAndEstadoTrue(normalized.codigo()),
                marcaRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre()),
                marcaRepository.existsBySlugIgnoreCaseAndEstadoTrue(slug)
        );

        Marca entity = marcaMapper.toEntity(normalized, slug, Boolean.TRUE);
        entity.activar();

        Marca saved = marcaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.MARCA_CREADA,
                EntidadAuditada.MARCA,
                String.valueOf(saved.getIdMarca()),
                "CREAR_MARCA",
                "Marca registrada correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Marca creada. idMarca={}, codigo={}, slug={}, actor={}",
                saved.getIdMarca(),
                saved.getCodigo(),
                saved.getSlug(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Marca registrada correctamente.",
                marcaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<MarcaResponseDto> actualizar(Long idMarca, MarcaUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));

        Marca entity = findActiveRequired(idMarca);
        MarcaUpdateRequestDto normalized = normalizeUpdate(request);

        String slug = slugGeneratorService.generarSlugUnicoExcluyendoId(
                normalized.nombre(),
                entity.getIdMarca(),
                marcaRepository::existsBySlugIgnoreCaseAndEstadoTrueAndIdMarcaNot
        );

        marcaValidator.validateUpdate(
                entity,
                normalized.codigo(),
                normalized.nombre(),
                slug,
                marcaRepository.existsByCodigoIgnoreCaseAndEstadoTrueAndIdMarcaNot(
                        normalized.codigo(),
                        entity.getIdMarca()
                ),
                marcaRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdMarcaNot(
                        normalized.nombre(),
                        entity.getIdMarca()
                ),
                marcaRepository.existsBySlugIgnoreCaseAndEstadoTrueAndIdMarcaNot(
                        slug,
                        entity.getIdMarca()
                )
        );

        Map<String, Object> before = auditSnapshot(entity);
        boolean publicIdentityChanged = hasPublicIdentityChanged(entity, normalized, slug);

        marcaMapper.updateEntity(entity, normalized, slug, Boolean.TRUE);
        Marca saved = marcaRepository.save(entity);

        List<Long> affectedProductIds = publicIdentityChanged
                ? findActiveProductIdsByMarca(saved.getIdMarca())
                : List.of();

        if (!affectedProductIds.isEmpty()) {
            registrarMarcaProductoSnapshotOutbox(saved, affectedProductIds, actor);
        }

        Map<String, Object> auditExtra = new LinkedHashMap<>();
        auditExtra.put("before", before);
        auditExtra.put("productosAfectados", affectedProductIds.size());

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.MARCA_ACTUALIZADA,
                EntidadAuditada.MARCA,
                String.valueOf(saved.getIdMarca()),
                "ACTUALIZAR_MARCA",
                "Marca actualizada correctamente.",
                auditMetadata(saved, actor, auditExtra)
        );

        log.info(
                "Marca actualizada. idMarca={}, codigo={}, slug={}, actor={}, productosAfectados={}",
                saved.getIdMarca(),
                saved.getCodigo(),
                saved.getSlug(),
                actor.actorLabel(),
                affectedProductIds.size()
        );

        return apiResponseFactory.dtoOk(
                "Marca actualizada correctamente.",
                marcaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<MarcaResponseDto> cambiarEstado(Long idMarca, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanChangeState(actor);

        validateEstadoRequest(request);

        Marca entity = findAnyRequired(idMarca);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    marcaMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            marcaValidator.validateCanActivate(entity);
            entity.activar();

            Marca saved = marcaRepository.save(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.MARCA_ACTIVADA,
                    EntidadAuditada.MARCA,
                    String.valueOf(saved.getIdMarca()),
                    "ACTIVAR_MARCA",
                    "Marca activada correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            return apiResponseFactory.dtoOk(
                    "Marca activada correctamente.",
                    marcaMapper.toResponse(saved)
            );
        }

        boolean hasActiveProducts = productoRepository.existsByMarca_IdMarcaAndEstadoTrue(entity.getIdMarca());
        marcaValidator.validateCanDeactivate(entity, hasActiveProducts);

        entity.inactivar();
        Marca saved = marcaRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.MARCA_INACTIVADA,
                EntidadAuditada.MARCA,
                String.valueOf(saved.getIdMarca()),
                "INACTIVAR_MARCA",
                "Marca inactivada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        return apiResponseFactory.dtoOk(
                "Marca inactivada correctamente.",
                marcaMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MarcaResponseDto> obtenerPorId(Long idMarca) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanViewAdmin(actor);

        Marca entity = findActiveRequired(idMarca);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                marcaMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MarcaResponseDto> obtenerPorCodigo(String codigo) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanViewAdmin(actor);

        String cleanCodigo = requireText(codigo, "MARCA_CODIGO_REQUERIDO", "Debe indicar el código de la marca.");

        Marca entity = marcaRepository.findByCodigoIgnoreCaseAndEstadoTrue(cleanCodigo)
                .orElseThrow(() -> new NotFoundException(
                        "MARCA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                marcaMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MarcaResponseDto> obtenerPorSlug(String slug) {
        marcaPolicy.canViewPublic();

        String cleanSlug = requireText(slug, "MARCA_SLUG_REQUERIDO", "Debe indicar el slug de la marca.");

        Marca entity = marcaRepository.findBySlugIgnoreCaseAndEstadoTrue(cleanSlug)
                .orElseThrow(() -> new NotFoundException(
                        "MARCA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                marcaMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MarcaResponseDto> obtenerPorReferencia(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "MARCA_REFERENCIA_REQUERIDA",
                    "Debe indicar la referencia de la marca."
            );
        }

        if (reference.id() != null) {
            return obtenerPorId(reference.id());
        }

        if (StringNormalizer.hasText(reference.codigo())) {
            return obtenerPorCodigo(reference.codigo());
        }

        if (StringNormalizer.hasText(reference.slug())) {
            return obtenerPorSlug(reference.slug());
        }

        if (StringNormalizer.hasText(reference.nombre())) {
            AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
            marcaPolicy.ensureCanViewAdmin(actor);

            Marca entity = marcaRepository.findByNombreIgnoreCaseAndEstadoTrue(StringNormalizer.clean(reference.nombre()))
                    .orElseThrow(() -> new NotFoundException(
                            "MARCA_NO_ENCONTRADA",
                            "No se encontró el registro solicitado."
                    ));

            return apiResponseFactory.dtoOk(
                    "Detalle obtenido correctamente.",
                    marcaMapper.toResponse(entity)
            );
        }

        throw new ValidationException(
                "MARCA_REFERENCIA_INVALIDA",
                "Debe indicar id, código, slug o nombre de la marca."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MarcaDetailResponseDto> obtenerDetalle(Long idMarca) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanViewAdmin(actor);

        Marca entity = findAnyRequired(idMarca);
        Long cantidadProductos = productoRepository.countByMarca_IdMarcaAndEstadoTrue(entity.getIdMarca());

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                marcaMapper.toDetailResponse(entity, cantidadProductos)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<MarcaResponseDto>> listar(
            MarcaFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        marcaPolicy.ensureCanViewAdmin(actor);

        MarcaFilterDto safeFilter = normalizeFilter(filter);
        PageRequestDto safePage = safePageRequest(pageRequest, "nombre");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "nombre"
        );

        PageResponseDto<MarcaResponseDto> response = paginationService.toPageResponseDto(
                marcaRepository.findAll(MarcaSpecifications.fromFilter(safeFilter), pageable),
                marcaMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<MarcaOptionDto>> lookup(String search, Integer limit) {
        int safeLimit = resolveLookupLimit(limit);

        MarcaFilterDto filter = MarcaFilterDto.builder()
                .search(StringNormalizer.cleanOrNull(search))
                .estado(Boolean.TRUE)
                .build();

        Pageable pageable = paginationService.pageable(
                0,
                safeLimit,
                "nombre",
                "ASC",
                ALLOWED_SORT_FIELDS,
                "nombre"
        );

        List<MarcaOptionDto> response = marcaRepository.findAll(MarcaSpecifications.fromFilter(filter), pageable)
                .getContent()
                .stream()
                .map(this::toOption)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private Marca findActiveRequired(Long idMarca) {
        if (idMarca == null) {
            throw new ValidationException(
                    "MARCA_ID_REQUERIDO",
                    "Debe indicar la marca solicitada."
            );
        }

        Marca entity = marcaRepository.findByIdMarcaAndEstadoTrue(idMarca)
                .orElseThrow(() -> new NotFoundException(
                        "MARCA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        marcaValidator.requireActive(entity);
        return entity;
    }

    private Marca findAnyRequired(Long idMarca) {
        if (idMarca == null) {
            throw new ValidationException(
                    "MARCA_ID_REQUERIDO",
                    "Debe indicar la marca solicitada."
            );
        }

        Marca entity = marcaRepository.findById(idMarca)
                .orElseThrow(() -> new NotFoundException(
                        "MARCA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        marcaValidator.requireExists(entity);
        return entity;
    }

    private MarcaCreateRequestDto normalizeCreate(MarcaCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "MARCA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la marca."
            );
        }

        return MarcaCreateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .descripcion(StringNormalizer.truncateOrNull(request.descripcion(), 300))
                .build();
    }

    private MarcaUpdateRequestDto normalizeUpdate(MarcaUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "MARCA_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la marca."
            );
        }

        return MarcaUpdateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .descripcion(StringNormalizer.truncateOrNull(request.descripcion(), 300))
                .build();
    }

    private MarcaFilterDto normalizeFilter(MarcaFilterDto filter) {
        if (filter == null) {
            return MarcaFilterDto.builder()
                    .estado(Boolean.TRUE)
                    .build();
        }

        return MarcaFilterDto.builder()
                .search(StringNormalizer.cleanOrNull(filter.search()))
                .codigo(StringNormalizer.cleanOrNull(filter.codigo()))
                .nombre(StringNormalizer.cleanOrNull(filter.nombre()))
                .slug(StringNormalizer.cleanOrNull(filter.slug()))
                .estado(filter.estado() == null ? Boolean.TRUE : filter.estado())
                .fechaCreacion(filter.fechaCreacion())
                .build();
    }

    private void validateEstadoRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "MARCA_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        if (!StringUtils.hasText(request.motivo())) {
            throw new ValidationException(
                    "MARCA_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private boolean hasPublicIdentityChanged(Marca entity, MarcaUpdateRequestDto normalized, String slug) {
        return !Objects.equals(entity.getCodigo(), normalized.codigo())
                || !Objects.equals(entity.getNombre(), normalized.nombre())
                || !Objects.equals(entity.getSlug(), slug);
    }

    private List<Long> findActiveProductIdsByMarca(Long idMarca) {
        if (idMarca == null) {
            return List.of();
        }

        return productoRepository.findByMarca_IdMarcaAndEstadoTrueOrderByIdProductoAsc(idMarca)
                .stream()
                .map(Producto::getIdProducto)
                .toList();
    }

    private void registrarMarcaProductoSnapshotOutbox(
            Marca marca,
            List<Long> affectedProductIds,
            AuthenticatedUserContext actor
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", EVENTO_PRODUCTO_MARCA_ACTUALIZADA);
        payload.put("source", "MarcaService");
        payload.put("idMarca", marca.getIdMarca());
        payload.put("codigoMarca", marca.getCodigo());
        payload.put("nombreMarca", marca.getNombre());
        payload.put("slugMarca", marca.getSlug());
        payload.put("estadoMarca", marca.getEstado());
        payload.put("productosAfectados", affectedProductIds);
        payload.put("totalProductosAfectados", affectedProductIds.size());
        payload.put("actorIdUsuarioMs1", actor == null ? null : actor.getIdUsuarioMs1());
        payload.put("actor", actor == null ? null : actor.actorLabel());

        eventoDominioOutboxService.registrarEvento(
                AggregateType.PRODUCTO,
                "MARCA:" + marca.getIdMarca(),
                EVENTO_PRODUCTO_MARCA_ACTUALIZADA,
                payload
        );
    }

    private boolean employeeCanCreateProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeCrearProductoBasico(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanEditProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeEditarProductoBasico(actor.getIdUsuarioMs1());
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("ASC")
                    .build();
        }

        return pageRequest;
    }

    private int resolveLookupLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        if (limit < 1) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(limit, MAX_LOOKUP_LIMIT);
    }

    private MarcaOptionDto toOption(Marca entity) {
        return MarcaOptionDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estado(entity.getEstado())
                .build();
    }

    private Map<String, Object> auditMetadata(
            Marca entity,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idMarca", entity.getIdMarca());
        metadata.put("codigo", safe(entity.getCodigo()));
        metadata.put("nombre", safe(entity.getNombre()));
        metadata.put("slug", safe(entity.getSlug()));
        metadata.put("estado", Boolean.TRUE.equals(entity.getEstado()));
        metadata.put("actor", actor == null ? null : actor.actorLabel());
        metadata.put("idUsuarioMs1", actor == null ? null : actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(Marca entity) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("codigo", safe(entity.getCodigo()));
        metadata.put("nombre", safe(entity.getNombre()));
        metadata.put("slug", safe(entity.getSlug()));
        metadata.put("descripcion", safe(entity.getDescripcion()));
        metadata.put("estado", Boolean.TRUE.equals(entity.getEstado()));
        return metadata;
    }

    private String requireText(String value, String code, String message) {
        if (!StringNormalizer.hasText(value)) {
            throw new ValidationException(code, message);
        }

        return StringNormalizer.clean(value);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}