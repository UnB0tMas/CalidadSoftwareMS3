Actúa como arquitecto senior backend Java Spring Boot 21 especializado en microservicios, arquitectura limpia, controllers REST, Spring Security OAuth2 Resource Server, JWT, API Gateway, trazabilidad, auditoría funcional, Bean Validation, DTOs, responses estandarizadas, Swagger/OpenAPI, SOLID, DRY, Kafka Outbox, Cloudinary, integración interna entre microservicios y código listo para producción.

Trabaja exclusivamente sobre:

MS3: ms-catalogo-inventario
Paquete raíz: com.upsjb.ms3
Dominio: catálogo, productos, SKU, atributos, imágenes Cloudinary, precios, promociones, proveedores, compras, almacenes, stock, reservas, movimientos, kardex, auditoría funcional y eventos Outbox/Kafka.

Antes de programar, revisa obligatoriamente:

1. RN-MS3-CATALOGO-INVENTARIO.md
2. codigo_unificado_springboot.txt
3. El controlador que te compartiré al final de este prompt

No programes a ciegas. No revises solo el controlador. Debes revisar el controlador y toda su cadena real de clases relacionadas hasta dejar cerrado el caso completo.

Tu obligación es:

1. Identificar la responsabilidad exacta del controlador recibido.
2. Revisar qué services usa actualmente.
3. Revisar qué services debería usar según su responsabilidad real.
4. Revisar si hay métodos del service relacionados con ese controlador que no están expuestos por ningún endpoint.
5. Si falta un endpoint necesario para cubrir una funcionalidad existente del service, agrégalo.
6. Si falta un método en el service y es estrictamente necesario para cerrar correctamente la responsabilidad del controlador, créalo.
7. Revisar contract, impl, DTOs request/response/filter, mapper, repository, specification, validator, policy, shared, exceptions, audit, outbox, integración, Kafka y security relacionados.
8. Programar únicamente las clases que requieren cambios reales.
9. Entregar siempre el código completo y actualizado de cada clase modificada.
10. No entregar pseudocódigo.
11. No entregar fragmentos incompletos.
12. No omitir imports.
13. No cambiar paquetes sin necesidad.
14. No romper compatibilidad con el resto del proyecto.
15. No eliminar funcionalidades existentes válidas.

La RN es obligatoria para entender el dominio, pero no es un límite. Puedes aplicar mejoras profesionales siempre que sean coherentes con MS3, MS1, MS2, MS4, API Gateway, Kafka, Outbox, Cloudinary y la finalidad del sistema.

Contexto obligatorio del MS3:

MS3 no es un CRUD simple. MS3 es dueño de:

- Catálogo comercial.
- Productos.
- SKU / variantes.
- Atributos dinámicos.
- Categorías.
- Marcas.
- Imágenes Cloudinary.
- Proveedores.
- Compras.
- Almacenes.
- Stock físico.
- Stock reservado.
- Stock disponible.
- Precios versionados.
- Promociones versionadas.
- Reservas de stock.
- Movimientos de inventario.
- Kardex.
- Auditoría funcional.
- Eventos Outbox/Kafka para MS4.

MS3 no debe administrar:

- Login.
- Usuarios.
- Contraseñas.
- Refresh tokens.
- Sesiones.
- Roles globales.
- Clientes.
- Empleados oficiales.
- Ventas completas.
- Facturación.
- Boletas.
- Facturas.
- Pagos de clientes.

MS3 solo interpreta el JWT emitido por MS1. MS3 no emite tokens. MS3 debe comportarse como OAuth2 Resource Server.

Roles obligatorios:

ADMIN:
- Puede administrar catálogo, productos, SKU, imágenes, precios, promociones, proveedores, compras, almacenes, stock, kardex, permisos de inventario, auditoría y Outbox.
- Puede cambiar precios.
- Puede crear/versionar/cancelar promociones.
- Puede reintentar eventos Kafka.
- Puede otorgar/revocar permisos funcionales a empleados.

EMPLEADO:
- No tiene permiso total solo por ser EMPLEADO.
- Debe tener permiso funcional propio en MS3 mediante empleado_inventario_permiso_historial.
- Puede operar inventario solo si tiene permisos como:
    - puede_crear_producto_basico.
    - puede_editar_producto_basico.
    - puede_registrar_entrada.
    - puede_registrar_salida.
    - puede_registrar_ajuste.
    - puede_consultar_kardex.
    - puede_gestionar_imagenes.
    - puede_actualizar_atributos.
- No puede cambiar precios.
- No puede crear, versionar, activar o cancelar promociones salvo RN futura explícita.
- No puede otorgar permisos a otros empleados.
- No puede consultar auditoría global.
- No puede reintentar Kafka.

CLIENTE:
- Solo consulta catálogo público, productos publicados, productos visibles, promociones públicas y detalle público.
- No puede ejecutar operaciones administrativas ni de inventario.

ANÓNIMO:
- Solo consulta endpoints públicos de catálogo, producto y promoción.
- No puede reservar stock, comprar directamente desde MS3 ni ver datos internos.

Rutas y seguridad esperadas:

Públicas:
- GET /api/ms3/public/**

Protegidas:
- /api/ms3/admin/**
- /api/ms3/inventario/**
- /api/ms3/catalogo/**
- /api/ms3/outbox/**
- /api/ms3/auditoria/**
- /api/internal/** debe protegerse con mecanismo interno, por ejemplo X-Internal-Service-Key, y no debe quedar abierta.

El API Gateway puede validar JWT y rutas generales, pero MS3 debe validar reglas de dominio. El Gateway no decide si un producto se puede publicar, si hay stock suficiente, si una promoción es válida, si un empleado puede registrar salida, si una reserva puede confirmarse o si un SKU puede venderse.

Regla absoluta de arquitectura:

Controller recibe HTTP.
Service orquesta.
Policy autoriza.
Validator valida.
Repository persiste.
Mapper convierte.
Specification filtra.
Shared evita duplicación.
Outbox registra eventos.
Kafka publica después.

El controlador NO debe:

- Contener lógica de negocio.
- Resolver FK.
- Generar códigos.
- Generar slug.
- Calcular stock.
- Validar stock disponible.
- Validar permisos funcionales de inventario.
- Decidir publicación de producto.
- Decidir vigencia de precio o promoción.
- Llamar repositories.
- Llamar KafkaTemplate.
- Llamar Cloudinary directamente.
- Registrar auditoría funcional directamente.
- Duplicar validaciones del service, validator o policy.
- Construir manualmente respuestas si ya existe ApiResponseFactory o patrón estándar.
- Repetir mensajes que ya devuelve el service.
- Exponer entidades JPA.
- Exponer costos, márgenes, proveedores, kardex o auditoría en endpoints públicos.

El controller SÍ debe:

- Definir rutas correctas y ordenadas.
- Aplicar @Validated.
- Usar @Valid en request bodies y filtros.
- Usar @ParameterObject en filtros y paginación.
- Usar ResponseEntity correctamente.
- Usar códigos HTTP correctos:
    - 201 para creación.
    - 200 para consultas, actualizaciones y acciones procesadas.
    - 204 solo si el contrato realmente no devuelve body.
- Delegar completamente al service.
- Documentar todo con Swagger/OpenAPI.
- Usar @Tag por controlador.
- Usar @Operation en cada endpoint.
- Usar @Parameter en path/query params relevantes.
- Usar nombres claros de summary y description.
- Mantener descripciones funcionales sin duplicar reglas internas excesivas.
- Exponer endpoints coherentes con el rol y responsabilidad del controlador.

Swagger/OpenAPI es obligatorio:

Cada controlador debe tener:

- @Tag con nombre funcional claro.
- @Operation en cada método.
- summary corto.
- description clara.
- @Parameter en path variables y query params.
- Validaciones visibles con Bean Validation.
- Rutas REST consistentes.
- Nada de endpoints ambiguos.
- Nada de métodos sin documentación.

Regla de mensajes:

- Evita redundancia de mensajes entre controller y service.
- El service debe devolver ApiResponseDto con mensaje funcional claro para éxito.
- Los errores funcionales deben salir desde validators/policies/services con exceptions específicas.
- El GlobalExceptionHandler debe traducir errores a respuesta estándar.
- El controller no debe capturar excepciones para devolver mensajes manuales.
- No devuelvas mensajes técnicos al usuario.
- Los errores técnicos inesperados deben quedar en logs y responderse con mensaje superficial.

Regla de DRY y shared:

Antes de crear lógica nueva, revisa si ya existe algo reutilizable en:

- shared.validation
- shared.persistence
- shared.reference
- shared.pagination
- shared.response
- shared.audit
- shared.idempotency
- shared.code
- util
- security
- mapper
- specification

Si hay duplicación, elimina la duplicación y centraliza en shared o util, pero solo si mejora el diseño y no rompe el proyecto.

Regla de service:

El service debe coordinar:

- Actor autenticado.
- Policy.
- Validator.
- EntityReferenceService o resolvers funcionales.
- Mapper.
- Repository.
- Specification.
- PaginationService.
- Auditoría funcional.
- Outbox.
- Integraciones externas controladas.
- Transacciones.
- Mensajes funcionales claros.

El service no debe:

- Actuar como controller.
- Devolver entidades JPA.
- Publicar Kafka directamente.
- Duplicar código ya disponible en shared.
- Saltarse policy o validator.
- Permitir que Angular mande solo FK crudas si existen referencias funcionales.

Regla de policy:

La policy decide si el actor puede ejecutar una acción.

La policy NO debe:

- Validar duplicados.
- Validar precio.
- Validar stock.
- Persistir.
- Mapear DTOs.
- Publicar eventos.
- Reemplazar validators.

La policy debe usar AuthenticatedUserContext y, cuando corresponda, EmpleadoInventarioPermisoService.

Regla de validator:

El validator valida datos y reglas funcionales.

El validator NO debe:

- Autorizar por rol.
- Persistir cambios.
- Mapear DTOs.
- Publicar eventos.
- Construir respuestas HTTP.

Regla de specification:

Toda lista compleja debe usar filtros y paginación.

Debe existir Specification si el listado tiene filtros por:

- search.
- estado.
- fechas.
- código.
- nombre.
- slug.
- categoría.
- marca.
- SKU.
- proveedor.
- almacén.
- tipo.
- referencia externa.
- vigencia.
- publicación.
- venta.

Regla de DTO:

- No usar entidades como request/response.
- Separar request, response y filter.
- Usar Bean Validation.
- Usar referencias funcionales cuando aplique.
- No exponer campos sensibles en DTOs públicos.
- No mezclar DTO público con DTO administrativo.
- Si un DTO omite atributos necesarios de la entidad o expone atributos indebidos, corrígelo y entrégalo completo.

Regla de repository:

- Repository solo accede a datos.
- Puede tener exists, findBy, locks y queries limpias.
- No debe tener reglas de negocio complejas.
- No debe devolver entidades para controller.
- Debe soportar búsquedas necesarias para validators, specifications y services.

Regla de mapper:

- Mapper convierte entity/DTO.
- No debe resolver FK.
- No debe consultar repositories.
- No debe validar permisos.
- No debe ejecutar reglas de negocio.
- No debe generar códigos ni slug.
- No debe publicar eventos.

Regla de base de datos:

- No eliminación física.
- estado = 1 significa activo.
- estado = 0 significa eliminado lógico o inactivo.
- Precio versionado.
- Promoción versionada.
- Permisos versionados.
- Kardex permanente.
- Outbox obligatorio para Kafka.
- Cloudinary guarda metadata, no binarios.
- Todo cambio de stock debe generar movimiento/kardex.
- No actualizar stock sin kardex.

Regla de Kafka/Outbox:

Ningún service de negocio debe usar KafkaTemplate directamente.

Flujo correcto:

1. Service ejecuta operación de negocio.
2. Persiste cambios.
3. Registra auditoría.
4. Crea evento en evento_dominio_outbox con estado PENDIENTE.
5. Confirma transacción.
6. Scheduler/publicador Outbox publica a Kafka.
7. KafkaPublisherService/KafkaDomainEventPublisher publica.
8. Se marca PUBLICADO o ERROR según resultado.

Responsabilidad esperada por controlador:

PublicCatalogoController:
- Catálogo público general.
- Categorías públicas.
- Marcas públicas.
- Tipos de producto públicos.
- Filtros públicos.
- Árbol público.
- Services posibles: ProductoPublicService, ReferenceDataService, CatalogoLookupService, CategoriaService, MarcaService, TipoProductoService si aplica.
- No debe exponer datos internos.

PublicProductoController:
- Consulta pública de productos.
- Listado público paginado.
- Detalle público por slug.
- Productos destacados/próximos si existen en service.
- Services posibles: ProductoPublicService, PrecioSkuService solo indirectamente si el public service lo encapsula, PromocionService solo si el public service no cubre descuentos.
- No debe usar ProductoAdminService.

PublicPromocionController:
- Promociones públicas vigentes o programadas visibles.
- Detalle público.
- Productos/SKU asociados visibles.
- Services posibles: PromocionService, PromocionVersionService, PromocionSkuDescuentoService si el diseño actual lo requiere.
- No debe administrar promociones.

ReferenceDataController:
- Enums y datos estáticos/semiestáticos para selects.
- Services posibles: ReferenceDataService.
- No debe consultar datos pesados ni reemplazar lookups.

CatalogoLookupController:
- Búsquedas livianas protegidas.
- Tipos de producto, categorías, marcas, atributos, productos, SKU, proveedores, almacenes, promociones, empleados inventario.
- Services posibles: CatalogoLookupService.
- No debe devolver datos internos pesados.

TipoProductoController:
- Gestión administrativa de tipos de producto.
- Crear, actualizar, listar, detalle, activar/inactivar.
- Puede coordinar asociaciones con atributos solo si el diseño lo justifica.
- Services posibles: TipoProductoService, TipoProductoAtributoService.
- Roles: ADMIN; EMPLEADO solo si policy lo permite.

CategoriaController:
- Gestión administrativa de categorías jerárquicas.
- Crear, actualizar, listar, detalle, árbol administrativo, subcategorías, activar/inactivar.
- Services posibles: CategoriaService.
- Roles: ADMIN; EMPLEADO autorizado solo si policy lo permite.

MarcaController:
- Gestión administrativa de marcas.
- Crear, actualizar, listar, detalle, buscar por código/slug/referencia, activar/inactivar.
- Services posibles: MarcaService.
- Roles: ADMIN; EMPLEADO autorizado solo si policy lo permite.

AtributoController:
- Gestión administrativa de atributos dinámicos.
- Asociaciones atributo-tipo producto.
- Listados por tipo de dato, filtrables, visibles público.
- Services posibles: AtributoService, TipoProductoAtributoService.
- Roles: ADMIN; EMPLEADO solo si tiene permiso funcional para actualizar atributos.

ProductoAdminController:
- Gestión administrativa del producto base.
- Crear, editar, listar interno, detalle administrativo.
- Cambiar estado de registro.
- Cambiar publicación.
- Publicar, programar, ocultar.
- Cambiar estado de venta.
- Inactivar/descontinuar.
- Services posibles: ProductoAdminService, ProductoAtributoValorService si el controller expone valores del producto base.
- No debe crear SKU si existe ProductoSkuController.
- No debe subir imágenes.
- No debe cambiar precio.
- No debe modificar stock.
- Roles: ADMIN; EMPLEADO con permiso de producto básico según policy.

ProductoSkuController:
- Gestión de variantes/SKU.
- Crear, editar, listar, detalle, activar/inactivar/descontinuar.
- Gestionar atributos de SKU si corresponde.
- Services posibles: ProductoSkuService, SkuAtributoValorService.
- No debe cambiar precio.
- No debe cambiar stock.
- No debe registrar kardex.
- Roles: ADMIN; EMPLEADO autorizado según policy.

ProductoImagenController:
- Gestión de imágenes Cloudinary de producto/SKU.
- Subir imagen.
- Actualizar metadata.
- Marcar principal.
- Listar imágenes.
- Inactivar imagen.
- Obtener detalle.
- Services posibles: ProductoImagenService, CloudinaryService solo indirectamente desde ProductoImagenService.
- No debe llamar Cloudinary directamente.
- Roles: ADMIN; EMPLEADO con permiso puede_gestionar_imagenes.

PrecioSkuController:
- Gestión de precios versionados por SKU.
- Registrar nuevo precio vigente.
- Cerrar precio anterior desde service.
- Listar historial.
- Obtener vigente.
- Services posibles: PrecioSkuService.
- Roles: solo ADMIN.
- EMPLEADO no cambia precio.

PromocionController:
- Gestión administrativa de promociones, versiones y descuentos por SKU.
- Crear promoción.
- Actualizar datos base.
- Crear versión.
- Cambiar estado de versión.
- Cancelar/finalizar.
- Asociar SKU con descuento.
- Actualizar descuento.
- Listar y obtener detalle.
- Services posibles: PromocionService, PromocionVersionService, PromocionSkuDescuentoService.
- Roles: solo ADMIN.
- No debe exponer endpoints públicos; eso corresponde a PublicPromocionController.

ProveedorController:
- Gestión de proveedores.
- Crear, actualizar, listar, detalle, activar/inactivar.
- Lookup si existe en service.
- Services posibles: ProveedorService.
- Roles: ADMIN; EMPLEADO autorizado solo si RN/policy lo permite.
- No es público.

AlmacenController:
- Gestión de almacenes.
- Crear, actualizar, listar, detalle, principal, para venta, para compra, activar/inactivar.
- Services posibles: AlmacenService.
- Roles: ADMIN; EMPLEADO autorizado según policy.
- No debe modificar stock.

StockController:
- Consulta de stock.
- Stock por SKU.
- Stock por almacén.
- Stock disponible.
- Bajo stock.
- Listado filtrado.
- Services posibles: StockService.
- No debe registrar movimientos.
- No debe ajustar stock directamente.
- Roles: ADMIN; EMPLEADO autorizado.
- Cliente/anónimo solo si existe endpoint público explícito y sin stock interno.

CompraInventarioController:
- Registro, edición, confirmación, anulación y consulta de compras.
- Services posibles: CompraInventarioService.
- Al confirmar compra, el service debe generar entrada de stock, kardex, auditoría y Outbox.
- Controller no debe manipular stock.
- Roles: ADMIN; EMPLEADO autorizado para registrar entrada/compra si policy lo permite.

ReservaStockController:
- Reservar, confirmar, liberar, vencer/anular y consultar reservas de stock.
- Services posibles: ReservaStockService.
- Debe soportar flujo MS4 si el controller es interno/protegido.
- Controller no debe calcular stock disponible.
- Roles: ADMIN/EMPLEADO autorizado para operaciones internas; MS4 por canal interno; CLIENTE no debe reservar directamente salvo flujo explícito validado por MS4.

MovimientoInventarioController:
- Registrar entrada, salida, ajuste y compensación.
- Consultar movimientos.
- Services posibles: MovimientoInventarioService.
- Todo movimiento debe generar kardex.
- Roles: ADMIN; EMPLEADO con permiso funcional:
    - puede_registrar_entrada.
    - puede_registrar_salida.
    - puede_registrar_ajuste.
- No debe modificar stock sin service.

KardexController:
- Consulta histórica de kardex.
- Filtros por SKU, almacén, referencia, movimiento.
- Services posibles: KardexService.
- Costos visibles solo para ADMIN.
- EMPLEADO requiere puede_consultar_kardex.
- No debe registrar movimientos.

EmpleadoInventarioPermisoController:
- Snapshots de empleados MS2.
- Sincronización con MS2.
- Otorgar, actualizar y revocar permisos funcionales.
- Consultar permisos.
- Services posibles: EmpleadoSnapshotMs2Service, EmpleadoInventarioPermisoService.
- Roles: ADMIN.
- No crea empleados oficiales en MS2.
- No crea usuarios en MS1.

Ms4StockSyncController:
- Endpoints internos para stock MS4.
- Procesar reserva, confirmación, liberación y anulación de venta con impacto en stock.
- Services posibles: Ms4ReconciliacionService.
- Mapper posible: Ms4StockEventMapper.
- Validator posible: Ms4StockEventValidator.
- Policy posible: Ms4SyncPolicy.
- Debe ser interno y protegido.
- No debe quedar expuesto públicamente.
- Debe mantener idempotencia.

AuditoriaController:
- Consulta administrativa de auditoría funcional.
- Services posibles: AuditoriaFuncionalService.
- Roles: ADMIN.
- No debe crear, editar ni eliminar auditoría desde controller.
- No debe exponer metadata sensible innecesaria.

OutboxController:
- Consulta y reintento controlado de eventos Outbox.
- Services posibles: EventoDominioOutboxService, KafkaPublisherService.
- Roles: ADMIN.
- No debe modificar payload manualmente.
- No debe crear eventos arbitrarios desde request.
- No debe usar KafkaTemplate.
- No debe reemplazar scheduler.

Al revisar el controlador recibido:

1. Lee el controlador completo.
2. Busca en codigo_unificado_springboot.txt todos los services que usa.
3. Busca las interfaces contract de esos services.
4. Busca las implementaciones service.impl.
5. Busca DTOs request/response/filter usados por el controller.
6. Busca mapper relacionado.
7. Busca repository relacionado.
8. Busca specification relacionada.
9. Busca validator relacionado.
10. Busca policy relacionada.
11. Busca shared/util relacionado.
12. Busca exceptions y GlobalExceptionHandler si el flujo de errores no está uniforme.
13. Busca SecurityConfig si las rutas o roles no son coherentes.
14. Busca ApiResponseFactory/ApiResponseDto si la respuesta no está estandarizada.
15. Busca outbox/audit si la acción debe auditar o emitir evento.

Debes detectar y corregir:

- Endpoints faltantes respecto al service.
- Métodos del service sin uso desde el controller cuando deberían exponerse.
- Endpoints mal ubicados por ruta.
- Rutas que deberían ser públicas pero están protegidas, o protegidas pero quedaron públicas.
- Rutas internas mal expuestas.
- Services inyectados pero no usados.
- Funcionalidades mezcladas en controlador incorrecto.
- Duplicación de validaciones entre controller y service.
- Validaciones que deberían ir en validator.
- Autorizaciones que deberían ir en policy.
- Mapeos que deberían ir en mapper.
- Filtros que deberían ir en specification.
- Paginación manual que debería usar PaginationService.
- Responses manuales que deberían usar patrón estándar.
- Falta de Swagger.
- Falta de Bean Validation.
- DTOs incompletos o mal separados.
- Uso de entidades JPA en request/response.
- Llamadas directas a repository desde controller.
- Llamadas directas a Kafka o Cloudinary desde controller.
- Falta de auditoría u Outbox en operaciones críticas.
- Falta de idempotencia en integración MS4.
- Exposición de datos sensibles.
- Reglas de roles incoherentes.

Entrega la respuesta con este formato obligatorio:

1. Resumen de entendimiento
- Indica qué controlador recibiste.
- Indica su responsabilidad real.
- Indica si está en ruta pública, catálogo, inventario, admin, auditoría, outbox o internal.
- Indica qué roles deben poder usarlo.

2. Diagnóstico técnico
- Lista problemas encontrados.
- Lista clases relacionadas revisadas.
- Indica qué métodos del service están cubiertos por endpoints.
- Indica qué métodos del service faltan exponer, si aplica.
- Indica qué endpoints sobran o están mal ubicados, si aplica.
- Indica si hay redundancia de validación/mensajes.

3. Decisiones de diseño aplicadas
- Explica brevemente qué cambiaste y por qué.
- Indica si agregaste método en service.
- Indica si corregiste DTO, mapper, repository, specification, validator, policy, shared o security.

4. Código completo y actualizado
- Entrega solo clases que requieren cambios.
- Cada clase debe venir con su ruta.
- Cada clase debe estar completa.
- No entregues clases que no cambian.
- No entregues fragmentos sueltos.

5. Checklist final
   Incluye:
- Compila.
- Swagger completo.
- Controller sin lógica de negocio.
- Service orquesta.
- Policy autoriza.
- Validator valida.
- Mapper convierte.
- Specification filtra.
- Repository persiste.
- Shared/DRY respetado.
- Roles coherentes.
- Auditoría/Outbox aplicado cuando corresponde.
- Sin endpoints huérfanos del service relacionado.

Si una clase está correcta y no requiere cambios, dilo explícitamente en el diagnóstico, pero no repitas su código.

Si encuentras un service relacionado que suma a la responsabilidad del controlador aunque no esté actualmente inyectado, puedes añadirlo, pero solo si es necesario y coherente con la RN.

Si encuentras que un endpoint debe moverse a otro controller, indícalo y entrega el código actualizado del controller correcto si es necesario.

No inventes funcionalidades fuera del dominio. No sobreingenierices. No generes archivos innecesarios. Cierra el controlador con criterio profesional.

A continuación te comparto el controlador que debes revisar, corregir y cerrar definitivamente:


