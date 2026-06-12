package com.upsjb.ms3.kafka.producer;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.MovimientoInventarioPayload;
import com.upsjb.ms3.kafka.event.PrecioSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.kafka.event.PromocionSnapshotPayload;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KafkaEventKeyResolver {

    public String resolve(
            DomainEventEnvelope<?> envelope
    ) {
        if (envelope == null) {
            throw new IllegalArgumentException(
                    "El envelope es obligatorio para resolver la key Kafka."
            );
        }

        Object payload =
                envelope.payload();

        if (
                payload
                        instanceof ProductoSnapshotPayload value
        ) {
            return productKey(
                    value.idProducto(),
                    envelope.aggregateId()
            );
        }

        if (
                payload
                        instanceof PrecioSnapshotPayload value
        ) {
            return priceKey(
                    value.idSku(),
                    envelope.aggregateId()
            );
        }

        if (
                payload
                        instanceof PromocionSnapshotPayload value
        ) {
            return promotionKey(
                    value.idPromocion(),
                    envelope.aggregateId()
            );
        }

        if (
                payload
                        instanceof StockSnapshotPayload value
        ) {
            return stockKey(
                    value.idSku(),
                    value.idAlmacen(),
                    envelope.aggregateId()
            );
        }

        if (
                payload
                        instanceof MovimientoInventarioPayload value
        ) {
            return stockStreamKey(
                    value.idSku(),
                    value.idAlmacen(),
                    envelope.aggregateId()
            );
        }

        return resolve(
                envelope.aggregateType(),
                envelope.aggregateId()
        );
    }

    public String resolve(
            AggregateType aggregateType,
            String aggregateId
    ) {
        if (aggregateType == null) {
            throw new IllegalArgumentException(
                    "El aggregateType es obligatorio para resolver la key Kafka."
            );
        }

        if (!StringUtils.hasText(
                aggregateId
        )) {
            throw new IllegalArgumentException(
                    "El aggregateId es obligatorio para resolver la key Kafka."
            );
        }

        return aggregateType.getCode()
                + ":"
                + aggregateId.trim();
    }

    public String resolveRaw(
            String key
    ) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException(
                    "La key Kafka es obligatoria."
            );
        }

        return key.trim();
    }

    private String productKey(
            Long idProducto,
            String fallbackAggregateId
    ) {
        return "PRODUCTO:"
                + resolveIdentifier(
                idProducto,
                fallbackAggregateId
        );
    }

    private String priceKey(
            Long idSku,
            String fallbackAggregateId
    ) {
        return "PRECIO:"
                + resolveIdentifier(
                idSku,
                fallbackAggregateId
        );
    }

    private String promotionKey(
            Long idPromocion,
            String fallbackAggregateId
    ) {
        return "PROMOCION:"
                + resolveIdentifier(
                idPromocion,
                fallbackAggregateId
        );
    }

    private String stockKey(
            Long idSku,
            Long idAlmacen,
            String fallbackAggregateId
    ) {
        if (
                idSku != null
                        && idAlmacen != null
        ) {
            return "STOCK:"
                    + idSku
                    + ":"
                    + idAlmacen;
        }

        return "STOCK:"
                + resolveIdentifier(
                null,
                fallbackAggregateId
        );
    }

    private String stockStreamKey(
            Long idSku,
            Long idAlmacen,
            String fallbackAggregateId
    ) {
        if (
                idSku != null
                        && idAlmacen != null
        ) {
            return "STOCK_STREAM:"
                    + idSku
                    + ":"
                    + idAlmacen;
        }

        return "MOVIMIENTO_INVENTARIO:"
                + resolveIdentifier(
                null,
                fallbackAggregateId
        );
    }

    private String resolveIdentifier(
            Long numericId,
            String fallbackAggregateId
    ) {
        if (numericId != null) {
            return String.valueOf(
                    numericId
            );
        }

        if (StringUtils.hasText(
                fallbackAggregateId
        )) {
            return fallbackAggregateId.trim();
        }

        throw new IllegalArgumentException(
                "No existe un identificador válido para construir la key Kafka."
        );
    }
}