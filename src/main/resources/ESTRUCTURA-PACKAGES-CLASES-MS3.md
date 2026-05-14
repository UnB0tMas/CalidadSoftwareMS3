# Estructura definitiva de packages y clases - MS3: ms-catalogo-inventario

```text
ms-catalogo-inventario
└── src
    └── main
        └── java
            └── com
                └── upsjb
                    └── ms3
                        ├── Ms3Application.java
                        │
                        ├── config
                        │   ├── AppPropertiesConfig.java
                        │   ├── CloudinaryProperties.java
                        │   ├── CloudinaryClientConfig.java
                        │   ├── KafkaTopicProperties.java
                        │   ├── OutboxProperties.java
                        │   ├── Ms2IntegrationProperties.java
                        │   └── Ms4IntegrationProperties.java
                        │
                        ├── security
                        │   ├── config
                        │   │   ├── SecurityConfig.java
                        │   │   └── ResourceServerConfig.java
                        │   │
                        │   ├── jwt
                        │   │   ├── JwtClaimNames.java
                        │   │   └── RoleJwtAuthenticationConverter.java
                        │   │
                        │   ├── principal
                        │   │   ├── AuthenticatedUserContext.java
                        │   │   ├── CurrentUserResolver.java
                        │   │   └── AuthenticatedUserArgumentResolver.java
                        │   │
                        │   ├── roles
                        │   │   └── SecurityRoles.java
                        │   │
                        │   ├── filter
                        │   │   ├── RequestTraceFilter.java
                        │   │   └── RequestAuditContextFilter.java
                        │   │
                        │   └── handler
                        │       ├── RestAuthenticationEntryPoint.java
                        │       ├── RestAccessDeniedHandler.java
                        │       └── SecurityExceptionHandler.java
                        │
                        ├── controller
                        │   ├── PublicCatalogoController.java
                        │   ├── PublicProductoController.java
                        │   ├── PublicPromocionController.java
                        │   ├── ReferenceDataController.java
                        │   ├── CatalogoLookupController.java
                        │   ├── TipoProductoController.java
                        │   ├── CategoriaController.java
                        │   ├── MarcaController.java
                        │   ├── AtributoController.java
                        │   ├── ProductoAdminController.java
                        │   ├── ProductoSkuController.java
                        │   ├── ProductoImagenController.java
                        │   ├── PrecioSkuController.java
                        │   ├── PromocionController.java
                        │   ├── ProveedorController.java
                        │   ├── AlmacenController.java
                        │   ├── StockController.java
                        │   ├── CompraInventarioController.java
                        │   ├── ReservaStockController.java
                        │   ├── MovimientoInventarioController.java
                        │   ├── KardexController.java
                        │   ├── EmpleadoInventarioPermisoController.java
                        │   ├── Ms4StockSyncController.java
                        │   ├── AuditoriaController.java
                        │   └── OutboxController.java
                        │
                        ├── service
                        │   ├── contract
                        │   │   ├── ReferenceDataService.java
                        │   │   ├── CatalogoLookupService.java
                        │   │   ├── EntityReferenceService.java
                        │   │   ├── CodigoGeneradorService.java
                        │   │   ├── SlugGeneratorService.java
                        │   │   ├── TipoProductoService.java
                        │   │   ├── CategoriaService.java
                        │   │   ├── MarcaService.java
                        │   │   ├── AtributoService.java
                        │   │   ├── TipoProductoAtributoService.java
                        │   │   ├── ProductoAdminService.java
                        │   │   ├── ProductoPublicService.java
                        │   │   ├── ProductoSkuService.java
                        │   │   ├── ProductoAtributoValorService.java
                        │   │   ├── SkuAtributoValorService.java
                        │   │   ├── ProductoImagenService.java
                        │   │   ├── CloudinaryService.java
                        │   │   ├── PrecioSkuService.java
                        │   │   ├── PromocionService.java
                        │   │   ├── PromocionVersionService.java
                        │   │   ├── PromocionSkuDescuentoService.java
                        │   │   ├── ProveedorService.java
                        │   │   ├── AlmacenService.java
                        │   │   ├── StockService.java
                        │   │   ├── CompraInventarioService.java
                        │   │   ├── ReservaStockService.java
                        │   │   ├── MovimientoInventarioService.java
                        │   │   ├── KardexService.java
                        │   │   ├── EmpleadoSnapshotMs2Service.java
                        │   │   ├── EmpleadoInventarioPermisoService.java
                        │   │   ├── AuditoriaFuncionalService.java
                        │   │   ├── EventoDominioOutboxService.java
                        │   │   ├── KafkaPublisherService.java
                        │   │   └── Ms4ReconciliacionService.java
                        │   │
                        │   └── impl
                        │       ├── ReferenceDataServiceImpl.java
                        │       ├── CatalogoLookupServiceImpl.java
                        │       ├── EntityReferenceServiceImpl.java
                        │       ├── CodigoGeneradorServiceImpl.java
                        │       ├── SlugGeneratorServiceImpl.java
                        │       ├── TipoProductoServiceImpl.java
                        │       ├── CategoriaServiceImpl.java
                        │       ├── MarcaServiceImpl.java
                        │       ├── AtributoServiceImpl.java
                        │       ├── TipoProductoAtributoServiceImpl.java
                        │       ├── ProductoAdminServiceImpl.java
                        │       ├── ProductoPublicServiceImpl.java
                        │       ├── ProductoSkuServiceImpl.java
                        │       ├── ProductoAtributoValorServiceImpl.java
                        │       ├── SkuAtributoValorServiceImpl.java
                        │       ├── ProductoImagenServiceImpl.java
                        │       ├── CloudinaryServiceImpl.java
                        │       ├── PrecioSkuServiceImpl.java
                        │       ├── PromocionServiceImpl.java
                        │       ├── PromocionVersionServiceImpl.java
                        │       ├── PromocionSkuDescuentoServiceImpl.java
                        │       ├── ProveedorServiceImpl.java
                        │       ├── AlmacenServiceImpl.java
                        │       ├── StockServiceImpl.java
                        │       ├── CompraInventarioServiceImpl.java
                        │       ├── ReservaStockServiceImpl.java
                        │       ├── MovimientoInventarioServiceImpl.java
                        │       ├── KardexServiceImpl.java
                        │       ├── EmpleadoSnapshotMs2ServiceImpl.java
                        │       ├── EmpleadoInventarioPermisoServiceImpl.java
                        │       ├── AuditoriaFuncionalServiceImpl.java
                        │       ├── EventoDominioOutboxServiceImpl.java
                        │       ├── KafkaPublisherServiceImpl.java
                        │       └── Ms4ReconciliacionServiceImpl.java
                        │
                        ├── domain
                        │   ├── entity
                        │   │   ├── AuditableEntity.java
                        │   │   ├── CorrelativoCodigo.java
                        │   │   ├── TipoProducto.java
                        │   │   ├── Categoria.java
                        │   │   ├── Marca.java
                        │   │   ├── Atributo.java
                        │   │   ├── TipoProductoAtributo.java
                        │   │   ├── Producto.java
                        │   │   ├── ProductoSku.java
                        │   │   ├── ProductoAtributoValor.java
                        │   │   ├── SkuAtributoValor.java
                        │   │   ├── ProductoImagenCloudinary.java
                        │   │   ├── Proveedor.java
                        │   │   ├── Almacen.java
                        │   │   ├── StockSku.java
                        │   │   ├── EmpleadoSnapshotMs2.java
                        │   │   ├── EmpleadoInventarioPermisoHistorial.java
                        │   │   ├── PrecioSkuHistorial.java
                        │   │   ├── Promocion.java
                        │   │   ├── PromocionVersion.java
                        │   │   ├── PromocionSkuDescuentoVersion.java
                        │   │   ├── CompraInventario.java
                        │   │   ├── CompraInventarioDetalle.java
                        │   │   ├── ReservaStock.java
                        │   │   ├── MovimientoInventario.java
                        │   │   ├── AuditoriaFuncional.java
                        │   │   └── EventoDominioOutbox.java
                        │   │
                        │   ├── enums
                        │   │   ├── EstadoRegistro.java
                        │   │   ├── EstadoProductoRegistro.java
                        │   │   ├── EstadoProductoPublicacion.java
                        │   │   ├── EstadoProductoVenta.java
                        │   │   ├── EstadoSku.java
                        │   │   ├── GeneroObjetivo.java
                        │   │   ├── TipoDatoAtributo.java
                        │   │   ├── TipoProveedor.java
                        │   │   ├── TipoDocumentoProveedor.java
                        │   │   ├── Moneda.java
                        │   │   ├── EstadoCompraInventario.java
                        │   │   ├── EstadoReservaStock.java
                        │   │   ├── TipoReferenciaStock.java
                        │   │   ├── TipoMovimientoInventario.java
                        │   │   ├── EstadoMovimientoInventario.java
                        │   │   ├── MotivoMovimientoInventario.java
                        │   │   ├── EstadoPromocion.java
                        │   │   ├── TipoDescuento.java
                        │   │   ├── CloudinaryResourceType.java
                        │   │   ├── RolSistema.java
                        │   │   ├── TipoEventoAuditoria.java
                        │   │   ├── EntidadAuditada.java
                        │   │   ├── ResultadoAuditoria.java
                        │   │   ├── EstadoPublicacionEvento.java
                        │   │   ├── AggregateType.java
                        │   │   ├── ProductoEventType.java
                        │   │   ├── StockEventType.java
                        │   │   ├── PrecioEventType.java
                        │   │   ├── PromocionEventType.java
                        │   │   └── Ms4StockEventType.java
                        │   │
                        │   └── value
                        │       ├── CodigoGeneradoValue.java
                        │       ├── SlugValue.java
                        │       ├── MoneyValue.java
                        │       ├── StockValue.java
                        │       ├── PorcentajeValue.java
                        │       ├── DocumentoProveedorValue.java
                        │       ├── RucValue.java
                        │       ├── CloudinaryPublicIdValue.java
                        │       └── NombreNormalizadoValue.java
                        │
                        ├── repository
                        │   ├── CorrelativoCodigoRepository.java
                        │   ├── TipoProductoRepository.java
                        │   ├── CategoriaRepository.java
                        │   ├── MarcaRepository.java
                        │   ├── AtributoRepository.java
                        │   ├── TipoProductoAtributoRepository.java
                        │   ├── ProductoRepository.java
                        │   ├── ProductoSkuRepository.java
                        │   ├── ProductoAtributoValorRepository.java
                        │   ├── SkuAtributoValorRepository.java
                        │   ├── ProductoImagenCloudinaryRepository.java
                        │   ├── ProveedorRepository.java
                        │   ├── AlmacenRepository.java
                        │   ├── StockSkuRepository.java
                        │   ├── EmpleadoSnapshotMs2Repository.java
                        │   ├── EmpleadoInventarioPermisoHistorialRepository.java
                        │   ├── PrecioSkuHistorialRepository.java
                        │   ├── PromocionRepository.java
                        │   ├── PromocionVersionRepository.java
                        │   ├── PromocionSkuDescuentoVersionRepository.java
                        │   ├── CompraInventarioRepository.java
                        │   ├── CompraInventarioDetalleRepository.java
                        │   ├── ReservaStockRepository.java
                        │   ├── MovimientoInventarioRepository.java
                        │   ├── AuditoriaFuncionalRepository.java
                        │   └── EventoDominioOutboxRepository.java
                        │
                        ├── dto
                        │   ├── shared
                        │   │   ├── ApiResponseDto.java
                        │   │   ├── ErrorResponseDto.java
                        │   │   ├── FieldErrorDto.java
                        │   │   ├── PageRequestDto.java
                        │   │   ├── PageResponseDto.java
                        │   │   ├── SelectOptionDto.java
                        │   │   ├── IdCodigoNombreResponseDto.java
                        │   │   ├── EntityReferenceDto.java
                        │   │   ├── EstadoChangeRequestDto.java
                        │   │   ├── MotivoRequestDto.java
                        │   │   ├── DateRangeFilterDto.java
                        │   │   ├── MoneyResponseDto.java
                        │   │   └── StockResumenResponseDto.java
                        │   │
                        │   ├── reference
                        │   │   ├── request
                        │   │   │   └── EntityReferenceRequestDto.java
                        │   │   ├── response
                        │   │   │   ├── TipoProductoOptionDto.java
                        │   │   │   ├── CategoriaOptionDto.java
                        │   │   │   ├── MarcaOptionDto.java
                        │   │   │   ├── AtributoOptionDto.java
                        │   │   │   ├── ProductoOptionDto.java
                        │   │   │   ├── ProductoSkuOptionDto.java
                        │   │   │   ├── ProveedorOptionDto.java
                        │   │   │   ├── AlmacenOptionDto.java
                        │   │   │   ├── PromocionOptionDto.java
                        │   │   │   └── EmpleadoInventarioOptionDto.java
                        │   │   └── filter
                        │   │       └── ReferenceSearchFilterDto.java
                        │   │
                        │   ├── catalogo
                        │   │   ├── tipoproducto
                        │   │   │   ├── request
                        │   │   │   │   ├── TipoProductoCreateRequestDto.java
                        │   │   │   │   └── TipoProductoUpdateRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── TipoProductoResponseDto.java
                        │   │   │   │   └── TipoProductoDetailResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── TipoProductoFilterDto.java
                        │   │   │
                        │   │   ├── categoria
                        │   │   │   ├── request
                        │   │   │   │   ├── CategoriaCreateRequestDto.java
                        │   │   │   │   └── CategoriaUpdateRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── CategoriaResponseDto.java
                        │   │   │   │   ├── CategoriaDetailResponseDto.java
                        │   │   │   │   └── CategoriaTreeResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── CategoriaFilterDto.java
                        │   │   │
                        │   │   ├── marca
                        │   │   │   ├── request
                        │   │   │   │   ├── MarcaCreateRequestDto.java
                        │   │   │   │   └── MarcaUpdateRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── MarcaResponseDto.java
                        │   │   │   │   └── MarcaDetailResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── MarcaFilterDto.java
                        │   │   │
                        │   │   ├── atributo
                        │   │   │   ├── request
                        │   │   │   │   ├── AtributoCreateRequestDto.java
                        │   │   │   │   ├── AtributoUpdateRequestDto.java
                        │   │   │   │   └── TipoProductoAtributoAssignRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── AtributoResponseDto.java
                        │   │   │   │   ├── AtributoDetailResponseDto.java
                        │   │   │   │   └── TipoProductoAtributoResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── AtributoFilterDto.java
                        │   │   │
                        │   │   └── producto
                        │   │       ├── request
                        │   │       │   ├── ProductoCreateRequestDto.java
                        │   │       │   ├── ProductoUpdateRequestDto.java
                        │   │       │   ├── ProductoEstadoRegistroRequestDto.java
                        │   │       │   ├── ProductoPublicacionRequestDto.java
                        │   │       │   ├── ProductoVentaEstadoRequestDto.java
                        │   │       │   ├── ProductoAtributoValorRequestDto.java
                        │   │       │   ├── ProductoSkuCreateRequestDto.java
                        │   │       │   ├── ProductoSkuUpdateRequestDto.java
                        │   │       │   ├── SkuAtributoValorRequestDto.java
                        │   │       │   ├── ProductoImagenUploadRequestDto.java
                        │   │       │   ├── ProductoImagenUpdateRequestDto.java
                        │   │       │   └── ProductoImagenPrincipalRequestDto.java
                        │   │       ├── response
                        │   │       │   ├── ProductoResponseDto.java
                        │   │       │   ├── ProductoDetailResponseDto.java
                        │   │       │   ├── ProductoPublicResponseDto.java
                        │   │       │   ├── ProductoPublicDetailResponseDto.java
                        │   │       │   ├── ProductoSkuResponseDto.java
                        │   │       │   ├── ProductoSkuDetailResponseDto.java
                        │   │       │   ├── ProductoAtributoValorResponseDto.java
                        │   │       │   ├── SkuAtributoValorResponseDto.java
                        │   │       │   ├── ProductoImagenResponseDto.java
                        │   │       │   ├── ProductoCatalogoCardResponseDto.java
                        │   │       │   └── ProductoSnapshotResponseDto.java
                        │   │       └── filter
                        │   │           ├── ProductoFilterDto.java
                        │   │           ├── ProductoPublicFilterDto.java
                        │   │           └── ProductoSkuFilterDto.java
                        │   │
                        │   ├── precio
                        │   │   ├── request
                        │   │   │   └── PrecioSkuCreateRequestDto.java
                        │   │   ├── response
                        │   │   │   ├── PrecioSkuResponseDto.java
                        │   │   │   └── PrecioSkuHistorialResponseDto.java
                        │   │   └── filter
                        │   │       └── PrecioSkuFilterDto.java
                        │   │
                        │   ├── promocion
                        │   │   ├── request
                        │   │   │   ├── PromocionCreateRequestDto.java
                        │   │   │   ├── PromocionUpdateRequestDto.java
                        │   │   │   ├── PromocionVersionCreateRequestDto.java
                        │   │   │   ├── PromocionVersionEstadoRequestDto.java
                        │   │   │   ├── PromocionSkuDescuentoCreateRequestDto.java
                        │   │   │   └── PromocionSkuDescuentoUpdateRequestDto.java
                        │   │   ├── response
                        │   │   │   ├── PromocionResponseDto.java
                        │   │   │   ├── PromocionDetailResponseDto.java
                        │   │   │   ├── PromocionVersionResponseDto.java
                        │   │   │   ├── PromocionSkuDescuentoResponseDto.java
                        │   │   │   ├── PromocionPublicResponseDto.java
                        │   │   │   └── PromocionSnapshotResponseDto.java
                        │   │   └── filter
                        │   │       ├── PromocionFilterDto.java
                        │   │       └── PromocionVersionFilterDto.java
                        │   │
                        │   ├── proveedor
                        │   │   ├── request
                        │   │   │   ├── ProveedorCreateRequestDto.java
                        │   │   │   ├── ProveedorUpdateRequestDto.java
                        │   │   │   └── ProveedorEstadoRequestDto.java
                        │   │   ├── response
                        │   │   │   ├── ProveedorResponseDto.java
                        │   │   │   └── ProveedorDetailResponseDto.java
                        │   │   └── filter
                        │   │       └── ProveedorFilterDto.java
                        │   │
                        │   ├── inventario
                        │   │   ├── almacen
                        │   │   │   ├── request
                        │   │   │   │   ├── AlmacenCreateRequestDto.java
                        │   │   │   │   ├── AlmacenUpdateRequestDto.java
                        │   │   │   │   └── AlmacenEstadoRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── AlmacenResponseDto.java
                        │   │   │   │   └── AlmacenDetailResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── AlmacenFilterDto.java
                        │   │   │
                        │   │   ├── stock
                        │   │   │   ├── response
                        │   │   │   │   ├── StockSkuResponseDto.java
                        │   │   │   │   ├── StockSkuDetailResponseDto.java
                        │   │   │   │   └── StockDisponibleResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── StockSkuFilterDto.java
                        │   │   │
                        │   │   ├── compra
                        │   │   │   ├── request
                        │   │   │   │   ├── CompraInventarioCreateRequestDto.java
                        │   │   │   │   ├── CompraInventarioDetalleRequestDto.java
                        │   │   │   │   ├── CompraInventarioUpdateRequestDto.java
                        │   │   │   │   ├── CompraInventarioConfirmRequestDto.java
                        │   │   │   │   └── CompraInventarioAnularRequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   ├── CompraInventarioResponseDto.java
                        │   │   │   │   ├── CompraInventarioDetailResponseDto.java
                        │   │   │   │   └── CompraInventarioDetalleResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── CompraInventarioFilterDto.java
                        │   │   │
                        │   │   ├── reserva
                        │   │   │   ├── request
                        │   │   │   │   ├── ReservaStockCreateRequestDto.java
                        │   │   │   │   ├── ReservaStockConfirmRequestDto.java
                        │   │   │   │   ├── ReservaStockLiberarRequestDto.java
                        │   │   │   │   └── ReservaStockMs4RequestDto.java
                        │   │   │   ├── response
                        │   │   │   │   └── ReservaStockResponseDto.java
                        │   │   │   └── filter
                        │   │   │       └── ReservaStockFilterDto.java
                        │   │   │
                        │   │   └── movimiento
                        │   │       ├── request
                        │   │       │   ├── EntradaInventarioRequestDto.java
                        │   │       │   ├── SalidaInventarioRequestDto.java
                        │   │       │   ├── AjusteInventarioRequestDto.java
                        │   │       │   └── MovimientoCompensatorioRequestDto.java
                        │   │       ├── response
                        │   │       │   ├── MovimientoInventarioResponseDto.java
                        │   │       │   └── KardexResponseDto.java
                        │   │       └── filter
                        │   │           ├── MovimientoInventarioFilterDto.java
                        │   │           └── KardexFilterDto.java
                        │   │
                        │   ├── empleado
                        │   │   ├── request
                        │   │   │   ├── EmpleadoSnapshotMs2UpsertRequestDto.java
                        │   │   │   ├── EmpleadoInventarioPermisoUpdateRequestDto.java
                        │   │   │   └── EmpleadoInventarioPermisoRevokeRequestDto.java
                        │   │   ├── response
                        │   │   │   ├── EmpleadoSnapshotMs2ResponseDto.java
                        │   │   │   └── EmpleadoInventarioPermisoResponseDto.java
                        │   │   └── filter
                        │   │       └── EmpleadoInventarioPermisoFilterDto.java
                        │   │
                        │   ├── auditoria
                        │   │   ├── response
                        │   │   │   └── AuditoriaFuncionalResponseDto.java
                        │   │   └── filter
                        │   │       └── AuditoriaFuncionalFilterDto.java
                        │   │
                        │   ├── outbox
                        │   │   ├── request
                        │   │   │   └── OutboxRetryRequestDto.java
                        │   │   ├── response
                        │   │   │   └── EventoDominioOutboxResponseDto.java
                        │   │   └── filter
                        │   │       └── EventoDominioOutboxFilterDto.java
                        │   │
                        │   └── ms4
                        │       ├── request
                        │       │   ├── Ms4VentaStockReservadoEventDto.java
                        │       │   ├── Ms4VentaStockConfirmadoEventDto.java
                        │       │   ├── Ms4VentaStockLiberadoEventDto.java
                        │       │   └── Ms4VentaAnuladaStockEventDto.java
                        │       └── response
                        │           └── Ms4StockSyncResultDto.java
                        │
                        ├── mapper
                        │   ├── ReferenceMapper.java
                        │   ├── TipoProductoMapper.java
                        │   ├── CategoriaMapper.java
                        │   ├── MarcaMapper.java
                        │   ├── AtributoMapper.java
                        │   ├── TipoProductoAtributoMapper.java
                        │   ├── ProductoMapper.java
                        │   ├── ProductoSkuMapper.java
                        │   ├── ProductoAtributoValorMapper.java
                        │   ├── SkuAtributoValorMapper.java
                        │   ├── ProductoImagenMapper.java
                        │   ├── PrecioSkuMapper.java
                        │   ├── PromocionMapper.java
                        │   ├── PromocionVersionMapper.java
                        │   ├── PromocionSkuDescuentoMapper.java
                        │   ├── ProveedorMapper.java
                        │   ├── AlmacenMapper.java
                        │   ├── StockSkuMapper.java
                        │   ├── CompraInventarioMapper.java
                        │   ├── CompraInventarioDetalleMapper.java
                        │   ├── ReservaStockMapper.java
                        │   ├── MovimientoInventarioMapper.java
                        │   ├── KardexMapper.java
                        │   ├── EmpleadoSnapshotMs2Mapper.java
                        │   ├── EmpleadoInventarioPermisoMapper.java
                        │   ├── AuditoriaFuncionalMapper.java
                        │   ├── EventoDominioOutboxMapper.java
                        │   └── Ms4StockEventMapper.java
                        │
                        ├── validator
                        │   ├── TipoProductoValidator.java
                        │   ├── CategoriaValidator.java
                        │   ├── MarcaValidator.java
                        │   ├── AtributoValidator.java
                        │   ├── TipoProductoAtributoValidator.java
                        │   ├── ProductoValidator.java
                        │   ├── ProductoSkuValidator.java
                        │   ├── ProductoPublicacionValidator.java
                        │   ├── ProductoImagenValidator.java
                        │   ├── PrecioSkuValidator.java
                        │   ├── PromocionValidator.java
                        │   ├── PromocionVersionValidator.java
                        │   ├── PromocionSkuDescuentoValidator.java
                        │   ├── ProveedorValidator.java
                        │   ├── AlmacenValidator.java
                        │   ├── StockValidator.java
                        │   ├── CompraInventarioValidator.java
                        │   ├── ReservaStockValidator.java
                        │   ├── MovimientoInventarioValidator.java
                        │   ├── KardexValidator.java
                        │   ├── EmpleadoSnapshotMs2Validator.java
                        │   ├── EmpleadoInventarioPermisoValidator.java
                        │   ├── EventoDominioOutboxValidator.java
                        │   ├── Ms4StockEventValidator.java
                        │   └── CloudinaryImageValidator.java
                        │
                        ├── policy
                        │   ├── TipoProductoPolicy.java
                        │   ├── CategoriaPolicy.java
                        │   ├── MarcaPolicy.java
                        │   ├── AtributoPolicy.java
                        │   ├── ProductoPolicy.java
                        │   ├── ProductoSkuPolicy.java
                        │   ├── ProductoImagenPolicy.java
                        │   ├── PrecioSkuPolicy.java
                        │   ├── PromocionPolicy.java
                        │   ├── ProveedorPolicy.java
                        │   ├── AlmacenPolicy.java
                        │   ├── StockPolicy.java
                        │   ├── CompraInventarioPolicy.java
                        │   ├── ReservaStockPolicy.java
                        │   ├── MovimientoInventarioPolicy.java
                        │   ├── KardexPolicy.java
                        │   ├── EmpleadoInventarioPermisoPolicy.java
                        │   ├── AuditoriaPolicy.java
                        │   ├── OutboxPolicy.java
                        │   ├── CloudinaryPolicy.java
                        │   └── Ms4SyncPolicy.java
                        │
                        ├── specification
                        │   ├── TipoProductoSpecifications.java
                        │   ├── CategoriaSpecifications.java
                        │   ├── MarcaSpecifications.java
                        │   ├── AtributoSpecifications.java
                        │   ├── ProductoSpecifications.java
                        │   ├── ProductoPublicSpecifications.java
                        │   ├── ProductoSkuSpecifications.java
                        │   ├── ProductoImagenSpecifications.java
                        │   ├── PrecioSkuSpecifications.java
                        │   ├── PromocionSpecifications.java
                        │   ├── PromocionVersionSpecifications.java
                        │   ├── PromocionSkuDescuentoSpecifications.java
                        │   ├── ProveedorSpecifications.java
                        │   ├── AlmacenSpecifications.java
                        │   ├── StockSkuSpecifications.java
                        │   ├── CompraInventarioSpecifications.java
                        │   ├── ReservaStockSpecifications.java
                        │   ├── MovimientoInventarioSpecifications.java
                        │   ├── KardexSpecifications.java
                        │   ├── EmpleadoSnapshotMs2Specifications.java
                        │   ├── EmpleadoInventarioPermisoSpecifications.java
                        │   ├── AuditoriaFuncionalSpecifications.java
                        │   └── EventoDominioOutboxSpecifications.java
                        │
                        ├── integration
                        │   ├── cloudinary
                        │   │   ├── CloudinaryClient.java
                        │   │   ├── CloudinaryClientImpl.java
                        │   │   ├── CloudinaryUploadRequest.java
                        │   │   ├── CloudinaryUploadResponse.java
                        │   │   ├── CloudinaryDeleteRequest.java
                        │   │   ├── CloudinaryDeleteResponse.java
                        │   │   ├── CloudinaryException.java
                        │   │   └── CloudinaryErrorMapper.java
                        │   │
                        │   ├── ms2
                        │   │   ├── Ms2EmpleadoSnapshotClient.java
                        │   │   ├── Ms2EmpleadoSnapshotClientImpl.java
                        │   │   ├── Ms2ClientException.java
                        │   │   └── Ms2ClientErrorMapper.java
                        │   │
                        │   └── ms4
                        │       ├── Ms4StockSyncClient.java
                        │       ├── Ms4StockSyncClientImpl.java
                        │       ├── Ms4ClientException.java
                        │       └── Ms4ClientErrorMapper.java
                        │
                        ├── kafka
                        │   ├── event
                        │   │   ├── DomainEventEnvelope.java
                        │   │   ├── ProductoSnapshotEvent.java
                        │   │   ├── ProductoSnapshotPayload.java
                        │   │   ├── ProductoSkuSnapshotPayload.java
                        │   │   ├── ProductoImagenSnapshotPayload.java
                        │   │   ├── PrecioSnapshotEvent.java
                        │   │   ├── PrecioSnapshotPayload.java
                        │   │   ├── PromocionSnapshotEvent.java
                        │   │   ├── PromocionSnapshotPayload.java
                        │   │   ├── PromocionSkuDescuentoPayload.java
                        │   │   ├── StockSnapshotEvent.java
                        │   │   ├── StockSnapshotPayload.java
                        │   │   ├── MovimientoInventarioEvent.java
                        │   │   ├── MovimientoInventarioPayload.java
                        │   │   ├── Ms4StockCommandEvent.java
                        │   │   └── Ms4StockCommandPayload.java
                        │   │
                        │   ├── outbox
                        │   │   ├── OutboxEventFactory.java
                        │   │   ├── OutboxEventSerializer.java
                        │   │   ├── OutboxEventPublisher.java
                        │   │   ├── OutboxScheduler.java
                        │   │   ├── OutboxRetryPolicy.java
                        │   │   ├── OutboxPublishResult.java
                        │   │   └── OutboxLockService.java
                        │   │
                        │   ├── producer
                        │   │   ├── KafkaDomainEventPublisher.java
                        │   │   ├── KafkaEventKeyResolver.java
                        │   │   └── KafkaTopicResolver.java
                        │   │
                        │   └── consumer
                        │       ├── Ms4StockCommandConsumer.java
                        │       ├── Ms4StockCommandHandler.java
                        │       ├── KafkaConsumerErrorHandler.java
                        │       └── KafkaIdempotencyGuard.java
                        │
                        ├── shared
                        │   ├── audit
                        │   │   ├── AuditContext.java
                        │   │   ├── AuditContextHolder.java
                        │   │   ├── AuditEventFactory.java
                        │   │   ├── AuditMetadataBuilder.java
                        │   │   └── AuditResult.java
                        │   │
                        │   ├── constants
                        │   │   ├── ApiPaths.java
                        │   │   ├── HeaderNames.java
                        │   │   ├── SystemActors.java
                        │   │   ├── Ms3Constants.java
                        │   │   └── TopicNames.java
                        │   │
                        │   ├── exception
                        │   │   ├── BusinessException.java
                        │   │   ├── ValidationException.java
                        │   │   ├── NotFoundException.java
                        │   │   ├── ConflictException.java
                        │   │   ├── ForbiddenException.java
                        │   │   ├── UnauthorizedException.java
                        │   │   ├── ExternalServiceException.java
                        │   │   ├── CloudinaryIntegrationException.java
                        │   │   ├── KafkaPublishException.java
                        │   │   └── GlobalExceptionHandler.java
                        │   │
                        │   ├── pagination
                        │   │   ├── PaginationService.java
                        │   │   ├── PaginationMapper.java
                        │   │   ├── SortNormalizer.java
                        │   │   └── SortFieldValidator.java
                        │   │
                        │   ├── persistence
                        │   │   ├── EntityLookupService.java
                        │   │   ├── EntityStateValidator.java
                        │   │   ├── ActiveRecordResolver.java
                        │   │   └── SoftDeleteSupport.java
                        │   │
                        │   ├── reference
                        │   │   ├── EntityDisplayResolver.java
                        │   │   ├── ReferenceOptionMapper.java
                        │   │   ├── TipoProductoReferenceResolver.java
                        │   │   ├── CategoriaReferenceResolver.java
                        │   │   ├── MarcaReferenceResolver.java
                        │   │   ├── AtributoReferenceResolver.java
                        │   │   ├── ProductoReferenceResolver.java
                        │   │   ├── ProductoSkuReferenceResolver.java
                        │   │   ├── ProveedorReferenceResolver.java
                        │   │   ├── AlmacenReferenceResolver.java
                        │   │   ├── PromocionReferenceResolver.java
                        │   │   └── EmpleadoInventarioReferenceResolver.java
                        │   │
                        │   ├── response
                        │   │   ├── ApiResponseFactory.java
                        │   │   ├── ErrorResponseFactory.java
                        │   │   └── ApiErrorCode.java
                        │   │
                        │   ├── specification
                        │   │   ├── SpecificationBuilder.java
                        │   │   ├── SpecificationUtils.java
                        │   │   ├── DateRangeCriteria.java
                        │   │   ├── NumericRangeCriteria.java
                        │   │   └── BooleanCriteria.java
                        │   │
                        │   ├── validation
                        │   │   ├── ValidationErrorCollector.java
                        │   │   ├── BusinessRuleValidator.java
                        │   │   └── RequiredFieldValidator.java
                        │   │
                        │   ├── code
                        │   │   ├── CodigoGenerator.java
                        │   │   ├── CodigoFormat.java
                        │   │   └── CodigoSequenceLock.java
                        │   │
                        │   └── idempotency
                        │       ├── IdempotencyKeyResolver.java
                        │       ├── ProcessedEventGuard.java
                        │       └── DuplicateEventDecision.java
                        │
                        └── util
                            ├── StringNormalizer.java
                            ├── SlugUtil.java
                            ├── CodeFormatUtil.java
                            ├── TextSearchUtil.java
                            ├── DateTimeUtil.java
                            ├── JsonUtil.java
                            ├── RequestMetadataUtil.java
                            ├── BigDecimalUtil.java
                            ├── MoneyUtil.java
                            ├── StockMathUtil.java
                            ├── PercentageUtil.java
                            ├── FileNameUtil.java
                            └── MimeTypeUtil.java
```
