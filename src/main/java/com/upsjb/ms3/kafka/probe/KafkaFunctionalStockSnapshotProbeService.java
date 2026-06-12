package com.upsjb.ms3.kafka.probe;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.domain.enums.StockEventType;
import com.upsjb.ms3.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotPayload;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.KafkaPublisherService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class KafkaFunctionalStockSnapshotProbeService {

    private static final long SYNTHETIC_ID_MIN =
            100_000_000L;

    private static final long SYNTHETIC_ID_RANGE =
            800_000_000L;

    private static final int STOCK_FISICO =
            25;

    private static final int STOCK_RESERVADO =
            0;

    private static final int STOCK_DISPONIBLE =
            STOCK_FISICO - STOCK_RESERVADO;

    private final EventoDominioOutboxRepository outboxRepository;
    private final EventoDominioOutboxService outboxService;
    private final KafkaPublisherService kafkaPublisherService;
    private final PlatformTransactionManager transactionManager;

    public KafkaFunctionalStockSnapshotProbeService(
            EventoDominioOutboxRepository outboxRepository,
            EventoDominioOutboxService outboxService,
            KafkaPublisherService kafkaPublisherService,
            PlatformTransactionManager transactionManager
    ) {
        this.outboxRepository = outboxRepository;
        this.outboxService = outboxService;
        this.kafkaPublisherService = kafkaPublisherService;
        this.transactionManager = transactionManager;
    }

    public Publication publish(
            String probeId
    ) {
        String normalizedProbeId =
                requireProbeId(
                        probeId
                );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );

        transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        Publication publication =
                transactionTemplate.execute(
                        transactionStatus -> {
                            UUID identitySeed =
                                    UUID.randomUUID();

                            ProbeStock probeStock =
                                    createProbeStock(
                                            identitySeed
                                    );

                            LocalDateTime now =
                                    LocalDateTime.now();

                            StockSnapshotPayload payload =
                                    StockSnapshotPayload.builder()
                                            .idStock(
                                                    probeStock.idStockMs3()
                                            )
                                            .idSku(
                                                    probeStock.idSkuMs3()
                                            )
                                            .codigoSku(
                                                    probeStock.codigoSku()
                                            )
                                            .barcode(
                                                    probeStock.barcode()
                                            )
                                            .idProducto(
                                                    probeStock.idProductoMs3()
                                            )
                                            .codigoProducto(
                                                    probeStock.codigoProducto()
                                            )
                                            .nombreProducto(
                                                    probeStock.nombreProducto()
                                            )
                                            .idAlmacen(
                                                    probeStock.idAlmacenMs3()
                                            )
                                            .codigoAlmacen(
                                                    probeStock.codigoAlmacen()
                                            )
                                            .nombreAlmacen(
                                                    probeStock.nombreAlmacen()
                                            )
                                            .stockFisico(
                                                    STOCK_FISICO
                                            )
                                            .stockReservado(
                                                    STOCK_RESERVADO
                                            )
                                            .stockDisponible(
                                                    STOCK_DISPONIBLE
                                            )
                                            .stockMinimo(
                                                    5
                                            )
                                            .stockMaximo(
                                                    100
                                            )
                                            .costoPromedioActual(
                                                    new BigDecimal(
                                                            "50.0000"
                                                    )
                                            )
                                            .ultimoCostoCompra(
                                                    new BigDecimal(
                                                            "50.0000"
                                                    )
                                            )
                                            .bajoStock(
                                                    Boolean.FALSE
                                            )
                                            .sobreStock(
                                                    Boolean.FALSE
                                            )
                                            .estado(
                                                    Boolean.TRUE
                                            )
                                            .createdAt(
                                                    now
                                            )
                                            .updatedAt(
                                                    now
                                            )
                                            .build();

                            String correlationId =
                                    KafkaFunctionalProbeSupport
                                            .correlationId(
                                                    normalizedProbeId
                                            );

                            StockSnapshotEvent event =
                                    StockSnapshotEvent.of(
                                            StockEventType
                                                    .STOCK_SNAPSHOT_ACTUALIZADO,
                                            probeStock.idStockMs3(),
                                            "REQ-"
                                                    + normalizedProbeId,
                                            correlationId,
                                            payload,
                                            Map.of(
                                                    "functionalProbe",
                                                    true,
                                                    "rollbackOnly",
                                                    true,
                                                    "probeId",
                                                    normalizedProbeId,
                                                    "purpose",
                                                    "KAFKA_REAL_CONTRACT_VALIDATION"
                                            )
                                    );

                            UUID eventId =
                                    event.envelope()
                                            .eventId();

                            EventoDominioOutboxResponseDto outbox =
                                    outboxService.registrarEvento(
                                            event
                                    );

                            if (
                                    outbox == null
                                            || outbox.idEvento() == null
                            ) {
                                throw new IllegalStateException(
                                        "MS3 no pudo registrar el Outbox funcional de stock."
                                );
                            }

                            OutboxPublishResultResponseDto publishResult =
                                    kafkaPublisherService
                                            .publicarInterno(
                                                    outbox.idEvento()
                                            );

                            if (
                                    publishResult == null
                                            || !Boolean.TRUE.equals(
                                            publishResult.success()
                                    )
                            ) {
                                throw new IllegalStateException(
                                        "MS3 no pudo publicar el snapshot funcional de stock: "
                                                + safeMessage(
                                                publishResult
                                        )
                                );
                            }

                            outboxRepository.flush();

                            EventoDominioOutbox verified =
                                    outboxRepository
                                            .findByEventIdAndEstadoTrue(
                                                    eventId
                                            )
                                            .orElseThrow(
                                                    () -> new IllegalStateException(
                                                            "No se encontró el Outbox funcional de stock después de publicarlo."
                                                    )
                                            );

                            if (
                                    verified.getEstadoPublicacion()
                                            != EstadoPublicacionEvento.PUBLICADO
                            ) {
                                throw new IllegalStateException(
                                        "El Outbox funcional de stock no alcanzó el estado PUBLICADO."
                                );
                            }

                            /*
                             * Kafka conserva el mensaje publicado.
                             * La fila Outbox se revierte para evitar
                             * persistencia del probe en MS3.
                             */
                            transactionStatus.setRollbackOnly();

                            return new Publication(
                                    normalizedProbeId,
                                    eventId,
                                    probeStock.idStockMs3(),
                                    probeStock.idSkuMs3(),
                                    probeStock.codigoSku(),
                                    probeStock.idAlmacenMs3(),
                                    probeStock.codigoAlmacen(),
                                    STOCK_DISPONIBLE,
                                    publishResult.topic(),
                                    publishResult.eventKey(),
                                    publishResult.partition(),
                                    publishResult.offset(),
                                    correlationId
                            );
                        }
                );

        Publication safePublication =
                Objects.requireNonNull(
                        publication,
                        "No se obtuvo el resultado funcional de MS3."
                );

        verifyRollback(
                safePublication
        );

        return safePublication;
    }

    private ProbeStock createProbeStock(
            UUID seed
    ) {
        String token =
                eventToken(
                        seed
                );

        return new ProbeStock(
                syntheticId(
                        seed,
                        1
                ),
                syntheticId(
                        seed,
                        2
                ),
                "KFP-SKU-"
                        + token,
                "KFP"
                        + token,
                syntheticId(
                        seed,
                        3
                ),
                "KFP-PROD-"
                        + token,
                "Producto temporal Kafka Probe "
                        + token,
                syntheticId(
                        seed,
                        4
                ),
                "KFP-ALM-"
                        + token,
                "Almacén temporal Kafka Probe "
                        + token
        );
    }

    private void verifyRollback(
            Publication publication
    ) {
        if (
                outboxRepository
                        .existsByEventIdAndEstadoTrue(
                                publication.eventId()
                        )
        ) {
            throw new IllegalStateException(
                    "La prueba funcional MS3 dejó un evento Outbox persistido."
            );
        }
    }

    private long syntheticId(
            UUID seed,
            int offset
    ) {
        long base =
                SYNTHETIC_ID_MIN
                        + Math.floorMod(
                        seed.getLeastSignificantBits(),
                        SYNTHETIC_ID_RANGE
                );

        return base + offset;
    }

    private String eventToken(
            UUID eventId
    ) {
        return eventId
                .toString()
                .replace(
                        "-",
                        ""
                )
                .substring(
                        0,
                        12
                )
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String safeMessage(
            OutboxPublishResultResponseDto result
    ) {
        if (
                result == null
                        || result.message() == null
                        || result.message().isBlank()
        ) {
            return "error Kafka no especificado";
        }

        return result.message().trim();
    }

    private String requireProbeId(
            String probeId
    ) {
        if (
                probeId == null
                        || probeId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El probeId funcional de MS3 es obligatorio."
            );
        }

        return probeId.trim();
    }

    private record ProbeStock(
            Long idStockMs3,
            Long idSkuMs3,
            String codigoSku,
            String barcode,
            Long idProductoMs3,
            String codigoProducto,
            String nombreProducto,
            Long idAlmacenMs3,
            String codigoAlmacen,
            String nombreAlmacen
    ) {
    }

    public record Publication(
            String probeId,
            UUID eventId,
            Long idStockMs3,
            Long idSkuMs3,
            String codigoSku,
            Long idAlmacenMs3,
            String codigoAlmacen,
            Integer stockDisponible,
            String topic,
            String eventKey,
            Integer partition,
            Long offset,
            String correlationId
    ) {
    }
}