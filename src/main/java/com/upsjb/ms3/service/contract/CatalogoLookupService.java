// ruta: src/main/java/com/upsjb/ms3/service/contract/CatalogoLookupService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.reference.filter.ReferenceSearchFilterDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.reference.response.EmpleadoInventarioOptionDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoSkuOptionDto;
import com.upsjb.ms3.dto.reference.response.PromocionOptionDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import java.util.List;

public interface CatalogoLookupService {

    ApiResponseDto<List<TipoProductoOptionDto>> buscarTiposProducto(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<CategoriaOptionDto>> buscarCategorias(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<MarcaOptionDto>> buscarMarcas(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<AtributoOptionDto>> buscarAtributos(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<ProductoOptionDto>> buscarProductos(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<ProductoSkuOptionDto>> buscarSkus(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<ProveedorOptionDto>> buscarProveedores(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<AlmacenOptionDto>> buscarAlmacenes(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<PromocionOptionDto>> buscarPromociones(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EmpleadoInventarioOptionDto>> buscarEmpleadosInventario(ReferenceSearchFilterDto filter);
}