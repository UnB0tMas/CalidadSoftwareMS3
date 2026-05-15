// ruta: src/main/java/com/upsjb/ms3/kafka/producer/KafkaTopicResolver.java
package com.upsjb.ms3.kafka.producer;

import com.upsjb.ms3.config.KafkaTopicProperties;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class KafkaTopicResolver {

    private final KafkaTopicProperties properties;

    public String resolve(AggregateType aggregateType, String eventType) {
        if (aggregateType == null) {
            throw new IllegalArgumentException("El aggregateType es obligatorio para resolver topic Kafka.");
        }

        return switch (aggregateType) {
            case PRODUCTO, SKU, IMAGEN_PRODUCTO -> properties.resolveProductoSnapshotTopic();
            case PRECIO -> properties.resolvePrecioSnapshotTopic();
            case PROMOCION -> properties.resolvePromocionSnapshotTopic();
            case STOCK, RESERVA_STOCK -> properties.resolveStockSnapshotTopic();
            case MOVIMIENTO_INVENTARIO -> properties.resolveMovimientoInventarioTopic();
            default -> resolveByEventType(eventType);
        };
    }

    public String resolve(ProductoSnapshotEvent event) {
        return properties.resolveProductoSnapshotTopic();
    }

    public String resolve(PrecioSnapshotEvent event) {
        return properties.resolvePrecioSnapshotTopic();
    }

    public String resolve(PromocionSnapshotEvent event) {
        return properties.resolvePromocionSnapshotTopic();
    }

    public String resolve(StockSnapshotEvent event) {
        return properties.resolveStockSnapshotTopic();
    }

    public String resolve(MovimientoInventarioEvent event) {
        return properties.resolveMovimientoInventarioTopic();
    }

    public String resolveMs4StockCommandTopic() {
        return properties.getMs4StockCommand();
    }

    public String resolveDeadLetterTopic() {
        return properties.getDeadLetter();
    }

    private String resolveByEventType(String eventType) {
        if (!StringUtils.hasText(eventType)) {
            throw new IllegalArgumentException("No se pudo resolver topic Kafka: eventType vacío.");
        }

        String value = eventType.trim().toLowerCase();

        if (value.contains("producto") || value.contains("sku") || value.contains("imagen")) {
            return properties.resolveProductoSnapshotTopic();
        }

        if (value.contains("precio")) {
            return properties.resolvePrecioSnapshotTopic();
        }

        if (value.contains("promocion")) {
            return properties.resolvePromocionSnapshotTopic();
        }

        if (value.contains("stock") || value.contains("reserva")) {
            return properties.resolveStockSnapshotTopic();
        }

        if (value.contains("movimiento") || value.contains("inventario")) {
            return properties.resolveMovimientoInventarioTopic();
        }

        throw new IllegalArgumentException("No existe topic Kafka configurado para eventType: " + eventType);
    }
}