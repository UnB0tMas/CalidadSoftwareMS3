// ruta: src/main/java/com/upsjb/ms3/service/impl/ReservaStockServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.inventario.reserva.filter.ReservaStockFilterDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockCreateRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockLiberarRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockMs4RequestDto;
import com.upsjb.ms3.dto.inventario.reserva.response.ReservaStockResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.MovimientoInventarioPayload;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import com.upsjb.ms3.mapper.MovimientoInventarioMapper;
import com.upsjb.ms3.mapper.ReservaStockMapper;
import com.upsjb.ms3.policy.ReservaStockPolicy;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.ReservaStockService;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ReservaStockSpecifications;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.MovimientoInventarioValidator;
import com.upsjb.ms3.validator.ProductoSkuValidator;
import com.upsjb.ms3.validator.ReservaStockValidator;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaStockServiceImpl implements ReservaStockService {

    private static final Long SYSTEM_USER_ID = 0L;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idReservaStock",
            "codigoReserva",
            "sku.codigoSku",
            "sku.barcode",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "almacen.codigo",
            "almacen.nombre",
            "referenciaTipo",
            "referenciaIdExterno",
            "cantidad",
            "estadoReserva",
            "reservadoPorIdUsuarioMs1",
            "confirmadoPorIdUsuarioMs1",
            "liberadoPorIdUsuarioMs1",
            "reservadoAt",
            "confirmadoAt",
            "liberadoAt",
            "expiresAt",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_CODIGO_RESERVA_LENGTH = 80;
    private static final int MAX_CODIGO_SKU_LENGTH = 100;
    private static final int MAX_BARCODE_LENGTH = 100;
    private static final int MAX_CODIGO_PRODUCTO_LENGTH = 80;
    private static final int MAX_NOMBRE_PRODUCTO_LENGTH = 180;
    private static final int MAX_CODIGO_ALMACEN_LENGTH = 50;
    private static final int MAX_NOMBRE_ALMACEN_LENGTH = 150;
    private static final int MAX_REFERENCIA_LENGTH = 100;
    private static final int MAX_MOTIVO_LENGTH = 500;
    private static final int MAX_TRACE_LENGTH = 100;
    private static final int MAX_IDEMPOTENCY_LENGTH = 180;

    private final ReservaStockRepository reservaStockRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final AlmacenRepository almacenRepository;
    private final StockSkuRepository stockSkuRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ReservaStockMapper reservaStockMapper;
    private final MovimientoInventarioMapper movimientoInventarioMapper;
    private final ReservaStockValidator reservaStockValidator;
    private final ProductoSkuValidator productoSkuValidator;
    private final MovimientoInventarioValidator movimientoInventarioValidator;
    private final ReservaStockPolicy reservaStockPolicy;
    private final CodigoGeneradorService codigoGeneradorService;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;
    private final AppPropertiesConfig appPropertiesConfig;

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> crear(ReservaStockCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanCreateManualReservation(actor, employeeCanRegisterOutput(actor));

        ReservaStockCreateRequestDto normalized = normalizeCreateRequest(request);
        ProductoSku sku = resolveSku(normalized.sku());
        Almacen almacen = resolveAlmacen(normalized.almacen());

        productoSkuValidator.validateCanSell(sku);

        StockSku stock = resolveStockForUpdate(sku, almacen);
        boolean duplicated = existsActiveReservation(
                normalized.referenciaTipo(),
                normalized.referenciaIdExterno(),
                sku.getIdSku(),
                almacen.getIdAlmacen()
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = normalized.expiresAt() == null
                ? now.plus(appPropertiesConfig.reservationDuration())
                : normalized.expiresAt();

        reservaStockValidator.validateCreate(
                sku,
                almacen,
                stock,
                normalized.referenciaTipo(),
                normalized.referenciaIdExterno(),
                normalized.cantidad(),
                expiresAt,
                actor.getIdUsuarioMs1(),
                normalized.motivo(),
                duplicated
        );

        int disponibleAnterior = StockMathUtil.available(stock.getStockFisico(), stock.getStockReservado());

        ReservaStock reserva = reservaStockMapper.toEntity(
                normalized,
                sku,
                almacen,
                codigoGeneradorService.generarCodigoReservaStock(),
                actor.getIdUsuarioMs1(),
                now,
                expiresAt
        );
        reserva.activar();

        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        stock.setStockReservado(StockMathUtil.reserve(stock.getStockReservado(), savedReserva.getCantidad()));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        int disponibleNuevo = StockMathUtil.available(savedStock.getStockFisico(), savedStock.getStockReservado());

        MovimientoInventario movimiento = registrarMovimientoReserva(
                savedReserva,
                savedStock,
                TipoMovimientoInventario.RESERVA_VENTA,
                MotivoMovimientoInventario.RESERVA_VENTA,
                disponibleAnterior,
                disponibleNuevo,
                actor,
                normalized.motivo()
        );

        auditarReserva(
                TipoEventoAuditoria.RESERVA_STOCK_CREADA,
                savedReserva,
                "CREAR_RESERVA_STOCK",
                "Reserva creada correctamente.",
                actor,
                null
        );

        registrarOutbox(savedStock, movimiento, StockEventType.STOCK_RESERVADO);

        log.info(
                "Reserva de stock creada. idReserva={}, codigoReserva={}, sku={}, almacen={}, referenciaTipo={}, referenciaId={}, actor={}",
                savedReserva.getIdReservaStock(),
                savedReserva.getCodigoReserva(),
                sku.getCodigoSku(),
                almacen.getCodigo(),
                savedReserva.getReferenciaTipo(),
                savedReserva.getReferenciaIdExterno(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Reserva creada correctamente.",
                reservaStockMapper.toResponse(savedReserva)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> procesarReservaMs4(ReservaStockMs4RequestDto request) {
        ReservaStockMs4RequestDto normalized = normalizeMs4Request(request);
        AuthenticatedUserContext actor = currentUserResolver.resolveOptional().orElse(null);
        reservaStockPolicy.ensureCanProcessMs4Reservation(actor);

        ProductoSku sku = resolveSku(normalized.sku());
        Almacen almacen = resolveAlmacen(normalized.almacen());

        Optional<ReservaStock> existing = reservaStockRepository
                .findByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        normalized.referenciaTipo(),
                        normalized.referenciaIdExterno(),
                        sku.getIdSku(),
                        almacen.getIdAlmacen()
                );

        if (existing.isPresent()) {
            return apiResponseFactory.dtoOk(
                    "Reserva de stock ya procesada correctamente.",
                    reservaStockMapper.toResponse(existing.get())
            );
        }

        productoSkuValidator.validateCanSell(sku);

        StockSku stock = resolveStockForUpdate(sku, almacen);
        Long actorIdUsuario = resolveActorId(normalized.actorIdUsuarioMs1(), actor);
        RolSistema actorRol = normalized.actorRol() == null ? resolveRol(actor) : normalized.actorRol();

        LocalDateTime reservadoAt = normalized.occurredAt() == null ? LocalDateTime.now() : normalized.occurredAt();
        LocalDateTime expiresAt = normalized.expiresAt() == null
                ? reservadoAt.plus(appPropertiesConfig.reservationDuration())
                : normalized.expiresAt();

        reservaStockValidator.validateCreate(
                sku,
                almacen,
                stock,
                normalized.referenciaTipo(),
                normalized.referenciaIdExterno(),
                normalized.cantidad(),
                expiresAt,
                actorIdUsuario,
                normalized.motivo(),
                false
        );

        int disponibleAnterior = StockMathUtil.available(stock.getStockFisico(), stock.getStockReservado());

        ReservaStock reserva = reservaStockMapper.toEntity(
                normalized,
                sku,
                almacen,
                codigoGeneradorService.generarCodigoReservaStock(),
                reservadoAt,
                expiresAt
        );
        reserva.setReservadoPorIdUsuarioMs1(actorIdUsuario);
        reserva.activar();

        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        stock.setStockReservado(StockMathUtil.reserve(stock.getStockReservado(), savedReserva.getCantidad()));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        int disponibleNuevo = StockMathUtil.available(savedStock.getStockFisico(), savedStock.getStockReservado());

        MovimientoInventario movimiento = registrarMovimientoReservaMs4(
                savedReserva,
                savedStock,
                TipoMovimientoInventario.RESERVA_VENTA,
                MotivoMovimientoInventario.RESERVA_VENTA,
                disponibleAnterior,
                disponibleNuevo,
                actorIdUsuario,
                normalized.actorIdEmpleadoMs2(),
                actorRol,
                firstTrace(normalized.requestId(), normalized.eventId()),
                firstTrace(normalized.correlationId(), normalized.idempotencyKey()),
                normalized.motivo()
        );

        auditarReserva(
                TipoEventoAuditoria.RESERVA_STOCK_CREADA,
                savedReserva,
                "PROCESAR_RESERVA_STOCK_MS4",
                "Reserva de stock creada correctamente.",
                actor,
                Map.of(
                        "eventId", safeMetadataValue(normalized.eventId()),
                        "idempotencyKey", safeMetadataValue(normalized.idempotencyKey())
                )
        );

        registrarOutbox(savedStock, movimiento, StockEventType.STOCK_RESERVADO);

        return apiResponseFactory.dtoCreated(
                "Reserva creada correctamente.",
                reservaStockMapper.toResponse(savedReserva)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> confirmar(
            Long idReservaStock,
            ReservaStockConfirmRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanConfirmReservation(actor, employeeCanRegisterOutput(actor));

        ReservaStock reserva = reservaStockRepository.findActivoByIdForUpdate(idReservaStock)
                .orElseThrow(this::reservaNotFound);

        return confirmarInterno(reserva, request, actor);
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> confirmarPorCodigo(
            String codigoReserva,
            ReservaStockConfirmRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanConfirmReservation(actor, employeeCanRegisterOutput(actor));

        if (!StringNormalizer.hasText(codigoReserva)) {
            throw new ValidationException(
                    "CODIGO_RESERVA_REQUERIDO",
                    "Debe indicar el código de reserva."
            );
        }

        ReservaStock reserva = reservaStockRepository.findActivoByCodigoForUpdate(StringNormalizer.clean(codigoReserva))
                .orElseThrow(this::reservaNotFound);

        return confirmarInterno(reserva, request, actor);
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> confirmarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            ReservaStockConfirmRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanConfirmReservation(actor, employeeCanRegisterOutput(actor));

        reservaStockValidator.validateReferenceLookup(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen
        );

        ReservaStock reserva = reservaStockRepository
                .findActivoByReferenciaForUpdate(
                        referenciaTipo,
                        StringNormalizer.clean(referenciaIdExterno),
                        idSku,
                        idAlmacen
                )
                .orElseThrow(this::reservaNotFound);

        return confirmarInterno(reserva, request, actor);
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> confirmarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            ReservaStockConfirmRequestDto request
    ) {
        ProductoSku resolvedSku = resolveSku(sku);
        Almacen resolvedAlmacen = resolveAlmacen(almacen);

        return confirmarPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                resolvedSku.getIdSku(),
                resolvedAlmacen.getIdAlmacen(),
                request
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> liberar(
            Long idReservaStock,
            ReservaStockLiberarRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanReleaseReservation(actor, employeeCanRegisterOutput(actor));

        ReservaStock reserva = reservaStockRepository.findActivoByIdForUpdate(idReservaStock)
                .orElseThrow(this::reservaNotFound);

        return liberarInterno(
                reserva,
                request,
                actor,
                EstadoReservaStock.LIBERADA,
                StockEventType.STOCK_RESERVA_LIBERADA
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> liberarPorCodigo(
            String codigoReserva,
            ReservaStockLiberarRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanReleaseReservation(actor, employeeCanRegisterOutput(actor));

        if (!StringNormalizer.hasText(codigoReserva)) {
            throw new ValidationException(
                    "CODIGO_RESERVA_REQUERIDO",
                    "Debe indicar el código de reserva."
            );
        }

        ReservaStock reserva = reservaStockRepository.findActivoByCodigoForUpdate(StringNormalizer.clean(codigoReserva))
                .orElseThrow(this::reservaNotFound);

        return liberarInterno(
                reserva,
                request,
                actor,
                EstadoReservaStock.LIBERADA,
                StockEventType.STOCK_RESERVA_LIBERADA
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> liberarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            ReservaStockLiberarRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanReleaseReservation(actor, employeeCanRegisterOutput(actor));

        reservaStockValidator.validateReferenceLookup(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen
        );

        ReservaStock reserva = reservaStockRepository
                .findActivoByReferenciaForUpdate(
                        referenciaTipo,
                        StringNormalizer.clean(referenciaIdExterno),
                        idSku,
                        idAlmacen
                )
                .orElseThrow(this::reservaNotFound);

        return liberarInterno(
                reserva,
                request,
                actor,
                EstadoReservaStock.LIBERADA,
                StockEventType.STOCK_RESERVA_LIBERADA
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> liberarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            ReservaStockLiberarRequestDto request
    ) {
        ProductoSku resolvedSku = resolveSku(sku);
        Almacen resolvedAlmacen = resolveAlmacen(almacen);

        return liberarPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                resolvedSku.getIdSku(),
                resolvedAlmacen.getIdAlmacen(),
                request
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ReservaStockResponseDto> vencer(Long idReservaStock) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanExpireReservation(actor);

        ReservaStock reserva = reservaStockRepository.findActivoByIdForUpdate(idReservaStock)
                .orElseThrow(this::reservaNotFound);

        reservaStockValidator.validateCanExpire(reserva, LocalDateTime.now());

        ReservaStockLiberarRequestDto request = ReservaStockLiberarRequestDto.builder()
                .motivo("Reserva vencida automáticamente.")
                .build();

        return liberarInterno(
                reserva,
                request,
                actor,
                EstadoReservaStock.VENCIDA,
                StockEventType.STOCK_RESERVA_VENCIDA
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ReservaStockResponseDto> obtenerPorId(Long idReservaStock) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanViewAdmin(actor);

        if (idReservaStock == null) {
            throw new ValidationException(
                    "RESERVA_STOCK_ID_REQUERIDO",
                    "Debe indicar la reserva solicitada."
            );
        }

        ReservaStock reserva = reservaStockRepository.findByIdReservaStock(idReservaStock)
                .orElseThrow(this::reservaNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                reservaStockMapper.toResponse(reserva)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ReservaStockResponseDto> obtenerPorCodigo(String codigoReserva) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanViewAdmin(actor);

        if (!StringNormalizer.hasText(codigoReserva)) {
            throw new ValidationException(
                    "CODIGO_RESERVA_REQUERIDO",
                    "Debe indicar el código de reserva."
            );
        }

        ReservaStock reserva = reservaStockRepository
                .findByCodigoReservaIgnoreCaseAndEstadoTrue(StringNormalizer.clean(codigoReserva))
                .orElseThrow(this::reservaNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                reservaStockMapper.toResponse(reserva)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ReservaStockResponseDto> obtenerPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanViewAdmin(actor);

        reservaStockValidator.validateReferenceLookup(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen
        );

        ReservaStock reserva = reservaStockRepository
                .findByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        referenciaTipo,
                        StringNormalizer.clean(referenciaIdExterno),
                        idSku,
                        idAlmacen
                )
                .orElseThrow(this::reservaNotFound);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                reservaStockMapper.toResponse(reserva)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ReservaStockResponseDto> obtenerPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen
    ) {
        ProductoSku resolvedSku = resolveSku(sku);
        Almacen resolvedAlmacen = resolveAlmacen(almacen);

        return obtenerPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                resolvedSku.getIdSku(),
                resolvedAlmacen.getIdAlmacen()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ReservaStockResponseDto>> listar(
            ReservaStockFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        reservaStockPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");
        ReservaStockFilterDto normalizedFilter = normalizeFilter(filter);

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<ReservaStockResponseDto> response = paginationService.toPageResponseDto(
                reservaStockRepository.findAll(ReservaStockSpecifications.fromFilter(normalizedFilter), pageable),
                reservaStockMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private ApiResponseDto<ReservaStockResponseDto> confirmarInterno(
            ReservaStock reserva,
            ReservaStockConfirmRequestDto request,
            AuthenticatedUserContext actor
    ) {
        ReservaStockConfirmRequestDto normalized = normalizeConfirmRequest(request);

        if (EstadoReservaStock.CONFIRMADA.equals(reserva.getEstadoReserva())) {
            return apiResponseFactory.dtoOk(
                    "Reserva ya confirmada correctamente.",
                    reservaStockMapper.toResponse(reserva)
            );
        }

        reservaStockValidator.validateCanConfirm(
                reserva,
                actor.getIdUsuarioMs1(),
                LocalDateTime.now(),
                normalized.motivo()
        );

        StockSku stock = resolveStockForUpdate(reserva.getSku(), reserva.getAlmacen());
        reservaStockValidator.validateReservedStockEnough(stock, reserva);

        int fisicoAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int reservadoAnterior = StockMathUtil.zeroIfNull(stock.getStockReservado());

        if (fisicoAnterior < reserva.getCantidad()) {
            throw new ConflictException(
                    "STOCK_FISICO_INSUFICIENTE",
                    "No se puede confirmar la reserva porque el stock físico es insuficiente."
            );
        }

        stock.setStockFisico(fisicoAnterior - reserva.getCantidad());
        stock.setStockReservado(reservadoAnterior - reserva.getCantidad());
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        reservaStockMapper.markConfirmada(
                reserva,
                actor.getIdUsuarioMs1(),
                LocalDateTime.now(),
                normalized.motivo()
        );

        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = registrarMovimientoReserva(
                savedReserva,
                savedStock,
                TipoMovimientoInventario.CONFIRMACION_VENTA,
                MotivoMovimientoInventario.CONFIRMACION_VENTA,
                fisicoAnterior,
                savedStock.getStockFisico(),
                actor,
                normalized.motivo()
        );

        auditarReserva(
                TipoEventoAuditoria.RESERVA_STOCK_CONFIRMADA,
                savedReserva,
                "CONFIRMAR_RESERVA_STOCK",
                "Reserva confirmada correctamente.",
                actor,
                null
        );

        registrarOutbox(savedStock, movimiento, StockEventType.STOCK_RESERVA_CONFIRMADA);

        return apiResponseFactory.dtoOk(
                "Reserva confirmada correctamente.",
                reservaStockMapper.toResponse(savedReserva)
        );
    }

    private ApiResponseDto<ReservaStockResponseDto> liberarInterno(
            ReservaStock reserva,
            ReservaStockLiberarRequestDto request,
            AuthenticatedUserContext actor,
            EstadoReservaStock estadoDestino,
            StockEventType stockEventType
    ) {
        ReservaStockLiberarRequestDto normalized = normalizeLiberarRequest(request);

        if (EstadoReservaStock.LIBERADA.equals(reserva.getEstadoReserva())
                || EstadoReservaStock.VENCIDA.equals(reserva.getEstadoReserva())) {
            return apiResponseFactory.dtoOk(
                    "Reserva ya liberada correctamente.",
                    reservaStockMapper.toResponse(reserva)
            );
        }

        reservaStockValidator.validateCanRelease(
                reserva,
                actor.getIdUsuarioMs1(),
                normalized.motivo()
        );

        StockSku stock = resolveStockForUpdate(reserva.getSku(), reserva.getAlmacen());
        reservaStockValidator.validateReservedStockEnough(stock, reserva);

        int disponibleAnterior = StockMathUtil.available(stock.getStockFisico(), stock.getStockReservado());
        int reservadoAnterior = StockMathUtil.zeroIfNull(stock.getStockReservado());

        stock.setStockReservado(StockMathUtil.releaseReserved(reservadoAnterior, reserva.getCantidad()));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        int disponibleNuevo = StockMathUtil.available(savedStock.getStockFisico(), savedStock.getStockReservado());

        if (EstadoReservaStock.VENCIDA.equals(estadoDestino)) {
            reservaStockMapper.markVencida(reserva, LocalDateTime.now());
            reserva.setLiberadoPorIdUsuarioMs1(actor.getIdUsuarioMs1());
            reserva.setMotivo(normalized.motivo());
        } else {
            reservaStockMapper.markLiberada(
                    reserva,
                    actor.getIdUsuarioMs1(),
                    LocalDateTime.now(),
                    normalized.motivo()
            );
        }

        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = registrarMovimientoReserva(
                savedReserva,
                savedStock,
                TipoMovimientoInventario.LIBERACION_RESERVA,
                MotivoMovimientoInventario.LIBERACION_RESERVA,
                disponibleAnterior,
                disponibleNuevo,
                actor,
                normalized.motivo()
        );

        auditarReserva(
                TipoEventoAuditoria.RESERVA_STOCK_LIBERADA,
                savedReserva,
                EstadoReservaStock.VENCIDA.equals(estadoDestino)
                        ? "VENCER_RESERVA_STOCK"
                        : "LIBERAR_RESERVA_STOCK",
                EstadoReservaStock.VENCIDA.equals(estadoDestino)
                        ? "Reserva vencida correctamente."
                        : "Reserva liberada correctamente.",
                actor,
                null
        );

        registrarOutbox(savedStock, movimiento, stockEventType);

        return apiResponseFactory.dtoOk(
                EstadoReservaStock.VENCIDA.equals(estadoDestino)
                        ? "Reserva vencida correctamente."
                        : "Reserva liberada correctamente.",
                reservaStockMapper.toResponse(savedReserva)
        );
    }

    private MovimientoInventario registrarMovimientoReserva(
            ReservaStock reserva,
            StockSku stock,
            TipoMovimientoInventario tipoMovimiento,
            MotivoMovimientoInventario motivoMovimiento,
            Integer stockAnterior,
            Integer stockNuevo,
            AuthenticatedUserContext actor,
            String observacion
    ) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        return registrarMovimientoReservaMs4(
                reserva,
                stock,
                tipoMovimiento,
                motivoMovimiento,
                stockAnterior,
                stockNuevo,
                resolveActorId(null, actor),
                actor == null ? null : actor.getIdEmpleadoMs2(),
                resolveRol(actor),
                context.requestId(),
                context.correlationId(),
                observacion
        );
    }

    private MovimientoInventario registrarMovimientoReservaMs4(
            ReservaStock reserva,
            StockSku stock,
            TipoMovimientoInventario tipoMovimiento,
            MotivoMovimientoInventario motivoMovimiento,
            Integer stockAnterior,
            Integer stockNuevo,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            String requestId,
            String correlationId,
            String observacion
    ) {
        String safeRequestId = traceValue(requestId);
        String safeCorrelationId = traceValue(correlationId);

        MovimientoInventario movimiento = movimientoInventarioMapper.toEntity(
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                reserva.getSku(),
                reserva.getAlmacen(),
                null,
                reserva,
                tipoMovimiento,
                motivoMovimiento,
                reserva.getCantidad(),
                null,
                null,
                stockAnterior,
                stockNuevo,
                reserva.getReferenciaTipo().getCode(),
                reserva.getReferenciaIdExterno(),
                StringNormalizer.truncateOrNull(observacion, MAX_MOTIVO_LENGTH),
                actorIdUsuarioMs1,
                actorIdEmpleadoMs2,
                actorRol,
                safeRequestId,
                safeCorrelationId
        );

        movimientoInventarioValidator.validateCreate(
                reserva.getSku(),
                reserva.getAlmacen(),
                movimiento.getTipoMovimiento(),
                movimiento.getMotivoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getCostoUnitario(),
                movimiento.getReferenciaTipo(),
                movimiento.getReferenciaIdExterno(),
                movimiento.getActorIdUsuarioMs1(),
                movimiento.getActorRol(),
                movimiento.getRequestId(),
                movimiento.getCorrelationId()
        );

        MovimientoInventario saved = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.MOVIMIENTO_KARDEX_REGISTRADO,
                EntidadAuditada.MOVIMIENTO_INVENTARIO,
                String.valueOf(saved.getIdMovimiento()),
                "REGISTRAR_MOVIMIENTO_RESERVA_STOCK",
                "Movimiento de inventario registrado correctamente.",
                movimientoAuditMetadata(saved, reserva, stock)
        );

        return saved;
    }

    private void registrarOutbox(
            StockSku stock,
            MovimientoInventario movimiento,
            StockEventType stockEventType
    ) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        eventoDominioOutboxService.registrarEvento(
                StockSnapshotEvent.of(
                        stockEventType,
                        stock.getIdStock(),
                        traceValue(context.requestId()),
                        traceValue(context.correlationId()),
                        toStockSnapshotPayload(stock),
                        Map.of("source", "ReservaStockService")
                )
        );

        eventoDominioOutboxService.registrarEvento(
                MovimientoInventarioEvent.of(
                        StockEventType.MOVIMIENTO_INVENTARIO_REGISTRADO,
                        movimiento.getIdMovimiento(),
                        traceValue(context.requestId()),
                        traceValue(context.correlationId()),
                        toMovimientoPayload(movimiento),
                        Map.of("source", "ReservaStockService")
                )
        );
    }

    private StockSnapshotPayload toStockSnapshotPayload(StockSku stock) {
        ProductoSku sku = stock.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = stock.getAlmacen();

        int disponible = StockMathUtil.available(stock.getStockFisico(), stock.getStockReservado());

        return StockSnapshotPayload.builder()
                .idStock(stock.getIdStock())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .barcode(sku == null ? null : sku.getBarcode())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .stockFisico(StockMathUtil.zeroIfNull(stock.getStockFisico()))
                .stockReservado(StockMathUtil.zeroIfNull(stock.getStockReservado()))
                .stockDisponible(disponible)
                .stockMinimo(StockMathUtil.zeroIfNull(stock.getStockMinimo()))
                .stockMaximo(stock.getStockMaximo())
                .costoPromedioActual(stock.getCostoPromedioActual())
                .ultimoCostoCompra(stock.getUltimoCostoCompra())
                .bajoStock(StockMathUtil.isLowStock(disponible, stock.getStockMinimo()))
                .sobreStock(stock.getStockMaximo() != null && disponible > stock.getStockMaximo())
                .estado(stock.getEstado())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

    private MovimientoInventarioPayload toMovimientoPayload(MovimientoInventario movimiento) {
        ProductoSku sku = movimiento.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = movimiento.getAlmacen();
        ReservaStock reserva = movimiento.getReservaStock();

        return MovimientoInventarioPayload.builder()
                .idMovimiento(movimiento.getIdMovimiento())
                .codigoMovimiento(movimiento.getCodigoMovimiento())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .idCompraDetalle(movimiento.getCompraDetalle() == null
                        ? null
                        : movimiento.getCompraDetalle().getIdCompraDetalle())
                .idReservaStock(reserva == null ? null : reserva.getIdReservaStock())
                .codigoReserva(reserva == null ? null : reserva.getCodigoReserva())
                .tipoMovimiento(movimiento.getTipoMovimiento() == null
                        ? null
                        : movimiento.getTipoMovimiento().getCode())
                .motivoMovimiento(movimiento.getMotivoMovimiento() == null
                        ? null
                        : movimiento.getMotivoMovimiento().getCode())
                .cantidad(movimiento.getCantidad())
                .costoUnitario(movimiento.getCostoUnitario())
                .costoTotal(movimiento.getCostoTotal())
                .stockAnterior(movimiento.getStockAnterior())
                .stockNuevo(movimiento.getStockNuevo())
                .referenciaTipo(movimiento.getReferenciaTipo())
                .referenciaIdExterno(movimiento.getReferenciaIdExterno())
                .observacion(movimiento.getObservacion())
                .actorIdUsuarioMs1(movimiento.getActorIdUsuarioMs1())
                .actorIdEmpleadoMs2(movimiento.getActorIdEmpleadoMs2())
                .actorRol(movimiento.getActorRol() == null ? null : movimiento.getActorRol().getCode())
                .requestId(movimiento.getRequestId())
                .correlationId(movimiento.getCorrelationId())
                .estadoMovimiento(movimiento.getEstadoMovimiento() == null
                        ? null
                        : movimiento.getEstadoMovimiento().getCode())
                .estado(movimiento.getEstado())
                .createdAt(movimiento.getCreatedAt())
                .updatedAt(movimiento.getUpdatedAt())
                .build();
    }

    private void auditarReserva(
            TipoEventoAuditoria evento,
            ReservaStock reserva,
            String accion,
            String descripcion,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = reservaAuditMetadata(reserva);
        metadata.put("actor", actor == null ? "SYSTEM" : actor.actorLabel());

        if (actor != null) {
            metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());
            metadata.put("rol", actor.getRolPrincipal());
        }

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        auditoriaFuncionalService.registrarExito(
                evento,
                EntidadAuditada.RESERVA_STOCK,
                String.valueOf(reserva.getIdReservaStock()),
                accion,
                descripcion,
                metadata
        );
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU."
            );
        }

        if (reference.id() != null) {
            return productoSkuRepository.findByIdSkuAndEstadoTrue(reference.id())
                    .orElseThrow(this::skuNotFound);
        }

        String codigoSku = firstText(reference.codigoSku(), reference.codigo());
        if (StringNormalizer.hasText(codigoSku)) {
            return productoSkuRepository.findByCodigoSkuIgnoreCaseAndEstadoTrue(StringNormalizer.clean(codigoSku))
                    .orElseThrow(this::skuNotFound);
        }

        if (StringNormalizer.hasText(reference.barcode())) {
            return productoSkuRepository.findByBarcodeIgnoreCaseAndEstadoTrue(StringNormalizer.clean(reference.barcode()))
                    .orElseThrow(this::skuNotFound);
        }

        throw new ValidationException(
                "SKU_REFERENCIA_INVALIDA",
                "Debe indicar id, codigoSku, código o barcode del SKU."
        );
    }

    private Almacen resolveAlmacen(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ALMACEN_REFERENCIA_REQUERIDA",
                    "Debe indicar el almacén."
            );
        }

        if (reference.id() != null) {
            return almacenRepository.findByIdAlmacenAndEstadoTrue(reference.id())
                    .orElseThrow(this::almacenNotFound);
        }

        String codigoAlmacen = firstText(reference.codigoAlmacen(), reference.codigo());
        if (StringNormalizer.hasText(codigoAlmacen)) {
            return almacenRepository.findByCodigoIgnoreCaseAndEstadoTrue(StringNormalizer.clean(codigoAlmacen))
                    .orElseThrow(this::almacenNotFound);
        }

        if (StringNormalizer.hasText(reference.nombre())) {
            return almacenRepository.findByNombreIgnoreCaseAndEstadoTrue(StringNormalizer.clean(reference.nombre()))
                    .orElseThrow(this::almacenNotFound);
        }

        throw new ValidationException(
                "ALMACEN_REFERENCIA_INVALIDA",
                "Debe indicar id, codigoAlmacen, código o nombre del almacén."
        );
    }

    private StockSku resolveStockForUpdate(ProductoSku sku, Almacen almacen) {
        if (sku == null || sku.getIdSku() == null || almacen == null || almacen.getIdAlmacen() == null) {
            throw new ValidationException(
                    "STOCK_REFERENCIA_INVALIDA",
                    "Debe indicar SKU y almacén para bloquear el stock."
            );
        }

        return stockSkuRepository.findActivoBySkuAndAlmacenForUpdate(sku.getIdSku(), almacen.getIdAlmacen())
                .orElseThrow(() -> new NotFoundException(
                        "STOCK_SKU_NO_ENCONTRADO",
                        "No existe stock configurado para el SKU y almacén indicados."
                ));
    }

    private ReservaStockCreateRequestDto normalizeCreateRequest(ReservaStockCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "RESERVA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de la reserva de stock."
            );
        }

        return ReservaStockCreateRequestDto.builder()
                .sku(request.sku())
                .almacen(request.almacen())
                .referenciaTipo(request.referenciaTipo())
                .referenciaIdExterno(cleanRequiredLength(request.referenciaIdExterno(), MAX_REFERENCIA_LENGTH))
                .cantidad(request.cantidad())
                .expiresAt(request.expiresAt())
                .motivo(cleanRequiredLength(request.motivo(), MAX_MOTIVO_LENGTH))
                .build();
    }

    private ReservaStockMs4RequestDto normalizeMs4Request(ReservaStockMs4RequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "RESERVA_MS4_REQUEST_REQUERIDA",
                    "Debe enviar los datos de reserva provenientes de MS4."
            );
        }

        String motivo = StringNormalizer.cleanOrNull(request.motivo());
        if (!StringNormalizer.hasText(motivo)) {
            motivo = "Reserva solicitada por MS4.";
        }

        return ReservaStockMs4RequestDto.builder()
                .eventId(cleanRequiredLength(request.eventId(), MAX_TRACE_LENGTH))
                .idempotencyKey(cleanRequiredLength(request.idempotencyKey(), MAX_IDEMPOTENCY_LENGTH))
                .sku(request.sku())
                .almacen(request.almacen())
                .referenciaTipo(request.referenciaTipo())
                .referenciaIdExterno(cleanRequiredLength(request.referenciaIdExterno(), MAX_REFERENCIA_LENGTH))
                .cantidad(request.cantidad())
                .actorIdUsuarioMs1(request.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(request.actorIdEmpleadoMs2())
                .actorRol(request.actorRol())
                .occurredAt(request.occurredAt())
                .expiresAt(request.expiresAt())
                .motivo(cleanRequiredLength(motivo, MAX_MOTIVO_LENGTH))
                .requestId(StringNormalizer.truncateOrNull(request.requestId(), MAX_TRACE_LENGTH))
                .correlationId(StringNormalizer.truncateOrNull(request.correlationId(), MAX_TRACE_LENGTH))
                .metadataJson(request.metadataJson())
                .build();
    }

    private ReservaStockConfirmRequestDto normalizeConfirmRequest(ReservaStockConfirmRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CONFIRMACION_RESERVA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de confirmación de la reserva."
            );
        }

        return ReservaStockConfirmRequestDto.builder()
                .motivo(cleanRequiredLength(request.motivo(), MAX_MOTIVO_LENGTH))
                .build();
    }

    private ReservaStockLiberarRequestDto normalizeLiberarRequest(ReservaStockLiberarRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "LIBERACION_RESERVA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de liberación de la reserva."
            );
        }

        return ReservaStockLiberarRequestDto.builder()
                .motivo(cleanRequiredLength(request.motivo(), MAX_MOTIVO_LENGTH))
                .build();
    }

    private ReservaStockFilterDto normalizeFilter(ReservaStockFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return ReservaStockFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .codigoReserva(StringNormalizer.truncateOrNull(filter.codigoReserva(), MAX_CODIGO_RESERVA_LENGTH))
                .idSku(filter.idSku())
                .codigoSku(StringNormalizer.truncateOrNull(filter.codigoSku(), MAX_CODIGO_SKU_LENGTH))
                .barcode(StringNormalizer.truncateOrNull(filter.barcode(), MAX_BARCODE_LENGTH))
                .idProducto(filter.idProducto())
                .codigoProducto(StringNormalizer.truncateOrNull(filter.codigoProducto(), MAX_CODIGO_PRODUCTO_LENGTH))
                .nombreProducto(StringNormalizer.truncateOrNull(filter.nombreProducto(), MAX_NOMBRE_PRODUCTO_LENGTH))
                .idAlmacen(filter.idAlmacen())
                .codigoAlmacen(StringNormalizer.truncateOrNull(filter.codigoAlmacen(), MAX_CODIGO_ALMACEN_LENGTH))
                .nombreAlmacen(StringNormalizer.truncateOrNull(filter.nombreAlmacen(), MAX_NOMBRE_ALMACEN_LENGTH))
                .referenciaTipo(filter.referenciaTipo())
                .referenciaIdExterno(StringNormalizer.truncateOrNull(filter.referenciaIdExterno(), MAX_REFERENCIA_LENGTH))
                .estadoReserva(filter.estadoReserva())
                .expirada(filter.expirada())
                .expiradas(filter.expiradas())
                .reservadoPorIdUsuarioMs1(filter.reservadoPorIdUsuarioMs1())
                .confirmadoPorIdUsuarioMs1(filter.confirmadoPorIdUsuarioMs1())
                .liberadoPorIdUsuarioMs1(filter.liberadoPorIdUsuarioMs1())
                .estado(filter.estado())
                .incluirTodosLosEstados(Boolean.TRUE.equals(filter.incluirTodosLosEstados()))
                .fechaReserva(filter.fechaReserva())
                .fechaConfirmacion(filter.fechaConfirmacion())
                .fechaLiberacion(filter.fechaLiberacion())
                .fechaExpiracion(filter.fechaExpiracion())
                .fechaCreacion(filter.fechaCreacion())
                .fechaActualizacion(filter.fechaActualizacion())
                .build();
    }

    private boolean existsActiveReservation(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    ) {
        if (referenciaTipo == null || !StringNormalizer.hasText(referenciaIdExterno) || idSku == null || idAlmacen == null) {
            return false;
        }

        return reservaStockRepository
                .existsByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        referenciaTipo,
                        StringNormalizer.clean(referenciaIdExterno),
                        idSku,
                        idAlmacen
                );
    }

    private boolean employeeCanRegisterOutput(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeRegistrarSalida(actor.getIdUsuarioMs1());
    }

    private Long resolveActorId(Long externalActorId, AuthenticatedUserContext actor) {
        if (externalActorId != null) {
            return externalActorId;
        }

        if (actor != null && actor.getIdUsuarioMs1() != null) {
            return actor.getIdUsuarioMs1();
        }

        return SYSTEM_USER_ID;
    }

    private RolSistema resolveRol(AuthenticatedUserContext actor) {
        if (actor == null || !StringNormalizer.hasText(actor.getRolPrincipal())) {
            return RolSistema.SISTEMA;
        }

        return RolSistema.fromCode(actor.getRolPrincipal());
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

    private String firstTrace(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private String traceValue(String value) {
        if (StringNormalizer.hasText(value)) {
            return StringNormalizer.truncate(value, MAX_TRACE_LENGTH);
        }

        AuditContext context = AuditContextHolder.getOrEmpty();

        if (StringNormalizer.hasText(context.requestId())) {
            return StringNormalizer.truncate(context.requestId(), MAX_TRACE_LENGTH);
        }

        if (StringNormalizer.hasText(context.correlationId())) {
            return StringNormalizer.truncate(context.correlationId(), MAX_TRACE_LENGTH);
        }

        return UUID.randomUUID().toString();
    }

    private String cleanRequiredLength(String value, int maxLength) {
        String cleaned = StringNormalizer.cleanOrNull(value);

        if (!StringNormalizer.hasText(cleaned)) {
            return null;
        }

        if (cleaned.length() > maxLength) {
            throw new ValidationException(
                    "TEXTO_SUPERA_LONGITUD",
                    "El texto enviado supera la longitud máxima permitida."
            );
        }

        return cleaned;
    }

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private Map<String, Object> reservaAuditMetadata(ReservaStock reserva) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        ProductoSku sku = reserva.getSku();
        Almacen almacen = reserva.getAlmacen();

        metadata.put("idReservaStock", reserva.getIdReservaStock());
        metadata.put("codigoReserva", reserva.getCodigoReserva());
        metadata.put("idSku", sku == null ? null : sku.getIdSku());
        metadata.put("codigoSku", sku == null ? null : sku.getCodigoSku());
        metadata.put("idAlmacen", almacen == null ? null : almacen.getIdAlmacen());
        metadata.put("codigoAlmacen", almacen == null ? null : almacen.getCodigo());
        metadata.put("referenciaTipo", reserva.getReferenciaTipo() == null ? null : reserva.getReferenciaTipo().getCode());
        metadata.put("referenciaIdExterno", reserva.getReferenciaIdExterno());
        metadata.put("estadoReserva", reserva.getEstadoReserva() == null ? null : reserva.getEstadoReserva().getCode());
        metadata.put("cantidad", reserva.getCantidad());
        metadata.put("expiresAt", reserva.getExpiresAt());
        metadata.put("estado", reserva.getEstado());

        return metadata;
    }

    private Map<String, Object> movimientoAuditMetadata(
            MovimientoInventario movimiento,
            ReservaStock reserva,
            StockSku stock
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("idMovimiento", movimiento.getIdMovimiento());
        metadata.put("codigoMovimiento", movimiento.getCodigoMovimiento());
        metadata.put("idReservaStock", reserva.getIdReservaStock());
        metadata.put("codigoReserva", reserva.getCodigoReserva());
        metadata.put("idStock", stock.getIdStock());
        metadata.put("tipoMovimiento", movimiento.getTipoMovimiento() == null
                ? null
                : movimiento.getTipoMovimiento().getCode());
        metadata.put("motivoMovimiento", movimiento.getMotivoMovimiento() == null
                ? null
                : movimiento.getMotivoMovimiento().getCode());
        metadata.put("referenciaTipo", movimiento.getReferenciaTipo());
        metadata.put("referenciaIdExterno", movimiento.getReferenciaIdExterno());
        metadata.put("requestId", movimiento.getRequestId());
        metadata.put("correlationId", movimiento.getCorrelationId());

        return metadata;
    }

    private Object safeMetadataValue(Object value) {
        return value == null ? "" : value;
    }

    private NotFoundException reservaNotFound() {
        return new NotFoundException(
                "RESERVA_STOCK_NO_ENCONTRADA",
                "No se encontró el registro solicitado."
        );
    }

    private NotFoundException skuNotFound() {
        return new NotFoundException(
                "SKU_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }

    private NotFoundException almacenNotFound() {
        return new NotFoundException(
                "ALMACEN_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }
}