// ruta: src/main/java/com/upsjb/ms3/controller/Ms4StockSyncController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.ms4.request.Ms4VentaAnuladaStockEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockConfirmadoEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockLiberadoEventDto;
import com.upsjb.ms3.dto.ms4.request.Ms4VentaStockReservadoEventDto;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.mapper.Ms4StockEventMapper;
import com.upsjb.ms3.service.contract.Ms4ReconciliacionService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/ms4/stock-sync")
@Tag(
        name = "MS3 - Interno - Sincronización stock MS4",
        description = "Endpoints internos para procesar eventos de stock enviados por MS4. Requieren X-Internal-Service-Key."
)
public class Ms4StockSyncController {

    private final Ms4ReconciliacionService ms4ReconciliacionService;
    private final Ms4StockEventMapper ms4StockEventMapper;
    private final ApiResponseFactory apiResponseFactory;

    @PostMapping("/reservas")
    @Operation(
            summary = "Procesar reserva pendiente de stock desde MS4",
            description = "Procesa una reserva de stock generada por MS4. El service valida idempotencia, SKU, almacén, stock disponible, kardex, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<Ms4StockSyncResultDto>> procesarReservaPendiente(
            @Valid @RequestBody Ms4VentaStockReservadoEventDto request
    ) {
        Ms4StockCommandPayload payload = ms4StockEventMapper.toPayload(request);
        Ms4StockSyncResultDto result = ms4ReconciliacionService.procesarReservaPendiente(payload);

        return ResponseEntity.ok(
                apiResponseFactory.dtoOk("Reserva de stock de MS4 procesada correctamente.", result)
        );
    }

    @PostMapping("/confirmaciones")
    @Operation(
            summary = "Procesar confirmación pendiente de stock desde MS4",
            description = "Procesa la confirmación de una reserva de stock asociada a una venta MS4. El service descuenta stock físico/reservado y registra kardex."
    )
    public ResponseEntity<ApiResponseDto<Ms4StockSyncResultDto>> procesarConfirmacionPendiente(
            @Valid @RequestBody Ms4VentaStockConfirmadoEventDto request
    ) {
        Ms4StockCommandPayload payload = ms4StockEventMapper.toPayload(request);
        Ms4StockSyncResultDto result = ms4ReconciliacionService.procesarConfirmacionPendiente(payload);

        return ResponseEntity.ok(
                apiResponseFactory.dtoOk("Confirmación de stock de MS4 procesada correctamente.", result)
        );
    }

    @PostMapping("/liberaciones")
    @Operation(
            summary = "Procesar liberación pendiente de stock desde MS4",
            description = "Procesa la liberación de una reserva enviada por MS4. El service valida reserva, libera stock reservado, registra kardex y Outbox."
    )
    public ResponseEntity<ApiResponseDto<Ms4StockSyncResultDto>> procesarLiberacionPendiente(
            @Valid @RequestBody Ms4VentaStockLiberadoEventDto request
    ) {
        Ms4StockCommandPayload payload = ms4StockEventMapper.toPayload(request);
        Ms4StockSyncResultDto result = ms4ReconciliacionService.procesarLiberacionPendiente(payload);

        return ResponseEntity.ok(
                apiResponseFactory.dtoOk("Liberación de stock de MS4 procesada correctamente.", result)
        );
    }

    @PostMapping("/anulaciones")
    @Operation(
            summary = "Procesar anulación pendiente de stock desde MS4",
            description = "Procesa una anulación de venta enviada por MS4. El service aplica la regla correspondiente según estado de la reserva y mantiene idempotencia."
    )
    public ResponseEntity<ApiResponseDto<Ms4StockSyncResultDto>> procesarAnulacionPendiente(
            @Valid @RequestBody Ms4VentaAnuladaStockEventDto request
    ) {
        Ms4StockCommandPayload payload = ms4StockEventMapper.toPayload(request);
        Ms4StockSyncResultDto result = ms4ReconciliacionService.procesarAnulacionPendiente(payload);

        return ResponseEntity.ok(
                apiResponseFactory.dtoOk("Anulación de stock de MS4 procesada correctamente.", result)
        );
    }
}