// ruta: src/main/java/com/upsjb/ms3/service/impl/CompraInventarioServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.config.KafkaTopicProperties;
import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.entity.CompraInventarioDetalle;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.compra.filter.CompraInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioAnularRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioCreateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioDetalleRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetailResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetalleResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.MovimientoInventarioPayload;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import com.upsjb.ms3.mapper.CompraInventarioMapper;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.mapper.MovimientoInventarioMapper;
import com.upsjb.ms3.policy.CompraInventarioPolicy;
import com.upsjb.ms3.repository.CompraInventarioDetalleRepository;
import com.upsjb.ms3.repository.CompraInventarioRepository;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import com.upsjb.ms3.repository.EmpleadoInventarioPermisoHistorialRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.CompraInventarioService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AlmacenReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.reference.ProveedorReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.CompraInventarioSpecifications;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CompraInventarioValidator;
import com.upsjb.ms3.validator.MovimientoInventarioValidator;
import com.upsjb.ms3.validator.StockValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
public class CompraInventarioServiceImpl implements CompraInventarioService {

    private static final String REFERENCIA_TIPO_COMPRA = "COMPRA_INVENTARIO";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idCompra",
            "codigoCompra",
            "fechaCompra",
            "moneda",
            "estadoCompra",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final CompraInventarioRepository compraRepository;
    private final CompraInventarioDetalleRepository detalleRepository;
    private final StockSkuRepository stockSkuRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final EventoDominioOutboxRepository outboxRepository;
    private final EmpleadoInventarioPermisoHistorialRepository permisoRepository;
    private final ProveedorReferenceResolver proveedorReferenceResolver;
    private final ProductoSkuReferenceResolver skuReferenceResolver;
    private final AlmacenReferenceResolver almacenReferenceResolver;
    private final CompraInventarioMapper compraMapper;
    private final MovimientoInventarioMapper movimientoMapper;
    private final EventoDominioOutboxMapper outboxMapper;
    private final CompraInventarioValidator compraValidator;
    private final StockValidator stockValidator;
    private final MovimientoInventarioValidator movimientoValidator;
    private final CompraInventarioPolicy compraPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final CodigoGeneradorService codigoGeneradorService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponseDto<CompraInventarioDetailResponseDto> crear(CompraInventarioCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanCreateDraft(actor, employeeCanRegisterEntry(actor));

        Proveedor proveedor = resolveProveedor(request == null ? null : request.proveedor());
        List<CompraInventarioDetalleRequestDto> detalleRequests = safeDetalles(request == null ? null : request.detalles());

        compraValidator.validateCreate(
                proveedor,
                request == null || request.fechaCompra() == null ? LocalDateTime.now() : request.fechaCompra(),
                request == null ? null : request.moneda(),
                detalleRequests.size(),
                actor.getIdUsuarioMs1()
        );

        CompraInventario compra = compraMapper.toEntity(
                request,
                proveedor,
                codigoGeneradorService.generarCodigoCompra(),
                actor.getIdUsuarioMs1()
        );

        CompraInventario savedCompra = compraRepository.saveAndFlush(compra);
        List<CompraInventarioDetalle> detalles = buildDetalles(savedCompra, detalleRequests);
        detalleRepository.saveAllAndFlush(detalles);
        applyTotals(savedCompra, detalles);

        CompraInventario updated = compraRepository.saveAndFlush(savedCompra);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.COMPRA_REGISTRADA,
                EntidadAuditada.COMPRA_INVENTARIO,
                String.valueOf(updated.getIdCompra()),
                "CREAR_COMPRA_INVENTARIO",
                "Compra registrada correctamente.",
                metadataCompra(updated)
        );

        return apiResponseFactory.dtoCreated(
                "Compra registrada correctamente.",
                toDetail(updated)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CompraInventarioDetailResponseDto> actualizar(
            Long idCompra,
            CompraInventarioUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanUpdateDraft(actor, employeeCanRegisterEntry(actor));

        CompraInventario compra = compraRepository.findActivoByIdForUpdate(idCompra)
                .orElseThrow(() -> new NotFoundException(
                        "COMPRA_INVENTARIO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        ensureBorrador(compra);

        Proveedor proveedor = resolveProveedor(request == null ? null : request.proveedor());
        List<CompraInventarioDetalleRequestDto> detalleRequests = safeDetalles(request == null ? null : request.detalles());

        compraValidator.validateCreate(
                proveedor,
                request == null || request.fechaCompra() == null ? compra.getFechaCompra() : request.fechaCompra(),
                request == null ? null : request.moneda(),
                detalleRequests.size(),
                compra.getCreadoPorIdUsuarioMs1()
        );

        compraMapper.updateEntity(compra, request, proveedor);

        List<CompraInventarioDetalle> currentDetalles =
                detalleRepository.findByCompra_IdCompraAndEstadoTrueOrderByIdCompraDetalleAsc(compra.getIdCompra());
        currentDetalles.forEach(CompraInventarioDetalle::inactivar);
        detalleRepository.saveAllAndFlush(currentDetalles);

        List<CompraInventarioDetalle> nuevosDetalles = buildDetalles(compra, detalleRequests);
        detalleRepository.saveAllAndFlush(nuevosDetalles);
        applyTotals(compra, nuevosDetalles);

        CompraInventario updated = compraRepository.saveAndFlush(compra);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.COMPRA_REGISTRADA,
                EntidadAuditada.COMPRA_INVENTARIO,
                String.valueOf(updated.getIdCompra()),
                "ACTUALIZAR_COMPRA_INVENTARIO",
                "Compra actualizada correctamente.",
                metadataCompra(updated)
        );

        return apiResponseFactory.dtoOk(
                "Compra actualizada correctamente.",
                toDetail(updated)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CompraInventarioDetailResponseDto> confirmar(
            Long idCompra,
            CompraInventarioConfirmRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanConfirm(actor, employeeCanRegisterEntry(actor));

        CompraInventario compra = compraRepository.findActivoByIdForUpdate(idCompra)
                .orElseThrow(() -> new NotFoundException(
                        "COMPRA_INVENTARIO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        List<CompraInventarioDetalle> detalles =
                detalleRepository.findByCompra_IdCompraAndEstadoTrueOrderByIdCompraDetalleAsc(compra.getIdCompra());

        compraValidator.validateCanConfirm(compra, !detalles.isEmpty());

        AuditContext auditContext = AuditContextHolder.getOrEmpty();
        String requestId = traceValue(auditContext.requestId());
        String correlationId = traceValue(auditContext.correlationId());
        RolSistema actorRol = resolveRol(actor);

        for (CompraInventarioDetalle detalle : detalles) {
            registrarEntradaPorCompra(
                    compra,
                    detalle,
                    request == null ? null : request.motivo(),
                    actor,
                    actorRol,
                    requestId,
                    correlationId
            );
        }

        compraMapper.markConfirmada(compra, actor.getIdUsuarioMs1(), LocalDateTime.now());
        CompraInventario confirmed = compraRepository.saveAndFlush(compra);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.COMPRA_CONFIRMADA,
                EntidadAuditada.COMPRA_INVENTARIO,
                String.valueOf(confirmed.getIdCompra()),
                "CONFIRMAR_COMPRA_INVENTARIO",
                "Compra confirmada correctamente.",
                metadataCompra(confirmed)
        );

        return apiResponseFactory.dtoOk(
                "Compra confirmada correctamente.",
                toDetail(confirmed)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CompraInventarioResponseDto> anular(
            Long idCompra,
            CompraInventarioAnularRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanAnnul(actor);

        CompraInventario compra = compraRepository.findActivoByIdForUpdate(idCompra)
                .orElseThrow(() -> new NotFoundException(
                        "COMPRA_INVENTARIO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        compraValidator.validateCanAnular(compra, request == null ? null : request.motivo());
        compraMapper.markAnulada(compra);

        CompraInventario saved = compraRepository.saveAndFlush(compra);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.COMPRA_ANULADA,
                EntidadAuditada.COMPRA_INVENTARIO,
                String.valueOf(saved.getIdCompra()),
                "ANULAR_COMPRA_INVENTARIO",
                "Compra anulada correctamente.",
                metadataCompra(saved)
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                compraMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CompraInventarioResponseDto> obtenerPorId(Long idCompra) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanViewAdmin(actor);

        CompraInventario compra = compraRepository.findByIdCompraAndEstadoTrue(idCompra)
                .orElseThrow(() -> new NotFoundException(
                        "COMPRA_INVENTARIO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                compraMapper.toResponse(compra)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CompraInventarioDetailResponseDto> obtenerDetalle(Long idCompra) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanViewAdmin(actor);

        CompraInventario compra = compraRepository.findByIdCompraAndEstadoTrue(idCompra)
                .orElseThrow(() -> new NotFoundException(
                        "COMPRA_INVENTARIO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toDetail(compra)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<CompraInventarioResponseDto>> listar(
            CompraInventarioFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        compraPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "fechaCompra");
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "fechaCompra"
        );

        PageResponseDto<CompraInventarioResponseDto> response = paginationService.toPageResponseDto(
                compraRepository.findAll(CompraInventarioSpecifications.fromFilter(filter), pageable),
                compraMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private void registrarEntradaPorCompra(
            CompraInventario compra,
            CompraInventarioDetalle detalle,
            String motivoConfirmacion,
            AuthenticatedUserContext actor,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        ProductoSku sku = detalle.getSku();
        Almacen almacen = detalle.getAlmacen();

        StockSku stock = stockSkuRepository
                .findActivoBySkuAndAlmacenForUpdate(sku.getIdSku(), almacen.getIdAlmacen())
                .orElseGet(() -> createInitialStock(sku, almacen));

        stockValidator.validateEntry(detalle.getCantidad(), detalle.getCostoUnitario());

        int stockAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int stockNuevo = StockMathUtil.applyEntry(stock.getStockFisico(), detalle.getCantidad());

        BigDecimal costoPromedio = StockMathUtil.weightedAverageCost(
                stock.getStockFisico(),
                stock.getCostoPromedioActual(),
                detalle.getCantidad(),
                detalle.getCostoUnitario()
        );

        stock.setStockFisico(stockNuevo);
        stock.setCostoPromedioActual(costoPromedio);
        stock.setUltimoCostoCompra(detalle.getCostoUnitario());

        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        MovimientoInventario movimiento = movimientoMapper.toEntity(
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                sku,
                almacen,
                detalle,
                null,
                TipoMovimientoInventario.ENTRADA_COMPRA,
                MotivoMovimientoInventario.COMPRA,
                detalle.getCantidad(),
                detalle.getCostoUnitario(),
                detalle.getCostoTotal(),
                stockAnterior,
                stockNuevo,
                REFERENCIA_TIPO_COMPRA,
                compra.getCodigoCompra(),
                safeObservation(motivoConfirmacion, compra.getObservacion()),
                actor.getIdUsuarioMs1(),
                actor.getIdEmpleadoMs2(),
                actorRol,
                requestId,
                correlationId
        );

        movimientoValidator.validateCreate(
                sku,
                almacen,
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

        MovimientoInventario savedMovimiento = movimientoRepository.saveAndFlush(movimiento);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ENTRADA_INVENTARIO_REGISTRADA,
                EntidadAuditada.MOVIMIENTO_INVENTARIO,
                String.valueOf(savedMovimiento.getIdMovimiento()),
                "ENTRADA_COMPRA_CONFIRMADA",
                "Entrada de inventario registrada por confirmación de compra.",
                Map.of(
                        "idCompra", compra.getIdCompra(),
                        "codigoCompra", compra.getCodigoCompra(),
                        "idSku", sku.getIdSku(),
                        "codigoSku", sku.getCodigoSku(),
                        "idAlmacen", almacen.getIdAlmacen(),
                        "codigoAlmacen", almacen.getCodigo(),
                        "cantidad", detalle.getCantidad()
                )
        );

        registerStockOutbox(savedStock, StockEventType.STOCK_SNAPSHOT_ACTUALIZADO, requestId, correlationId);
        registerMovimientoOutbox(savedMovimiento, requestId, correlationId);
    }

    private StockSku createInitialStock(ProductoSku sku, Almacen almacen) {
        StockSku stock = new StockSku();
        stock.setSku(sku);
        stock.setAlmacen(almacen);
        stock.setStockFisico(0);
        stock.setStockReservado(0);
        stock.setStockMinimo(sku.getStockMinimo() == null ? 0 : sku.getStockMinimo());
        stock.setStockMaximo(sku.getStockMaximo());
        stock.activar();

        stockValidator.validateCreate(sku, almacen, stock.getStockFisico(), stock.getStockReservado());

        return stock;
    }

    private List<CompraInventarioDetalle> buildDetalles(
            CompraInventario compra,
            List<CompraInventarioDetalleRequestDto> detalleRequests
    ) {
        List<CompraInventarioDetalle> detalles = new ArrayList<>();
        Set<String> uniquePairs = new LinkedHashSet<>();

        for (CompraInventarioDetalleRequestDto detalleRequest : detalleRequests) {
            ProductoSku sku = resolveSku(detalleRequest.sku());
            Almacen almacen = resolveAlmacen(detalleRequest.almacen());

            compraValidator.validateDetail(
                    sku,
                    almacen,
                    detalleRequest.cantidad(),
                    detalleRequest.costoUnitario(),
                    detalleRequest.descuento(),
                    detalleRequest.impuesto()
            );

            String key = sku.getIdSku() + ":" + almacen.getIdAlmacen();
            if (!uniquePairs.add(key)) {
                throw new ConflictException(
                        "COMPRA_DETALLE_DUPLICADO",
                        "Ya existe un detalle para el mismo SKU y almacén dentro de la compra."
                );
            }

            CompraInventarioDetalle detalle = compraMapper.toDetalleEntity(
                    compra,
                    detalleRequest,
                    sku,
                    almacen
            );

            if (detalle.getCostoTotal().compareTo(BigDecimal.ZERO) < 0) {
                throw new ConflictException(
                        "COMPRA_DETALLE_TOTAL_NEGATIVO",
                        "El total del detalle de compra no puede ser negativo."
                );
            }

            detalles.add(detalle);
        }

        return detalles;
    }

    private void applyTotals(
            CompraInventario compra,
            List<CompraInventarioDetalle> detalles
    ) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        BigDecimal impuestoTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CompraInventarioDetalle detalle : detalles) {
            BigDecimal lineSubtotal = detalle.getCostoUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            subtotal = subtotal.add(lineSubtotal);
            descuentoTotal = descuentoTotal.add(defaultAmount(detalle.getDescuento()));
            impuestoTotal = impuestoTotal.add(defaultAmount(detalle.getImpuesto()));
            total = total.add(defaultAmount(detalle.getCostoTotal()));
        }

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException(
                    "COMPRA_TOTAL_NEGATIVO",
                    "El total de la compra no puede ser negativo."
            );
        }

        compraMapper.applyTotals(compra, subtotal, descuentoTotal, impuestoTotal, total);
    }

    private CompraInventarioDetailResponseDto toDetail(CompraInventario compra) {
        List<CompraInventarioDetalleResponseDto> detalles = detalleRepository
                .findByCompra_IdCompraAndEstadoTrueOrderByIdCompraDetalleAsc(compra.getIdCompra())
                .stream()
                .map(detalle -> compraMapper.toDetalleResponse(detalle, compra.getMoneda()))
                .toList();

        return compraMapper.toDetailResponse(compra, detalles);
    }

    private Proveedor resolveProveedor(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "PROVEEDOR_REFERENCIA_OBLIGATORIA",
                    "Debe indicar el proveedor de la compra."
            );
        }

        return proveedorReferenceResolver.resolve(
                reference.id(),
                reference.ruc(),
                reference.numeroDocumento(),
                firstText(reference.nombre(), reference.codigoProveedor())
        );
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_OBLIGATORIA",
                    "Debe indicar el SKU del detalle de compra."
            );
        }

        ProductoSku sku = skuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );

        if (sku.getEstadoSku() == null || !sku.getEstadoSku().isActivo()) {
            throw new ConflictException(
                    "SKU_NO_OPERATIVO",
                    "No se puede usar un SKU inactivo o descontinuado."
            );
        }

        return sku;
    }

    private Almacen resolveAlmacen(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ALMACEN_REFERENCIA_OBLIGATORIA",
                    "Debe indicar el almacén del detalle de compra."
            );
        }

        return almacenReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoAlmacen(), reference.codigo()),
                reference.nombre()
        );
    }

    private void registerStockOutbox(
            StockSku stock,
            StockEventType eventType,
            String requestId,
            String correlationId
    ) {
        StockSnapshotEvent event = StockSnapshotEvent.of(
                eventType,
                stock.getIdStock(),
                requestId,
                correlationId,
                toStockPayload(stock),
                Map.of("source", "CompraInventarioService")
        );

        saveOutbox(
                AggregateType.STOCK,
                event.aggregateId(),
                event.eventType(),
                kafkaTopicProperties.resolveStockSnapshotTopic(),
                eventKey(stock),
                event
        );
    }

    private void registerMovimientoOutbox(
            MovimientoInventario movimiento,
            String requestId,
            String correlationId
    ) {
        MovimientoInventarioEvent event = MovimientoInventarioEvent.of(
                StockEventType.MOVIMIENTO_INVENTARIO_REGISTRADO,
                movimiento.getIdMovimiento(),
                requestId,
                correlationId,
                toMovimientoPayload(movimiento),
                Map.of("source", "CompraInventarioService")
        );

        saveOutbox(
                AggregateType.MOVIMIENTO_INVENTARIO,
                event.aggregateId(),
                event.eventType(),
                kafkaTopicProperties.resolveMovimientoInventarioTopic(),
                movimiento.getCodigoMovimiento(),
                event
        );
    }

    private void saveOutbox(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            Object event
    ) {
        EventoDominioOutbox outbox = outboxMapper.toEntity(
                aggregateType,
                aggregateId,
                eventType,
                topic,
                eventKey,
                toJson(event)
        );

        outboxRepository.save(outbox);
    }

    private StockSnapshotPayload toStockPayload(StockSku stock) {
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
                .stockFisico(stock.getStockFisico())
                .stockReservado(stock.getStockReservado())
                .stockDisponible(disponible)
                .stockMinimo(stock.getStockMinimo())
                .stockMaximo(stock.getStockMaximo())
                .costoPromedioActual(stock.getCostoPromedioActual())
                .ultimoCostoCompra(stock.getUltimoCostoCompra())
                .bajoStock(StockMathUtil.isLowStock(disponible, stock.getStockMinimo()))
                .sobreStock(stock.getStockMaximo() != null && stock.getStockFisico() != null && stock.getStockFisico() > stock.getStockMaximo())
                .estado(stock.getEstado())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

    private MovimientoInventarioPayload toMovimientoPayload(MovimientoInventario movimiento) {
        ProductoSku sku = movimiento.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = movimiento.getAlmacen();

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
                .idCompraDetalle(movimiento.getCompraDetalle() == null ? null : movimiento.getCompraDetalle().getIdCompraDetalle())
                .idReservaStock(null)
                .codigoReserva(null)
                .tipoMovimiento(movimiento.getTipoMovimiento() == null ? null : movimiento.getTipoMovimiento().getCode())
                .motivoMovimiento(movimiento.getMotivoMovimiento() == null ? null : movimiento.getMotivoMovimiento().getCode())
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
                .estadoMovimiento(movimiento.getEstadoMovimiento() == null ? null : movimiento.getEstadoMovimiento().getCode())
                .estado(movimiento.getEstado())
                .createdAt(movimiento.getCreatedAt())
                .updatedAt(movimiento.getUpdatedAt())
                .build();
    }

    private boolean employeeCanRegisterEntry(AuthenticatedUserContext actor) {
        if (actor == null || actor.isAdmin()) {
            return true;
        }

        if (actor.getIdUsuarioMs1() == null) {
            return false;
        }

        return permisoRepository.existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeRegistrarEntradaTrue(
                actor.getIdUsuarioMs1()
        );
    }

    private void ensureBorrador(CompraInventario compra) {
        if (compra.getEstadoCompra() != EstadoCompraInventario.BORRADOR) {
            throw new ConflictException(
                    "COMPRA_NO_EDITABLE",
                    "No se puede actualizar porque el estado actual no lo permite."
            );
        }
    }

    private List<CompraInventarioDetalleRequestDto> safeDetalles(List<CompraInventarioDetalleRequestDto> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new ConflictException(
                    "COMPRA_SIN_DETALLE",
                    "No se puede confirmar la compra porque no tiene detalles."
            );
        }

        return detalles;
    }

    private Map<String, Object> metadataCompra(CompraInventario compra) {
        return Map.of(
                "idCompra", compra.getIdCompra(),
                "codigoCompra", compra.getCodigoCompra(),
                "estadoCompra", compra.getEstadoCompra() == null ? null : compra.getEstadoCompra().getCode(),
                "idProveedor", compra.getProveedor() == null ? null : compra.getProveedor().getIdProveedor(),
                "total", compra.getTotal()
        );
    }

    private String eventKey(StockSku stock) {
        String sku = stock.getSku() == null ? "SKU" : stock.getSku().getCodigoSku();
        String almacen = stock.getAlmacen() == null ? "ALMACEN" : stock.getAlmacen().getCodigo();

        return sku + ":" + almacen;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el evento Outbox.", ex);
        }
    }

    private String traceValue(String value) {
        if (StringNormalizer.hasText(value)) {
            return value;
        }

        return UUID.randomUUID().toString();
    }

    private RolSistema resolveRol(AuthenticatedUserContext actor) {
        if (actor == null || !StringNormalizer.hasText(actor.getRolPrincipal())) {
            return RolSistema.SISTEMA;
        }

        return RolSistema.fromCode(actor.getRolPrincipal());
    }

    private String safeObservation(String motivoConfirmacion, String observacionCompra) {
        if (StringNormalizer.hasText(motivoConfirmacion)) {
            return StringNormalizer.truncateOrNull(motivoConfirmacion, 500);
        }

        return StringNormalizer.truncateOrNull(observacionCompra, 500);
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

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}