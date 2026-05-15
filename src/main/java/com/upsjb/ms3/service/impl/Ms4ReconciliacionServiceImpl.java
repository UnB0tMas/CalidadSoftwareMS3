// ruta: src/main/java/com/upsjb/ms3/service/impl/Ms4ReconciliacionServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CorrelativoCodigo;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.consumer.KafkaIdempotencyGuard;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.MovimientoInventarioPayload;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import com.upsjb.ms3.mapper.Ms4StockEventMapper;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.CorrelativoCodigoRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.Ms4ReconciliacionService;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.StockMathUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.Ms4StockEventValidator;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class Ms4ReconciliacionServiceImpl implements Ms4ReconciliacionService {

    private static final String ENTIDAD_RESERVA = "RESERVA_STOCK";
    private static final String ENTIDAD_MOVIMIENTO = "MOVIMIENTO_INVENTARIO";

    private final ProductoSkuRepository productoSkuRepository;
    private final AlmacenRepository almacenRepository;
    private final StockSkuRepository stockSkuRepository;
    private final ReservaStockRepository reservaStockRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CorrelativoCodigoRepository correlativoCodigoRepository;
    private final KafkaIdempotencyGuard idempotencyGuard;
    private final Ms4StockEventValidator ms4StockEventValidator;
    private final Ms4StockEventMapper mapper;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarReservaPendiente(Ms4StockCommandPayload payload) {
        ResolvedCommand command = validateAndResolve(
                payload,
                Ms4StockEventType.VENTA_STOCK_RESERVADO_PENDIENTE,
                true
        );

        if (command.alreadyProcessed()) {
            return duplicate(payload);
        }

        ProductoSku sku = command.sku();
        Almacen almacen = command.almacen();
        StockSku stock = resolveStockForUpdate(sku, almacen);

        int cantidad = payload.cantidad();
        int disponibleAnterior = stockDisponible(stock);

        if (disponibleAnterior < cantidad) {
            throw new ConflictException(
                    "STOCK_INSUFICIENTE",
                    "No se puede registrar la salida porque el stock disponible es insuficiente."
            );
        }

        stock.setStockReservado(StockMathUtil.reserve(stock.getStockReservado(), cantidad));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        ReservaStock reserva = new ReservaStock();
        reserva.setCodigoReserva(nextCode(ENTIDAD_RESERVA, "RSV"));
        reserva.setCodigoGenerado(Boolean.TRUE);
        reserva.setSku(sku);
        reserva.setAlmacen(almacen);
        reserva.setReferenciaTipo(payload.referenciaTipo());
        reserva.setReferenciaIdExterno(payload.safeReferenciaIdExterno());
        reserva.setCantidad(cantidad);
        reserva.setEstadoReserva(EstadoReservaStock.RESERVADA);
        reserva.setReservadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setReservadoAt(resolveOccurredAt(payload));
        reserva.setExpiresAt(payload.expiresAt());
        reserva.setMotivo(resolveMotivo(payload, "Reserva de stock solicitada por MS4."));
        reserva.activar();
        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                savedReserva,
                TipoMovimientoInventario.RESERVA_VENTA,
                MotivoMovimientoInventario.RESERVA_VENTA,
                cantidad,
                disponibleAnterior,
                stockDisponible(savedStock),
                "Reserva de stock generada desde MS4."
        );
        MovimientoInventario savedMovimiento = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditarOperacion(
                savedReserva,
                savedMovimiento,
                TipoEventoAuditoria.RESERVA_STOCK_CREADA,
                "PROCESAR_RESERVA_MS4",
                "Reserva creada correctamente."
        );
        registrarOutbox(savedStock, savedMovimiento, StockEventType.STOCK_RESERVADO);

        log.info(
                "Reserva MS4 procesada. eventId={}, idempotencyKey={}, codigoReserva={}, codigoMovimiento={}",
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                savedReserva.getCodigoReserva(),
                savedMovimiento.getCodigoMovimiento()
        );

        return mapper.toSuccessResult(
                toReservadoDto(payload),
                savedReserva,
                savedMovimiento,
                savedStock,
                "Reserva de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarConfirmacionPendiente(Ms4StockCommandPayload payload) {
        ResolvedCommand command = validateAndResolve(
                payload,
                Ms4StockEventType.VENTA_STOCK_CONFIRMADO_PENDIENTE,
                false
        );

        if (command.alreadyProcessed()) {
            return duplicate(payload);
        }

        ProductoSku sku = command.sku();
        Almacen almacen = command.almacen();
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        ms4StockEventValidator.validateReservaMatchesCommand(
                reserva,
                sku.getIdSku(),
                almacen.getIdAlmacen(),
                payload.referenciaTipo(),
                payload.safeReferenciaIdExterno(),
                payload.cantidad()
        );

        if (reserva.getEstadoReserva().isConfirmada()) {
            return duplicate(payload);
        }

        if (!reserva.getEstadoReserva().isReservada()) {
            throw new ConflictException(
                    "RESERVA_NO_CONFIRMABLE",
                    "No se puede confirmar la reserva porque ya fue liberada, vencida o anulada."
            );
        }

        StockSku stock = resolveStockForUpdate(sku, almacen);
        int cantidad = resolveCantidad(payload, reserva);

        if (defaultInt(stock.getStockReservado()) < cantidad) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INSUFICIENTE",
                    "El stock reservado no alcanza para confirmar la venta de MS4."
            );
        }

        if (defaultInt(stock.getStockFisico()) < cantidad) {
            throw new ConflictException(
                    "STOCK_FISICO_INSUFICIENTE",
                    "El stock físico no alcanza para confirmar la venta de MS4."
            );
        }

        int stockFisicoAnterior = defaultInt(stock.getStockFisico());

        stock.setStockFisico(StockMathUtil.confirmReservationPhysical(stock.getStockFisico(), cantidad));
        stock.setStockReservado(StockMathUtil.releaseReserved(stock.getStockReservado(), cantidad));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        reserva.setEstadoReserva(EstadoReservaStock.CONFIRMADA);
        reserva.setConfirmadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setConfirmadoAt(resolveOccurredAt(payload));
        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                savedReserva,
                TipoMovimientoInventario.CONFIRMACION_VENTA,
                MotivoMovimientoInventario.CONFIRMACION_VENTA,
                cantidad,
                stockFisicoAnterior,
                defaultInt(savedStock.getStockFisico()),
                "Confirmación de stock generada desde MS4."
        );
        MovimientoInventario savedMovimiento = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditarOperacion(
                savedReserva,
                savedMovimiento,
                TipoEventoAuditoria.RESERVA_STOCK_CONFIRMADA,
                "PROCESAR_CONFIRMACION_MS4",
                "Reserva confirmada correctamente."
        );
        registrarOutbox(savedStock, savedMovimiento, StockEventType.STOCK_RESERVA_CONFIRMADA);

        log.info(
                "Confirmación MS4 procesada. eventId={}, idempotencyKey={}, codigoReserva={}, codigoMovimiento={}",
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                savedReserva.getCodigoReserva(),
                savedMovimiento.getCodigoMovimiento()
        );

        return mapper.toSuccessResult(
                toConfirmadoDto(payload),
                savedReserva,
                savedMovimiento,
                savedStock,
                "Confirmación de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarLiberacionPendiente(Ms4StockCommandPayload payload) {
        ResolvedCommand command = validateAndResolve(
                payload,
                Ms4StockEventType.VENTA_STOCK_LIBERADO_PENDIENTE,
                false
        );

        if (command.alreadyProcessed()) {
            return duplicate(payload);
        }

        ProductoSku sku = command.sku();
        Almacen almacen = command.almacen();
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        ms4StockEventValidator.validateReservaMatchesCommand(
                reserva,
                sku.getIdSku(),
                almacen.getIdAlmacen(),
                payload.referenciaTipo(),
                payload.safeReferenciaIdExterno(),
                payload.cantidad()
        );

        if (reserva.getEstadoReserva().isLiberada()) {
            return duplicate(payload);
        }

        if (!reserva.getEstadoReserva().isReservada()) {
            throw new ConflictException(
                    "RESERVA_NO_LIBERABLE",
                    "No se puede confirmar la reserva porque ya fue liberada, vencida o anulada."
            );
        }

        StockSku stock = resolveStockForUpdate(sku, almacen);
        int cantidad = resolveCantidad(payload, reserva);

        if (defaultInt(stock.getStockReservado()) < cantidad) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INCONSISTENTE",
                    "El stock reservado es menor que la cantidad a liberar."
            );
        }

        int disponibleAnterior = stockDisponible(stock);

        stock.setStockReservado(StockMathUtil.releaseReserved(stock.getStockReservado(), cantidad));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        reserva.setEstadoReserva(EstadoReservaStock.LIBERADA);
        reserva.setLiberadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setLiberadoAt(resolveOccurredAt(payload));
        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                savedReserva,
                TipoMovimientoInventario.LIBERACION_RESERVA,
                MotivoMovimientoInventario.LIBERACION_RESERVA,
                cantidad,
                disponibleAnterior,
                stockDisponible(savedStock),
                "Liberación de stock generada desde MS4."
        );
        MovimientoInventario savedMovimiento = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditarOperacion(
                savedReserva,
                savedMovimiento,
                TipoEventoAuditoria.RESERVA_STOCK_LIBERADA,
                "PROCESAR_LIBERACION_MS4",
                "Reserva liberada correctamente."
        );
        registrarOutbox(savedStock, savedMovimiento, StockEventType.STOCK_RESERVA_LIBERADA);

        log.info(
                "Liberación MS4 procesada. eventId={}, idempotencyKey={}, codigoReserva={}, codigoMovimiento={}",
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                savedReserva.getCodigoReserva(),
                savedMovimiento.getCodigoMovimiento()
        );

        return mapper.toSuccessResult(
                toLiberadoDto(payload),
                savedReserva,
                savedMovimiento,
                savedStock,
                "Liberación de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarAnulacionPendiente(Ms4StockCommandPayload payload) {
        ResolvedCommand command = validateAndResolve(
                payload,
                Ms4StockEventType.VENTA_ANULADA_STOCK_PENDIENTE,
                false
        );

        if (command.alreadyProcessed()) {
            return duplicate(payload);
        }

        ProductoSku sku = command.sku();
        Almacen almacen = command.almacen();
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        ms4StockEventValidator.validateReservaMatchesCommand(
                reserva,
                sku.getIdSku(),
                almacen.getIdAlmacen(),
                payload.referenciaTipo(),
                payload.safeReferenciaIdExterno(),
                payload.cantidad()
        );

        StockSku stock = resolveStockForUpdate(sku, almacen);
        int cantidad = resolveCantidad(payload, reserva);

        if (reserva.getEstadoReserva().isReservada()) {
            return anularReservaReservada(payload, sku, almacen, reserva, stock, cantidad);
        }

        if (reserva.getEstadoReserva().isConfirmada()) {
            return anularReservaConfirmada(payload, sku, almacen, reserva, stock, cantidad);
        }

        return duplicate(payload);
    }

    private Ms4StockSyncResultDto anularReservaReservada(
            Ms4StockCommandPayload payload,
            ProductoSku sku,
            Almacen almacen,
            ReservaStock reserva,
            StockSku stock,
            int cantidad
    ) {
        if (defaultInt(stock.getStockReservado()) < cantidad) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INCONSISTENTE",
                    "El stock reservado es menor que la cantidad anulada por MS4."
            );
        }

        int disponibleAnterior = stockDisponible(stock);

        stock.setStockReservado(StockMathUtil.releaseReserved(stock.getStockReservado(), cantidad));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        reserva.setEstadoReserva(EstadoReservaStock.ANULADA);
        reserva.setLiberadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setLiberadoAt(resolveOccurredAt(payload));
        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                savedReserva,
                TipoMovimientoInventario.ANULACION_COMPENSATORIA,
                MotivoMovimientoInventario.ANULACION_COMPENSATORIA,
                cantidad,
                disponibleAnterior,
                stockDisponible(savedStock),
                resolveMotivo(payload, "Anulación de reserva solicitada por MS4.")
        );
        MovimientoInventario savedMovimiento = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditarOperacion(
                savedReserva,
                savedMovimiento,
                TipoEventoAuditoria.RESERVA_STOCK_LIBERADA,
                "PROCESAR_ANULACION_MS4",
                "Anulación de reserva de MS4 procesada correctamente."
        );
        registrarOutbox(savedStock, savedMovimiento, StockEventType.STOCK_RESERVA_LIBERADA);

        log.info(
                "Anulación de reserva reservada MS4 procesada. eventId={}, idempotencyKey={}, codigoReserva={}, codigoMovimiento={}",
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                savedReserva.getCodigoReserva(),
                savedMovimiento.getCodigoMovimiento()
        );

        return mapper.toSuccessResult(
                toAnuladoDto(payload),
                savedReserva,
                savedMovimiento,
                savedStock,
                "Anulación de reserva de MS4 procesada correctamente."
        );
    }

    private Ms4StockSyncResultDto anularReservaConfirmada(
            Ms4StockCommandPayload payload,
            ProductoSku sku,
            Almacen almacen,
            ReservaStock reserva,
            StockSku stock,
            int cantidad
    ) {
        int stockFisicoAnterior = defaultInt(stock.getStockFisico());

        stock.setStockFisico(StockMathUtil.applyEntry(stock.getStockFisico(), cantidad));
        StockSku savedStock = stockSkuRepository.saveAndFlush(stock);

        reserva.setEstadoReserva(EstadoReservaStock.ANULADA);
        reserva.setLiberadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setLiberadoAt(resolveOccurredAt(payload));
        ReservaStock savedReserva = reservaStockRepository.saveAndFlush(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                savedReserva,
                TipoMovimientoInventario.ANULACION_COMPENSATORIA,
                MotivoMovimientoInventario.ANULACION_COMPENSATORIA,
                cantidad,
                stockFisicoAnterior,
                defaultInt(savedStock.getStockFisico()),
                resolveMotivo(payload, "Anulación compensatoria de venta confirmada por MS4.")
        );
        MovimientoInventario savedMovimiento = movimientoInventarioRepository.saveAndFlush(movimiento);

        auditarOperacion(
                savedReserva,
                savedMovimiento,
                TipoEventoAuditoria.MOVIMIENTO_KARDEX_REGISTRADO,
                "PROCESAR_ANULACION_COMPENSATORIA_MS4",
                "Movimiento de inventario registrado correctamente."
        );
        registrarOutbox(savedStock, savedMovimiento, StockEventType.STOCK_AJUSTADO);

        log.info(
                "Anulación compensatoria MS4 procesada. eventId={}, idempotencyKey={}, codigoReserva={}, codigoMovimiento={}",
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                savedReserva.getCodigoReserva(),
                savedMovimiento.getCodigoMovimiento()
        );

        return mapper.toSuccessResult(
                toAnuladoDto(payload),
                savedReserva,
                savedMovimiento,
                savedStock,
                "Anulación compensatoria de stock de MS4 procesada correctamente."
        );
    }

    private ResolvedCommand validateAndResolve(
            Ms4StockCommandPayload payload,
            Ms4StockEventType expectedEventType,
            boolean cantidadObligatoria
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "MS4_PAYLOAD_REQUERIDO",
                    "El payload de MS4 es obligatorio."
            );
        }

        Long idSku = idempotencyGuard.resolveSkuId(payload.sku()).orElse(null);
        Long idAlmacen = idempotencyGuard.resolveAlmacenId(payload.almacen()).orElse(null);

        ms4StockEventValidator.validateStockCommand(
                payload.safeIdempotencyKey(),
                payload.eventType(),
                expectedEventType,
                payload.referenciaTipo(),
                payload.safeReferenciaIdExterno(),
                idSku,
                idAlmacen,
                payload.cantidad(),
                cantidadObligatoria
        );

        if (idempotencyGuard.isProcessed(payload)) {
            return ResolvedCommand.duplicated();
        }

        ProductoSku sku = productoSkuRepository.findByIdSkuAndEstadoTrue(idSku)
                .orElseThrow(() -> new NotFoundException(
                        "SKU_NO_ENCONTRADO",
                        "No se encontró el SKU informado por MS4."
                ));

        ensureSkuOperativo(sku);

        Almacen almacen = almacenRepository.findByIdAlmacenAndEstadoTrue(idAlmacen)
                .orElseThrow(() -> new NotFoundException(
                        "ALMACEN_NO_ENCONTRADO",
                        "No se encontró el almacén informado por MS4."
                ));

        ensureAlmacenPermiteVenta(almacen);

        return new ResolvedCommand(sku, almacen, false);
    }

    private void ensureSkuOperativo(ProductoSku sku) {
        if (sku.getEstadoSku() == null || !sku.getEstadoSku().isOperativo()) {
            throw new ConflictException(
                    "SKU_NO_OPERATIVO",
                    "No se puede completar la operación porque el SKU no está operativo."
            );
        }
    }

    private void ensureAlmacenPermiteVenta(Almacen almacen) {
        if (!Boolean.TRUE.equals(almacen.getPermiteVenta())) {
            throw new ConflictException(
                    "ALMACEN_NO_PERMITE_VENTA",
                    "No se puede completar la operación porque el almacén no permite venta."
            );
        }
    }

    private StockSku resolveStockForUpdate(ProductoSku sku, Almacen almacen) {
        return stockSkuRepository.findActivoBySkuAndAlmacenForUpdate(
                        sku.getIdSku(),
                        almacen.getIdAlmacen()
                )
                .orElseThrow(() -> new NotFoundException(
                        "STOCK_NO_ENCONTRADO",
                        "No existe stock activo para el SKU y almacén informado."
                ));
    }

    private ReservaStock resolveReservaForUpdate(
            Ms4StockCommandPayload payload,
            ProductoSku sku,
            Almacen almacen
    ) {
        if (StringUtils.hasText(payload.safeCodigoReserva())) {
            return reservaStockRepository.findActivoByCodigoForUpdate(payload.safeCodigoReserva())
                    .orElseThrow(() -> new NotFoundException(
                            "RESERVA_NO_ENCONTRADA",
                            "No se encontró la reserva de stock informada por MS4."
                    ));
        }

        return reservaStockRepository.findActivoByReferenciaForUpdate(
                        payload.referenciaTipo(),
                        payload.safeReferenciaIdExterno(),
                        sku.getIdSku(),
                        almacen.getIdAlmacen()
                )
                .orElseThrow(() -> new NotFoundException(
                        "RESERVA_NO_ENCONTRADA",
                        "No se encontró la reserva de stock informada por MS4."
                ));
    }

    private int resolveCantidad(Ms4StockCommandPayload payload, ReservaStock reserva) {
        Integer cantidad = payload.cantidad() == null ? reserva.getCantidad() : payload.cantidad();

        if (cantidad == null || cantidad <= 0) {
            throw new ConflictException(
                    "MS4_CANTIDAD_INVALIDA",
                    "La cantidad informada por MS4 debe ser mayor a cero."
            );
        }

        if (reserva.getCantidad() != null && !Objects.equals(reserva.getCantidad(), cantidad)) {
            throw new ConflictException(
                    "MS4_CANTIDAD_NO_COINCIDE",
                    "La cantidad informada por MS4 no coincide con la cantidad reservada."
            );
        }

        return cantidad;
    }

    private MovimientoInventario createMovimiento(
            Ms4StockCommandPayload payload,
            ProductoSku sku,
            Almacen almacen,
            ReservaStock reserva,
            TipoMovimientoInventario tipoMovimiento,
            MotivoMovimientoInventario motivoMovimiento,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockNuevo,
            String observacion
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setCodigoMovimiento(nextCode(ENTIDAD_MOVIMIENTO, "MOV"));
        movimiento.setCodigoGenerado(Boolean.TRUE);
        movimiento.setSku(sku);
        movimiento.setAlmacen(almacen);
        movimiento.setReservaStock(reserva);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setMotivoMovimiento(motivoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setReferenciaTipo(payload.referenciaTipo().getCode());
        movimiento.setReferenciaIdExterno(payload.safeReferenciaIdExterno());
        movimiento.setObservacion(observacion);
        movimiento.setActorIdUsuarioMs1(resolveActorId(payload));
        movimiento.setActorIdEmpleadoMs2(payload.actorIdEmpleadoMs2());
        movimiento.setActorRol(payload.actorRol() == null ? RolSistema.SISTEMA : payload.actorRol());
        movimiento.setRequestId(traceValue(payload.requestId()));
        movimiento.setCorrelationId(traceValue(payload.correlationId()));
        movimiento.setEstadoMovimiento(EstadoMovimientoInventario.REGISTRADO);
        movimiento.activar();
        return movimiento;
    }

    private void registrarOutbox(
            StockSku stock,
            MovimientoInventario movimiento,
            StockEventType stockEventType
    ) {
        eventoDominioOutboxService.registrarEvento(
                StockSnapshotEvent.of(
                        stockEventType,
                        stock.getIdStock(),
                        movimiento.getRequestId(),
                        movimiento.getCorrelationId(),
                        toStockPayload(stock),
                        Map.of("source", "Ms4ReconciliacionService")
                )
        );

        eventoDominioOutboxService.registrarEvento(
                MovimientoInventarioEvent.of(
                        StockEventType.MOVIMIENTO_INVENTARIO_REGISTRADO,
                        movimiento.getIdMovimiento(),
                        movimiento.getRequestId(),
                        movimiento.getCorrelationId(),
                        toMovimientoPayload(movimiento),
                        Map.of("source", "Ms4ReconciliacionService")
                )
        );
    }

    private StockSnapshotPayload toStockPayload(StockSku stock) {
        ProductoSku sku = stock.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = stock.getAlmacen();
        int disponible = stockDisponible(stock);

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

    private void auditarOperacion(
            ReservaStock reserva,
            MovimientoInventario movimiento,
            TipoEventoAuditoria tipoEvento,
            String accion,
            String descripcion
    ) {
        auditoriaFuncionalService.registrarExito(
                tipoEvento,
                EntidadAuditada.MOVIMIENTO_INVENTARIO,
                String.valueOf(movimiento.getIdMovimiento()),
                accion,
                descripcion,
                Map.of(
                        "idReservaStock", reserva.getIdReservaStock(),
                        "codigoReserva", reserva.getCodigoReserva(),
                        "estadoReserva", reserva.getEstadoReserva().getCode(),
                        "idMovimiento", movimiento.getIdMovimiento(),
                        "codigoMovimiento", movimiento.getCodigoMovimiento(),
                        "tipoMovimiento", movimiento.getTipoMovimiento().getCode(),
                        "referenciaTipo", movimiento.getReferenciaTipo(),
                        "referenciaIdExterno", movimiento.getReferenciaIdExterno(),
                        "requestId", movimiento.getRequestId(),
                        "correlationId", movimiento.getCorrelationId()
                )
        );
    }

    private Ms4StockSyncResultDto duplicate(Ms4StockCommandPayload payload) {
        return mapper.toDuplicateResult(
                payload.eventType(),
                payload.safeEventId(),
                payload.safeIdempotencyKey(),
                payload.referenciaTipo().getCode(),
                payload.safeReferenciaIdExterno(),
                payload.requestId(),
                payload.correlationId()
        );
    }

    private String nextCode(String entidad, String fallbackPrefix) {
        CorrelativoCodigo correlativo = correlativoCodigoRepository.findActivoByEntidadForUpdate(entidad)
                .orElseGet(() -> {
                    CorrelativoCodigo nuevo = new CorrelativoCodigo();
                    nuevo.setEntidad(entidad);
                    nuevo.setPrefijo(fallbackPrefix);
                    nuevo.setUltimoNumero(0L);
                    nuevo.setLongitud(6);
                    nuevo.setDescripcion("Correlativo generado automáticamente para " + entidad + ".");
                    nuevo.activar();
                    return correlativoCodigoRepository.saveAndFlush(nuevo);
                });

        Long numero = correlativo.siguienteNumero();
        correlativoCodigoRepository.save(correlativo);

        String prefijo = StringUtils.hasText(correlativo.getPrefijo())
                ? correlativo.getPrefijo().trim()
                : fallbackPrefix;

        int longitud = correlativo.getLongitud() == null || correlativo.getLongitud() <= 0
                ? 6
                : correlativo.getLongitud();

        return prefijo + "-" + String.format("%0" + longitud + "d", numero);
    }

    private Integer stockDisponible(StockSku stock) {
        return StockMathUtil.available(stock.getStockFisico(), stock.getStockReservado());
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Long resolveActorId(Ms4StockCommandPayload payload) {
        return payload.actorIdUsuarioMs1() == null ? 0L : payload.actorIdUsuarioMs1();
    }

    private LocalDateTime resolveOccurredAt(Ms4StockCommandPayload payload) {
        return payload.occurredAt() == null ? LocalDateTime.now() : payload.occurredAt();
    }

    private String resolveMotivo(Ms4StockCommandPayload payload, String defaultValue) {
        return StringUtils.hasText(payload.motivo()) ? payload.motivo().trim() : defaultValue;
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }

    private com.upsjb.ms3.dto.ms4.request.Ms4VentaStockReservadoEventDto toReservadoDto(Ms4StockCommandPayload payload) {
        return com.upsjb.ms3.dto.ms4.request.Ms4VentaStockReservadoEventDto.builder()
                .eventId(payload.safeEventId())
                .idempotencyKey(payload.safeIdempotencyKey())
                .eventType(Ms4StockEventType.VENTA_STOCK_RESERVADO_PENDIENTE)
                .sku(payload.sku())
                .almacen(payload.almacen())
                .referenciaTipo(payload.referenciaTipo())
                .referenciaIdExterno(payload.safeReferenciaIdExterno())
                .cantidad(payload.cantidad())
                .actorIdUsuarioMs1(payload.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(payload.actorIdEmpleadoMs2())
                .actorRol(payload.actorRol())
                .occurredAt(payload.occurredAt())
                .expiresAt(payload.expiresAt())
                .motivo(payload.motivo())
                .requestId(payload.requestId())
                .correlationId(payload.correlationId())
                .metadataJson(payload.metadataJson())
                .build();
    }

    private com.upsjb.ms3.dto.ms4.request.Ms4VentaStockConfirmadoEventDto toConfirmadoDto(Ms4StockCommandPayload payload) {
        return com.upsjb.ms3.dto.ms4.request.Ms4VentaStockConfirmadoEventDto.builder()
                .eventId(payload.safeEventId())
                .idempotencyKey(payload.safeIdempotencyKey())
                .eventType(Ms4StockEventType.VENTA_STOCK_CONFIRMADO_PENDIENTE)
                .sku(payload.sku())
                .almacen(payload.almacen())
                .referenciaTipo(payload.referenciaTipo())
                .referenciaIdExterno(payload.safeReferenciaIdExterno())
                .cantidad(payload.cantidad())
                .codigoReserva(payload.safeCodigoReserva())
                .actorIdUsuarioMs1(payload.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(payload.actorIdEmpleadoMs2())
                .actorRol(payload.actorRol())
                .occurredAt(payload.occurredAt())
                .motivo(payload.motivo())
                .requestId(payload.requestId())
                .correlationId(payload.correlationId())
                .metadataJson(payload.metadataJson())
                .build();
    }

    private com.upsjb.ms3.dto.ms4.request.Ms4VentaStockLiberadoEventDto toLiberadoDto(Ms4StockCommandPayload payload) {
        return com.upsjb.ms3.dto.ms4.request.Ms4VentaStockLiberadoEventDto.builder()
                .eventId(payload.safeEventId())
                .idempotencyKey(payload.safeIdempotencyKey())
                .eventType(Ms4StockEventType.VENTA_STOCK_LIBERADO_PENDIENTE)
                .sku(payload.sku())
                .almacen(payload.almacen())
                .referenciaTipo(payload.referenciaTipo())
                .referenciaIdExterno(payload.safeReferenciaIdExterno())
                .cantidad(payload.cantidad())
                .codigoReserva(payload.safeCodigoReserva())
                .actorIdUsuarioMs1(payload.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(payload.actorIdEmpleadoMs2())
                .actorRol(payload.actorRol())
                .occurredAt(payload.occurredAt())
                .motivo(payload.motivo())
                .requestId(payload.requestId())
                .correlationId(payload.correlationId())
                .metadataJson(payload.metadataJson())
                .build();
    }

    private com.upsjb.ms3.dto.ms4.request.Ms4VentaAnuladaStockEventDto toAnuladoDto(Ms4StockCommandPayload payload) {
        return com.upsjb.ms3.dto.ms4.request.Ms4VentaAnuladaStockEventDto.builder()
                .eventId(payload.safeEventId())
                .idempotencyKey(payload.safeIdempotencyKey())
                .eventType(Ms4StockEventType.VENTA_ANULADA_STOCK_PENDIENTE)
                .sku(payload.sku())
                .almacen(payload.almacen())
                .referenciaTipo(payload.referenciaTipo())
                .referenciaIdExterno(payload.safeReferenciaIdExterno())
                .cantidad(payload.cantidad())
                .codigoReserva(payload.safeCodigoReserva())
                .actorIdUsuarioMs1(payload.actorIdUsuarioMs1())
                .actorIdEmpleadoMs2(payload.actorIdEmpleadoMs2())
                .actorRol(payload.actorRol())
                .occurredAt(payload.occurredAt())
                .motivo(resolveMotivo(payload, "Anulación de stock solicitada por MS4."))
                .requestId(payload.requestId())
                .correlationId(payload.correlationId())
                .metadataJson(payload.metadataJson())
                .build();
    }

    private record ResolvedCommand(
            ProductoSku sku,
            Almacen almacen,
            boolean alreadyProcessed
    ) {

        private static ResolvedCommand duplicated() {
            return new ResolvedCommand(null, null, true);
        }
    }
}