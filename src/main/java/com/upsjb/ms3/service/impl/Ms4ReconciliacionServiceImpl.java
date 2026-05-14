// ruta: src/main/java/com/upsjb/ms3/service/impl/Ms4ReconciliacionServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CorrelativoCodigo;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.consumer.KafkaIdempotencyGuard;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.mapper.Ms4StockEventMapper;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.CorrelativoCodigoRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.service.contract.Ms4ReconciliacionService;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final Ms4StockEventMapper mapper;

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarReservaPendiente(Ms4StockCommandPayload payload) {
        idempotencyGuard.ensureNotProcessed(payload);

        ProductoSku sku = resolveSku(payload);
        Almacen almacen = resolveAlmacen(payload);
        StockSku stock = resolveStockForUpdate(sku, almacen);

        int cantidad = payload.cantidad();
        int disponibleAnterior = stockDisponible(stock);

        if (disponibleAnterior < cantidad) {
            throw new ConflictException(
                    "STOCK_INSUFICIENTE",
                    "No hay stock disponible suficiente para reservar la venta de MS4."
            );
        }

        stock.setStockReservado(defaultInt(stock.getStockReservado()) + cantidad);
        stockSkuRepository.save(stock);

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
        reservaStockRepository.save(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                reserva,
                TipoMovimientoInventario.RESERVA_VENTA,
                MotivoMovimientoInventario.RESERVA_VENTA,
                cantidad,
                disponibleAnterior,
                stockDisponible(stock),
                "Reserva de stock generada desde MS4."
        );

        movimientoInventarioRepository.save(movimiento);

        return mapper.toSuccessResult(
                toReservadoDto(payload),
                reserva,
                movimiento,
                stock,
                "Reserva de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarConfirmacionPendiente(Ms4StockCommandPayload payload) {
        idempotencyGuard.ensureNotProcessed(payload);

        ProductoSku sku = resolveSku(payload);
        Almacen almacen = resolveAlmacen(payload);
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        StockSku stock = resolveStockForUpdate(sku, almacen);

        if (reserva.getEstadoReserva().isConfirmada()) {
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

        if (!reserva.getEstadoReserva().isReservada()) {
            throw new ConflictException(
                    "RESERVA_NO_CONFIRMABLE",
                    "Solo una reserva en estado RESERVADA puede confirmarse."
            );
        }

        int cantidad = payload.cantidad() == null ? reserva.getCantidad() : payload.cantidad();

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

        int stockAnterior = defaultInt(stock.getStockFisico());

        stock.setStockFisico(stockAnterior - cantidad);
        stock.setStockReservado(defaultInt(stock.getStockReservado()) - cantidad);
        stockSkuRepository.save(stock);

        reserva.setEstadoReserva(EstadoReservaStock.CONFIRMADA);
        reserva.setConfirmadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setConfirmadoAt(resolveOccurredAt(payload));
        reservaStockRepository.save(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                reserva,
                TipoMovimientoInventario.CONFIRMACION_VENTA,
                MotivoMovimientoInventario.CONFIRMACION_VENTA,
                cantidad,
                stockAnterior,
                defaultInt(stock.getStockFisico()),
                "Confirmación de stock generada desde MS4."
        );

        movimientoInventarioRepository.save(movimiento);

        return mapper.toSuccessResult(
                toConfirmadoDto(payload),
                reserva,
                movimiento,
                stock,
                "Confirmación de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarLiberacionPendiente(Ms4StockCommandPayload payload) {
        idempotencyGuard.ensureNotProcessed(payload);

        ProductoSku sku = resolveSku(payload);
        Almacen almacen = resolveAlmacen(payload);
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        StockSku stock = resolveStockForUpdate(sku, almacen);

        if (reserva.getEstadoReserva().isLiberada()) {
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

        if (!reserva.getEstadoReserva().isReservada()) {
            throw new ConflictException(
                    "RESERVA_NO_LIBERABLE",
                    "Solo una reserva en estado RESERVADA puede liberarse."
            );
        }

        int cantidad = payload.cantidad() == null ? reserva.getCantidad() : payload.cantidad();

        if (defaultInt(stock.getStockReservado()) < cantidad) {
            throw new ConflictException(
                    "STOCK_RESERVADO_INCONSISTENTE",
                    "El stock reservado es menor que la cantidad a liberar."
            );
        }

        int disponibleAnterior = stockDisponible(stock);

        stock.setStockReservado(defaultInt(stock.getStockReservado()) - cantidad);
        stockSkuRepository.save(stock);

        reserva.setEstadoReserva(EstadoReservaStock.LIBERADA);
        reserva.setLiberadoPorIdUsuarioMs1(resolveActorId(payload));
        reserva.setLiberadoAt(resolveOccurredAt(payload));
        reservaStockRepository.save(reserva);

        MovimientoInventario movimiento = createMovimiento(
                payload,
                sku,
                almacen,
                reserva,
                TipoMovimientoInventario.LIBERACION_RESERVA,
                MotivoMovimientoInventario.LIBERACION_RESERVA,
                cantidad,
                disponibleAnterior,
                stockDisponible(stock),
                "Liberación de stock generada desde MS4."
        );

        movimientoInventarioRepository.save(movimiento);

        return mapper.toSuccessResult(
                toLiberadoDto(payload),
                reserva,
                movimiento,
                stock,
                "Liberación de stock de MS4 procesada correctamente."
        );
    }

    @Override
    @Transactional
    public Ms4StockSyncResultDto procesarAnulacionPendiente(Ms4StockCommandPayload payload) {
        idempotencyGuard.ensureNotProcessed(payload);

        ProductoSku sku = resolveSku(payload);
        Almacen almacen = resolveAlmacen(payload);
        ReservaStock reserva = resolveReservaForUpdate(payload, sku, almacen);
        StockSku stock = resolveStockForUpdate(sku, almacen);

        int cantidad = payload.cantidad() == null ? reserva.getCantidad() : payload.cantidad();

        if (reserva.getEstadoReserva().isReservada()) {
            int disponibleAnterior = stockDisponible(stock);

            if (defaultInt(stock.getStockReservado()) < cantidad) {
                throw new ConflictException(
                        "STOCK_RESERVADO_INCONSISTENTE",
                        "El stock reservado es menor que la cantidad anulada por MS4."
                );
            }

            stock.setStockReservado(defaultInt(stock.getStockReservado()) - cantidad);
            stockSkuRepository.save(stock);

            reserva.setEstadoReserva(EstadoReservaStock.ANULADA);
            reserva.setLiberadoPorIdUsuarioMs1(resolveActorId(payload));
            reserva.setLiberadoAt(resolveOccurredAt(payload));
            reservaStockRepository.save(reserva);

            MovimientoInventario movimiento = createMovimiento(
                    payload,
                    sku,
                    almacen,
                    reserva,
                    TipoMovimientoInventario.ANULACION_COMPENSATORIA,
                    MotivoMovimientoInventario.ANULACION_COMPENSATORIA,
                    cantidad,
                    disponibleAnterior,
                    stockDisponible(stock),
                    resolveMotivo(payload, "Anulación de reserva solicitada por MS4.")
            );

            movimientoInventarioRepository.save(movimiento);

            return mapper.toSuccessResult(
                    toAnuladoDto(payload),
                    reserva,
                    movimiento,
                    stock,
                    "Anulación de reserva de MS4 procesada correctamente."
            );
        }

        if (reserva.getEstadoReserva().isConfirmada()) {
            int stockAnterior = defaultInt(stock.getStockFisico());

            stock.setStockFisico(stockAnterior + cantidad);
            stockSkuRepository.save(stock);

            MovimientoInventario movimiento = createMovimiento(
                    payload,
                    sku,
                    almacen,
                    reserva,
                    TipoMovimientoInventario.ANULACION_COMPENSATORIA,
                    MotivoMovimientoInventario.ANULACION_COMPENSATORIA,
                    cantidad,
                    stockAnterior,
                    defaultInt(stock.getStockFisico()),
                    resolveMotivo(payload, "Anulación compensatoria de venta confirmada por MS4.")
            );

            movimientoInventarioRepository.save(movimiento);

            return mapper.toSuccessResult(
                    toAnuladoDto(payload),
                    reserva,
                    movimiento,
                    stock,
                    "Anulación compensatoria de stock de MS4 procesada correctamente."
            );
        }

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

    private ProductoSku resolveSku(Ms4StockCommandPayload payload) {
        Long idSku = idempotencyGuard.resolveSkuId(payload.sku()).orElse(null);

        if (idSku == null) {
            throw new NotFoundException(
                    "SKU_NO_ENCONTRADO",
                    "No se encontró el SKU informado por MS4."
            );
        }

        return productoSkuRepository.findByIdSkuAndEstadoTrue(idSku)
                .orElseThrow(() -> new NotFoundException(
                        "SKU_NO_ENCONTRADO",
                        "No se encontró el SKU informado por MS4."
                ));
    }

    private Almacen resolveAlmacen(Ms4StockCommandPayload payload) {
        Long idAlmacen = idempotencyGuard.resolveAlmacenId(payload.almacen()).orElse(null);

        if (idAlmacen == null) {
            throw new NotFoundException(
                    "ALMACEN_NO_ENCONTRADO",
                    "No se encontró el almacén informado por MS4."
            );
        }

        return almacenRepository.findByIdAlmacenAndEstadoTrue(idAlmacen)
                .orElseThrow(() -> new NotFoundException(
                        "ALMACEN_NO_ENCONTRADO",
                        "No se encontró el almacén informado por MS4."
                ));
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
        movimiento.setRequestId(payload.requestId());
        movimiento.setCorrelationId(payload.correlationId());
        movimiento.setEstadoMovimiento(EstadoMovimientoInventario.REGISTRADO);
        return movimiento;
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
        if (stock.getStockDisponible() != null) {
            return stock.getStockDisponible();
        }

        return defaultInt(stock.getStockFisico()) - defaultInt(stock.getStockReservado());
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
}