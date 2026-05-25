// ruta: src/main/java/com/upsjb/ms3/controller/StockController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.stock.filter.StockSkuFilterDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockDisponibleResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuDetailResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/inventario/stock")
@Tag(
        name = "MS3 - Inventario - Stock",
        description = "Endpoints protegidos y consultivos para revisar stock por SKU, almacén y disponibilidad. No registran movimientos ni modifican stock."
)
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(
            summary = "Listar stock",
            description = "Lista stock por SKU y almacén con filtros y paginación. La visibilidad de costos, permisos y reglas funcionales se validan en el service."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute StockSkuFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(stockService.listar(filter, pageRequest, incluirCostos));
    }

    @GetMapping("/{idStock}")
    @Operation(
            summary = "Obtener detalle de stock",
            description = "Obtiene el detalle de stock por ID técnico. No modifica stock ni registra kardex."
    )
    public ResponseEntity<ApiResponseDto<StockSkuDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del registro de stock.", required = true)
            @Positive(message = "El ID del stock debe ser positivo.")
            @PathVariable Long idStock,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(stockService.obtenerDetalle(idStock, incluirCostos));
    }

    @GetMapping("/skus/{idSku}/almacenes/{idAlmacen}")
    @Operation(
            summary = "Obtener stock por SKU y almacén",
            description = "Obtiene el stock de un SKU en un almacén usando IDs técnicos. La resolución y validación final corresponden al service."
    )
    public ResponseEntity<ApiResponseDto<StockSkuDetailResponseDto>> obtenerPorSkuYAlmacen(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.obtenerPorSkuYAlmacen(
                        skuIdReference(idSku),
                        almacenIdReference(idAlmacen),
                        incluirCostos
                )
        );
    }

    @GetMapping("/por-sku-almacen")
    @Operation(
            summary = "Obtener stock por referencias funcionales",
            description = "Obtiene el stock usando referencias funcionales de SKU y almacén. No resuelve FK en controller; solo arma DTOs de referencia."
    )
    public ResponseEntity<ApiResponseDto<StockSkuDetailResponseDto>> obtenerPorReferencias(
            @Parameter(description = "ID técnico del SKU.")
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam(required = false) Long idSku,
            @Parameter(description = "Código funcional del SKU.")
            @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
            @RequestParam(required = false) String codigoSku,
            @Parameter(description = "Código de barras del SKU.")
            @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
            @RequestParam(required = false) String barcode,
            @Parameter(description = "ID técnico del almacén.")
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam(required = false) Long idAlmacen,
            @Parameter(description = "Código funcional del almacén.")
            @Size(max = 50, message = "El código del almacén no debe superar 50 caracteres.")
            @RequestParam(required = false) String codigoAlmacen,
            @Parameter(description = "Nombre del almacén.")
            @Size(max = 150, message = "El nombre del almacén no debe superar 150 caracteres.")
            @RequestParam(required = false) String nombreAlmacen,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.obtenerPorSkuYAlmacen(
                        skuReference(idSku, codigoSku, barcode),
                        almacenReference(idAlmacen, codigoAlmacen, nombreAlmacen),
                        incluirCostos
                )
        );
    }

    @GetMapping("/skus/{idSku}/almacenes/{idAlmacen}/disponible")
    @Operation(
            summary = "Consultar stock disponible por SKU y almacén",
            description = "Consulta disponibilidad para una cantidad solicitada usando IDs técnicos. No reserva ni descuenta stock."
    )
    public ResponseEntity<ApiResponseDto<StockDisponibleResponseDto>> consultarDisponiblePorIds(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Parameter(description = "Cantidad solicitada para validar disponibilidad.")
            @Min(value = 1, message = "La cantidad solicitada debe ser mayor a cero.")
            @RequestParam(defaultValue = "1") Integer cantidadSolicitada
    ) {
        return ResponseEntity.ok(
                stockService.consultarDisponible(
                        skuIdReference(idSku),
                        almacenIdReference(idAlmacen),
                        cantidadSolicitada
                )
        );
    }

    @GetMapping("/disponible")
    @Operation(
            summary = "Consultar stock disponible por referencias funcionales",
            description = "Consulta disponibilidad usando referencias funcionales de SKU y almacén. No registra reserva, movimiento ni kardex."
    )
    public ResponseEntity<ApiResponseDto<StockDisponibleResponseDto>> consultarDisponible(
            @Parameter(description = "ID técnico del SKU.")
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam(required = false) Long idSku,
            @Parameter(description = "Código funcional del SKU.")
            @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
            @RequestParam(required = false) String codigoSku,
            @Parameter(description = "Código de barras del SKU.")
            @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
            @RequestParam(required = false) String barcode,
            @Parameter(description = "ID técnico del almacén.")
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam(required = false) Long idAlmacen,
            @Parameter(description = "Código funcional del almacén.")
            @Size(max = 50, message = "El código del almacén no debe superar 50 caracteres.")
            @RequestParam(required = false) String codigoAlmacen,
            @Parameter(description = "Nombre del almacén.")
            @Size(max = 150, message = "El nombre del almacén no debe superar 150 caracteres.")
            @RequestParam(required = false) String nombreAlmacen,
            @Parameter(description = "Cantidad solicitada para validar disponibilidad.")
            @Min(value = 1, message = "La cantidad solicitada debe ser mayor a cero.")
            @RequestParam(defaultValue = "1") Integer cantidadSolicitada
    ) {
        return ResponseEntity.ok(
                stockService.consultarDisponible(
                        skuReference(idSku, codigoSku, barcode),
                        almacenReference(idAlmacen, codigoAlmacen, nombreAlmacen),
                        cantidadSolicitada
                )
        );
    }

    @GetMapping("/para-venta")
    @Operation(
            summary = "Consultar stock para venta",
            description = "Valida disponibilidad de stock para una posible venta. Este endpoint no crea venta, no reserva stock y no descuenta inventario."
    )
    public ResponseEntity<ApiResponseDto<StockDisponibleResponseDto>> consultarStockParaVenta(
            @Parameter(description = "ID técnico del SKU.")
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam(required = false) Long idSku,
            @Parameter(description = "Código funcional del SKU.")
            @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
            @RequestParam(required = false) String codigoSku,
            @Parameter(description = "Código de barras del SKU.")
            @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
            @RequestParam(required = false) String barcode,
            @Parameter(description = "ID técnico del almacén.")
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam(required = false) Long idAlmacen,
            @Parameter(description = "Código funcional del almacén.")
            @Size(max = 50, message = "El código del almacén no debe superar 50 caracteres.")
            @RequestParam(required = false) String codigoAlmacen,
            @Parameter(description = "Nombre del almacén.")
            @Size(max = 150, message = "El nombre del almacén no debe superar 150 caracteres.")
            @RequestParam(required = false) String nombreAlmacen,
            @Parameter(description = "Cantidad solicitada para venta.")
            @Min(value = 1, message = "La cantidad solicitada debe ser mayor a cero.")
            @RequestParam(defaultValue = "1") Integer cantidadSolicitada
    ) {
        return ResponseEntity.ok(
                stockService.consultarDisponible(
                        skuReference(idSku, codigoSku, barcode),
                        almacenReference(idAlmacen, codigoAlmacen, nombreAlmacen),
                        cantidadSolicitada
                )
        );
    }

    @GetMapping("/skus/{idSku}")
    @Operation(
            summary = "Listar stock por SKU",
            description = "Lista stock de un SKU en sus almacenes. No modifica stock ni calcula kardex en controller."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listarPorSku(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.listarPorSku(
                        skuIdReference(idSku),
                        pageRequest,
                        incluirCostos
                )
        );
    }

    @GetMapping("/por-sku")
    @Operation(
            summary = "Listar stock por referencia funcional de SKU",
            description = "Lista stock usando referencia funcional de SKU: id, codigoSku o barcode."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listarPorSkuReferencia(
            @Parameter(description = "ID técnico del SKU.")
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam(required = false) Long idSku,
            @Parameter(description = "Código funcional del SKU.")
            @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
            @RequestParam(required = false) String codigoSku,
            @Parameter(description = "Código de barras del SKU.")
            @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
            @RequestParam(required = false) String barcode,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.listarPorSku(
                        skuReference(idSku, codigoSku, barcode),
                        pageRequest,
                        incluirCostos
                )
        );
    }

    @GetMapping("/almacenes/{idAlmacen}")
    @Operation(
            summary = "Listar stock por almacén",
            description = "Lista stock de un almacén por ID técnico. No modifica stock ni almacenes."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listarPorAlmacen(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.listarPorAlmacen(
                        almacenIdReference(idAlmacen),
                        pageRequest,
                        incluirCostos
                )
        );
    }

    @GetMapping("/por-almacen")
    @Operation(
            summary = "Listar stock por referencia funcional de almacén",
            description = "Lista stock usando referencia funcional de almacén: id, código o nombre."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listarPorAlmacenReferencia(
            @Parameter(description = "ID técnico del almacén.")
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam(required = false) Long idAlmacen,
            @Parameter(description = "Código funcional del almacén.")
            @Size(max = 50, message = "El código del almacén no debe superar 50 caracteres.")
            @RequestParam(required = false) String codigoAlmacen,
            @Parameter(description = "Nombre del almacén.")
            @Size(max = 150, message = "El nombre del almacén no debe superar 150 caracteres.")
            @RequestParam(required = false) String nombreAlmacen,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                stockService.listarPorAlmacen(
                        almacenReference(idAlmacen, codigoAlmacen, nombreAlmacen),
                        pageRequest,
                        incluirCostos
                )
        );
    }

    @GetMapping("/bajo-stock")
    @Operation(
            summary = "Listar stock bajo mínimo",
            description = "Lista SKU/almacenes bajo stock mínimo. La consulta es paginada y la visibilidad de costos se valida en el service."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockSkuResponseDto>>> listarBajoStock(
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo actores autorizados pueden verlos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(stockService.listarBajoStock(pageRequest, incluirCostos));
    }

    private EntityReferenceDto skuIdReference(Long idSku) {
        return EntityReferenceDto.builder()
                .id(idSku)
                .build();
    }

    private EntityReferenceDto almacenIdReference(Long idAlmacen) {
        return EntityReferenceDto.builder()
                .id(idAlmacen)
                .build();
    }

    private EntityReferenceDto skuReference(Long idSku, String codigoSku, String barcode) {
        return EntityReferenceDto.builder()
                .id(idSku)
                .codigo(codigoSku)
                .codigoSku(codigoSku)
                .barcode(barcode)
                .build();
    }

    private EntityReferenceDto almacenReference(Long idAlmacen, String codigoAlmacen, String nombreAlmacen) {
        return EntityReferenceDto.builder()
                .id(idAlmacen)
                .codigo(codigoAlmacen)
                .codigoAlmacen(codigoAlmacen)
                .nombre(nombreAlmacen)
                .build();
    }
}