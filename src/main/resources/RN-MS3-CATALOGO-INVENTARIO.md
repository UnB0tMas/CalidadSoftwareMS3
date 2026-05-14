# RN General Definitiva - MS3: ms-catalogo-inventario

**Proyecto:** Sistema de tienda de ropa, calzado y accesorios deportivos basado en microservicios  
**Microservicio:** `ms-catalogo-inventario`  
**Nombre corto:** MS3  
**Estado:** Documentación base oficial para entender, diseñar y programar el dominio de catálogo e inventario  
**Stack previsto:** Java 21, Spring Boot, SQL Server, Kafka, Cloudinary, API Gateway, Angular  
**Base de datos:** SQL Server, usando el script definitivo de entidades, relaciones, restricciones, índices y eliminación lógica  

---

## 1. Propósito del documento

Este documento define de forma clara cómo debe funcionar el **MS3 - ms-catalogo-inventario** dentro del sistema completo.

La finalidad es que cualquier integrante del equipo pueda entender:

- Qué responsabilidad tiene el MS3.
- Qué datos administra.
- Qué operaciones permite.
- Qué relación tiene con MS1, MS2, MS4, Angular, API Gateway, Kafka y Cloudinary.
- Cómo se comporta cuando otros microservicios están disponibles.
- Cómo se comporta cuando MS3 o MS4 se caen.
- Cómo debe mantenerse la coherencia entre catálogo, inventario, ventas y stock.
- Qué reglas de negocio se deben respetar.
- Qué tablas de la base de datos soportan cada proceso.
- Qué debe y qué no debe programarse dentro del MS3.

El MS3 no debe verse como un CRUD de productos. Debe verse como el **microservicio dueño del catálogo, variantes, precios, promociones, proveedores, almacenes, stock y kardex**.

---

## 2. Arquitectura general del sistema

La arquitectura oficial queda de esta manera:

```text
Angular
   ↓
API Gateway
   ↓
Microservicios
   ├── ms-seguridad-usuarios
   ├── ms-personas-clientes-empleados
   ├── ms-catalogo-inventario
   └── ms-ventas-facturacion
```

Cada microservicio tiene una responsabilidad separada:

```text
MS1 - ms-seguridad-usuarios
    Autenticación, usuarios, roles, JWT, sesiones y seguridad.

MS2 - ms-personas-clientes-empleados
    Personas, empresas, clientes, empleados, direcciones, teléfonos y datos funcionales personales/laborales.

MS3 - ms-catalogo-inventario
    Catálogo, productos, SKU, atributos, imágenes Cloudinary, precios, promociones, proveedores, almacenes, stock, reservas, kardex y eventos de inventario.

MS4 - ms-ventas-facturacion
    Ventas, carrito, pedidos, comprobantes, pagos de clientes, facturación y snapshots locales para operar aunque MS3 esté caído.

API Gateway
    Entrada HTTP única, validación JWT general, CORS, trazabilidad, rutas y resiliencia.
```

---

## 3. Objetivo general del MS3

El MS3 tiene como objetivo administrar profesionalmente todo lo relacionado con:

```text
- Catálogo comercial.
- Productos.
- Variantes o SKU.
- Atributos dinámicos.
- Categorías.
- Marcas.
- Imágenes de productos en Cloudinary.
- Proveedores.
- Compras/adquisiciones.
- Almacenes.
- Stock físico.
- Stock reservado.
- Stock disponible.
- Precios históricos.
- Promociones versionadas.
- Reservas de stock para ventas.
- Movimientos de inventario.
- Kardex.
- Auditoría funcional.
- Eventos Kafka para sincronización con MS4.
```

El MS3 es la fuente oficial de la verdad para:

```text
- Producto.
- SKU.
- Precio vigente.
- Promoción vigente.
- Stock real.
- Disponibilidad para venta.
- Estado público del producto.
- Historial de inventario.
```

---

## 4. Qué NO debe administrar el MS3

El MS3 no debe administrar:

```text
- Login.
- Registro de usuarios.
- Contraseñas.
- Refresh tokens.
- Sesiones.
- Roles globales.
- Clientes.
- Empleados como ficha laboral.
- Sueldos.
- Pagos de empleados.
- Ventas completas.
- Facturación.
- Boletas.
- Facturas.
- Pagos de clientes.
- Recuperación o cambio de contraseña.
```

Regla central:

```text
MS3 no reemplaza a MS1, MS2 ni MS4.
MS3 solo administra catálogo e inventario.
```

---

## 5. Relación con MS1

MS1 es dueño de:

```text
- Usuario.
- Username.
- Email de login.
- Password hash.
- Rol.
- Estado del usuario.
- Login.
- Logout.
- JWT.
- Refresh token.
- Sesiones.
```

MS3 solo interpreta el JWT emitido por MS1.

El JWT debe permitir conocer:

```text
- id_usuario_ms1.
- username.
- email.
- rol.
- authorities.
```

MS3 usa esa información para:

```text
- Saber quién ejecutó una operación.
- Validar si el actor es ADMIN, EMPLEADO, CLIENTE o ANÓNIMO.
- Registrar auditoría.
- Registrar movimientos de inventario.
- Registrar quién creó productos, promociones, compras o ajustes.
```

MS3 no debe tener FK física hacia tablas del MS1.

En la base de datos del MS3 se usan campos como:

```text
creado_por_id_usuario_ms1
actualizado_por_id_usuario_ms1
actor_id_usuario_ms1
reservado_por_id_usuario_ms1
confirmado_por_id_usuario_ms1
otorgado_por_id_usuario_ms1
```

Estos campos son referencias externas lógicas, no relaciones físicas con la base del MS1.

---

## 6. Relación con MS2

MS2 es dueño de:

```text
- Personas.
- Empresas.
- Clientes.
- Empleados.
- Direcciones.
- Teléfonos.
- Datos laborales.
```

MS3 no debe crear empleados ni clientes.

Sin embargo, MS3 necesita saber si un empleado existe, si está activo y si puede operar inventario.

Para eso se usa la tabla:

```text
empleado_snapshot_ms2
```

Esta tabla guarda una copia mínima del empleado proveniente del MS2:

```text
- id_empleado_ms2.
- id_usuario_ms1.
- codigo_empleado.
- nombres_completos.
- area_codigo.
- area_nombre.
- empleado_activo.
- snapshot_version.
- snapshot_at.
- estado.
```

Esta información permite que MS3 valide operaciones de empleados sin depender todo el tiempo de una llamada HTTP al MS2.

### 6.1. Permisos propios de inventario

Aunque MS2 tenga permisos básicos del empleado, MS3 debe tener permisos propios para inventario.

Para eso se usa:

```text
empleado_inventario_permiso_historial
```

Permisos principales:

```text
- puede_crear_producto_basico.
- puede_editar_producto_basico.
- puede_registrar_entrada.
- puede_registrar_salida.
- puede_registrar_ajuste.
- puede_consultar_kardex.
- puede_gestionar_imagenes.
- puede_actualizar_atributos.
```

Regla:

```text
El rol EMPLEADO no basta para administrar inventario.
El empleado necesita permisos funcionales otorgados por un ADMIN dentro del MS3.
```

---

## 7. Relación con el API Gateway

Angular no debe consumir directamente el puerto del MS3.

Correcto:

```text
Angular → API Gateway → MS3
```

Incorrecto:

```text
Angular → MS3 directo
```

Rutas esperadas:

```text
/api/ms3/public/**
/api/ms3/admin/**
/api/ms3/inventario/**
/api/ms3/catalogo/**
```

El Gateway puede validar:

```text
- Si la ruta es pública.
- Si la ruta requiere JWT.
- Si el token es válido.
- Si hay fallback técnico.
```

Pero el Gateway no debe decidir:

```text
- Si el producto se puede publicar.
- Si hay stock suficiente.
- Si una promoción es válida.
- Si el empleado puede registrar salida.
- Si una reserva puede confirmarse.
- Si un SKU puede venderse.
```

Esas reglas pertenecen al MS3.

---

## 8. Relación con Angular

Angular usará MS3 para dos tipos de experiencia:

### 8.1. Catálogo público

Puede ser consumido por:

```text
- Cliente logueado.
- Persona no logueada.
- Visitante anónimo.
```

Debe mostrar solo:

```text
- Productos públicos.
- Productos visibles.
- Productos publicados.
- Productos programados como próximos si la RN lo permite.
- Promociones públicas.
- Imágenes activas.
- Precio público vigente.
- Stock visible si se decide mostrar.
```

No debe mostrar:

```text
- Costo de compra.
- Margen.
- Proveedor.
- Kardex.
- Stock interno por almacén.
- Auditoría.
- Movimientos de inventario.
```

### 8.2. Panel administrativo u operativo

Usado por:

```text
- ADMIN.
- EMPLEADO autorizado.
```

Puede mostrar:

```text
- Productos internos.
- Estados de publicación.
- Estados de venta.
- SKU.
- Stock por almacén.
- Kardex.
- Compras.
- Proveedores.
- Promociones.
- Precios.
- Imágenes Cloudinary.
- Permisos.
```

El frontend no debe decidir reglas sensibles. Angular puede ocultar botones, pero MS3 debe validar todo en backend.

---

## 9. Roles y comportamiento funcional

## 9.1. ADMIN

El ADMIN puede:

```text
- Crear productos.
- Editar productos.
- Inactivar productos.
- Descontinuar productos.
- Crear SKU.
- Editar SKU.
- Gestionar categorías.
- Gestionar marcas.
- Gestionar atributos.
- Gestionar imágenes Cloudinary.
- Registrar proveedores.
- Registrar compras.
- Confirmar compras.
- Registrar entradas.
- Registrar salidas.
- Registrar ajustes.
- Consultar kardex.
- Crear promociones.
- Versionar promociones.
- Activar promociones.
- Cancelar promociones.
- Cambiar precios.
- Versionar precios.
- Publicar productos.
- Programar publicación.
- Ocultar productos.
- Cambiar estado de venta.
- Otorgar permisos de inventario a empleados.
- Revocar permisos.
- Consultar auditoría funcional.
- Reintentar eventos Kafka fallidos.
```

El ADMIN no debe:

```text
- Crear ventas directamente en MS3.
- Emitir facturas desde MS3.
- Cambiar contraseñas desde MS3.
- Crear usuarios desde MS3.
```

## 9.2. EMPLEADO

El EMPLEADO puede participar en ventas y operaciones de inventario, pero con límites.

Puede:

```text
- Consultar catálogo para vender.
- Consultar productos disponibles.
- Registrar venta desde MS4.
- Generar reserva o salida de stock asociada a venta, según flujo.
- Registrar entrada si tiene permiso.
- Registrar salida si tiene permiso.
- Registrar ajuste si tiene permiso.
- Crear producto básico si tiene permiso.
- Editar producto básico si tiene permiso.
- Gestionar imágenes si tiene permiso.
- Consultar kardex si tiene permiso.
```

No puede, salvo autorización explícita:

```text
- Crear productos completos sin permiso.
- Editar datos sensibles del producto.
- Cambiar precio.
- Crear promociones.
- Cancelar promociones.
- Eliminar productos físicamente.
- Descontinuar productos.
- Reintentar Kafka.
- Consultar auditoría global.
- Otorgar permisos a otros empleados.
```

Regla:

```text
El empleado puede registrar una venta, pero la venta pertenece a MS4.
MS3 solo actualiza stock mediante reserva, confirmación o movimiento asociado a esa venta.
```

## 9.3. CLIENTE

El CLIENTE puede:

```text
- Ver catálogo público.
- Ver productos publicados.
- Ver productos próximos si se decidió mostrarlos.
- Ver promociones públicas.
- Ver detalle público de producto.
```

No puede:

```text
- Crear productos.
- Editar productos.
- Ver costos.
- Ver proveedores.
- Ver kardex.
- Ver stock interno.
- Registrar movimientos.
- Cambiar precios.
```

## 9.4. ANÓNIMO

La persona no logueada puede:

```text
- Ver catálogo público.
- Ver detalle público de productos visibles.
- Ver promociones públicas.
```

No puede:

```text
- Comprar directamente si el flujo exige login.
- Reservar stock.
- Ver información interna.
- Ejecutar operaciones protegidas.
```

---

## 10. Modelo central de producto

El MS3 no debe manejar un producto como una sola fila simple.

Debe separar:

```text
Producto base
    ↓
SKU / variante
    ↓
Stock por almacén
    ↓
Precio vigente
    ↓
Promoción vigente
```

Ejemplo:

```text
Producto:
    Camiseta deportiva Nike Dry Fit

SKU:
    CAM-NIKE-ROJO-S
    CAM-NIKE-ROJO-M
    CAM-NIKE-AZUL-S
```

Cada SKU puede tener:

```text
- Color.
- Talla.
- Material.
- Modelo.
- Barcode.
- Stock.
- Precio.
- Promoción.
```

Regla:

```text
La venta debe descontar stock del SKU, no del producto general.
```

---

## 11. Generación inteligente de SKU, slug y códigos

El usuario no debe ingresar manualmente:

```text
- codigo_producto.
- codigo_sku.
- slug.
- codigo_compra.
- codigo_reserva.
- codigo_movimiento.
- codigo_promocion.
```

El backend del MS3 debe generarlos automáticamente.

La base de datos tiene la tabla:

```text
correlativo_codigo
```

Esta tabla soporta generación de códigos para:

```text
- PRODUCTO.
- SKU.
- PROMOCION.
- COMPRA.
- RESERVA_STOCK.
- MOVIMIENTO_INVENTARIO.
```

Ejemplo:

```text
Producto:
    PROD-000001

SKU:
    SKU-000001

Compra:
    COMP-000001

Movimiento:
    MOV-000001
```

### 11.1. Slug

El slug se genera a partir del nombre.

Ejemplo:

```text
Nombre:
    Zapatillas Adidas Running Galaxy 6

Slug:
    zapatillas-adidas-running-galaxy-6
```

Si el slug ya existe, el backend debe hacerlo único:

```text
zapatillas-adidas-running-galaxy-6
zapatillas-adidas-running-galaxy-6-2
zapatillas-adidas-running-galaxy-6-3
```

Regla:

```text
El slug es para URL pública.
El usuario no lo escribe.
El backend lo genera y garantiza unicidad.
```

### 11.2. SKU

El SKU representa una variante exacta vendible.

Ejemplo:

```text
Producto:
    Camiseta Nike Dry Fit

Variante:
    Rojo, talla M

SKU generado:
    SKU-000145
```

Opcionalmente el backend puede generar un código más expresivo:

```text
CAM-NIKE-ROJO-M-000145
```

Pero no debe depender de que el usuario lo escriba.

---

## 12. Estados del producto

El producto tiene tres niveles de estado funcional:

```text
estado
estado_registro
estado_publicacion
estado_venta
```

## 12.1. `estado`

Campo general de eliminación lógica:

```text
1 = activo
0 = eliminado/inactivo lógico
```

Regla:

```text
Solo se trabaja funcionalmente con estado = 1.
No se eliminan registros físicamente.
```

## 12.2. `estado_registro`

Indica la vida administrativa del producto:

```text
BORRADOR
ACTIVO
INACTIVO
DESCONTINUADO
```

Uso:

```text
BORRADOR:
    Producto en preparación.

ACTIVO:
    Producto válido para operar.

INACTIVO:
    Producto oculto o suspendido.

DESCONTINUADO:
    Producto que ya no se venderá ni repondrá.
```

## 12.3. `estado_publicacion`

Indica si aparece al público:

```text
NO_PUBLICADO
PUBLICADO
PROGRAMADO
OCULTO
```

Uso:

```text
NO_PUBLICADO:
    No aparece.

PUBLICADO:
    Aparece al público.

PROGRAMADO:
    Aparecerá en fecha futura.

OCULTO:
    Existe, pero no se muestra.
```

## 12.4. `estado_venta`

Indica si puede venderse:

```text
NO_VENDIBLE
VENDIBLE
SOLO_VISIBLE
AGOTADO
PROXIMAMENTE
```

Casos:

```text
Producto visible y comprable:
    estado_publicacion = PUBLICADO
    estado_venta = VENDIBLE

Producto visible pero no seleccionable:
    estado_publicacion = PUBLICADO
    estado_venta = SOLO_VISIBLE

Producto programado:
    estado_publicacion = PROGRAMADO
    estado_venta = PROXIMAMENTE
```

---

## 13. Cloudinary en MS3

Las imágenes no se guardan como binarios en SQL Server.

El archivo se sube a Cloudinary y MS3 guarda solo metadata en:

```text
producto_imagen_cloudinary
```

Campos principales:

```text
- cloudinary_asset_id.
- cloudinary_public_id.
- cloudinary_version.
- secure_url.
- url.
- resource_type.
- format.
- bytes.
- width.
- height.
- folder.
- original_filename.
- alt_text.
- titulo.
- orden.
- principal.
```

Reglas:

```text
- Toda imagen pertenece a un producto.
- Opcionalmente puede pertenecer a un SKU.
- Un producto puede tener muchas imágenes.
- Solo una imagen principal activa por producto.
- Solo una imagen principal activa por SKU.
- La imagen pública debe usar secure_url.
- No se debe guardar la imagen física en SQL Server.
```

### 13.1. Flujo de subida de imagen

Flujo recomendado:

```text
1. Angular selecciona imagen.
2. Angular envía imagen al endpoint del MS3.
3. MS3 valida permiso.
4. MS3 sube archivo a Cloudinary.
5. Cloudinary devuelve public_id, secure_url, asset_id y metadata.
6. MS3 guarda metadata en producto_imagen_cloudinary.
7. MS3 audita la operación.
8. MS3 genera evento ProductoSnapshotActualizado.
9. MS4 actualiza su snapshot local.
```

### 13.2. Eliminación de imagen

No se elimina físicamente de la BD.

Regla:

```text
estado = 0
```

Opcionalmente el service puede solicitar a Cloudinary eliminar el recurso, pero debe decidirse con cuidado.

Recomendación:

```text
Para trazabilidad, primero inactivar en MS3.
Luego decidir si se elimina en Cloudinary o se mantiene como histórico.
```

---

## 14. Precio versionado

El precio es sensible porque afecta ventas pasadas.

Nunca debe editarse el precio histórico directamente.

Tabla:

```text
precio_sku_historial
```

Campos clave:

```text
- id_sku.
- precio_venta.
- moneda.
- fecha_inicio.
- fecha_fin.
- vigente.
- motivo.
- creado_por_id_usuario_ms1.
- estado.
```

Reglas:

```text
- Solo debe existir un precio vigente por SKU.
- Un nuevo precio cierra el precio anterior.
- Las ventas pasadas deben conservar el precio usado en ese momento.
- MS4 debe guardar el precio usado en la venta.
- Cambiar precio en MS3 no debe alterar ventas históricas.
```

Flujo:

```text
1. ADMIN registra nuevo precio.
2. MS3 valida que precio > 0.
3. MS3 cierra precio vigente anterior.
4. MS3 crea nuevo precio vigente.
5. MS3 audita.
6. MS3 genera evento PrecioSnapshotActualizado.
7. MS4 consume evento y actualiza snapshot de precio.
```

---

## 15. Promociones versionadas

Las promociones también son sensibles.

La promoción se divide en:

```text
promocion
promocion_version
promocion_sku_descuento_version
```

## 15.1. `promocion`

Representa la campaña general.

Ejemplo:

```text
Promoción Invierno
```

## 15.2. `promocion_version`

Define vigencia y estado de la campaña:

```text
- fecha_inicio.
- fecha_fin.
- estado_promocion.
- visible_publico.
- vigente.
- motivo.
```

Estados:

```text
BORRADOR
PROGRAMADA
ACTIVA
FINALIZADA
CANCELADA
```

## 15.3. `promocion_sku_descuento_version`

Define el descuento real por SKU.

Tipos:

```text
PORCENTAJE
MONTO_FIJO
PRECIO_FINAL
```

Regla importante:

```text
La campaña no aplica igual a todos los productos.
Cada SKU define su descuento.
```

Ejemplo:

```text
Promoción Invierno:
    SKU A -> 15%
    SKU B -> 30%
    SKU C -> S/ 20.00 de descuento
```

Reglas:

```text
- La promoción debe tener fecha de inicio y fin.
- No puede tener fecha fin menor que fecha inicio.
- El descuento no puede ser negativo.
- El porcentaje no puede superar 100.
- El precio final no puede ser negativo.
- Si genera margen negativo, debe exigir autorización y motivo.
- MS4 debe guardar el descuento aplicado en la venta.
```

---

## 16. Proveedores y compras

El MS3 administra proveedores porque están ligados a inventario.

Tabla:

```text
proveedor
```

Tipos:

```text
PERSONA_NATURAL
EMPRESA
```

Reglas:

```text
Si es EMPRESA:
    ruc y razon_social obligatorios.

Si es PERSONA_NATURAL:
    tipo_documento, numero_documento y nombres obligatorios.
```

Las compras se registran en:

```text
compra_inventario
compra_inventario_detalle
```

Flujo de compra:

```text
1. ADMIN o empleado autorizado registra compra.
2. Se selecciona proveedor.
3. Se agregan SKU, almacén, cantidad y costo.
4. La compra queda en BORRADOR.
5. Al confirmar compra, se genera entrada de inventario.
6. Se actualiza stock.
7. Se registra movimiento de kardex.
8. Se actualiza costo promedio o último costo.
9. Se audita.
10. Se emite evento StockSnapshotActualizado.
```

Estados de compra:

```text
BORRADOR
CONFIRMADA
ANULADA
```

---

## 17. Almacenes y stock

La tabla:

```text
almacen
```

representa ubicaciones donde se guarda inventario.

Campos funcionales:

```text
- principal.
- permite_venta.
- permite_compra.
- estado.
```

La tabla:

```text
stock_sku
```

controla stock por SKU y almacén.

Campos clave:

```text
stock_fisico
stock_reservado
stock_disponible
stock_minimo
stock_maximo
costo_promedio_actual
ultimo_costo_compra
```

Regla central:

```text
stock_disponible = stock_fisico - stock_reservado
```

El sistema no debe vender usando stock físico directamente.

Debe vender usando:

```text
stock_disponible
```

---

## 18. Reserva de stock para MS4

Cuando MS4 registra una venta o carrito, puede necesitar reservar stock.

Tabla:

```text
reserva_stock
```

Campos principales:

```text
- codigo_reserva.
- id_sku.
- id_almacen.
- referencia_tipo.
- referencia_id_externo.
- cantidad.
- estado_reserva.
- reservado_por_id_usuario_ms1.
- confirmado_por_id_usuario_ms1.
- liberado_por_id_usuario_ms1.
- reservado_at.
- confirmado_at.
- liberado_at.
- expires_at.
```

Estados:

```text
RESERVADA
CONFIRMADA
LIBERADA
VENCIDA
ANULADA
```

Referencia:

```text
VENTA_MS4
CARRITO_MS4
AJUSTE_MS3
```

Flujo normal:

```text
1. MS4 solicita reservar stock.
2. MS3 valida stock disponible.
3. MS3 crea reserva.
4. MS3 incrementa stock_reservado.
5. MS3 registra movimiento RESERVA_VENTA.
6. MS3 publica StockSnapshotActualizado.
7. MS4 continúa con venta.
```

Si venta se confirma:

```text
1. MS4 confirma venta.
2. MS3 confirma reserva.
3. MS3 reduce stock_fisico.
4. MS3 reduce stock_reservado.
5. MS3 registra movimiento CONFIRMACION_VENTA o SALIDA_VENTA.
6. MS3 publica StockSnapshotActualizado.
```

Si venta se cancela:

```text
1. MS4 libera reserva.
2. MS3 reduce stock_reservado.
3. MS3 registra movimiento LIBERACION_RESERVA.
4. MS3 publica StockSnapshotActualizado.
```

---

## 19. Kardex

El kardex se registra en:

```text
movimiento_inventario
```

Regla central:

```text
Todo cambio de stock debe generar movimiento de inventario.
```

Nunca se debe actualizar stock sin kardex.

Tipos de movimiento:

```text
ENTRADA_COMPRA
ENTRADA_AJUSTE
ENTRADA_DEVOLUCION
SALIDA_VENTA
SALIDA_AJUSTE
SALIDA_MERMA
SALIDA_TRASLADO
ENTRADA_TRASLADO
RESERVA_VENTA
LIBERACION_RESERVA
CONFIRMACION_VENTA
ANULACION_COMPENSATORIA
```

Campos importantes:

```text
- stock_anterior.
- stock_nuevo.
- cantidad.
- costo_unitario.
- costo_total.
- referencia_tipo.
- referencia_id_externo.
- actor_id_usuario_ms1.
- actor_id_empleado_ms2.
- actor_rol.
- request_id.
- correlation_id.
```

Reglas:

```text
- No se elimina un movimiento.
- Si hay error, se crea movimiento compensatorio.
- El movimiento debe decir quién lo hizo.
- El movimiento debe registrar stock anterior y nuevo.
- El movimiento debe poder relacionarse con venta MS4, compra o reserva.
```

---

## 20. Auditoría funcional

MS3 debe auditar operaciones críticas en:

```text
auditoria_funcional
```

Eventos recomendados:

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

La auditoría debe registrar:

```text
- Actor.
- Rol.
- Entidad.
- Acción.
- Resultado.
- Metadata.
- IP.
- User-Agent.
- Request ID.
- Correlation ID.
- Fecha.
```

---

## 21. Kafka en MS3

Kafka es obligatorio para que MS4 tenga datos locales y pueda operar aunque MS3 esté caído.

MS3 debe usar patrón Outbox.

Tabla:

```text
evento_dominio_outbox
```

El flujo correcto:

```text
1. Se ejecuta operación de negocio.
2. Se guarda la información principal.
3. Se registra auditoría.
4. Se crea evento en evento_dominio_outbox con estado PENDIENTE.
5. Se confirma la transacción.
6. Un publicador lee eventos PENDIENTES.
7. Publica a Kafka.
8. Si se publica, marca PUBLICADO.
9. Si falla, incrementa intentos.
10. Si supera intentos, marca ERROR.
11. ADMIN puede reintentar.
```

Regla:

```text
Los services de negocio no publican directo a Kafka.
Primero registran outbox.
```

---

## 22. Topics recomendados del MS3

Topics:

```text
ms3.producto.snapshot.v1
ms3.stock.snapshot.v1
ms3.precio.snapshot.v1
ms3.promocion.snapshot.v1
ms3.inventario.movimiento.v1
```

Eventos:

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

Regla:

```text
Cada evento debe ser entendible por MS4 sin consultar MS3.
```

---

## 23. Qué debe guardar MS4 como snapshot local del MS3

MS4 debe guardar localmente, como mínimo:

```text
- id_producto_ms3.
- id_sku_ms3.
- codigo_producto.
- codigo_sku.
- nombre_producto.
- slug.
- categoría.
- marca.
- atributos principales.
- imagen principal.
- precio vigente.
- promoción vigente.
- precio final.
- stock disponible.
- estado_publicacion.
- estado_venta.
- visible_publico.
- vendible.
- updated_at.
```

Esto permite que MS4 siga operando aunque MS3 esté temporalmente caído.

---

## 24. Si MS3 se cae

Si MS3 se cae, MS4 debe poder seguir funcionando con su snapshot local.

MS4 puede:

```text
- Mostrar productos desde snapshot local.
- Continuar ventas con stock local controlado.
- Reservar stock en su propia cola local.
- Registrar ventas pendientes de sincronización.
- Marcar operaciones como PENDIENTE_SYNC_MS3.
```

MS4 no debe:

```text
- Inventar productos nuevos como oficiales.
- Cambiar catálogo maestro.
- Cambiar precio maestro.
- Cambiar promoción maestra.
- Actualizar Cloudinary.
```

Regla:

```text
MS4 puede operar temporalmente con snapshot local, pero MS3 sigue siendo la fuente oficial del catálogo e inventario.
```

---

## 25. MS4 como productor hacia MS3

Cuando MS3 está caído y MS4 opera con snapshot local, MS4 debe registrar eventos pendientes para sincronizar luego con MS3.

Eventos que MS4 puede producir hacia MS3:

```text
VentaStockReservadoPendiente
VentaStockConfirmadoPendiente
VentaStockLiberadoPendiente
VentaAnuladaStockPendiente
```

Cuando MS3 revive:

```text
1. MS4 publica eventos pendientes.
2. MS3 consume o recibe los eventos.
3. MS3 valida idempotencia.
4. MS3 aplica reserva, confirmación o liberación.
5. MS3 actualiza stock real.
6. MS3 registra movimiento de inventario.
7. MS3 genera auditoría.
8. MS3 publica StockSnapshotActualizado.
9. MS4 consume el nuevo snapshot.
10. MS4 marca operación como sincronizada.
```

---

## 26. Idempotencia entre MS3 y MS4

Como Kafka puede reenviar mensajes, MS3 debe soportar idempotencia.

Regla:

```text
Un evento de MS4 no debe aplicarse dos veces.
```

MS3 debe validar por:

```text
referencia_tipo
referencia_id_externo
id_sku
id_almacen
tipo_movimiento
```

La tabla `reserva_stock` ayuda con:

```text
referencia_tipo
referencia_id_externo
id_sku
id_almacen
```

La tabla `movimiento_inventario` ayuda con:

```text
referencia_tipo
referencia_id_externo
tipo_movimiento
```

Si llega el mismo evento dos veces:

```text
- No debe duplicar salida.
- No debe duplicar reserva.
- No debe duplicar kardex.
- Debe responder como ya procesado o ignorar de forma segura.
```

---

## 27. Reconciliación entre MS3 y MS4

Puede pasar que MS4 venda mientras MS3 está caído.

Cuando MS3 vuelva, debe reconciliar.

Proceso recomendado:

```text
1. MS4 envía eventos pendientes.
2. MS3 aplica eventos idempotentes.
3. MS3 recalcula stock real por SKU.
4. MS3 publica snapshot actualizado.
5. MS4 compara su snapshot local con el snapshot oficial.
6. Si hay diferencia, MS4 ajusta su copia local.
7. Si hay conflicto grave, se marca para revisión administrativa.
```

Conflictos posibles:

```text
- MS4 vendió más stock del disponible real.
- MS3 recibió una compra mientras MS4 vendía offline.
- Un producto fue descontinuado mientras MS4 tenía snapshot antiguo.
- Una promoción terminó mientras MS4 operó offline.
- Un precio cambió mientras MS4 vendió con precio anterior.
```

Regla:

```text
Las ventas ya confirmadas no deben romperse automáticamente.
Se debe conservar el precio y descuento aplicados en MS4.
El stock se corrige mediante movimientos compensatorios o revisión.
```

---

## 28. Consistencia eventual

MS3 y MS4 no deben depender siempre de llamadas síncronas.

La arquitectura debe aceptar consistencia eventual:

```text
MS3 publica cambios.
MS4 consume y actualiza snapshot.
MS4 publica ventas/stock usado.
MS3 consume y actualiza inventario.
```

Esto significa:

```text
Durante algunos segundos puede haber diferencia entre MS3 y MS4.
El sistema debe tolerarlo.
Los eventos deben corregir la diferencia.
```

Regla:

```text
La consistencia final debe lograrse por eventos, snapshots, idempotencia y reconciliación.
```

---

## 29. Flujo normal de venta con MS3 disponible

```text
1. Cliente o empleado inicia venta en MS4.
2. MS4 consulta su snapshot local de productos.
3. MS4 valida precio, promoción y stock local.
4. MS4 solicita a MS3 reservar stock o envía comando de reserva.
5. MS3 valida stock_disponible.
6. MS3 crea reserva_stock.
7. MS3 actualiza stock_reservado.
8. MS3 registra movimiento RESERVA_VENTA.
9. MS3 publica StockSnapshotActualizado.
10. MS4 confirma venta.
11. MS4 solicita confirmar reserva.
12. MS3 descuenta stock_fisico y stock_reservado.
13. MS3 registra CONFIRMACION_VENTA.
14. MS3 publica nuevo snapshot.
15. MS4 guarda venta final con precio, promoción y stock confirmado.
```

---

## 30. Flujo de venta con MS3 caído

```text
1. Cliente o empleado inicia venta en MS4.
2. MS4 usa snapshot local.
3. MS4 valida stock local.
4. MS4 registra venta como confirmada o pendiente según política.
5. MS4 registra evento pendiente para MS3.
6. MS4 descuenta su stock local temporal.
7. Cuando MS3 revive, MS4 publica eventos pendientes.
8. MS3 aplica eventos de forma idempotente.
9. MS3 registra kardex.
10. MS3 publica StockSnapshotActualizado.
11. MS4 reconcilia su snapshot.
```

Política recomendada:

```text
Para ventas críticas, MS4 puede marcar PENDIENTE_CONFIRMACION_STOCK.
Para ventas permitidas offline, MS4 marca PENDIENTE_SYNC_MS3.
```

---

## 31. Flujo de creación de producto

```text
1. ADMIN ingresa datos base.
2. MS3 valida permisos.
3. MS3 genera codigo_producto.
4. MS3 genera slug.
5. MS3 guarda producto en BORRADOR.
6. MS3 audita PRODUCTO_CREADO.
7. Si ya tiene datos suficientes, puede publicar luego.
```

El producto no debe ser vendible automáticamente.

Debe cumplir:

```text
- Producto activo.
- SKU activo.
- Precio vigente.
- Stock disponible.
- Estado publicación correcto.
- Estado venta VENDIBLE.
```

---

## 32. Flujo de creación de SKU

```text
1. ADMIN o empleado autorizado crea variante.
2. MS3 genera codigo_sku.
3. MS3 valida que el producto exista y esté activo.
4. MS3 guarda SKU.
5. MS3 audita SKU_CREADO.
6. MS3 publica ProductoSnapshotActualizado si el producto ya está público.
```

---

## 33. Flujo de publicación de producto

Para publicar producto se debe validar:

```text
- Producto activo.
- Categoría activa.
- Tipo de producto activo.
- Marca activa si aplica.
- Al menos un SKU activo.
- Precio vigente por SKU vendible.
- Imagen principal si la política la exige.
- Estado de venta coherente.
```

Si falta algo:

```text
MS3 debe rechazar publicación con error funcional.
```

Ejemplo:

```text
PRODUCTO_NO_TIENE_SKU_ACTIVO
PRODUCTO_NO_TIENE_PRECIO_VIGENTE
PRODUCTO_NO_TIENE_IMAGEN_PRINCIPAL
PRODUCTO_NO_TIENE_STOCK_DISPONIBLE
```

---

## 34. Flujo de actualización de precio

```text
1. ADMIN registra nuevo precio.
2. MS3 valida SKU activo.
3. MS3 valida precio > 0.
4. MS3 cierra precio vigente anterior.
5. MS3 crea nuevo precio vigente.
6. MS3 audita.
7. MS3 crea evento outbox PrecioSnapshotActualizado.
8. MS4 consume y actualiza snapshot.
```

No se actualiza el precio anterior.

---

## 35. Flujo de promoción

```text
1. ADMIN crea promoción.
2. ADMIN define versión con fechas.
3. ADMIN asocia SKU.
4. ADMIN define descuento por SKU.
5. MS3 valida fecha, descuento y margen.
6. MS3 activa o programa promoción.
7. MS3 audita.
8. MS3 publica PromocionSnapshotActualizada.
9. MS4 consume promoción.
```

Regla:

```text
La promoción no afecta ventas pasadas.
MS4 guarda descuento usado en cada venta.
```

---

## 36. Flujo de compra y entrada de inventario

```text
1. ADMIN o empleado autorizado registra compra.
2. Selecciona proveedor.
3. Agrega SKU, almacén, cantidad y costo.
4. Compra queda en BORRADOR.
5. Al confirmar compra, MS3 registra entrada.
6. Actualiza stock_fisico.
7. Recalcula costo promedio o último costo.
8. Registra kardex ENTRADA_COMPRA.
9. Audita.
10. Publica StockSnapshotActualizado.
```

---

## 37. Flujo de ajuste de stock

```text
1. ADMIN o empleado autorizado solicita ajuste.
2. Debe indicar SKU, almacén, cantidad y motivo.
3. MS3 valida permisos.
4. MS3 valida stock.
5. MS3 registra movimiento ENTRADA_AJUSTE o SALIDA_AJUSTE.
6. MS3 actualiza stock.
7. MS3 audita.
8. MS3 publica StockSnapshotActualizado.
```

Regla:

```text
Todo ajuste manual debe exigir motivo.
```

---

## 38. Seguridad interna del MS3

MS3 debe tener:

```text
security
├── config
├── jwt
├── principal
├── roles
├── filter
└── handler
```

Debe comportarse como Resource Server:

```text
- Valida JWT emitido por MS1.
- Lee roles.
- Construye actor autenticado.
- No crea tokens.
- No hace login.
```

Rutas públicas:

```text
GET /api/ms3/public/**
```

Rutas protegidas:

```text
/api/ms3/admin/**
/api/ms3/inventario/**
/api/ms3/catalogo/interno/**
/api/ms3/outbox/**
/api/ms3/auditoria/**
```

---

## 39. Policies del MS3

Policies recomendadas:

```text
ProductoPolicy
CategoriaPolicy
MarcaPolicy
SkuPolicy
PrecioPolicy
PromocionPolicy
ProveedorPolicy
CompraInventarioPolicy
StockPolicy
MovimientoInventarioPolicy
EmpleadoInventarioPermisoPolicy
AuditoriaPolicy
OutboxPolicy
CloudinaryPolicy
```

Responsabilidad:

```text
Policy decide si el actor puede ejecutar una acción.
```

No debe validar duplicados, precios o stock. Eso corresponde a validators.

---

## 40. Validators del MS3

Validators recomendados:

```text
ProductoValidator
CategoriaValidator
MarcaValidator
SkuValidator
AtributoValidator
PrecioValidator
PromocionValidator
ProveedorValidator
CompraInventarioValidator
StockValidator
ReservaStockValidator
MovimientoInventarioValidator
CloudinaryImageValidator
EmpleadoInventarioPermisoValidator
OutboxValidator
```

Responsabilidad:

```text
Validator valida datos y reglas de negocio.
```

Ejemplo:

```text
PrecioValidator:
    precio > 0
    moneda válida
    solo un precio vigente
    fechas coherentes

StockValidator:
    stock disponible suficiente
    stock no negativo
    reserva no mayor que físico

PromocionValidator:
    fecha fin >= fecha inicio
    descuento válido
    porcentaje <= 100
```

---

## 41. Services del MS3

Services principales:

```text
ProductoService
ProductoSkuService
CategoriaService
MarcaService
AtributoService
ProductoImagenService
CloudinaryService
PrecioSkuService
PromocionService
ProveedorService
CompraInventarioService
StockService
ReservaStockService
MovimientoInventarioService
KardexService
EmpleadoInventarioPermisoService
AuditoriaFuncionalService
EventoDominioOutboxService
KafkaPublisherService
ReconciliacionMs4Service
```

Regla:

```text
Controller recibe HTTP.
Service orquesta.
Policy autoriza.
Validator valida.
Repository persiste.
Mapper convierte.
Outbox registra eventos.
Kafka publica después.
```

---

## 42. CloudinaryService

Responsabilidades:

```text
- Subir imagen.
- Eliminar o invalidar imagen si la política lo permite.
- Obtener metadata.
- Validar respuesta de Cloudinary.
- Manejar errores externos.
```

No debe:

```text
- Decidir si el usuario tiene permiso.
- Decidir si el producto se puede publicar.
- Guardar producto.
```

Eso lo coordina `ProductoImagenService`.

---

## 43. EventoDominioOutboxService

Responsabilidades:

```text
- Crear evento pendiente.
- Listar eventos.
- Marcar publicado.
- Marcar error.
- Reintentar evento.
```

Ningún service de negocio debe usar KafkaTemplate directamente.

---

## 44. Reglas de base de datos

La base de datos del MS3 sigue estas reglas:

```text
- Sin eliminación física.
- estado = 1 significa activo.
- estado = 0 significa eliminado lógico/inactivo.
- Unicidades filtradas para estado = 1.
- Precio versionado.
- Promoción versionada.
- Permisos versionados.
- Kardex como historial permanente.
- Outbox para Kafka.
- Cloudinary como metadata, no binario.
```

Tablas principales:

```text
correlativo_codigo
tipo_producto
categoria
marca
atributo
tipo_producto_atributo
producto
producto_sku
producto_atributo_valor
sku_atributo_valor
producto_imagen_cloudinary
proveedor
almacen
stock_sku
empleado_snapshot_ms2
empleado_inventario_permiso_historial
precio_sku_historial
promocion
promocion_version
promocion_sku_descuento_version
compra_inventario
compra_inventario_detalle
reserva_stock
movimiento_inventario
auditoria_funcional
evento_dominio_outbox
```

---

## 45. Errores funcionales sugeridos

Productos:

```text
PRODUCTO_NO_ENCONTRADO
PRODUCTO_INACTIVO
PRODUCTO_DESCONTINUADO
PRODUCTO_NO_PUBLICABLE
PRODUCTO_NO_VENDIBLE
PRODUCTO_NO_TIENE_SKU_ACTIVO
PRODUCTO_NO_TIENE_IMAGEN_PRINCIPAL
PRODUCTO_NO_TIENE_PRECIO_VIGENTE
```

SKU:

```text
SKU_NO_ENCONTRADO
SKU_INACTIVO
SKU_SIN_STOCK
SKU_NO_VENDIBLE
```

Stock:

```text
STOCK_INSUFICIENTE
STOCK_RESERVADO_SUPERA_FISICO
RESERVA_NO_ENCONTRADA
RESERVA_YA_CONFIRMADA
RESERVA_YA_LIBERADA
RESERVA_VENCIDA
```

Precio:

```text
PRECIO_INVALIDO
PRECIO_VIGENTE_YA_EXISTE
PRECIO_REQUIERE_MOTIVO
```

Promoción:

```text
PROMOCION_FECHA_INVALIDA
PROMOCION_DESCUENTO_INVALIDO
PROMOCION_SKU_DUPLICADO
PROMOCION_MARGEN_NEGATIVO_REQUIERE_AUTORIZACION
```

Cloudinary:

```text
IMAGEN_INVALIDA
IMAGEN_CLOUDINARY_ERROR
IMAGEN_PRINCIPAL_DUPLICADA
```

Kafka:

```text
EVENTO_OUTBOX_NO_REINTENTABLE
EVENTO_KAFKA_PUBLICACION_FALLIDA
EVENTO_DUPLICADO_YA_PROCESADO
```

---

## 46. Criterios para considerar MS3 listo

```text
[ ] Compila correctamente.
[ ] Valida JWT emitido por MS1.
[ ] Tiene rutas públicas solo para catálogo.
[ ] Bloquea rutas internas sin JWT.
[ ] Genera códigos automáticamente.
[ ] Genera slug automáticamente.
[ ] Maneja producto base y SKU.
[ ] Maneja atributos dinámicos.
[ ] Sube imágenes a Cloudinary.
[ ] Guarda metadata Cloudinary.
[ ] Versiona precios.
[ ] Versiona promociones.
[ ] Maneja proveedores.
[ ] Maneja compras.
[ ] Maneja almacenes.
[ ] Maneja stock físico, reservado y disponible.
[ ] Registra kardex en todo cambio de stock.
[ ] Permite reserva para MS4.
[ ] Permite confirmación/liberación de reserva.
[ ] Audita operaciones críticas.
[ ] Usa Outbox.
[ ] Publica eventos Kafka.
[ ] MS4 puede consumir snapshots.
[ ] MS4 puede operar si MS3 cae.
[ ] MS3 puede reconciliar eventos pendientes de MS4.
[ ] No elimina físicamente datos funcionales.
```

---

## 47. Regla final del MS3

El MS3 debe comportarse como el microservicio dueño del catálogo e inventario.

Regla definitiva:

```text
MS3 administra la verdad oficial del producto, SKU, precio, promoción, imagen,
proveedor, compra, almacén, stock, reserva y kardex.

MS4 administra ventas, pero depende de snapshots de MS3 para operar.
Si MS3 cae, MS4 puede operar temporalmente con su copia local.
Cuando MS3 revive, MS4 debe enviar los movimientos pendientes.
MS3 debe aplicar esos movimientos de forma idempotente y reconciliar stock.
```

El objetivo no es solo registrar productos. El objetivo es construir un sistema capaz de:

```text
- Publicar productos.
- Vender productos.
- Controlar inventario.
- Soportar caídas.
- Mantener consistencia eventual.
- Evitar pérdida de stock.
- Evitar afectar ventas pasadas.
- Mantener trazabilidad profesional.
- Sincronizar MS3 y MS4 con Kafka.
```

---

## 48. Decisión oficial

`ms-catalogo-inventario` será el microservicio responsable de catálogo e inventario.

Tendrá base de datos propia en SQL Server, integrará Cloudinary para imágenes, usará Kafka con patrón Outbox para sincronizar con MS4 y mantendrá auditoría funcional de operaciones críticas.

MS3 no dependerá de llamadas permanentes a MS4 para mostrar catálogo, y MS4 no dependerá de llamadas permanentes a MS3 para vender. Ambos trabajarán con sincronización por eventos, snapshots, idempotencia y reconciliación.

Esta RN será la base para definir luego:

```text
- RN-CODE-MS3.md
- Contratos HTTP del MS3
- Eventos Kafka MS3 → MS4
- Eventos Kafka MS4 → MS3
- DTOs
- Mappers
- Validators
- Policies
- Services
- Controllers
```


