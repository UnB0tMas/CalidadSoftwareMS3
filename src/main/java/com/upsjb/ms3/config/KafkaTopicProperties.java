package com.upsjb.ms3.config;

import com.upsjb.ms3.shared.constants.TopicNames;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {

    @Min(1)
    private Integer partitions = 3;

    @Min(1)
    private Short replicationFactor = 1;

    @NotBlank
    private String productoSnapshot =
            TopicNames.PRODUCTO_SNAPSHOT;

    @NotBlank
    private String precioSnapshot =
            TopicNames.PRECIO_SNAPSHOT;

    @NotBlank
    private String promocionSnapshot =
            TopicNames.PROMOCION_SNAPSHOT;

    @NotBlank
    private String stockSnapshot =
            TopicNames.STOCK_SNAPSHOT;

    @NotBlank
    private String movimientoInventario =
            TopicNames.MOVIMIENTO_INVENTARIO;

    @NotBlank
    private String ms4StockCommand =
            TopicNames.MS4_STOCK_COMMAND;

    @NotBlank
    private String ms4StockReconciliation =
            TopicNames.MS4_STOCK_RECONCILIATION;

    @NotBlank
    private String deadLetter =
            TopicNames.DEAD_LETTER;

    public String resolveProductoSnapshotTopic() {
        return normalize(
                productoSnapshot,
                TopicNames.PRODUCTO_SNAPSHOT
        );
    }

    public String resolvePrecioSnapshotTopic() {
        return normalize(
                precioSnapshot,
                TopicNames.PRECIO_SNAPSHOT
        );
    }

    public String resolvePromocionSnapshotTopic() {
        return normalize(
                promocionSnapshot,
                TopicNames.PROMOCION_SNAPSHOT
        );
    }

    public String resolveStockSnapshotTopic() {
        return normalize(
                stockSnapshot,
                TopicNames.STOCK_SNAPSHOT
        );
    }

    public String resolveMovimientoInventarioTopic() {
        return normalize(
                movimientoInventario,
                TopicNames.MOVIMIENTO_INVENTARIO
        );
    }

    public String resolveMs4StockCommandTopic() {
        return normalize(
                ms4StockCommand,
                TopicNames.MS4_STOCK_COMMAND
        );
    }

    public String resolveMs4StockReconciliationTopic() {
        return normalize(
                ms4StockReconciliation,
                TopicNames.MS4_STOCK_RECONCILIATION
        );
    }

    public String resolveDeadLetterTopic() {
        return normalize(
                deadLetter,
                TopicNames.DEAD_LETTER
        );
    }

    public int resolvePartitions() {
        return partitions == null
                || partitions < 1
                ? 3
                : partitions;
    }

    public short resolveReplicationFactor() {
        return replicationFactor == null
                || replicationFactor < 1
                ? 1
                : replicationFactor;
    }

    private String normalize(
            String value,
            String fallback
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return fallback;
        }

        return value.trim();
    }
}