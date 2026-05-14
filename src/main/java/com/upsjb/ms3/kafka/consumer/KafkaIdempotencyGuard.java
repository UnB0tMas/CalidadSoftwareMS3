// ruta: src/main/java/com/upsjb/ms3/kafka/consumer/KafkaIdempotencyGuard.java
package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.shared.exception.ConflictException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class KafkaIdempotencyGuard {

    private final ReservaStockRepository reservaStockRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final AlmacenRepository almacenRepository;

    public boolean isProcessed(Ms4StockCommandPayload payload) {
        if (payload == null || payload.eventType() == null || payload.referenciaTipo() == null) {
            return false;
        }

        Long idSku = resolveSkuId(payload.sku()).orElse(null);
        Long idAlmacen = resolveAlmacenId(payload.almacen()).orElse(null);

        if (idSku == null || idAlmacen == null || !StringUtils.hasText(payload.safeReferenciaIdExterno())) {
            return false;
        }

        if (payload.eventType().isReserva()) {
            boolean reservaExiste = reservaStockRepository
                    .existsByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                            payload.referenciaTipo(),
                            payload.safeReferenciaIdExterno(),
                            idSku,
                            idAlmacen
                    );

            if (reservaExiste) {
                return true;
            }
        }

        TipoMovimientoInventario tipoMovimiento = resolveTipoMovimiento(payload.eventType());

        return movimientoInventarioRepository
                .existsByReferenciaTipoAndReferenciaIdExternoAndTipoMovimientoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        payload.referenciaTipo().getCode(),
                        payload.safeReferenciaIdExterno(),
                        tipoMovimiento,
                        idSku,
                        idAlmacen
                );
    }

    public void ensureNotProcessed(Ms4StockCommandPayload payload) {
        if (isProcessed(payload)) {
            throw new ConflictException(
                    "EVENTO_DUPLICADO_YA_PROCESADO",
                    "El evento de MS4 ya fue procesado previamente."
            );
        }
    }

    public Optional<Long> resolveSkuId(EntityReferenceDto reference) {
        if (reference == null) {
            return Optional.empty();
        }

        if (reference.id() != null) {
            return productoSkuRepository.findByIdSkuAndEstadoTrue(reference.id())
                    .map(entity -> entity.getIdSku());
        }

        if (StringUtils.hasText(reference.codigo())) {
            return productoSkuRepository.findByCodigoSkuIgnoreCaseAndEstadoTrue(reference.codigo().trim())
                    .map(entity -> entity.getIdSku());
        }

        if (StringUtils.hasText(reference.barcode())) {
            return productoSkuRepository.findByBarcodeIgnoreCaseAndEstadoTrue(reference.barcode().trim())
                    .map(entity -> entity.getIdSku());
        }

        return Optional.empty();
    }

    public Optional<Long> resolveAlmacenId(EntityReferenceDto reference) {
        if (reference == null) {
            return Optional.empty();
        }

        if (reference.id() != null) {
            return almacenRepository.findByIdAlmacenAndEstadoTrue(reference.id())
                    .map(entity -> entity.getIdAlmacen());
        }

        if (StringUtils.hasText(reference.codigo())) {
            return almacenRepository.findByCodigoIgnoreCaseAndEstadoTrue(reference.codigo().trim())
                    .map(entity -> entity.getIdAlmacen());
        }

        if (StringUtils.hasText(reference.nombre())) {
            return almacenRepository.findByNombreIgnoreCaseAndEstadoTrue(reference.nombre().trim())
                    .map(entity -> entity.getIdAlmacen());
        }

        return Optional.empty();
    }

    public TipoMovimientoInventario resolveTipoMovimiento(Ms4StockEventType eventType) {
        if (eventType == null) {
            throw new ConflictException(
                    "MS4_EVENT_TYPE_INVALIDO",
                    "El tipo de evento de stock de MS4 es obligatorio."
            );
        }

        return switch (eventType) {
            case VENTA_STOCK_RESERVADO_PENDIENTE -> TipoMovimientoInventario.RESERVA_VENTA;
            case VENTA_STOCK_CONFIRMADO_PENDIENTE -> TipoMovimientoInventario.CONFIRMACION_VENTA;
            case VENTA_STOCK_LIBERADO_PENDIENTE -> TipoMovimientoInventario.LIBERACION_RESERVA;
            case VENTA_ANULADA_STOCK_PENDIENTE -> TipoMovimientoInventario.ANULACION_COMPENSATORIA;
        };
    }
}