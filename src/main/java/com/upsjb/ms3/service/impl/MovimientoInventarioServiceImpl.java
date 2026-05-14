// ruta: src/main/java/com/upsjb/ms3/service/impl/MovimientoInventarioServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.filter.MovimientoInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.MovimientoCompensatorioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.SalidaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.MovimientoInventarioPayload;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import com.upsjb.ms3.mapper.MovimientoInventarioMapper;
import com.upsjb.ms3.policy.MovimientoInventarioPolicy;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.MovimientoInventarioService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AlmacenReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.MovimientoInventarioSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.MovimientoInventarioValidator;
import com.upsjb.ms3.validator.StockValidator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
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
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private static final String REFERENCIA_TIPO_COMPENSACION = "MOVIMIENTO_COMPENSATORIO";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idMovimiento",
            "codigoMovimiento",
            "sku.codigoSku",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "almacen.codigo",
            "almacen.nombre",
            "tipoMovimiento",
            "motivoMovimiento",
            "cantidad",
            "stockAnterior",
            "stockNuevo",
            "referenciaTipo",
            "referenciaIdExterno",
            "actorIdUsuarioMs1",
            "actorIdEmpleadoMs2",
            "actorRol",
            "requestId",
            "correlationId",
            "estadoMovimiento",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockSkuRepository stockSkuRepository;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final AlmacenReferenceResolver almacenReferenceResolver;
    private final MovimientoInventarioMapper movimientoMapper;
    private final MovimientoInventarioValidator movimientoValidator;
    private final StockValidator stockValidator;
    private final MovimientoInventarioPolicy movimientoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final CodigoGeneradorService codigoGeneradorService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioResponseDto> registrarEntrada(EntradaInventarioRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanRegisterEntry(actor, employeeCanRegisterEntry(actor));

        EntradaInventarioRequestDto normalized = normalizeEntrada(request);
        ProductoSku sku = resolveSku(normalized.sku());
        Almacen almacen = resolveAlmacen(normalized.almacen());
        validateAlmacenCompra(almacen);

        StockSku stock = resolveOrCreateStockForEntry(sku, almacen);

        stockValidator.validateEntry(normalized.cantidad(), normalized.costoUnitario());

        int stockAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int stockNuevo = StockMathUtil.applyEntry(stock.getStockFisico(), normalized.cantidad());

        stock.setStockFisico(stockNuevo);
        applyCostOnEntry(stock, normalized.cantidad(), normalized.costoUnitario());

        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);
        MovimientoInventario savedMovimiento = saveEntradaMovimiento(
                normalized,
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor
        );

        auditMovimiento(
                TipoEventoAuditoria.ENTRADA_INVENTARIO_REGISTRADA,
                savedMovimiento,
                "REGISTRAR_ENTRADA_INVENTARIO",
                "Movimiento de inventario registrado correctamente."
        );

        registerStockOutbox(savedStock, StockEventType.STOCK_SNAPSHOT_ACTUALIZADO);
        registerMovimientoOutbox(savedMovimiento);

        return apiResponseFactory.dtoCreated(
                "Movimiento de inventario registrado correctamente.",
                movimientoMapper.toResponse(savedMovimiento, Moneda.PEN, canViewCosts(actor))
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioResponseDto> registrarSalida(SalidaInventarioRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanRegisterOutput(actor, employeeCanRegisterOutput(actor));

        SalidaInventarioRequestDto normalized = normalizeSalida(request);
        ProductoSku sku = resolveSku(normalized.sku());
        Almacen almacen = resolveAlmacen(normalized.almacen());
        validateAlmacenVenta(almacen);

        StockSku stock = resolveStockForUpdate(sku, almacen);
        stockValidator.validateOutput(stock, normalized.cantidad());

        int stockAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int stockNuevo = StockMathUtil.applyOutput(stock.getStockFisico(), normalized.cantidad());

        stock.setStockFisico(stockNuevo);

        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);
        MovimientoInventario savedMovimiento = saveSalidaMovimiento(
                normalized,
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor
        );

        auditMovimiento(
                TipoEventoAuditoria.SALIDA_INVENTARIO_REGISTRADA,
                savedMovimiento,
                "REGISTRAR_SALIDA_INVENTARIO",
                "Movimiento de inventario registrado correctamente."
        );

        registerStockOutbox(savedStock, StockEventType.STOCK_SNAPSHOT_ACTUALIZADO);
        registerMovimientoOutbox(savedMovimiento);

        return apiResponseFactory.dtoCreated(
                "Movimiento de inventario registrado correctamente.",
                movimientoMapper.toResponse(savedMovimiento, Moneda.PEN, canViewCosts(actor))
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioResponseDto> registrarAjuste(AjusteInventarioRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanRegisterAdjustment(actor, employeeCanRegisterAdjustment(actor));

        AjusteInventarioRequestDto normalized = normalizeAjuste(request);
        ProductoSku sku = resolveSku(normalized.sku());
        Almacen almacen = resolveAlmacen(normalized.almacen());
        StockSku stock = resolveStockForUpdate(sku, almacen);

        int stockAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int stockNuevo = resolveStockNuevoAjuste(stock, normalized);

        if (normalized.stockFisicoEsperado() != null && !normalized.stockFisicoEsperado().equals(stockNuevo)) {
            throw new ConflictException(
                    "AJUSTE_STOCK_ESPERADO_INVALIDO",
                    "El stock físico esperado no coincide con el resultado del ajuste."
            );
        }

        stock.setStockFisico(stockNuevo);

        if (normalized.tipoMovimiento().isEntradaFisica()) {
            applyCostOnEntry(stock, normalized.cantidad(), normalized.costoUnitario());
        }

        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);
        MovimientoInventario savedMovimiento = saveAjusteMovimiento(
                normalized,
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor
        );

        auditMovimiento(
                TipoEventoAuditoria.AJUSTE_STOCK_REGISTRADO,
                savedMovimiento,
                "REGISTRAR_AJUSTE_STOCK",
                "Movimiento de inventario registrado correctamente."
        );

        registerStockOutbox(savedStock, StockEventType.STOCK_AJUSTADO);
        registerMovimientoOutbox(savedMovimiento);

        return apiResponseFactory.dtoCreated(
                "Movimiento de inventario registrado correctamente.",
                movimientoMapper.toResponse(savedMovimiento, Moneda.PEN, canViewCosts(actor))
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioResponseDto> registrarCompensacion(MovimientoCompensatorioRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanRegisterCompensatoryMovement(actor);

        if (request == null || request.idMovimientoOriginal() == null) {
            throw new ValidationException(
                    "MOVIMIENTO_ORIGINAL_REQUERIDO",
                    "Debe indicar el movimiento original."
            );
        }

        MovimientoInventario original = findMovimientoRequired(request.idMovimientoOriginal());
        movimientoValidator.validateCanCompensate(original);
        movimientoValidator.validateCompensatoryMovement(original, request.motivoCompensacion());

        ProductoSku sku = original.getSku();
        Almacen almacen = original.getAlmacen();
        StockSku stock = resolveStockForUpdate(sku, almacen);

        TipoMovimientoInventario tipoCompensacion = resolveTipoCompensacion(original);
        int stockAnterior = StockMathUtil.zeroIfNull(stock.getStockFisico());
        int stockNuevo;

        if (tipoCompensacion.isEntradaFisica()) {
            stockNuevo = StockMathUtil.applyEntry(stock.getStockFisico(), original.getCantidad());
        } else {
            stockValidator.validateOutput(stock, original.getCantidad());
            stockNuevo = StockMathUtil.applyOutput(stock.getStockFisico(), original.getCantidad());
        }

        stock.setStockFisico(stockNuevo);
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        MovimientoInventario compensacion = movimientoMapper.toEntity(
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                sku,
                almacen,
                null,
                null,
                tipoCompensacion,
                MotivoMovimientoInventario.ANULACION_COMPENSATORIA,
                original.getCantidad(),
                original.getCostoUnitario(),
                original.getCostoTotal(),
                stockAnterior,
                stockNuevo,
                REFERENCIA_TIPO_COMPENSACION,
                original.getCodigoMovimiento(),
                StringNormalizer.truncateOrNull(request.motivoCompensacion(), 500),
                actor.getIdUsuarioMs1(),
                actor.getIdEmpleadoMs2(),
                resolveRol(actor),
                traceRequestId(),
                traceCorrelationId()
        );

        movimientoValidator.validateCreate(
                sku,
                almacen,
                compensacion.getTipoMovimiento(),
                compensacion.getMotivoMovimiento(),
                compensacion.getCantidad(),
                compensacion.getStockAnterior(),
                compensacion.getStockNuevo(),
                compensacion.getCostoUnitario(),
                compensacion.getReferenciaTipo(),
                compensacion.getReferenciaIdExterno(),
                compensacion.getActorIdUsuarioMs1(),
                compensacion.getActorRol(),
                compensacion.getRequestId(),
                compensacion.getCorrelationId()
        );

        MovimientoInventario savedCompensacion = movimientoRepository.saveAndFlush(compensacion);

        original.setEstadoMovimiento(EstadoMovimientoInventario.COMPENSADO);
        movimientoRepository.save(original);

        auditMovimiento(
                TipoEventoAuditoria.AJUSTE_STOCK_REGISTRADO,
                savedCompensacion,
                "REGISTRAR_MOVIMIENTO_COMPENSATORIO",
                "Movimiento de inventario registrado correctamente."
        );

        registerStockOutbox(savedStock, StockEventType.STOCK_AJUSTADO);
        registerMovimientoOutbox(savedCompensacion);

        return apiResponseFactory.dtoCreated(
                "Movimiento de inventario registrado correctamente.",
                movimientoMapper.toResponse(savedCompensacion, Moneda.PEN, true)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<MovimientoInventarioResponseDto> obtenerDetalle(Long idMovimiento, Boolean incluirCostos) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanViewMovements(actor);

        boolean includeCosts = resolveCostVisibility(actor, incluirCostos);
        MovimientoInventario movimiento = findMovimientoRequired(idMovimiento);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                movimientoMapper.toResponse(movimiento, Moneda.PEN, includeCosts)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<MovimientoInventarioResponseDto>> listar(
            MovimientoInventarioFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        movimientoPolicy.ensureCanViewMovements(actor);

        boolean includeCosts = resolveCostVisibility(actor, incluirCostos);
        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<MovimientoInventarioResponseDto> response = paginationService.toPageResponseDto(
                movimientoRepository.findAll(MovimientoInventarioSpecifications.fromFilter(filter), pageable),
                movimiento -> movimientoMapper.toResponse(movimiento, Moneda.PEN, includeCosts)
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private MovimientoInventario saveEntradaMovimiento(
            EntradaInventarioRequestDto request,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            AuthenticatedUserContext actor
    ) {
        MovimientoInventario movimiento = movimientoMapper.toEntradaEntity(
                request,
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor.getIdUsuarioMs1(),
                actor.getIdEmpleadoMs2(),
                resolveRol(actor),
                traceRequestId(),
                traceCorrelationId()
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

        return movimientoRepository.saveAndFlush(movimiento);
    }

    private MovimientoInventario saveSalidaMovimiento(
            SalidaInventarioRequestDto request,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            AuthenticatedUserContext actor
    ) {
        MovimientoInventario movimiento = movimientoMapper.toSalidaEntity(
                request,
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor.getIdUsuarioMs1(),
                actor.getIdEmpleadoMs2(),
                resolveRol(actor),
                traceRequestId(),
                traceCorrelationId()
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

        return movimientoRepository.saveAndFlush(movimiento);
    }

    private MovimientoInventario saveAjusteMovimiento(
            AjusteInventarioRequestDto request,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            AuthenticatedUserContext actor
    ) {
        MovimientoInventario movimiento = movimientoMapper.toAjusteEntity(
                request,
                codigoGeneradorService.generarCodigoMovimientoInventario(),
                sku,
                almacen,
                stockAnterior,
                stockNuevo,
                actor.getIdUsuarioMs1(),
                actor.getIdEmpleadoMs2(),
                resolveRol(actor),
                traceRequestId(),
                traceCorrelationId()
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

        return movimientoRepository.saveAndFlush(movimiento);
    }

    private EntradaInventarioRequestDto normalizeEntrada(EntradaInventarioRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "ENTRADA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de entrada de inventario."
            );
        }

        if (request.tipoMovimiento() == null || !request.tipoMovimiento().isEntradaFisica()) {
            throw new ConflictException(
                    "TIPO_MOVIMIENTO_ENTRADA_INVALIDO",
                    "El tipo de movimiento debe representar una entrada física."
            );
        }

        return EntradaInventarioRequestDto.builder()
                .sku(request.sku())
                .almacen(request.almacen())
                .tipoMovimiento(request.tipoMovimiento())
                .motivoMovimiento(request.motivoMovimiento())
                .cantidad(request.cantidad())
                .costoUnitario(request.costoUnitario())
                .referenciaTipo(StringNormalizer.truncateOrNull(request.referenciaTipo(), 50))
                .referenciaIdExterno(StringNormalizer.truncateOrNull(request.referenciaIdExterno(), 100))
                .observacion(StringNormalizer.truncateOrNull(request.observacion(), 500))
                .build();
    }

    private SalidaInventarioRequestDto normalizeSalida(SalidaInventarioRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "SALIDA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de salida de inventario."
            );
        }

        if (request.tipoMovimiento() == null || !request.tipoMovimiento().isSalidaFisica()) {
            throw new ConflictException(
                    "TIPO_MOVIMIENTO_SALIDA_INVALIDO",
                    "El tipo de movimiento debe representar una salida física."
            );
        }

        return SalidaInventarioRequestDto.builder()
                .sku(request.sku())
                .almacen(request.almacen())
                .tipoMovimiento(request.tipoMovimiento())
                .motivoMovimiento(request.motivoMovimiento())
                .cantidad(request.cantidad())
                .referenciaTipo(StringNormalizer.truncateOrNull(request.referenciaTipo(), 50))
                .referenciaIdExterno(StringNormalizer.truncateOrNull(request.referenciaIdExterno(), 100))
                .observacion(StringNormalizer.truncateOrNull(request.observacion(), 500))
                .build();
    }

    private AjusteInventarioRequestDto normalizeAjuste(AjusteInventarioRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "AJUSTE_REQUEST_REQUERIDO",
                    "Debe enviar los datos del ajuste de inventario."
            );
        }

        if (request.tipoMovimiento() == null
                || (!request.tipoMovimiento().isEntradaFisica() && !request.tipoMovimiento().isSalidaFisica())) {
            throw new ConflictException(
                    "TIPO_MOVIMIENTO_AJUSTE_INVALIDO",
                    "El ajuste debe representar una entrada o salida física."
            );
        }

        return AjusteInventarioRequestDto.builder()
                .sku(request.sku())
                .almacen(request.almacen())
                .tipoMovimiento(request.tipoMovimiento())
                .motivoMovimiento(request.motivoMovimiento())
                .cantidad(request.cantidad())
                .costoUnitario(request.costoUnitario())
                .stockFisicoEsperado(request.stockFisicoEsperado())
                .referenciaIdExterno(StringNormalizer.truncateOrNull(request.referenciaIdExterno(), 100))
                .observacion(StringNormalizer.truncateOrNull(request.observacion(), 500))
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

    private Almacen resolveAlmacen(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ALMACEN_REFERENCIA_REQUERIDA",
                    "Debe indicar el almacén solicitado."
            );
        }

        return almacenReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoAlmacen(), reference.codigo()),
                reference.nombre()
        );
    }

    private StockSku resolveOrCreateStockForEntry(ProductoSku sku, Almacen almacen) {
        return stockSkuRepository.findActivoBySkuAndAlmacenForUpdate(
                sku.getIdSku(),
                almacen.getIdAlmacen()
        ).orElseGet(() -> createInitialStock(sku, almacen));
    }

    private StockSku resolveStockForUpdate(ProductoSku sku, Almacen almacen) {
        return stockSkuRepository.findActivoBySkuAndAlmacenForUpdate(
                sku.getIdSku(),
                almacen.getIdAlmacen()
        ).orElseThrow(() -> new NotFoundException(
                "STOCK_SKU_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        ));
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

        stockValidator.validateCreate(
                sku,
                almacen,
                stock.getStockFisico(),
                stock.getStockReservado()
        );

        return stock;
    }

    private void applyCostOnEntry(StockSku stock, Integer cantidad, BigDecimal costoUnitario) {
        if (costoUnitario == null) {
            return;
        }

        stock.setCostoPromedioActual(
                StockMathUtil.weightedAverageCost(
                        stock.getStockFisico(),
                        stock.getCostoPromedioActual(),
                        cantidad,
                        costoUnitario
                )
        );
        stock.setUltimoCostoCompra(costoUnitario);
    }

    private int resolveStockNuevoAjuste(StockSku stock, AjusteInventarioRequestDto request) {
        if (request.tipoMovimiento().isEntradaFisica()) {
            stockValidator.validateEntry(request.cantidad(), request.costoUnitario());
            return StockMathUtil.applyEntry(stock.getStockFisico(), request.cantidad());
        }

        stockValidator.validateOutput(stock, request.cantidad());
        return StockMathUtil.applyOutput(stock.getStockFisico(), request.cantidad());
    }

    private MovimientoInventario findMovimientoRequired(Long idMovimiento) {
        if (idMovimiento == null) {
            throw new ValidationException(
                    "MOVIMIENTO_ID_REQUERIDO",
                    "Debe indicar el movimiento solicitado."
            );
        }

        MovimientoInventario movimiento = movimientoRepository.findByIdMovimientoAndEstadoTrue(idMovimiento)
                .orElseThrow(() -> new NotFoundException(
                        "MOVIMIENTO_INVENTARIO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        movimientoValidator.requireActive(movimiento);
        return movimiento;
    }

    private TipoMovimientoInventario resolveTipoCompensacion(MovimientoInventario original) {
        if (original.getTipoMovimiento() == null) {
            throw new ConflictException(
                    "MOVIMIENTO_TIPO_INVALIDO",
                    "No se puede compensar un movimiento sin tipo."
            );
        }

        if (original.getTipoMovimiento().isEntradaFisica()) {
            return TipoMovimientoInventario.SALIDA_AJUSTE;
        }

        if (original.getTipoMovimiento().isSalidaFisica()) {
            return TipoMovimientoInventario.ENTRADA_AJUSTE;
        }

        throw new ConflictException(
                "MOVIMIENTO_NO_COMPENSABLE",
                "Solo se pueden compensar movimientos físicos de entrada o salida."
        );
    }

    private void validateAlmacenVenta(Almacen almacen) {
        if (!Boolean.TRUE.equals(almacen.getPermiteVenta())) {
            throw new ConflictException(
                    "ALMACEN_NO_PERMITE_VENTA",
                    "El almacén no permite operaciones de venta o salida."
            );
        }
    }

    private void validateAlmacenCompra(Almacen almacen) {
        if (!Boolean.TRUE.equals(almacen.getPermiteCompra())) {
            throw new ConflictException(
                    "ALMACEN_NO_PERMITE_COMPRA",
                    "El almacén no permite operaciones de compra o entrada."
            );
        }
    }

    private boolean resolveCostVisibility(AuthenticatedUserContext actor, Boolean incluirCostos) {
        if (Boolean.TRUE.equals(incluirCostos)) {
            movimientoPolicy.ensureCanViewCosts(actor);
            return true;
        }

        return canViewCosts(actor);
    }

    private boolean canViewCosts(AuthenticatedUserContext actor) {
        return movimientoPolicy.canViewCosts(actor);
    }

    private boolean employeeCanRegisterEntry(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeRegistrarEntrada(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanRegisterOutput(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeRegistrarSalida(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanRegisterAdjustment(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeRegistrarAjuste(actor.getIdUsuarioMs1());
    }

    private void auditMovimiento(
            TipoEventoAuditoria tipoEvento,
            MovimientoInventario movimiento,
            String accion,
            String descripcion
    ) {
        auditoriaFuncionalService.registrarExito(
                tipoEvento,
                EntidadAuditada.MOVIMIENTO_INVENTARIO,
                String.valueOf(movimiento.getIdMovimiento()),
                accion,
                descripcion,
                metadataMovimiento(movimiento)
        );
    }

    private void registerStockOutbox(StockSku stock, StockEventType eventType) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        StockSnapshotEvent event = StockSnapshotEvent.of(
                eventType,
                stock.getIdStock(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toStockPayload(stock),
                Map.of("source", "MovimientoInventarioService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private void registerMovimientoOutbox(MovimientoInventario movimiento) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        MovimientoInventarioEvent event = MovimientoInventarioEvent.of(
                StockEventType.MOVIMIENTO_INVENTARIO_REGISTRADO,
                movimiento.getIdMovimiento(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toMovimientoPayload(movimiento),
                Map.of("source", "MovimientoInventarioService")
        );

        eventoDominioOutboxService.registrarEvento(event);
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
                .sobreStock(stock.getStockMaximo() != null
                        && stock.getStockFisico() != null
                        && stock.getStockFisico() > stock.getStockMaximo())
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
                .idReservaStock(movimiento.getReservaStock() == null ? null : movimiento.getReservaStock().getIdReservaStock())
                .codigoReserva(movimiento.getReservaStock() == null ? null : movimiento.getReservaStock().getCodigoReserva())
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

    private Map<String, Object> metadataMovimiento(MovimientoInventario movimiento) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idMovimiento", movimiento.getIdMovimiento());
        metadata.put("codigoMovimiento", movimiento.getCodigoMovimiento());
        metadata.put("idSku", movimiento.getSku() == null ? null : movimiento.getSku().getIdSku());
        metadata.put("codigoSku", movimiento.getSku() == null ? null : movimiento.getSku().getCodigoSku());
        metadata.put("idAlmacen", movimiento.getAlmacen() == null ? null : movimiento.getAlmacen().getIdAlmacen());
        metadata.put("codigoAlmacen", movimiento.getAlmacen() == null ? null : movimiento.getAlmacen().getCodigo());
        metadata.put("tipoMovimiento", movimiento.getTipoMovimiento() == null ? null : movimiento.getTipoMovimiento().getCode());
        metadata.put("motivoMovimiento", movimiento.getMotivoMovimiento() == null ? null : movimiento.getMotivoMovimiento().getCode());
        metadata.put("cantidad", movimiento.getCantidad());
        metadata.put("stockAnterior", movimiento.getStockAnterior());
        metadata.put("stockNuevo", movimiento.getStockNuevo());
        metadata.put("referenciaTipo", movimiento.getReferenciaTipo());
        metadata.put("referenciaIdExterno", movimiento.getReferenciaIdExterno());
        metadata.put("requestId", movimiento.getRequestId());
        metadata.put("correlationId", movimiento.getCorrelationId());
        return metadata;
    }

    private RolSistema resolveRol(AuthenticatedUserContext actor) {
        if (actor == null || !StringNormalizer.hasText(actor.getRolPrincipal())) {
            return RolSistema.SISTEMA;
        }

        return RolSistema.fromCode(actor.getRolPrincipal());
    }

    private String traceRequestId() {
        return traceValue(AuditContextHolder.getOrEmpty().requestId());
    }

    private String traceCorrelationId() {
        return traceValue(AuditContextHolder.getOrEmpty().correlationId());
    }

    private String traceValue(String value) {
        if (StringNormalizer.hasText(value)) {
            return value;
        }

        return UUID.randomUUID().toString();
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
}