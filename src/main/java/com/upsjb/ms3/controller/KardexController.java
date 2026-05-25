// ruta: src/main/java/com/upsjb/ms3/controller/KardexController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.movimiento.filter.KardexFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.KardexResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.KardexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/ms3/inventario/kardex")
@Tag(
        name = "MS3 - Inventario - Kardex",
        description = "Endpoints protegidos para consultar kardex histórico de inventario."
)
public class KardexController {

    private final KardexService kardexService;

    @GetMapping
    @Operation(
            summary = "Consultar kardex",
            description = "Consulta kardex con filtros y paginación. El service valida autorización, permisos funcionales y visibilidad de costos."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultar(
            @Valid @ParameterObject @ModelAttribute KardexFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.consultar(filter, pageRequest, incluirCostos));
    }

    @GetMapping("/movimientos/{idMovimiento}")
    @Operation(
            summary = "Obtener movimiento de kardex por ID",
            description = "Obtiene el detalle histórico de un movimiento de inventario."
    )
    public ResponseEntity<ApiResponseDto<KardexResponseDto>> obtenerMovimiento(
            @Parameter(description = "ID técnico del movimiento.", required = true)
            @Positive(message = "El ID del movimiento debe ser positivo.")
            @PathVariable Long idMovimiento,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.obtenerMovimiento(idMovimiento, incluirCostos));
    }

    @GetMapping("/movimientos/codigo/{codigoMovimiento}")
    @Operation(
            summary = "Obtener movimiento de kardex por código",
            description = "Obtiene el detalle histórico de un movimiento usando su código funcional."
    )
    public ResponseEntity<ApiResponseDto<KardexResponseDto>> obtenerMovimientoPorCodigo(
            @Parameter(description = "Código funcional del movimiento.", required = true)
            @NotBlank(message = "El código de movimiento es obligatorio.")
            @Size(max = 100, message = "El código de movimiento no debe superar 100 caracteres.")
            @PathVariable String codigoMovimiento,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.obtenerMovimientoPorCodigo(codigoMovimiento, incluirCostos));
    }

    @GetMapping("/sku/{idSku}")
    @Operation(
            summary = "Consultar kardex por SKU",
            description = "Consulta movimientos históricos de un SKU por ID técnico."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorSku(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.consultarPorSku(idSku, pageRequest, incluirCostos));
    }

    @GetMapping("/almacenes/{idAlmacen}")
    @Operation(
            summary = "Consultar kardex por almacén",
            description = "Consulta movimientos históricos de un almacén por ID técnico."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorAlmacen(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.consultarPorAlmacen(idAlmacen, pageRequest, incluirCostos));
    }

    @GetMapping("/sku/{idSku}/almacenes/{idAlmacen}")
    @Operation(
            summary = "Consultar kardex por SKU y almacén",
            description = "Consulta movimientos históricos de un SKU en un almacén específico."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorSkuYAlmacen(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                kardexService.consultarPorSkuYAlmacen(
                        idReference(idSku),
                        idReference(idAlmacen),
                        pageRequest,
                        incluirCostos
                )
        );
    }

    @GetMapping("/por-sku")
    @Operation(
            summary = "Consultar kardex por referencia funcional de SKU",
            description = "Consulta kardex usando referencia funcional del SKU: id, código, códigoSku o barcode."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorSkuReferencia(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto skuReference,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(kardexService.consultarPorSkuReferencia(skuReference, pageRequest, incluirCostos));
    }

    @GetMapping("/por-almacen")
    @Operation(
            summary = "Consultar kardex por referencia funcional de almacén",
            description = "Consulta kardex usando referencia funcional del almacén: id, código, códigoAlmacen o nombre."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorAlmacenReferencia(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto almacenReference,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                kardexService.consultarPorAlmacenReferencia(almacenReference, pageRequest, incluirCostos)
        );
    }

    @GetMapping("/referencias")
    @Operation(
            summary = "Consultar kardex por referencia externa",
            description = "Consulta movimientos asociados a una referencia externa, por ejemplo una venta MS4, reserva, compra o ajuste."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<KardexResponseDto>>> consultarPorReferencia(
            @Parameter(description = "Tipo de referencia externa.", required = true)
            @NotBlank(message = "El tipo de referencia es obligatorio.")
            @Size(max = 50, message = "El tipo de referencia no debe superar 50 caracteres.")
            @RequestParam String referenciaTipo,
            @Parameter(description = "Identificador externo de la referencia.", required = true)
            @NotBlank(message = "El identificador externo de referencia es obligatorio.")
            @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
            @RequestParam String referenciaIdExterno,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo ADMIN puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                kardexService.consultarPorReferencia(
                        referenciaTipo,
                        referenciaIdExterno,
                        pageRequest,
                        incluirCostos
                )
        );
    }

    private EntityReferenceDto idReference(Long id) {
        return EntityReferenceDto.builder()
                .id(id)
                .build();
    }
}