// ruta: src/main/java/com/upsjb/ms3/mapper/CatalogoLookupMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.TipoProveedor;
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
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class CatalogoLookupMapper {

    public TipoProductoOptionDto toTipoProductoOption(TipoProducto entity) {
        if (entity == null) {
            return null;
        }

        return TipoProductoOptionDto.builder()
                .idTipoProducto(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }

    public CategoriaOptionDto toCategoriaOption(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return CategoriaOptionDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(entity.getCategoriaPadre() == null ? null : entity.getCategoriaPadre().getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .build();
    }

    public MarcaOptionDto toMarcaOption(Marca entity) {
        if (entity == null) {
            return null;
        }

        return MarcaOptionDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estado(entity.getEstado())
                .build();
    }

    public AtributoOptionDto toAtributoOption(Atributo entity) {
        if (entity == null) {
            return null;
        }

        return AtributoOptionDto.builder()
                .idAtributo(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .tipoDato(entity.getTipoDato())
                .tipoDatoLabel(entity.getTipoDato() == null ? null : entity.getTipoDato().getLabel())
                .unidadMedida(entity.getUnidadMedida())
                .requerido(entity.getRequerido())
                .filtrable(entity.getFiltrable())
                .visiblePublico(entity.getVisiblePublico())
                .estado(entity.getEstado())
                .build();
    }

    public ProductoOptionDto toProductoOption(Producto entity) {
        if (entity == null) {
            return null;
        }

        return ProductoOptionDto.builder()
                .idProducto(entity.getIdProducto())
                .codigoProducto(entity.getCodigoProducto())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estadoRegistro(entity.getEstadoRegistro())
                .estadoPublicacion(entity.getEstadoPublicacion())
                .estadoVenta(entity.getEstadoVenta())
                .visiblePublico(entity.getVisiblePublico())
                .vendible(entity.getVendible())
                .estado(entity.getEstado())
                .build();
    }

    public ProductoSkuOptionDto toProductoSkuOption(ProductoSku entity) {
        if (entity == null) {
            return null;
        }

        Producto producto = entity.getProducto();

        return ProductoSkuOptionDto.builder()
                .idSku(entity.getIdSku())
                .codigoSku(entity.getCodigoSku())
                .barcode(entity.getBarcode())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .color(entity.getColor())
                .talla(entity.getTalla())
                .material(entity.getMaterial())
                .modelo(entity.getModelo())
                .estadoSku(entity.getEstadoSku())
                .estado(entity.getEstado())
                .build();
    }

    public ProveedorOptionDto toProveedorOption(Proveedor entity) {
        if (entity == null) {
            return null;
        }

        return ProveedorOptionDto.builder()
                .idProveedor(entity.getIdProveedor())
                .tipoProveedor(entity.getTipoProveedor())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .ruc(entity.getRuc())
                .razonSocial(entity.getRazonSocial())
                .nombreComercial(entity.getNombreComercial())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .displayName(proveedorDisplayName(entity))
                .estado(entity.getEstado())
                .build();
    }

    public AlmacenOptionDto toAlmacenOption(Almacen entity) {
        if (entity == null) {
            return null;
        }

        return AlmacenOptionDto.builder()
                .idAlmacen(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .principal(entity.getPrincipal())
                .permiteVenta(entity.getPermiteVenta())
                .permiteCompra(entity.getPermiteCompra())
                .estado(entity.getEstado())
                .build();
    }

    public PromocionOptionDto toPromocionOption(Promocion entity) {
        if (entity == null) {
            return null;
        }

        return PromocionOptionDto.builder()
                .idPromocion(entity.getIdPromocion())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }

    public EmpleadoInventarioOptionDto toEmpleadoOption(EmpleadoSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return EmpleadoInventarioOptionDto.builder()
                .idEmpleadoSnapshot(entity.getIdEmpleadoSnapshot())
                .idEmpleadoMs2(entity.getIdEmpleadoMs2())
                .idUsuarioMs1(entity.getIdUsuarioMs1())
                .codigoEmpleado(entity.getCodigoEmpleado())
                .nombresCompletos(entity.getNombresCompletos())
                .areaCodigo(entity.getAreaCodigo())
                .areaNombre(entity.getAreaNombre())
                .empleadoActivo(entity.getEmpleadoActivo())
                .estado(entity.getEstado())
                .build();
    }

    private String proveedorDisplayName(Proveedor entity) {
        if (entity == null) {
            return null;
        }

        if (TipoProveedor.EMPRESA.equals(entity.getTipoProveedor())) {
            if (StringNormalizer.hasText(entity.getNombreComercial())) {
                return entity.getNombreComercial();
            }
            return entity.getRazonSocial();
        }

        String nombres = entity.getNombres() == null ? "" : entity.getNombres().trim();
        String apellidos = entity.getApellidos() == null ? "" : entity.getApellidos().trim();
        String fullName = (nombres + " " + apellidos).trim();

        return StringNormalizer.hasText(fullName) ? fullName : entity.getNumeroDocumento();
    }
}