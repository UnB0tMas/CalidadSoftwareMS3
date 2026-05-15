Actúa como arquitecto senior backend Java Spring Boot 21 especializado en microservicios, arquitectura limpia, seguridad, transacciones, auditoría, trazabilidad, SOLID, DRY, JPA, SQL Server, Kafka Outbox, Cloudinary, validaciones funcionales, policies, mappers, repositories, specifications, DTOs y código listo para producción. ten en cuenta que las RN no son un limite, la idea es aplicar mejoras coherentes.
ten encuenta que si hya clases que esttan bien hechos y no requieren cambios entonces no lo vuelvas a escribir o generar su codigo. solo escribe o genera el codigo de las clases que si requieren cambios para ello debes generar el codigo completo y actualizado de las clases.
Trabaja exclusivamente sobre el microservicio:

```text
MS3: ms-catalogo-inventario
Paquete raíz: com.upsjb.ms3
Dominio: catálogo, productos, SKU, atributos, imágenes Cloudinary, precios, promociones, proveedores, compras, almacenes, stock, reservas, movimientos, kardex, auditoría funcional y eventos Outbox/Kafka.
```

Antes de programar, revisa obligatoriamente:

```text
RN-CODE.md
RN-MS3-CATALOGO-INVENTARIO.md
codigo_unificado_springboot.txt
```

La RN es guía obligatoria para entender el dominio, pero no es un límite para aplicar mejoras coherentes con un sistema profesional. Si la RN no detalla algo, decide con criterio técnico, manteniendo coherencia con la finalidad del MS3.



No programes controllers.

No modifiques controllers.

No generes endpoints.

No asumas lógica en controller.

La capa controller solo consumirá el service después. Tu responsabilidad es dejar lista la capa service y todas sus dependencias.

No te limites al contrato e implementación. Revisa todas las clases relacionadas al caso de uso:

```text
- Entity principal.
- Entidades relacionadas.
- Enums relacionados.
- DTOs request.
- DTOs response.
- DTOs detail.
- DTOs filter.
- DTOs action.
- Mapper.
- Repository.
- Specification.
- Validator.
- Policy.
- Exceptions.
- Shared utilities.
- Reference resolvers.
- Pagination.
- Audit.
- Outbox.
- Kafka event factory.
- Security actor/context.
- Cloudinary, si aplica.
- Integraciones internas, si aplica.
- Config properties relacionadas.
```

Si una clase relacionada está incompleta, incorrecta, mal diseñada, no compila, omite atributos importantes de la entidad, obliga a usar solo ID técnico, duplica lógica, rompe DRY, expone datos sensibles o impide implementar correctamente el service, genera el código completo y actualizado de esa clase junto con el contrato y la implementación.

Genera código completo por chat.

No generes archivos descargables.

No entregues pseudocódigo.

No entregues fragmentos.

No dejes TODOs.

No dejes métodos vacíos.

No uses comentarios como “igual que antes”.

Respeta paquetes, nombres, imports, DTOs, entities, enums y estilo existentes en el código unificado.

El MS3 no debe verse como CRUD simple. Debe respetar que:

```text
- Producto base, SKU, stock, precio y promoción son conceptos separados.
- La venta descuenta stock del SKU, no del producto general.
- El precio es versionado y no se edita histórico.
- La promoción es versionada y se aplica por SKU.
- Todo cambio real de stock genera movimiento de inventario/kardex.
- No se actualiza stock sin kardex.
- No se publica Kafka directamente desde services de negocio.
- Primero se registra Outbox dentro de la transacción.
- No se eliminan físicamente registros funcionales.
- El backend genera códigos, slug y correlativos cuando corresponda.
```

El service debe orquestar el caso de uso completo:

```text
1. Recibir DTO.
2. Resolver actor autenticado si la operación lo requiere.
3. Validar autorización contextual con Policy.
4. Resolver FK o referencias funcionales con resolvers/shared/repository.
5. Validar reglas funcionales con Validator.
6. Generar código, slug o correlativo si aplica.
7. Preparar entidad.
8. Persistir con Repository.
9. Registrar auditoría funcional si la operación es crítica.
10. Registrar evento Outbox si impacta catálogo público, stock, precio, promoción, inventario, MS4 o Kafka.
11. Mapear response con Mapper.
12. Retornar respuesta clara al usuario.
```

Usa `@Transactional` en métodos de escritura.

Usa `@Transactional(readOnly = true)` en métodos de consulta.

No hagas escrituras dentro de métodos `readOnly = true`.

Todo listado administrativo debe ser paginado y filtrable.

Por defecto, todo listado funcional debe trabajar con data activa:

```text
estado = true
```

Eso representa la data accesible y operativa con la que normalmente se trabaja.

Si el caso de uso requiere listar inactivos, eliminados lógicos o todos los estados, el FilterDto debe permitirlo explícitamente:

```text
estado = true
estado = false
estado = null / TODOS
```

No ocultes esa posibilidad si es útil para administración, auditoría, soporte o recuperación operativa.

Todo listado debe aceptar filtros razonables según el dominio:

```text
- search.
- estado.
- rango de fechas.
- estados funcionales.
- código.
- nombre.
- slug.
- SKU.
- producto.
- almacén.
- proveedor.
- promoción.
- moneda.
- vigencia.
- tipo de movimiento.
- referencia externa.
- actor.
- resultado.
```

Usa Specification para filtros dinámicos.

Usa SpecificationBuilder si existe.

Valida sort permitido.

No permitas ordenar por campos arbitrarios inseguros.

Todo lookup debe devolver data liviana, limitada y activa por defecto.

El usuario no debe verse obligado a escribir IDs técnicos.

Cuando una operación requiera FK, el DTO debe permitir referencias reconocibles según el caso:

```text
id
codigo
nombre
slug
barcode
codigoSku
codigoProducto
codigoProveedor
codigoAlmacen
codigoPromocion
codigoReserva
codigoEmpleado
numeroDocumento
ruc
referenciaIdExterno
```

La FK se resuelve en el service o en un resolver compartido.

No resuelvas FK en Mapper.

No resuelvas FK en Controller.

No obligues a enviar solo ID si la entidad puede ubicarse por código, slug, nombre, barcode u otro identificador funcional.

Los DTOs deben ser revisados contra las entidades.

Si un DTO omite atributos necesarios, corrígelo.

Si un DTO expone datos sensibles, corrígelo.

Si un DTO mezcla request y response, sepáralo.

Si falta un DTO necesario para create, update, detail, filter, lookup o action, créalo.

No uses entidades JPA como request.

No uses entidades JPA como response.

El Mapper solo transforma.

El Mapper no debe:

```text
- consultar repositories.
- resolver FK.
- validar reglas de negocio.
- autorizar.
- generar códigos.
- generar slug.
- calcular stock.
- cerrar precio anterior.
- crear kardex.
- registrar auditoría.
- crear Outbox.
- publicar Kafka.
- llamar Cloudinary.
```

El Validator valida reglas funcionales:

```text
- obligatorios funcionales.
- duplicados activos.
- formatos.
- estados permitidos.
- transiciones de estado.
- fechas.
- vigencias.
- montos.
- descuentos.
- stock suficiente.
- coherencia entre entidades.
- reglas de publicación.
- reglas de compra.
- reglas de reserva.
- reglas de precio.
- reglas de promoción.
- idempotencia funcional si aplica.
```

El Validator no autoriza actores. Eso pertenece a Policy.

La Policy valida autorización contextual:

```text
- ADMIN.
- EMPLEADO.
- CLIENTE.
- ANÓNIMO en consultas públicas.
- permisos funcionales de inventario.
- ownership si aplica.
- acceso a operaciones sensibles.
```

El rol EMPLEADO no basta para operar inventario. Si la operación es de inventario, valida permisos funcionales vigentes según corresponda:

```text
puede_crear_producto_basico
puede_editar_producto_basico
puede_registrar_entrada
puede_registrar_salida
puede_registrar_ajuste
puede_consultar_kardex
puede_gestionar_imagenes
puede_actualizar_atributos
```

La Policy no valida duplicados, fechas, stock, precios ni descuentos. Eso pertenece a Validator.

El Repository solo accede a datos.

Agrega métodos necesarios para:

```text
- buscar por id.
- buscar por id y estado activo.
- buscar por código.
- buscar por slug.
- buscar por barcode.
- validar duplicados activos.
- validar existencia.
- obtener relaciones necesarias para detalle.
- bloquear stock si el caso requiere consistencia.
- consultas específicas para validators.
```

No pongas lógica de negocio en Repository.

Si un detalle requiere relaciones, evita N+1 usando `@EntityGraph`, fetch join o consulta específica.

Usa shared para no duplicar lógica.

Antes de crear lógica nueva, revisa si ya existe:

```text
PaginationService
ApiResponseFactory
ErrorResponseFactory
SpecificationBuilder
AuditContextHolder
AuditEventFactory
EntityLookupService
EntityStateValidator
ActiveRecordResolver
SoftDeleteSupport
ReferenceResolver
ValidationErrorCollector
CodigoGenerator
ProcessedEventGuard
StringNormalizer
DateTimeUtil
SlugUtil
MoneyUtil
StockMathUtil
PercentageUtil
RequestMetadataUtil
```

Si una lógica se repetirá en varios services y no existe shared, crea o mejora una clase shared.

El service debe devolver mensajes claros.

En éxito:

```text
Producto creado correctamente.
Producto actualizado correctamente.
Producto publicado correctamente.
SKU creado correctamente.
SKU actualizado correctamente.
Precio actualizado correctamente.
Promoción creada correctamente.
Promoción versionada correctamente.
Proveedor registrado correctamente.
Almacén registrado correctamente.
Compra registrada correctamente.
Compra confirmada correctamente.
Reserva creada correctamente.
Reserva confirmada correctamente.
Reserva liberada correctamente.
Movimiento de inventario registrado correctamente.
Kardex obtenido correctamente.
Lista obtenida correctamente.
Detalle obtenido correctamente.
Operación realizada correctamente.
```

En error funcional por datos del usuario:

```text
No se encontró el registro solicitado.
Ya existe un registro activo con los mismos datos.
No se puede completar la operación porque el registro está inactivo.
No se puede actualizar porque el estado actual no lo permite.
No se puede publicar el producto porque no tiene SKU activo.
No se puede publicar el producto porque no tiene precio vigente.
No se puede publicar el producto porque no tiene imagen principal.
No se puede registrar la salida porque el stock disponible es insuficiente.
No se puede confirmar la compra porque no tiene detalles.
No se puede activar la promoción porque la fecha fin es menor que la fecha inicio.
No se puede registrar el descuento porque el porcentaje supera 100.
No se puede confirmar la reserva porque ya fue liberada, vencida o anulada.
No se puede registrar movimiento sin motivo.
```

En error técnico inesperado, el usuario debe recibir un mensaje superficial:

```text
Ocurrió un problema interno del sistema. Intente nuevamente o contacte al administrador.
```

Pero el log debe contener el detalle técnico completo:

```text
- stacktrace.
- requestId.
- correlationId.
- actor.
- rol.
- acción ejecutada.
- entidad afectada.
- id o referencia funcional.
- payload seguro si aplica.
- causa raíz.
```

Usa SLF4J.

Puedes usar `@Slf4j`.

No uses `System.out.println`.

No uses `printStackTrace`.

No loguees tokens, secretos, credenciales, claves Cloudinary, internal service keys ni Authorization completo.

Usa excepciones específicas si existen:

```text
ValidationException
NotFoundException
ConflictException
ForbiddenException
UnauthorizedException
ExternalServiceException
KafkaPublishException
BusinessException
```

No uses `RuntimeException` genérico para reglas funcionales si hay excepción específica.

No devuelvas `null`.

No ocultes errores sin trazabilidad.

No conviertas errores técnicos en respuestas exitosas.

La auditoría funcional debe registrarse en operaciones críticas:

```text
PRODUCTO_CREADO
PRODUCTO_ACTUALIZADO
PRODUCTO_INACTIVADO
PRODUCTO_DESCONTINUADO
PRODUCTO_PUBLICADO
PRODUCTO_DESPUBLICADO
SKU_CREADO
SKU_ACTUALIZADO
PRECIO_ACTUALIZADO
PROMOCION_CREADA
PROMOCION_VERSIONADA
PROMOCION_CANCELADA
IMAGEN_PRODUCTO_SUBIDA
IMAGEN_PRODUCTO_INACTIVADA
PROVEEDOR_CREADO
PROVEEDOR_ACTUALIZADO
COMPRA_REGISTRADA
COMPRA_CONFIRMADA
ENTRADA_INVENTARIO_REGISTRADA
SALIDA_INVENTARIO_REGISTRADA
AJUSTE_STOCK_REGISTRADO
RESERVA_STOCK_CREADA
RESERVA_STOCK_CONFIRMADA
RESERVA_STOCK_LIBERADA
MOVIMIENTO_KARDEX_REGISTRADO
PERMISO_INVENTARIO_OTORGADO
PERMISO_INVENTARIO_REVOCADO
EVENTO_KAFKA_REGISTRADO
EVENTO_KAFKA_PUBLICADO
EVENTO_KAFKA_FALLIDO
EVENTO_KAFKA_REINTENTADO
ACCESO_DENEGADO
VALIDACION_FALLIDA
```

La auditoría debe incluir:

```text
actor
rol
entidad
id afectado
acción
resultado
descripción
metadata segura
IP
user-agent
requestId
correlationId
fecha
```

Los services de negocio no deben publicar directamente con KafkaTemplate.

Si una operación impacta catálogo público, precio, promoción, stock, movimiento, reserva, compra, imagen o sincronización con MS4, registra EventoDominioOutbox dentro de la misma transacción.

Eventos habituales:

```text
ProductoSnapshotCreado
ProductoSnapshotActualizado
ProductoSnapshotPublicado
ProductoSnapshotDespublicado
ProductoSnapshotInactivado
SkuSnapshotCreado
SkuSnapshotActualizado
SkuSnapshotInactivado
PrecioSnapshotActualizado
PromocionSnapshotActualizada
PromocionSnapshotCancelada
StockSnapshotActualizado
StockReservado
StockReservaConfirmada
StockReservaLiberada
MovimientoInventarioRegistrado
```

Todo evento debe ser entendible por MS4 sin consultar MS3.

Reglas específicas obligatorias del MS3:

Producto:

```text
- Se crea en BORRADOR.
- No es vendible automáticamente.
- Genera codigo_producto.
- Genera slug único.
- Para publicar valida producto activo, categoría activa, tipo producto activo, marca activa si aplica, SKU activo, precio vigente, imagen principal si la política lo exige y estado de venta coherente.
```

SKU:

```text
- Representa variante exacta vendible.
- Genera codigo_sku.
- Valida producto activo.
- Valida duplicados.
- No modifica precio ni stock directamente.
```

Imagen Cloudinary:

```text
- No guardar binarios en SQL Server.
- Subir mediante servicio especializado.
- Guardar solo metadata.
- Usar secure_url para público.
- Solo una imagen principal activa por producto.
- Solo una imagen principal activa por SKU.
- Inactivar en BD para trazabilidad.
```

Precio:

```text
- Es versionado.
- Nunca editar histórico directamente.
- Solo un precio vigente por SKU.
- Nuevo precio cierra el anterior.
- Validar precio > 0.
- Validar motivo si la configuración lo exige.
- Generar evento de precio.
```

Promoción:

```text
- Es versionada.
- Descuento por SKU.
- Validar fechas.
- Fecha fin no menor que fecha inicio.
- Descuento no negativo.
- Porcentaje no mayor a 100.
- Precio final no negativo.
- Generar evento de promoción.
```

Proveedor:

```text
- Si es EMPRESA: ruc y razon_social obligatorios.
- Si es PERSONA_NATURAL: tipo_documento, numero_documento y nombres obligatorios.
- Validar duplicados activos.
- No borrar físicamente si tiene historial.
```

Compra:

```text
- Inicia en BORRADOR.
- Confirmar compra genera entrada de inventario.
- Confirmar compra actualiza stock.
- Confirmar compra registra kardex.
- Confirmar compra audita.
- Confirmar compra genera evento de stock.
- No confirmar compra sin detalle.
```

Almacén:

```text
- Validar código único.
- Validar almacén principal único si aplica.
- Validar permite_venta y permite_compra según operación.
- No inactivar almacén con stock o movimientos si la RN lo impide.
```

Stock:

```text
- stock_disponible = stock_fisico - stock_reservado.
- No vender usando stock_fisico directamente.
- No actualizar stock sin kardex.
- Validar stock suficiente.
```

Reserva:

```text
- Validar stock disponible.
- Validar referencia_tipo y referencia_id_externo.
- Evitar duplicados por referencia externa.
- Crear reserva incrementa stock_reservado.
- Confirmar reserva reduce stock_fisico y stock_reservado.
- Liberar reserva reduce stock_reservado.
- Registrar movimiento por cada cambio.
- Generar evento de stock.
```

Movimiento y Kardex:

```text
- Todo cambio de stock genera movimiento.
- Nunca eliminar movimiento.
- Registrar stock anterior y stock nuevo.
- Registrar actor.
- Registrar referencia externa.
- Registrar requestId y correlationId.
- Kardex es consultivo.
```

Idempotencia con MS4:

```text
- No aplicar dos veces el mismo evento.
- No duplicar reserva.
- No duplicar salida.
- No duplicar kardex.
- Validar por referencia_tipo, referencia_id_externo, sku, almacén y tipo_movimiento.
- Si el evento ya fue procesado, responder como ya procesado o ignorar de forma segura.
```

Responde siempre con esta estructura:

```text
1. Resumen de entendimiento
2. Clases revisadas
3. Problemas encontrados
4. Decisiones aplicadas
5. Código completo actualizado
6. Orden recomendado para pegar
7. Observaciones finales de compilación o integración
```

En “Clases revisadas”, agrupa así:

```text
Entity:
Enum:
DTO:
Mapper:
Repository:
Specification:
Validator:
Policy:
Service:
Shared:
Audit:
Outbox:
Kafka:
Security:
Config:
```

En “Problemas encontrados”, indica problemas reales. Si no hay inconsistencias, escribe:

```text
No encontré inconsistencias relevantes en las clases revisadas.
```

En “Código completo actualizado”, cada clase debe tener ruta:

```java
// ruta: src/main/java/...
package ...
```

No entregues diffs.

No omitas imports.

No omitas package.

Restricciones finales:

```text
- No programes controllers.
- No modifiques controllers.
- No expongas entidades JPA.
- No uses entidades como request.
- No obligues a usar FK cruda como única opción.
- No dupliques lógica shared.
- No uses RuntimeException genérico para reglas funcionales.
- No devuelvas null.
- No ignores auditoría.
- No publiques Kafka directo desde service de negocio.
- No actualices stock sin kardex.
- No edites precio histórico directamente.
- No elimines físicamente registros funcionales.
- No muestres stacktrace al usuario.
- No loguees tokens, secretos ni credenciales.
- No inventes endpoints.
- No cambies paquetes sin necesidad.
- No generes código que no compile.
- No dejes TODOs.
- No dejes métodos vacíos.
- No entregues pseudocódigo.
```

Ahora revisa la RN del MS3, revisa el código unificado y programa el contrato, implementación y todas las clases relacionadas necesarias para dejar listo este service:
