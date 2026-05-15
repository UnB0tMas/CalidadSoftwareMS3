// ruta: src/main/java/com/upsjb/ms3/service/impl/ProveedorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import com.upsjb.ms3.dto.proveedor.filter.ProveedorFilterDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorCreateRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorEstadoRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorUpdateRequestDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorDetailResponseDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorResponseDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.ProveedorMapper;
import com.upsjb.ms3.policy.ProveedorPolicy;
import com.upsjb.ms3.repository.CompraInventarioRepository;
import com.upsjb.ms3.repository.ProveedorRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.ProveedorService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProveedorSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.ProveedorValidator;
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
public class ProveedorServiceImpl implements ProveedorService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idProveedor",
            "tipoProveedor",
            "tipoDocumento",
            "numeroDocumento",
            "ruc",
            "razonSocial",
            "nombreComercial",
            "nombres",
            "apellidos",
            "correo",
            "telefono",
            "direccion",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;
    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_DOCUMENTO_LENGTH = 30;
    private static final int MAX_RUC_LENGTH = 20;
    private static final int MAX_RAZON_SOCIAL_LENGTH = 200;
    private static final int MAX_NOMBRE_COMERCIAL_LENGTH = 200;
    private static final int MAX_NOMBRES_LENGTH = 150;
    private static final int MAX_APELLIDOS_LENGTH = 150;
    private static final int MAX_CORREO_LENGTH = 180;
    private static final int MAX_TELEFONO_LENGTH = 30;
    private static final int MAX_DIRECCION_LENGTH = 300;
    private static final int MAX_OBSERVACION_LENGTH = 500;

    private final ProveedorRepository proveedorRepository;
    private final CompraInventarioRepository compraInventarioRepository;
    private final ProveedorMapper proveedorMapper;
    private final ProveedorValidator proveedorValidator;
    private final ProveedorPolicy proveedorPolicy;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProveedorResponseDto> crear(ProveedorCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanCreate(actor, employeeCanRegisterEntry(actor));

        ProveedorCreateRequestDto normalized = normalizeCreate(request);

        proveedorValidator.validateCreate(
                normalized.tipoProveedor(),
                normalized.tipoDocumento(),
                normalized.numeroDocumento(),
                normalized.ruc(),
                normalized.razonSocial(),
                normalized.nombreComercial(),
                normalized.nombres(),
                normalized.apellidos(),
                normalized.correo(),
                normalized.telefono(),
                normalized.direccion(),
                normalized.observacion(),
                actor.getIdUsuarioMs1(),
                existsDocumento(normalized.tipoDocumento(), normalized.numeroDocumento()),
                existsRuc(normalized.ruc())
        );

        Proveedor entity = proveedorMapper.toEntity(normalized, actor.getIdUsuarioMs1());
        entity.activar();

        Proveedor saved = proveedorRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROVEEDOR_CREADO,
                EntidadAuditada.PROVEEDOR,
                String.valueOf(saved.getIdProveedor()),
                "CREAR_PROVEEDOR",
                "Proveedor registrado correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Proveedor creado. idProveedor={}, tipoProveedor={}, documento={}, ruc={}, actor={}",
                saved.getIdProveedor(),
                saved.getTipoProveedor(),
                safe(saved.getNumeroDocumento()),
                safe(saved.getRuc()),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Proveedor registrado correctamente.",
                proveedorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProveedorResponseDto> actualizar(Long idProveedor, ProveedorUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanUpdate(actor, employeeCanRegisterEntry(actor));

        Proveedor entity = findActiveRequired(idProveedor);
        ProveedorUpdateRequestDto normalized = normalizeUpdate(request);

        proveedorValidator.validateUpdate(
                entity,
                normalized.tipoProveedor(),
                normalized.tipoDocumento(),
                normalized.numeroDocumento(),
                normalized.ruc(),
                normalized.razonSocial(),
                normalized.nombreComercial(),
                normalized.nombres(),
                normalized.apellidos(),
                normalized.correo(),
                normalized.telefono(),
                normalized.direccion(),
                normalized.observacion(),
                actor.getIdUsuarioMs1(),
                existsDocumentoExcluding(
                        normalized.tipoDocumento(),
                        normalized.numeroDocumento(),
                        entity.getIdProveedor()
                ),
                existsRucExcluding(normalized.ruc(), entity.getIdProveedor())
        );

        Map<String, Object> before = auditSnapshot(entity);

        proveedorMapper.updateEntity(entity, normalized, actor.getIdUsuarioMs1());

        Proveedor saved = proveedorRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROVEEDOR_ACTUALIZADO,
                EntidadAuditada.PROVEEDOR,
                String.valueOf(saved.getIdProveedor()),
                "ACTUALIZAR_PROVEEDOR",
                "Proveedor actualizado correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        log.info(
                "Proveedor actualizado. idProveedor={}, actor={}",
                saved.getIdProveedor(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Proveedor actualizado correctamente.",
                proveedorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProveedorResponseDto> cambiarEstado(Long idProveedor, ProveedorEstadoRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanChangeState(actor);

        ProveedorEstadoRequestDto normalized = normalizeEstado(request);
        Proveedor entity = findAnyRequired(idProveedor);

        if (Objects.equals(entity.getEstado(), normalized.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    proveedorMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(normalized.estado())) {
            proveedorValidator.validateCanActivate(entity);
            entity.activar();
            entity.setActualizadoPorIdUsuarioMs1(actor.getIdUsuarioMs1());

            Proveedor saved = proveedorRepository.save(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.PROVEEDOR_ACTUALIZADO,
                    EntidadAuditada.PROVEEDOR,
                    String.valueOf(saved.getIdProveedor()),
                    "ACTIVAR_PROVEEDOR",
                    "Operación realizada correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", normalized.motivo()))
            );

            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    proveedorMapper.toResponse(saved)
            );
        }

        boolean hasPendingPurchases = compraInventarioRepository
                .existsByProveedor_IdProveedorAndEstadoCompraAndEstadoTrue(
                        entity.getIdProveedor(),
                        EstadoCompraInventario.BORRADOR
                );

        proveedorValidator.validateCanDeactivate(entity, hasPendingPurchases);

        entity.inactivar();
        entity.setActualizadoPorIdUsuarioMs1(actor.getIdUsuarioMs1());

        Proveedor saved = proveedorRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROVEEDOR_INACTIVADO,
                EntidadAuditada.PROVEEDOR,
                String.valueOf(saved.getIdProveedor()),
                "INACTIVAR_PROVEEDOR",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", normalized.motivo()))
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                proveedorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProveedorResponseDto> obtenerPorId(Long idProveedor) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        Proveedor entity = findAnyRequired(idProveedor);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                proveedorMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProveedorDetailResponseDto> obtenerDetalle(Long idProveedor) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        Proveedor entity = findAnyRequired(idProveedor);
        Long cantidadCompras = compraInventarioRepository.countByProveedor_IdProveedorAndEstadoTrue(
                entity.getIdProveedor()
        );

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                proveedorMapper.toDetailResponse(entity, cantidadCompras)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProveedorResponseDto>> listar(
            ProveedorFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<ProveedorResponseDto> response = paginationService.toPageResponseDto(
                proveedorRepository.findAll(ProveedorSpecifications.fromFilter(normalizeFilter(filter)), pageable),
                proveedorMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProveedorOptionDto>> lookup(String search, Integer limit) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        int safeLimit = sanitizeLimit(limit);

        Pageable pageable = paginationService.pageable(
                0,
                safeLimit,
                "razonSocial",
                "ASC",
                ALLOWED_SORT_FIELDS,
                "razonSocial"
        );

        ProveedorFilterDto filter = ProveedorFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(search, MAX_SEARCH_LENGTH))
                .estado(Boolean.TRUE)
                .incluirTodosLosEstados(Boolean.FALSE)
                .build();

        List<ProveedorOptionDto> response = proveedorRepository.findAll(
                        ProveedorSpecifications.fromFilter(filter),
                        pageable
                )
                .stream()
                .map(this::toOption)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProveedorResponseDto> obtenerPorRuc(String ruc) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        String normalizedRuc = normalizeRuc(ruc);

        if (!StringNormalizer.hasText(normalizedRuc)) {
            throw new ValidationException(
                    "PROVEEDOR_RUC_REQUERIDO",
                    "Debe indicar el RUC del proveedor."
            );
        }

        Proveedor entity = proveedorRepository.findByRucAndEstadoTrue(normalizedRuc)
                .orElseThrow(this::proveedorNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                proveedorMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProveedorResponseDto> obtenerPorDocumento(String numeroDocumento) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        String normalizedDocumento = normalizeDocumento(null, numeroDocumento);

        if (!StringNormalizer.hasText(normalizedDocumento)) {
            throw new ValidationException(
                    "PROVEEDOR_DOCUMENTO_REQUERIDO",
                    "Debe indicar el documento del proveedor."
            );
        }

        Proveedor entity = proveedorRepository.findByNumeroDocumentoAndEstadoTrue(normalizedDocumento)
                .orElseThrow(this::proveedorNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                proveedorMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProveedorResponseDto> obtenerPorDocumento(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        proveedorPolicy.ensureCanViewAdmin(actor, employeeHasInventoryPermission(actor));

        if (tipoDocumento == null) {
            throw new ValidationException(
                    "PROVEEDOR_TIPO_DOCUMENTO_REQUERIDO",
                    "Debe indicar el tipo de documento del proveedor."
            );
        }

        String normalizedDocumento = normalizeDocumento(tipoDocumento, numeroDocumento);

        if (!StringNormalizer.hasText(normalizedDocumento)) {
            throw new ValidationException(
                    "PROVEEDOR_DOCUMENTO_REQUERIDO",
                    "Debe indicar el documento del proveedor."
            );
        }

        Proveedor entity = proveedorRepository
                .findByTipoDocumentoAndNumeroDocumentoAndEstadoTrue(tipoDocumento, normalizedDocumento)
                .orElseThrow(this::proveedorNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                proveedorMapper.toResponse(entity)
        );
    }

    private ProveedorCreateRequestDto normalizeCreate(ProveedorCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROVEEDOR_REQUEST_REQUERIDO",
                    "Debe enviar los datos del proveedor."
            );
        }

        TipoProveedor tipoProveedor = request.tipoProveedor();

        return ProveedorCreateRequestDto.builder()
                .tipoProveedor(tipoProveedor)
                .tipoDocumento(isPersonaNatural(tipoProveedor) ? request.tipoDocumento() : null)
                .numeroDocumento(isPersonaNatural(tipoProveedor)
                        ? normalizeDocumento(request.tipoDocumento(), request.numeroDocumento())
                        : null)
                .ruc(isEmpresa(tipoProveedor) ? normalizeRuc(request.ruc()) : null)
                .razonSocial(isEmpresa(tipoProveedor) ? clean(request.razonSocial()) : null)
                .nombreComercial(isEmpresa(tipoProveedor) ? clean(request.nombreComercial()) : null)
                .nombres(isPersonaNatural(tipoProveedor) ? clean(request.nombres()) : null)
                .apellidos(isPersonaNatural(tipoProveedor) ? clean(request.apellidos()) : null)
                .correo(normalizeEmail(request.correo()))
                .telefono(clean(request.telefono()))
                .direccion(clean(request.direccion()))
                .observacion(clean(request.observacion()))
                .build();
    }

    private ProveedorUpdateRequestDto normalizeUpdate(ProveedorUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROVEEDOR_REQUEST_REQUERIDO",
                    "Debe enviar los datos del proveedor."
            );
        }

        TipoProveedor tipoProveedor = request.tipoProveedor();

        return ProveedorUpdateRequestDto.builder()
                .tipoProveedor(tipoProveedor)
                .tipoDocumento(isPersonaNatural(tipoProveedor) ? request.tipoDocumento() : null)
                .numeroDocumento(isPersonaNatural(tipoProveedor)
                        ? normalizeDocumento(request.tipoDocumento(), request.numeroDocumento())
                        : null)
                .ruc(isEmpresa(tipoProveedor) ? normalizeRuc(request.ruc()) : null)
                .razonSocial(isEmpresa(tipoProveedor) ? clean(request.razonSocial()) : null)
                .nombreComercial(isEmpresa(tipoProveedor) ? clean(request.nombreComercial()) : null)
                .nombres(isPersonaNatural(tipoProveedor) ? clean(request.nombres()) : null)
                .apellidos(isPersonaNatural(tipoProveedor) ? clean(request.apellidos()) : null)
                .correo(normalizeEmail(request.correo()))
                .telefono(clean(request.telefono()))
                .direccion(clean(request.direccion()))
                .observacion(clean(request.observacion()))
                .build();
    }

    private ProveedorEstadoRequestDto normalizeEstado(ProveedorEstadoRequestDto request) {
        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "PROVEEDOR_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        String motivo = clean(request.motivo());

        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }

        if (motivo.length() > MAX_OBSERVACION_LENGTH) {
            throw new ValidationException(
                    "MOTIVO_INVALIDO",
                    "El motivo no debe superar 500 caracteres."
            );
        }

        return ProveedorEstadoRequestDto.builder()
                .estado(request.estado())
                .motivo(motivo)
                .build();
    }

    private ProveedorFilterDto normalizeFilter(ProveedorFilterDto filter) {
        if (filter == null) {
            return ProveedorFilterDto.builder()
                    .estado(Boolean.TRUE)
                    .incluirTodosLosEstados(Boolean.FALSE)
                    .build();
        }

        return ProveedorFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .tipoProveedor(filter.tipoProveedor())
                .tipoDocumento(filter.tipoDocumento())
                .numeroDocumento(StringNormalizer.truncateOrNull(
                        normalizeDocumento(filter.tipoDocumento(), filter.numeroDocumento()),
                        MAX_DOCUMENTO_LENGTH
                ))
                .ruc(StringNormalizer.truncateOrNull(normalizeRuc(filter.ruc()), MAX_RUC_LENGTH))
                .razonSocial(StringNormalizer.truncateOrNull(filter.razonSocial(), MAX_RAZON_SOCIAL_LENGTH))
                .nombreComercial(StringNormalizer.truncateOrNull(filter.nombreComercial(), MAX_NOMBRE_COMERCIAL_LENGTH))
                .nombres(StringNormalizer.truncateOrNull(filter.nombres(), MAX_NOMBRES_LENGTH))
                .apellidos(StringNormalizer.truncateOrNull(filter.apellidos(), MAX_APELLIDOS_LENGTH))
                .correo(StringNormalizer.truncateOrNull(normalizeEmail(filter.correo()), MAX_CORREO_LENGTH))
                .telefono(StringNormalizer.truncateOrNull(filter.telefono(), MAX_TELEFONO_LENGTH))
                .direccion(StringNormalizer.truncateOrNull(filter.direccion(), MAX_DIRECCION_LENGTH))
                .creadoPorIdUsuarioMs1(filter.creadoPorIdUsuarioMs1())
                .actualizadoPorIdUsuarioMs1(filter.actualizadoPorIdUsuarioMs1())
                .estado(filter.estado())
                .incluirTodosLosEstados(Boolean.TRUE.equals(filter.incluirTodosLosEstados()))
                .fechaCreacion(filter.fechaCreacion())
                .fechaActualizacion(filter.fechaActualizacion())
                .build();
    }

    private Proveedor findActiveRequired(Long idProveedor) {
        if (idProveedor == null) {
            throw new ValidationException(
                    "PROVEEDOR_ID_REQUERIDO",
                    "Debe indicar el proveedor solicitado."
            );
        }

        Proveedor entity = proveedorRepository.findByIdProveedorAndEstadoTrue(idProveedor)
                .orElseThrow(this::proveedorNotFound);

        proveedorValidator.requireActive(entity);
        return entity;
    }

    private Proveedor findAnyRequired(Long idProveedor) {
        if (idProveedor == null) {
            throw new ValidationException(
                    "PROVEEDOR_ID_REQUERIDO",
                    "Debe indicar el proveedor solicitado."
            );
        }

        Proveedor entity = proveedorRepository.findById(idProveedor)
                .orElseThrow(this::proveedorNotFound);

        proveedorValidator.requireExists(entity);
        return entity;
    }

    private boolean employeeCanRegisterEntry(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeRegistrarEntrada(actor.getIdUsuarioMs1());
    }

    private boolean employeeHasInventoryPermission(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.tienePermisoVigente(actor.getIdUsuarioMs1());
    }

    private boolean existsDocumento(TipoDocumentoProveedor tipoDocumento, String numeroDocumento) {
        if (!StringNormalizer.hasText(numeroDocumento)) {
            return false;
        }

        if (tipoDocumento == null) {
            return proveedorRepository.existsByNumeroDocumentoAndEstadoTrue(numeroDocumento);
        }

        return proveedorRepository.existsByTipoDocumentoAndNumeroDocumentoAndEstadoTrue(
                tipoDocumento,
                numeroDocumento
        );
    }

    private boolean existsDocumentoExcluding(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento,
            Long idProveedor
    ) {
        if (!StringNormalizer.hasText(numeroDocumento)) {
            return false;
        }

        if (tipoDocumento == null) {
            return proveedorRepository.existsByNumeroDocumentoAndEstadoTrueAndIdProveedorNot(
                    numeroDocumento,
                    idProveedor
            );
        }

        return proveedorRepository.existsByTipoDocumentoAndNumeroDocumentoAndEstadoTrueAndIdProveedorNot(
                tipoDocumento,
                numeroDocumento,
                idProveedor
        );
    }

    private boolean existsRuc(String ruc) {
        return StringNormalizer.hasText(ruc)
                && proveedorRepository.existsByRucAndEstadoTrue(ruc);
    }

    private boolean existsRucExcluding(String ruc, Long idProveedor) {
        return StringNormalizer.hasText(ruc)
                && proveedorRepository.existsByRucAndEstadoTrueAndIdProveedorNot(ruc, idProveedor);
    }

    private ProveedorOptionDto toOption(Proveedor entity) {
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
                .displayName(proveedorMapper.displayName(entity))
                .estado(entity.getEstado())
                .build();
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

    private String normalizeRuc(String value) {
        String digits = StringNormalizer.onlyDigits(value);
        return digits.isBlank() ? null : digits;
    }

    private String normalizeDocumento(TipoDocumentoProveedor tipoDocumento, String value) {
        if (!StringNormalizer.hasText(value)) {
            return null;
        }

        String normalized = tipoDocumento != null && tipoDocumento.isSoloNumeros()
                ? StringNormalizer.onlyDigits(value)
                : StringNormalizer.clean(value);

        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeEmail(String value) {
        String cleaned = StringNormalizer.cleanOrNull(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    private String clean(String value) {
        return StringNormalizer.cleanOrNull(value);
    }

    private boolean isEmpresa(TipoProveedor tipoProveedor) {
        return TipoProveedor.EMPRESA.equals(tipoProveedor);
    }

    private boolean isPersonaNatural(TipoProveedor tipoProveedor) {
        return TipoProveedor.PERSONA_NATURAL.equals(tipoProveedor);
    }

    private Map<String, Object> auditMetadata(
            Proveedor proveedor,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(proveedor);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(Proveedor proveedor) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("idProveedor", proveedor.getIdProveedor());
        metadata.put("tipoProveedor", proveedor.getTipoProveedor() == null ? null : proveedor.getTipoProveedor().getCode());
        metadata.put("tipoDocumento", proveedor.getTipoDocumento() == null ? null : proveedor.getTipoDocumento().getCode());
        metadata.put("numeroDocumento", safe(proveedor.getNumeroDocumento()));
        metadata.put("ruc", safe(proveedor.getRuc()));
        metadata.put("razonSocial", safe(proveedor.getRazonSocial()));
        metadata.put("nombreComercial", safe(proveedor.getNombreComercial()));
        metadata.put("nombres", safe(proveedor.getNombres()));
        metadata.put("apellidos", safe(proveedor.getApellidos()));
        metadata.put("correo", safe(proveedor.getCorreo()));
        metadata.put("telefono", safe(proveedor.getTelefono()));
        metadata.put("estado", proveedor.getEstado());

        return metadata;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private NotFoundException proveedorNotFound() {
        return new NotFoundException(
                "PROVEEDOR_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }
}