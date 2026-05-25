// ruta: src/main/java/com/upsjb/ms3/controller/OutboxController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.outbox.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms3.dto.outbox.request.OutboxRetryRequestDto;
import com.upsjb.ms3.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.KafkaPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/outbox")
@Tag(
        name = "MS3 - Outbox Kafka",
        description = "Endpoints administrativos para consultar, preparar reintentos y publicar eventos Outbox hacia Kafka."
)
public class OutboxController {

    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final KafkaPublisherService kafkaPublisherService;

    @GetMapping
    @Operation(
            summary = "Listar eventos Outbox",
            description = "Lista eventos Outbox con filtros y paginación. No expone payload por defecto."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<EventoDominioOutboxResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute EventoDominioOutboxFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(eventoDominioOutboxService.listar(filter, pageRequest));
    }

    @GetMapping("/{idEvento}")
    @Operation(
            summary = "Obtener evento Outbox por ID",
            description = "Obtiene el detalle de un evento Outbox. El payload solo se incluye si se solicita explícitamente y el service lo autoriza."
    )
    public ResponseEntity<ApiResponseDto<EventoDominioOutboxResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del evento Outbox.", required = true)
            @Positive(message = "El ID del evento debe ser positivo.")
            @PathVariable Long idEvento,
            @Parameter(description = "Indica si se debe incluir payloadJson en la respuesta.")
            @RequestParam(defaultValue = "false") Boolean incluirPayload
    ) {
        return ResponseEntity.ok(eventoDominioOutboxService.obtenerDetalle(idEvento, incluirPayload));
    }

    @GetMapping("/event-id/{eventId}")
    @Operation(
            summary = "Obtener evento Outbox por eventId",
            description = "Obtiene el detalle de un evento Outbox usando su UUID funcional."
    )
    public ResponseEntity<ApiResponseDto<EventoDominioOutboxResponseDto>> obtenerPorEventId(
            @Parameter(description = "UUID funcional del evento Outbox.", required = true)
            @PathVariable UUID eventId,
            @Parameter(description = "Indica si se debe incluir payloadJson en la respuesta.")
            @RequestParam(defaultValue = "false") Boolean incluirPayload
    ) {
        return ResponseEntity.ok(eventoDominioOutboxService.obtenerPorEventId(eventId, incluirPayload));
    }

    @PatchMapping("/{idEvento}/preparar-reintento")
    @Operation(
            summary = "Preparar evento Outbox para reintento",
            description = "Marca un evento Outbox como pendiente para que el scheduler o publicador pueda reprocesarlo. No publica Kafka desde el controller."
    )
    public ResponseEntity<ApiResponseDto<EventoDominioOutboxResponseDto>> prepararReintento(
            @Parameter(description = "ID técnico del evento Outbox.", required = true)
            @Positive(message = "El ID del evento debe ser positivo.")
            @PathVariable Long idEvento,
            @Valid @RequestBody OutboxRetryRequestDto request
    ) {
        return ResponseEntity.ok(eventoDominioOutboxService.prepararReintento(idEvento, request));
    }

    @PostMapping("/{idEvento}/reintentar")
    @Operation(
            summary = "Reintentar publicación de evento Outbox",
            description = "Solicita al service el reintento de publicación Kafka para un evento Outbox. El controller no usa KafkaTemplate."
    )
    public ResponseEntity<ApiResponseDto<OutboxPublishResultResponseDto>> reintentar(
            @Parameter(description = "ID técnico del evento Outbox.", required = true)
            @Positive(message = "El ID del evento debe ser positivo.")
            @PathVariable Long idEvento,
            @Valid @RequestBody OutboxRetryRequestDto request
    ) {
        return ResponseEntity.ok(eventoDominioOutboxService.reintentar(idEvento, request));
    }

    @PostMapping("/{idEvento}/publicar")
    @Operation(
            summary = "Publicar evento Outbox manualmente",
            description = "Publica un evento Outbox específico mediante el service de publicación Kafka. Requiere ADMIN por SecurityConfig y policy."
    )
    public ResponseEntity<ApiResponseDto<OutboxPublishResultResponseDto>> publicar(
            @Parameter(description = "ID técnico del evento Outbox.", required = true)
            @Positive(message = "El ID del evento debe ser positivo.")
            @PathVariable Long idEvento
    ) {
        return ResponseEntity.ok(kafkaPublisherService.publicar(idEvento));
    }

    @PostMapping("/publicar-pendientes")
    @Operation(
            summary = "Publicar lote de eventos Outbox pendientes",
            description = "Procesa manualmente el siguiente lote publicable de eventos Outbox. La lógica de lock, retry y Kafka permanece en service/outbox."
    )
    public ResponseEntity<ApiResponseDto<List<OutboxPublishResultResponseDto>>> publicarPendientes() {
        return ResponseEntity.ok(kafkaPublisherService.publicarPendientes());
    }
}