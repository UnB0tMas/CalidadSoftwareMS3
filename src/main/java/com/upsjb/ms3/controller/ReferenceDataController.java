// ruta: src/main/java/com/upsjb/ms3/controller/ReferenceDataController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.SelectOptionDto;
import com.upsjb.ms3.service.contract.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/catalogo/reference-data")
@Tag(
        name = "MS3 - Catálogo - Datos de referencia",
        description = "Endpoints protegidos para obtener opciones de enums y valores controlados usados por formularios administrativos."
)
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping
    @Operation(
            summary = "Listar todos los datos de referencia",
            description = "Devuelve en una sola respuesta las opciones de enums y valores controlados disponibles para formularios y filtros del frontend."
    )
    public ResponseEntity<ApiResponseDto<Map<String, List<SelectOptionDto>>>> listarTodo() {
        return ResponseEntity.ok(referenceDataService.listarTodo());
    }

    @GetMapping("/productos/estados-registro")
    @Operation(summary = "Listar estados de registro de producto")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosProductoRegistro() {
        return ResponseEntity.ok(referenceDataService.estadosProductoRegistro());
    }

    @GetMapping("/productos/estados-publicacion")
    @Operation(summary = "Listar estados de publicación de producto")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosProductoPublicacion() {
        return ResponseEntity.ok(referenceDataService.estadosProductoPublicacion());
    }

    @GetMapping("/productos/estados-venta")
    @Operation(summary = "Listar estados de venta de producto")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosProductoVenta() {
        return ResponseEntity.ok(referenceDataService.estadosProductoVenta());
    }

    @GetMapping("/skus/estados")
    @Operation(summary = "Listar estados de SKU")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosSku() {
        return ResponseEntity.ok(referenceDataService.estadosSku());
    }

    @GetMapping("/productos/generos-objetivo")
    @Operation(summary = "Listar géneros objetivo de producto")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> generosObjetivo() {
        return ResponseEntity.ok(referenceDataService.generosObjetivo());
    }

    @GetMapping("/monedas")
    @Operation(summary = "Listar monedas")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> monedas() {
        return ResponseEntity.ok(referenceDataService.monedas());
    }

    @GetMapping("/proveedores/tipos")
    @Operation(summary = "Listar tipos de proveedor")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposProveedor() {
        return ResponseEntity.ok(referenceDataService.tiposProveedor());
    }

    @GetMapping("/proveedores/tipos-documento")
    @Operation(summary = "Listar tipos de documento de proveedor")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposDocumentoProveedor() {
        return ResponseEntity.ok(referenceDataService.tiposDocumentoProveedor());
    }

    @GetMapping("/promociones/tipos-descuento")
    @Operation(summary = "Listar tipos de descuento")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposDescuento() {
        return ResponseEntity.ok(referenceDataService.tiposDescuento());
    }

    @GetMapping("/promociones/estados")
    @Operation(summary = "Listar estados de promoción")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosPromocion() {
        return ResponseEntity.ok(referenceDataService.estadosPromocion());
    }

    @GetMapping("/compras/estados")
    @Operation(summary = "Listar estados de compra de inventario")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosCompraInventario() {
        return ResponseEntity.ok(referenceDataService.estadosCompraInventario());
    }

    @GetMapping("/reservas/estados")
    @Operation(summary = "Listar estados de reserva de stock")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosReservaStock() {
        return ResponseEntity.ok(referenceDataService.estadosReservaStock());
    }

    @GetMapping("/stock/tipos-referencia")
    @Operation(summary = "Listar tipos de referencia de stock")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposReferenciaStock() {
        return ResponseEntity.ok(referenceDataService.tiposReferenciaStock());
    }

    @GetMapping("/inventario/tipos-movimiento")
    @Operation(summary = "Listar tipos de movimiento de inventario")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposMovimientoInventario() {
        return ResponseEntity.ok(referenceDataService.tiposMovimientoInventario());
    }

    @GetMapping("/inventario/motivos-movimiento")
    @Operation(summary = "Listar motivos de movimiento de inventario")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> motivosMovimientoInventario() {
        return ResponseEntity.ok(referenceDataService.motivosMovimientoInventario());
    }

    @GetMapping("/inventario/estados-movimiento")
    @Operation(summary = "Listar estados de movimiento de inventario")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosMovimientoInventario() {
        return ResponseEntity.ok(referenceDataService.estadosMovimientoInventario());
    }

    @GetMapping("/outbox/estados-publicacion")
    @Operation(summary = "Listar estados de publicación de eventos Outbox")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosPublicacionEvento() {
        return ResponseEntity.ok(referenceDataService.estadosPublicacionEvento());
    }

    @GetMapping("/atributos/tipos-dato")
    @Operation(summary = "Listar tipos de dato de atributo")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposDatoAtributo() {
        return ResponseEntity.ok(referenceDataService.tiposDatoAtributo());
    }

    @GetMapping("/seguridad/roles")
    @Operation(summary = "Listar roles del sistema")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> rolesSistema() {
        return ResponseEntity.ok(referenceDataService.rolesSistema());
    }

    @GetMapping("/auditoria/resultados")
    @Operation(summary = "Listar resultados de auditoría")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> resultadosAuditoria() {
        return ResponseEntity.ok(referenceDataService.resultadosAuditoria());
    }

    @GetMapping("/auditoria/entidades")
    @Operation(summary = "Listar entidades auditadas")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> entidadesAuditadas() {
        return ResponseEntity.ok(referenceDataService.entidadesAuditadas());
    }

    @GetMapping("/auditoria/tipos-evento")
    @Operation(summary = "Listar tipos de evento de auditoría")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> tiposEventoAuditoria() {
        return ResponseEntity.ok(referenceDataService.tiposEventoAuditoria());
    }

    @GetMapping("/outbox/aggregate-types")
    @Operation(summary = "Listar aggregate types de eventos Outbox")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> aggregateTypes() {
        return ResponseEntity.ok(referenceDataService.aggregateTypes());
    }

    @GetMapping("/shared/estados-registro")
    @Operation(summary = "Listar estados lógicos generales")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> estadosRegistro() {
        return ResponseEntity.ok(referenceDataService.estadosRegistro());
    }

    @GetMapping("/cloudinary/resource-types")
    @Operation(summary = "Listar resource types de Cloudinary")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> cloudinaryResourceTypes() {
        return ResponseEntity.ok(referenceDataService.cloudinaryResourceTypes());
    }

    @GetMapping("/kafka/producto-event-types")
    @Operation(summary = "Listar eventos Kafka de producto")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> productoEventTypes() {
        return ResponseEntity.ok(referenceDataService.productoEventTypes());
    }

    @GetMapping("/kafka/precio-event-types")
    @Operation(summary = "Listar eventos Kafka de precio")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> precioEventTypes() {
        return ResponseEntity.ok(referenceDataService.precioEventTypes());
    }

    @GetMapping("/kafka/promocion-event-types")
    @Operation(summary = "Listar eventos Kafka de promoción")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> promocionEventTypes() {
        return ResponseEntity.ok(referenceDataService.promocionEventTypes());
    }

    @GetMapping("/kafka/stock-event-types")
    @Operation(summary = "Listar eventos Kafka de stock")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> stockEventTypes() {
        return ResponseEntity.ok(referenceDataService.stockEventTypes());
    }

    @GetMapping("/kafka/ms4-stock-event-types")
    @Operation(summary = "Listar eventos Kafka de stock provenientes de MS4")
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> ms4StockEventTypes() {
        return ResponseEntity.ok(referenceDataService.ms4StockEventTypes());
    }
}