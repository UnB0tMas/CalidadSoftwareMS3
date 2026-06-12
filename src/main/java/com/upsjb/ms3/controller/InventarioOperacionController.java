// ruta: src/main/java/com/upsjb/ms3/controller/InventarioOperacionController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.operacion.request.AjusteInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.EntradaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.SalidaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.TransferenciaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.response.MovimientoInventarioLoteResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.service.contract.InventarioOperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/inventario/operaciones")
@Tag(
        name = "MS3 - Inventario - Operaciones",
        description = "Operaciones transaccionales de inventario que permiten procesar uno o varios productos."
)
public class InventarioOperacionController {

    private final InventarioOperacionService inventarioOperacionService;

    @PostMapping("/entradas/lote")
    @Operation(summary = "Registrar una entrada de uno o varios productos")
    public ResponseEntity<ApiResponseDto<MovimientoInventarioLoteResponseDto>> registrarEntradas(
            @Valid @RequestBody EntradaInventarioLoteRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioOperacionService.registrarEntradas(request));
    }

    @PostMapping("/salidas/lote")
    @Operation(summary = "Registrar una salida de uno o varios productos")
    public ResponseEntity<ApiResponseDto<MovimientoInventarioLoteResponseDto>> registrarSalidas(
            @Valid @RequestBody SalidaInventarioLoteRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioOperacionService.registrarSalidas(request));
    }

    @PostMapping("/ajustes/lote")
    @Operation(summary = "Registrar un ajuste de uno o varios productos")
    public ResponseEntity<ApiResponseDto<MovimientoInventarioLoteResponseDto>> registrarAjustes(
            @Valid @RequestBody AjusteInventarioLoteRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioOperacionService.registrarAjustes(request));
    }

    @PostMapping("/transferencias")
    @Operation(summary = "Transferir uno o varios productos entre almacenes de manera atómica")
    public ResponseEntity<ApiResponseDto<MovimientoInventarioLoteResponseDto>> registrarTransferencia(
            @Valid @RequestBody TransferenciaInventarioRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioOperacionService.registrarTransferencia(request));
    }
}
