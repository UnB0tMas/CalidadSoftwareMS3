package com.upsjb.ms3.config;

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

    @NotBlank
    private String productoSnapshot = "ms3.producto.snapshot.v1";

    @NotBlank
    private String precioSnapshot = "ms3.precio.snapshot.v1";

    @NotBlank
    private String promocionSnapshot = "ms3.promocion.snapshot.v1";

    @NotBlank
    private String stockSnapshot = "ms3.stock.snapshot.v1";

    @NotBlank
    private String movimientoInventario = "ms3.movimiento-inventario.v1";

    @NotBlank
    private String ms4StockCommand = "ms4.stock.command.v1";

    @NotBlank
    private String ms4StockReconciliation = "ms4.stock.reconciliation.v1";

    @NotBlank
    private String deadLetter = "ms3.dead-letter.v1";

    public String resolveProductoSnapshotTopic() {
        return productoSnapshot;
    }

    public String resolvePrecioSnapshotTopic() {
        return precioSnapshot;
    }

    public String resolvePromocionSnapshotTopic() {
        return promocionSnapshot;
    }

    public String resolveStockSnapshotTopic() {
        return stockSnapshot;
    }

    public String resolveMovimientoInventarioTopic() {
        return movimientoInventario;
    }
}