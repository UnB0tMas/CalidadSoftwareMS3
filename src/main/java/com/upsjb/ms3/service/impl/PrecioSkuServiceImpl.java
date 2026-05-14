// ruta: src/main/java/com/upsjb/ms3/service/impl/PrecioSkuServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.PrecioEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.precio.filter.PrecioSkuFilterDto;
import com.upsjb.ms3.dto.precio.request.PrecioSkuCreateRequestDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuHistorialResponseDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotPayload;
import com.upsjb.ms3.mapper.PrecioSkuMapper;
import com.upsjb.ms3.policy.PrecioSkuPolicy;
import com.upsjb.ms3.repository.PrecioSkuHistorialRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.PrecioSkuService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.PrecioSkuSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.PrecioSkuValidator;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecioSkuServiceImpl implements PrecioSkuService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idPrecioHistorial",
            "sku.codigoSku",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "precioVenta",
            "moneda",
            "fechaInicio",
            "fechaFin",
            "vigente",
            "creadoPorIdUsuarioMs1",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final PrecioSkuHistorialRepository precioRepository;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final PrecioSkuMapper precioSkuMapper;
    private final PrecioSkuValidator precioSkuValidator;
    private final PrecioSkuPolicy precioSkuPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<PrecioSkuResponseDto> registrarNuevoPrecio(PrecioSkuCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        precioSkuPolicy.ensureCanCreatePrice(actor);

        PrecioSkuCreateRequestDto normalized = normalizeCreateRequest(request);
        ProductoSku sku = resolveSku(normalized.sku());
        LocalDateTime fechaInicio = normalized.fechaInicio() == null
                ? DateTimeUtil.nowUtc()
                : normalized.fechaInicio();

        PrecioSkuHistorial current = precioRepository
                .findFirstBySku_IdSkuAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(
                        sku.getIdSku()
                )
                .orElse(null);

        boolean sameStartDateExists = precioRepository.existsBySku_IdSkuAndFechaInicioAndEstadoTrue(
                sku.getIdSku(),
                fechaInicio
        );

        precioSkuValidator.validateNewVersion(
                sku,
                normalized.precioVenta(),
                normalized.moneda(),
                fechaInicio,
                normalized.motivo(),
                actor.getIdUsuarioMs1(),
                current,
                sameStartDateExists
        );

        if (current != null) {
            precioSkuMapper.closeVigencia(current, fechaInicio.minusNanos(1));
            precioRepository.save(current);
        }

        PrecioSkuHistorial nuevoPrecio = precioSkuMapper.toEntity(
                normalized,
                sku,
                fechaInicio,
                actor.getIdUsuarioMs1()
        );
        nuevoPrecio.setPrecioVenta(MoneyUtil.normalize(normalized.precioVenta()));
        nuevoPrecio.activar();

        PrecioSkuHistorial saved = precioRepository.saveAndFlush(nuevoPrecio);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRECIO_ACTUALIZADO,
                EntidadAuditada.PRECIO_SKU_HISTORIAL,
                String.valueOf(saved.getIdPrecioHistorial()),
                "REGISTRAR_PRECIO_SKU",
                "Precio actualizado correctamente.",
                auditMetadata(saved, actor, current)
        );

        registrarPrecioOutbox(saved);

        log.info(
                "Precio SKU actualizado. idPrecioHistorial={}, idSku={}, codigoSku={}, actor={}",
                saved.getIdPrecioHistorial(),
                sku.getIdSku(),
                sku.getCodigoSku(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Precio actualizado correctamente.",
                precioSkuMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PrecioSkuResponseDto> obtenerVigente(EntityReferenceDto skuReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        precioSkuPolicy.ensureCanViewCurrentPrice(actor);

        ProductoSku sku = resolveSku(skuReference);

        return obtenerVigentePorSku(sku.getIdSku());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PrecioSkuResponseDto> obtenerVigentePorSku(Long idSku) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        precioSkuPolicy.ensureCanViewCurrentPrice(actor);

        if (idSku == null) {
            throw new ValidationException(
                    "SKU_ID_REQUERIDO",
                    "Debe indicar el SKU solicitado."
            );
        }

        PrecioSkuHistorial precio = precioRepository
                .findFirstBySku_IdSkuAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(idSku)
                .orElseThrow(() -> new NotFoundException(
                        "PRECIO_SKU_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        precioSkuValidator.requireActive(precio);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                precioSkuMapper.toResponse(precio)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PrecioSkuHistorialResponseDto> obtenerDetalle(Long idPrecioHistorial) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        precioSkuPolicy.ensureCanViewHistory(actor);

        PrecioSkuHistorial precio = findPrecioRequired(idPrecioHistorial);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                precioSkuMapper.toHistorialResponse(precio)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>> listarHistorial(
            PrecioSkuFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        precioSkuPolicy.ensureCanViewHistory(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "fechaInicio");
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "fechaInicio"
        );

        PageResponseDto<PrecioSkuHistorialResponseDto> response = paginationService.toPageResponseDto(
                precioRepository.findAll(PrecioSkuSpecifications.fromFilter(filter), pageable),
                precioSkuMapper::toHistorialResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>> listarHistorialPorSku(
            EntityReferenceDto skuReference,
            PageRequestDto pageRequest
    ) {
        ProductoSku sku = resolveSku(skuReference);

        PrecioSkuFilterDto filter = PrecioSkuFilterDto.builder()
                .idSku(sku.getIdSku())
                .estado(Boolean.TRUE)
                .build();

        return listarHistorial(filter, pageRequest);
    }

    private PrecioSkuCreateRequestDto normalizeCreateRequest(PrecioSkuCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PRECIO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del precio."
            );
        }

        return PrecioSkuCreateRequestDto.builder()
                .sku(request.sku())
                .precioVenta(MoneyUtil.normalize(request.precioVenta()))
                .moneda(request.moneda() == null ? Moneda.PEN : request.moneda())
                .fechaInicio(request.fechaInicio())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU solicitado."
            );
        }

        return productoSkuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );
    }

    private PrecioSkuHistorial findPrecioRequired(Long idPrecioHistorial) {
        if (idPrecioHistorial == null) {
            throw new ValidationException(
                    "PRECIO_ID_REQUERIDO",
                    "Debe indicar el precio solicitado."
            );
        }

        PrecioSkuHistorial precio = precioRepository.findByIdPrecioHistorialAndEstadoTrue(idPrecioHistorial)
                .orElseThrow(() -> new NotFoundException(
                        "PRECIO_SKU_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        precioSkuValidator.requireActive(precio);
        return precio;
    }

    private void registrarPrecioOutbox(PrecioSkuHistorial precio) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        PrecioSnapshotEvent event = PrecioSnapshotEvent.of(
                PrecioEventType.PRECIO_SNAPSHOT_ACTUALIZADO,
                precio.getSku().getIdSku(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toPrecioPayload(precio),
                Map.of("source", "PrecioSkuService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private PrecioSnapshotPayload toPrecioPayload(PrecioSkuHistorial precio) {
        ProductoSku sku = precio.getSku();
        Producto producto = sku == null ? null : sku.getProducto();

        return PrecioSnapshotPayload.builder()
                .idPrecioHistorial(precio.getIdPrecioHistorial())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .precioVenta(precio.getPrecioVenta())
                .moneda(precio.getMoneda() == null ? null : precio.getMoneda().getCode())
                .simboloMoneda(precio.getMoneda() == null ? null : precio.getMoneda().getSymbol())
                .fechaInicio(precio.getFechaInicio())
                .fechaFin(precio.getFechaFin())
                .vigente(precio.getVigente())
                .motivo(precio.getMotivo())
                .creadoPorIdUsuarioMs1(precio.getCreadoPorIdUsuarioMs1())
                .estado(precio.getEstado())
                .createdAt(precio.getCreatedAt())
                .updatedAt(precio.getUpdatedAt())
                .build();
    }

    private Map<String, Object> auditMetadata(
            PrecioSkuHistorial precio,
            AuthenticatedUserContext actor,
            PrecioSkuHistorial previous
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        ProductoSku sku = precio.getSku();

        metadata.put("idPrecioHistorial", precio.getIdPrecioHistorial());
        metadata.put("idSku", sku == null ? null : sku.getIdSku());
        metadata.put("codigoSku", sku == null ? null : sku.getCodigoSku());
        metadata.put("precioVenta", precio.getPrecioVenta());
        metadata.put("moneda", precio.getMoneda() == null ? null : precio.getMoneda().getCode());
        metadata.put("fechaInicio", precio.getFechaInicio());
        metadata.put("fechaFin", precio.getFechaFin());
        metadata.put("vigente", precio.getVigente());
        metadata.put("actor", actor.actorLabel());

        if (previous != null) {
            metadata.put("precioAnteriorId", previous.getIdPrecioHistorial());
            metadata.put("precioAnterior", previous.getPrecioVenta());
            metadata.put("precioAnteriorFechaFin", previous.getFechaFin());
        }

        return metadata;
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

    private String firstText(String first, String second) {
        if (StringNormalizer.hasText(first)) {
            return first;
        }

        return second;
    }

    private String traceValue(String value) {
        if (StringNormalizer.hasText(value)) {
            return value;
        }

        return java.util.UUID.randomUUID().toString();
    }
}