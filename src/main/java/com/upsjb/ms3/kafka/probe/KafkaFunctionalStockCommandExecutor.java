package com.upsjb.ms3.kafka.probe;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.kafka.consumer.KafkaIdempotencyGuard;
import com.upsjb.ms3.kafka.consumer.Ms4StockCommandHandler;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.Ms4StockCommandEvent;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.CategoriaRepository;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class KafkaFunctionalStockCommandExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaFunctionalStockCommandExecutor.class
            );

    private static final int BASE_STOCK_FISICO =
            25;

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final AlmacenRepository almacenRepository;
    private final StockSkuRepository stockRepository;
    private final ReservaStockRepository reservaStockRepository;
    private final MovimientoInventarioRepository
            movimientoInventarioRepository;
    private final EventoDominioOutboxRepository outboxRepository;
    private final KafkaIdempotencyGuard idempotencyGuard;
    private final KafkaProbePublisher probePublisher;
    private final KafkaProbeProperties probeProperties;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;

    public KafkaFunctionalStockCommandExecutor(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            ProductoSkuRepository productoSkuRepository,
            AlmacenRepository almacenRepository,
            StockSkuRepository stockRepository,
            ReservaStockRepository reservaStockRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            EventoDominioOutboxRepository outboxRepository,
            KafkaIdempotencyGuard idempotencyGuard,
            KafkaProbePublisher probePublisher,
            KafkaProbeProperties probeProperties,
            PlatformTransactionManager transactionManager,
            EntityManager entityManager
    ) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.productoSkuRepository = productoSkuRepository;
        this.almacenRepository = almacenRepository;
        this.stockRepository = stockRepository;
        this.reservaStockRepository = reservaStockRepository;
        this.movimientoInventarioRepository =
                movimientoInventarioRepository;
        this.outboxRepository = outboxRepository;
        this.idempotencyGuard = idempotencyGuard;
        this.probePublisher = probePublisher;
        this.probeProperties = probeProperties;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    public boolean isFunctionalProbe(
            Ms4StockCommandEvent event
    ) {
        return event != null
                && event.envelope() != null
                && KafkaFunctionalProbeSupport
                .isFunctionalProbe(
                        event.envelope()
                                .correlationId()
                );
    }

    public Ms4StockSyncResultDto execute(
            ConsumerRecord<String, String> record,
            Ms4StockCommandEvent event,
            Ms4StockCommandHandler handler
    ) {
        Objects.requireNonNull(
                record,
                "El registro Kafka funcional es obligatorio."
        );

        Objects.requireNonNull(
                handler,
                "El handler real de comandos de stock es obligatorio."
        );

        if (!isFunctionalProbe(event)) {
            throw new IllegalArgumentException(
                    "El evento recibido no pertenece a una prueba funcional Kafka."
            );
        }

        Ms4StockCommandPayload originalPayload =
                Objects.requireNonNull(
                        event.payload(),
                        "El payload funcional Kafka es obligatorio."
                );

        String probeId =
                KafkaFunctionalProbeSupport
                        .extractProbeId(
                                event.envelope()
                                        .correlationId()
                        );

        Integer cantidad =
                requirePositiveQuantity(
                        originalPayload.cantidad()
                );

        TransactionTemplate transactionTemplate =
                transactionTemplate(
                        false
                );

        ProcessingResult processingResult =
                transactionTemplate.execute(
                        transactionStatus -> {
                            ProbeFixture fixture =
                                    createTemporaryFixture(
                                            probeId,
                                            cantidad
                                    );

                            Ms4StockCommandEvent normalizedEvent =
                                    remapEventToFixture(
                                            event,
                                            fixture
                                    );

                            Ms4StockCommandPayload normalizedPayload =
                                    normalizedEvent.payload();

                            int originalFisico =
                                    fixture.stockFisico();

                            int originalReservado =
                                    fixture.stockReservado();

                            int originalDisponible =
                                    availableStock(
                                            originalFisico,
                                            originalReservado
                                    );

                            Ms4StockSyncResultDto firstResult =
                                    handler.handle(
                                            normalizedEvent
                                    );

                            validateFirstResult(
                                    firstResult,
                                    fixture
                            );

                            entityManager.flush();
                            entityManager.clear();

                            StockSku changedStock =
                                    stockRepository
                                            .findByIdStockAndEstadoTrue(
                                                    fixture.idStock()
                                            )
                                            .orElseThrow(
                                                    () -> new IllegalStateException(
                                                            "No se encontró el stock temporal después de procesar la reserva."
                                                    )
                                            );

                            int expectedReservado =
                                    originalReservado
                                            + cantidad;

                            int expectedDisponible =
                                    originalDisponible
                                            - cantidad;

                            int currentFisico =
                                    safeInt(
                                            changedStock
                                                    .getStockFisico()
                                    );

                            int currentReservado =
                                    safeInt(
                                            changedStock
                                                    .getStockReservado()
                                    );

                            int currentDisponible =
                                    availableStock(
                                            currentFisico,
                                            currentReservado
                                    );

                            if (
                                    currentFisico
                                            != originalFisico
                                            || currentReservado
                                            != expectedReservado
                                            || currentDisponible
                                            != expectedDisponible
                            ) {
                                throw new IllegalStateException(
                                        "El comando real de reserva no produjo el cambio esperado en el stock temporal de MS3."
                                );
                            }

                            /*
                             * Segunda entrega del mismo contrato.
                             * El handler real debe reconocerla como
                             * duplicada mediante sus repositorios.
                             */
                            Ms4StockSyncResultDto duplicateResult =
                                    handler.handle(
                                            normalizedEvent
                                    );

                            validateDuplicateResult(
                                    duplicateResult
                            );

                            entityManager.flush();

                            /*
                             * Revierte:
                             * - categoría temporal;
                             * - producto temporal;
                             * - SKU temporal;
                             * - almacén temporal;
                             * - stock temporal;
                             * - reserva;
                             * - movimiento;
                             * - auditoría;
                             * - correlativos modificados;
                             * - Outbox generado por el handler.
                             */
                            transactionStatus.setRollbackOnly();

                            return new ProcessingResult(
                                    firstResult,
                                    normalizedPayload,
                                    fixture,
                                    originalFisico,
                                    originalReservado,
                                    originalDisponible,
                                    expectedReservado,
                                    expectedDisponible
                            );
                        }
                );

        ProcessingResult safeResult =
                Objects.requireNonNull(
                        processingResult,
                        "No se obtuvo el resultado de procesamiento funcional MS3."
                );

        verifyCompleteRollback(
                safeResult
        );

        publishAck(
                probeId,
                record,
                safeResult
        );

        log.info(
                """
                [KAFKA-FUNCTIONAL-E2E][MS3] Comando real validado. \
                probeId={}, eventId={}, idSkuTemporal={}, \
                idAlmacenTemporal={}, stockReservadoTemporal={}, \
                stockDisponibleTemporal={}, idempotencia=OK, \
                rollback=OK, residuos=NINGUNO
                """,
                probeId,
                safeResult.firstResult()
                        .eventId(),
                safeResult.fixture()
                        .idSku(),
                safeResult.fixture()
                        .idAlmacen(),
                safeResult.expectedReservado(),
                safeResult.expectedDisponible()
        );

        return safeResult.firstResult();
    }

    private ProbeFixture createTemporaryFixture(
            String probeId,
            int cantidad
    ) {
        String token =
                probeToken(
                        probeId
                );

        Categoria categoria =
                new Categoria();

        categoria.setCodigo(
                "KFP-CAT-"
                        + token
        );
        categoria.setNombre(
                "Categoría temporal Kafka "
                        + token
        );
        categoria.setSlug(
                "kfp-categoria-"
                        + token.toLowerCase(
                        Locale.ROOT
                )
        );
        categoria.setSlugGenerado(
                Boolean.TRUE
        );
        categoria.setDescripcion(
                "Categoría temporal del probe funcional Kafka."
        );
        categoria.setNivel(
                1
        );
        categoria.setOrden(
                0
        );
        categoria.setPermiteProductos(
                Boolean.TRUE
        );
        categoria.activar();

        Categoria savedCategoria =
                categoriaRepository.saveAndFlush(
                        categoria
                );

        Producto producto =
                new Producto();

        producto.setCategoria(
                savedCategoria
        );
        producto.setCodigoProducto(
                "KFP-PROD-"
                        + token
        );
        producto.setCodigoGenerado(
                Boolean.TRUE
        );
        producto.setNombre(
                "Producto temporal Kafka "
                        + token
        );
        producto.setSlug(
                "kfp-producto-"
                        + token.toLowerCase(
                        Locale.ROOT
                )
        );
        producto.setSlugGenerado(
                Boolean.TRUE
        );
        producto.setDescripcionCorta(
                "Producto temporal para validación funcional Kafka."
        );
        producto.setDescripcionLarga(
                "Este producto se crea dentro de una transacción "
                        + "marcada obligatoriamente para rollback."
        );
        producto.setGeneroObjetivo(
                GeneroObjetivo.GENERAL
        );
        producto.setEstadoRegistro(
                EstadoProductoRegistro.ACTIVO
        );
        producto.setEstadoPublicacion(
                EstadoProductoPublicacion.PUBLICADO
        );
        producto.setEstadoVenta(
                EstadoProductoVenta.VENDIBLE
        );
        producto.setVisiblePublico(
                Boolean.FALSE
        );
        producto.setVendible(
                Boolean.TRUE
        );
        producto.setCreadoPorIdUsuarioMs1(
                1L
        );
        producto.setActualizadoPorIdUsuarioMs1(
                1L
        );
        producto.activar();

        Producto savedProducto =
                productoRepository.saveAndFlush(
                        producto
                );

        ProductoSku sku =
                new ProductoSku();

        sku.setProducto(
                savedProducto
        );
        sku.setCodigoSku(
                "KFP-SKU-"
                        + token
        );
        sku.setCodigoGenerado(
                Boolean.TRUE
        );
        sku.setBarcode(
                "KFP"
                        + token
        );
        sku.setColor(
                "NEGRO"
        );
        sku.setTalla(
                "M"
        );
        sku.setMaterial(
                "PROBE"
        );
        sku.setModelo(
                "KAFKA-E2E"
        );
        sku.setStockMinimo(
                5
        );
        sku.setStockMaximo(
                100
        );
        sku.setEstadoSku(
                EstadoSku.ACTIVO
        );
        sku.activar();

        ProductoSku savedSku =
                productoSkuRepository.saveAndFlush(
                        sku
                );

        Almacen almacen =
                new Almacen();

        almacen.setCodigo(
                "KFP-ALM-"
                        + token
        );
        almacen.setNombre(
                "Almacén temporal Kafka "
                        + token
        );
        almacen.setDireccion(
                "Ubicación temporal del probe funcional."
        );
        almacen.setPrincipal(
                Boolean.FALSE
        );
        almacen.setPermiteVenta(
                Boolean.TRUE
        );
        almacen.setPermiteCompra(
                Boolean.TRUE
        );
        almacen.setObservacion(
                "Registro temporal; la transacción siempre se revierte."
        );
        almacen.activar();

        Almacen savedAlmacen =
                almacenRepository.saveAndFlush(
                        almacen
                );

        int stockFisico =
                Math.max(
                        BASE_STOCK_FISICO,
                        cantidad + 10
                );

        StockSku stock =
                new StockSku();

        stock.setSku(
                savedSku
        );
        stock.setAlmacen(
                savedAlmacen
        );
        stock.setStockFisico(
                stockFisico
        );
        stock.setStockReservado(
                0
        );
        stock.setStockMinimo(
                5
        );
        stock.setStockMaximo(
                100
        );
        stock.setCostoPromedioActual(
                new BigDecimal(
                        "50.0000"
                )
        );
        stock.setUltimoCostoCompra(
                new BigDecimal(
                        "50.0000"
                )
        );
        stock.activar();

        StockSku savedStock =
                stockRepository.saveAndFlush(
                        stock
                );

        entityManager.flush();

        return new ProbeFixture(
                savedCategoria.getIdCategoria(),
                savedCategoria.getCodigo(),
                savedProducto.getIdProducto(),
                savedProducto.getCodigoProducto(),
                savedProducto.getNombre(),
                savedProducto.getSlug(),
                savedSku.getIdSku(),
                savedSku.getCodigoSku(),
                savedSku.getBarcode(),
                savedAlmacen.getIdAlmacen(),
                savedAlmacen.getCodigo(),
                savedAlmacen.getNombre(),
                savedStock.getIdStock(),
                stockFisico,
                0
        );
    }

    private Ms4StockCommandEvent remapEventToFixture(
            Ms4StockCommandEvent originalEvent,
            ProbeFixture fixture
    ) {
        Ms4StockCommandPayload original =
                originalEvent.payload();

        EntityReferenceDto skuReference =
                EntityReferenceDto.builder()
                        .id(
                                fixture.idSku()
                        )
                        .codigo(
                                fixture.codigoSku()
                        )
                        .nombre(
                                fixture.nombreProducto()
                        )
                        .slug(
                                fixture.slugProducto()
                        )
                        .barcode(
                                fixture.barcode()
                        )
                        .codigoSku(
                                fixture.codigoSku()
                        )
                        .codigoProducto(
                                fixture.codigoProducto()
                        )
                        .build();

        EntityReferenceDto almacenReference =
                EntityReferenceDto.builder()
                        .id(
                                fixture.idAlmacen()
                        )
                        .codigo(
                                fixture.codigoAlmacen()
                        )
                        .nombre(
                                fixture.nombreAlmacen()
                        )
                        .codigoAlmacen(
                                fixture.codigoAlmacen()
                        )
                        .build();

        Ms4StockCommandPayload normalizedPayload =
                Ms4StockCommandPayload.builder()
                        .eventId(
                                original.safeEventId()
                        )
                        .idempotencyKey(
                                original.safeIdempotencyKey()
                        )
                        .eventType(
                                original.eventType()
                        )
                        .sku(
                                skuReference
                        )
                        .almacen(
                                almacenReference
                        )
                        .referenciaTipo(
                                original.referenciaTipo()
                        )
                        .referenciaIdExterno(
                                original.safeReferenciaIdExterno()
                        )
                        .cantidad(
                                original.cantidad()
                        )
                        .codigoReserva(
                                original.safeCodigoReserva()
                        )
                        .actorIdUsuarioMs1(
                                original.actorIdUsuarioMs1()
                        )
                        .actorIdEmpleadoMs2(
                                original.actorIdEmpleadoMs2()
                        )
                        .actorRol(
                                original.actorRol()
                        )
                        .occurredAt(
                                original.occurredAt()
                        )
                        .expiresAt(
                                original.expiresAt()
                        )
                        .motivo(
                                original.motivo()
                        )
                        .requestId(
                                original.requestId()
                        )
                        .correlationId(
                                original.correlationId()
                        )
                        .metadataJson(
                                original.metadataJson()
                        )
                        .build();

        String functionalEventKey =
                "STOCK_STREAM:"
                        + fixture.idSku()
                        + ":"
                        + fixture.idAlmacen();

        DomainEventEnvelope<Ms4StockCommandPayload>
                normalizedEnvelope =
                originalEvent.envelope()
                        .withPayload(
                                normalizedPayload,
                                Ms4StockCommandEvent.SCHEMA_VERSION,
                                Map.of(
                                        "eventKey",
                                        functionalEventKey,
                                        "functionalFixture",
                                        true,
                                        "rollbackOnly",
                                        true
                                )
                        );

        return new Ms4StockCommandEvent(
                normalizedEnvelope
        );
    }

    private void validateFirstResult(
            Ms4StockSyncResultDto result,
            ProbeFixture fixture
    ) {
        if (
                result == null
                        || !Boolean.TRUE.equals(
                        result.success()
                )
                        || !Boolean.TRUE.equals(
                        result.processed()
                )
                        || Boolean.TRUE.equals(
                        result.duplicated()
                )
        ) {
            throw new IllegalStateException(
                    "MS3 no procesó correctamente el primer comando real de reserva."
            );
        }

        if (
                !Objects.equals(
                        result.idSku(),
                        fixture.idSku()
                )
                        || !Objects.equals(
                        result.idAlmacen(),
                        fixture.idAlmacen()
                )
        ) {
            throw new IllegalStateException(
                    "El resultado funcional no corresponde al SKU y almacén temporales."
            );
        }
    }

    private void validateDuplicateResult(
            Ms4StockSyncResultDto result
    ) {
        if (
                result == null
                        || !Boolean.TRUE.equals(
                        result.success()
                )
                        || !Boolean.TRUE.equals(
                        result.duplicated()
                )
                        || Boolean.TRUE.equals(
                        result.processed()
                )
        ) {
            throw new IllegalStateException(
                    "MS3 no detectó correctamente la segunda entrega como duplicada."
            );
        }
    }

    private void verifyCompleteRollback(
            ProcessingResult result
    ) {
        ProbeFixture fixture =
                result.fixture();

        if (
                categoriaRepository.existsById(
                        fixture.idCategoria()
                )
                        || productoRepository.existsById(
                        fixture.idProducto()
                )
                        || productoSkuRepository.existsById(
                        fixture.idSku()
                )
                        || almacenRepository.existsById(
                        fixture.idAlmacen()
                )
                        || stockRepository.existsById(
                        fixture.idStock()
                )
        ) {
            throw new IllegalStateException(
                    "El rollback funcional dejó registros temporales de catálogo o stock en MS3."
            );
        }

        Ms4StockCommandPayload payload =
                result.normalizedPayload();

        if (
                reservaStockRepository
                        .existsByReferenciaTipoAndReferenciaIdExternoAndEstadoTrue(
                                payload.referenciaTipo(),
                                payload.safeReferenciaIdExterno()
                        )
        ) {
            throw new IllegalStateException(
                    "El rollback funcional dejó una reserva de stock persistida."
            );
        }

        TipoMovimientoInventario movimientoEsperado =
                idempotencyGuard.resolveTipoMovimiento(
                        payload.eventType()
                );

        if (
                movimientoInventarioRepository
                        .existsByReferenciaTipoAndReferenciaIdExternoAndTipoMovimientoAndEstadoTrue(
                                payload.referenciaTipo()
                                        .getCode(),
                                payload.safeReferenciaIdExterno(),
                                movimientoEsperado
                        )
        ) {
            throw new IllegalStateException(
                    "El rollback funcional dejó un movimiento de inventario persistido."
            );
        }

        if (
                outboxRepository
                        .existsByAggregateTypeAndAggregateIdAndEventTypeAndEstadoTrue(
                                AggregateType.STOCK,
                                String.valueOf(
                                        fixture.idStock()
                                ),
                                StockEventType
                                        .STOCK_RESERVADO
                                        .getCode()
                        )
        ) {
            throw new IllegalStateException(
                    "El rollback funcional dejó un evento Outbox de stock persistido."
            );
        }

        if (
                idempotencyGuard.isProcessed(
                        payload
                )
        ) {
            throw new IllegalStateException(
                    "El rollback funcional dejó una marca de idempotencia persistida."
            );
        }
    }

    private void publishAck(
            String probeId,
            ConsumerRecord<String, String> record,
            ProcessingResult result
    ) {
        String ackTopic =
                probeProperties
                        .ms3ToMs4AckTopic();

        String ackKey =
                "functional-ack:"
                        + probeId;

        KafkaProbeAckPayload ack =
                KafkaProbeAckPayload.functional(
                        probeId,
                        probeProperties.getServiceName(),
                        probeProperties.getTargetMs4(),
                        "MS3_TO_MS4_FUNCTIONAL_ACK",
                        record.topic(),
                        record.key(),
                        "MS3 procesó el comando real de reserva, "
                                + "validó el cambio temporal de stock, "
                                + "verificó idempotencia y revirtió "
                                + "completamente todos los registros. "
                                + "eventId="
                                + result.firstResult()
                                .eventId()
                );

        RecordMetadata metadata =
                probePublisher.publishAck(
                        ack,
                        ackTopic,
                        ackKey
                );

        log.info(
                """
                [KAFKA-FUNCTIONAL-E2E][MS3] ACK funcional enviado. \
                probeId={}, topic={}, key={}, partition={}, offset={}
                """,
                probeId,
                ackTopic,
                ackKey,
                metadata == null
                        ? null
                        : metadata.partition(),
                metadata == null
                        ? null
                        : metadata.offset()
        );
    }

    private TransactionTemplate transactionTemplate(
            boolean readOnly
    ) {
        TransactionTemplate template =
                new TransactionTemplate(
                        transactionManager
                );

        template.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        template.setReadOnly(
                readOnly
        );

        return template;
    }

    private Integer requirePositiveQuantity(
            Integer cantidad
    ) {
        if (
                cantidad == null
                        || cantidad <= 0
        ) {
            throw new IllegalArgumentException(
                    "La cantidad funcional debe ser mayor a cero."
            );
        }

        return cantidad;
    }

    private int availableStock(
            int stockFisico,
            int stockReservado
    ) {
        return stockFisico
                - stockReservado;
    }

    private int safeInt(
            Integer value
    ) {
        return value == null
                ? 0
                : value;
    }

    private String probeToken(
            String probeId
    ) {
        String normalized =
                probeId == null
                        ? ""
                        : probeId.replaceAll(
                                "[^A-Za-z0-9]",
                                ""
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalized.isBlank()) {
            normalized =
                    Long.toUnsignedString(
                                    System.nanoTime(),
                                    36
                            )
                            .toUpperCase(
                                    Locale.ROOT
                            );
        }

        if (normalized.length() > 12) {
            return normalized.substring(
                    normalized.length() - 12
            );
        }

        return normalized;
    }

    private record ProbeFixture(
            Long idCategoria,
            String codigoCategoria,
            Long idProducto,
            String codigoProducto,
            String nombreProducto,
            String slugProducto,
            Long idSku,
            String codigoSku,
            String barcode,
            Long idAlmacen,
            String codigoAlmacen,
            String nombreAlmacen,
            Long idStock,
            Integer stockFisico,
            Integer stockReservado
    ) {
    }

    private record ProcessingResult(
            Ms4StockSyncResultDto firstResult,
            Ms4StockCommandPayload normalizedPayload,
            ProbeFixture fixture,
            Integer originalFisico,
            Integer originalReservado,
            Integer originalDisponible,
            Integer expectedReservado,
            Integer expectedDisponible
    ) {
    }
}