# Documentación técnica de código - MS3 Parte 1

**Microservicio:** `ms-catalogo-inventario`  
**Paquetes documentados:** clase raíz, `config`, `security`, `controller`  
**Objetivo:** definir la responsabilidad técnica de cada archivo antes de programarlo.  
**Regla general:** esta documentación no implementa código; define cómo debe programarse cada clase, qué debe consumir, quién debe usarla, cómo debe coexistir y qué debe evitar.

---

# 1. Clase raíz

## `Ms3Application.java`

**Ruta:** `com.upsjb.ms3.Ms3Application`

### Responsabilidad

Clase principal de arranque del microservicio `ms-catalogo-inventario`.

Debe contener únicamente la inicialización de Spring Boot.

Debe ser la entrada oficial del runtime del MS3 y no debe contener lógica de negocio, configuración manual compleja ni inicialización de datos.

### Qué debe hacer

- Declarar `@SpringBootApplication`.
- Ejecutar `SpringApplication.run(Ms3Application.class, args)`.
- Permitir que Spring Boot escanee los paquetes bajo `com.upsjb.ms3`.

### Qué consume

- Spring Boot.
- Configuración declarada por propiedades externas.
- Beans definidos en paquetes `config`, `security`, `service`, `repository`, etc.

### Quién la usa

- El runtime de la aplicación.
- Docker, Maven, Gradle o cualquier comando que levante el microservicio.
- Tests de contexto si se crean pruebas de carga de contexto.

### Coexistencia

Coexiste con todo el proyecto como punto de entrada, pero no debe conocer detalles internos de:

- Productos.
- Inventario.
- Stock.
- Kafka.
- Cloudinary.
- Seguridad.
- Controladores.
- Repositories.

### Qué NO debe hacer

- No declarar beans manuales.
- No ejecutar inserts iniciales.
- No cargar secretos.
- No crear topics Kafka.
- No llamar Cloudinary.
- No registrar datos de prueba.
- No contener lógica de auditoría.
- No manejar excepciones.
- No configurar seguridad directamente.

---

# 2. Paquete `config`

---

## `AppPropertiesConfig.java`

**Ruta:** `com.upsjb.ms3.config.AppPropertiesConfig`

### Responsabilidad

Centralizar propiedades generales propias del MS3 mediante `@ConfigurationProperties`.

Esta clase debe representar parámetros funcionales y técnicos del microservicio que no pertenecen directamente a Cloudinary, Kafka, Outbox, MS2 ni MS4.

### Qué debe contener

Propiedades generales como:

- Nombre lógico del microservicio.
- Identificador del sistema.
- Configuración de paginación por defecto.
- Tamaño máximo permitido para listados.
- Configuración funcional de catálogo público.
- Configuración funcional de productos.
- Configuración de publicación.
- Reglas parametrizables de imágenes obligatorias.
- Reglas generales de stock visible.
- Configuración de trazabilidad si no se centraliza en otra clase.

Ejemplos conceptuales de propiedades:

```properties
ms3.application-name=ms-catalogo-inventario
ms3.pagination.default-size=20
ms3.pagination.max-size=100
ms3.catalogo.public-stock-visible=false
ms3.producto.require-main-image-to-publish=true
```

### Qué consume

- Jakarta Validation para validar propiedades obligatorias.
- Spring Boot `@ConfigurationProperties`.
- Valores externos desde `application.yml`, variables de entorno o configuración por perfil.

### Quién la usa

- Services que requieren reglas parametrizables.
- Validators que dependen de banderas funcionales.
- Controllers indirectamente mediante services.
- Factories de respuesta si requieren metadata del microservicio.
- Componentes de auditoría si requieren nombre de servicio.

### Coexistencia

Debe coexistir con:

- `CloudinaryProperties`: no debe duplicar credenciales o configuración de Cloudinary.
- `KafkaTopicProperties`: no debe guardar nombres de topics.
- `OutboxProperties`: no debe guardar configuración de reintentos o scheduler de outbox.
- `Ms2IntegrationProperties`: no debe guardar URLs de MS2.
- `Ms4IntegrationProperties`: no debe guardar URLs de MS4.

### Qué NO debe hacer

- No contener secretos.
- No contener credenciales.
- No crear beans de infraestructura.
- No leer `Environment` manualmente.
- No tener lógica de negocio.
- No tener métodos que consulten base de datos.
- No duplicar propiedades específicas de otros archivos de configuración.

---

## `CloudinaryProperties.java`

**Ruta:** `com.upsjb.ms3.config.CloudinaryProperties`

### Responsabilidad

Representar la configuración externa requerida para integrar MS3 con Cloudinary.

Debe ser una clase de propiedades validada, no un cliente operativo.

### Qué debe contener

- Cloud name.
- API key.
- API secret.
- Carpeta base para productos.
- Carpeta base para SKU si aplica.
- Tamaño máximo permitido.
- Formatos permitidos.
- Resource type permitido.
- Tiempo máximo de operación si se configura.
- Bandera para habilitar/deshabilitar integración en entornos locales.

Ejemplos conceptuales:

```properties
cloudinary.cloud-name=...
cloudinary.api-key=...
cloudinary.api-secret=...
cloudinary.folder.productos=ms3/productos
cloudinary.max-file-size-mb=5
cloudinary.allowed-formats=jpg,jpeg,png,webp
```

### Qué consume

- Valores externos.
- Jakarta Validation.
- Spring Boot `@ConfigurationProperties`.

### Quién la usa

- `CloudinaryClientConfig`.
- `CloudinaryClientImpl`.
- `CloudinaryServiceImpl`.
- `CloudinaryImageValidator`.
- `ProductoImagenServiceImpl`.

### Coexistencia

- `CloudinaryClientConfig` usa esta clase para construir el cliente oficial de Cloudinary.
- `CloudinaryImageValidator` usa reglas como tamaño máximo y formatos permitidos.
- `ProductoImagenServiceImpl` usa reglas de carpeta y metadata esperada.
- `ProductoImagenController` no debe leer esta clase directamente.

### Qué NO debe hacer

- No subir imágenes.
- No eliminar imágenes.
- No validar permisos.
- No decidir si una imagen puede ser principal.
- No exponer secretos en logs.
- No devolver esta configuración en endpoints públicos.
- No usar valores hardcodeados en código.

---

## `CloudinaryClientConfig.java`

**Ruta:** `com.upsjb.ms3.config.CloudinaryClientConfig`

### Responsabilidad

Crear y configurar el bean técnico del cliente de Cloudinary usando `CloudinaryProperties`.

Debe aislar la construcción del cliente externo para que el resto de la aplicación consuma una abstracción mediante `CloudinaryClient`.

### Qué debe hacer

- Crear el bean oficial del SDK de Cloudinary, si se usa SDK.
- Aplicar cloud name, API key y API secret desde `CloudinaryProperties`.
- Configurar opciones comunes de conexión si el SDK lo permite.
- No exponer credenciales.
- Permitir que `CloudinaryClientImpl` consuma el cliente ya configurado.

### Qué consume

- `CloudinaryProperties`.
- SDK oficial de Cloudinary, si se usa.
- Spring `@Configuration`.

### Quién la usa

- Spring Container.
- `CloudinaryClientImpl`, directa o indirectamente.
- `CloudinaryServiceImpl`, indirectamente.

### Coexistencia

- `CloudinaryProperties` define datos.
- `CloudinaryClientConfig` crea cliente.
- `CloudinaryClientImpl` ejecuta operaciones.
- `ProductoImagenServiceImpl` orquesta la lógica funcional.
- `ProductoImagenController` solo recibe HTTP.

### Qué NO debe hacer

- No validar archivo de imagen.
- No decidir rutas de negocio.
- No subir imagen en el arranque.
- No consultar base de datos.
- No registrar secretos.
- No reemplazar `CloudinaryClientImpl`.

---

## `KafkaTopicProperties.java`

**Ruta:** `com.upsjb.ms3.config.KafkaTopicProperties`

### Responsabilidad

Centralizar nombres de topics Kafka usados por MS3.

Debe permitir que los topics sean configurables por ambiente sin hardcodear strings en services, outbox o producers.

### Qué debe contener

Topics producidos por MS3:

- Producto snapshot.
- Stock snapshot.
- Precio snapshot.
- Promoción snapshot.
- Movimiento de inventario.

Topics consumidos desde MS4:

- Comandos o eventos de stock generados por ventas.
- Reservas pendientes.
- Confirmaciones pendientes.
- Liberaciones pendientes.
- Anulaciones de venta con impacto en stock.

Ejemplos conceptuales:

```properties
kafka.topics.producto-snapshot=ms3.producto.snapshot.v1
kafka.topics.stock-snapshot=ms3.stock.snapshot.v1
kafka.topics.precio-snapshot=ms3.precio.snapshot.v1
kafka.topics.promocion-snapshot=ms3.promocion.snapshot.v1
kafka.topics.ms4-stock-command=ms4.stock.command.v1
```

### Qué consume

- Propiedades externas.
- Jakarta Validation.
- Spring Boot `@ConfigurationProperties`.

### Quién la usa

- `KafkaTopicResolver`.
- `OutboxEventFactory`.
- `EventoDominioOutboxServiceImpl`.
- `KafkaPublisherServiceImpl`.
- `Ms4StockCommandConsumer`.
- `KafkaDomainEventPublisher`.

### Coexistencia

- No publica eventos por sí misma.
- No serializa eventos.
- No decide qué evento corresponde a qué operación.
- Solo centraliza nombres de topics para evitar duplicidad.

### Qué NO debe hacer

- No crear topics.
- No conectarse a Kafka.
- No contener payloads.
- No guardar configuración de producer si Spring Boot ya la maneja por properties.
- No guardar reglas de negocio.

---

## `OutboxProperties.java`

**Ruta:** `com.upsjb.ms3.config.OutboxProperties`

### Responsabilidad

Centralizar configuración funcional/técnica del patrón Outbox del MS3.

El patrón Outbox permite que las operaciones de negocio y el registro de eventos pendientes sean transaccionales, evitando publicar directamente a Kafka desde los services de negocio.

### Qué debe contener

- Tamaño de lote de publicación.
- Máximo de intentos.
- Intervalo de scheduler.
- Tiempo de bloqueo de evento.
- Identificador del publicador.
- Bandera para habilitar o deshabilitar scheduler.
- Tiempo mínimo para reintentar eventos fallidos.
- Estado máximo de retry si aplica.

Ejemplo conceptual:

```properties
outbox.enabled=true
outbox.batch-size=50
outbox.max-attempts=5
outbox.lock-timeout-seconds=60
outbox.publisher-id=ms3-outbox-publisher-1
```

### Qué consume

- Propiedades externas.
- Jakarta Validation.
- Spring Boot `@ConfigurationProperties`.

### Quién la usa

- `OutboxScheduler`.
- `OutboxEventPublisher`.
- `OutboxRetryPolicy`.
- `OutboxLockService`.
- `EventoDominioOutboxServiceImpl`.
- `KafkaPublisherServiceImpl`.

### Coexistencia

- `EventoDominioOutboxServiceImpl` registra eventos.
- `OutboxScheduler` programa la publicación.
- `OutboxEventPublisher` lee pendientes.
- `KafkaDomainEventPublisher` publica a Kafka.
- `OutboxProperties` solo define parámetros.

### Qué NO debe hacer

- No publicar a Kafka.
- No consultar eventos.
- No bloquear filas.
- No cambiar estados.
- No contener lógica de negocio.
- No reemplazar `OutboxRetryPolicy`.

---

## `Ms2IntegrationProperties.java`

**Ruta:** `com.upsjb.ms3.config.Ms2IntegrationProperties`

### Responsabilidad

Representar configuración de integración con `ms-personas-clientes-empleados`.

MS3 usa MS2 principalmente para sincronizar o consultar datos mínimos de empleado cuando sea necesario.

### Qué debe contener

- URL base interna del MS2.
- Timeout de conexión.
- Timeout de lectura.
- Endpoints internos si se parametrizan.
- Bandera de integración habilitada.
- Configuración de fallback si aplica.

Ejemplo conceptual:

```properties
integrations.ms2.base-url=http://ms-personas-clientes-empleados:8082
integrations.ms2.enabled=true
integrations.ms2.timeout-seconds=5
```

### Qué consume

- Propiedades externas.
- Jakarta Validation.

### Quién la usa

- `Ms2EmpleadoSnapshotClientImpl`.
- `EmpleadoSnapshotMs2ServiceImpl`.
- `EmpleadoInventarioPermisoServiceImpl`, indirectamente.
- `Ms2ClientErrorMapper`.

### Coexistencia

- No debe ser usado por controllers directamente.
- La sincronización ideal debe ser por eventos o procesos controlados.
- Las llamadas HTTP a MS2 deben ser excepcionales o para recuperación/sincronización puntual.

### Qué NO debe hacer

- No consultar MS2 por sí misma.
- No guardar datos de empleados.
- No validar permisos.
- No duplicar reglas de MS2.
- No contener credenciales hardcodeadas.

---

## `Ms4IntegrationProperties.java`

**Ruta:** `com.upsjb.ms3.config.Ms4IntegrationProperties`

### Responsabilidad

Representar configuración de integración HTTP con `ms-ventas-facturacion`.

Aunque la sincronización principal debe ser por Kafka, puede existir integración HTTP controlada para reconciliación, diagnóstico o endpoints internos.

### Qué debe contener

- URL base interna de MS4.
- Timeouts.
- Bandera de integración habilitada.
- Endpoints internos para reconciliación si se requieren.
- Configuración de retry HTTP si se decide implementar a nivel de cliente.

Ejemplo conceptual:

```properties
integrations.ms4.base-url=http://ms-ventas-facturacion:8084
integrations.ms4.enabled=true
integrations.ms4.timeout-seconds=5
```

### Qué consume

- Propiedades externas.
- Jakarta Validation.

### Quién la usa

- `Ms4StockSyncClientImpl`.
- `Ms4ReconciliacionServiceImpl`.
- `Ms4ClientErrorMapper`.

### Coexistencia

- Kafka debe ser la sincronización principal.
- HTTP hacia MS4 no debe ser requerido para cada venta normal.
- MS3 debe poder procesar eventos Kafka de MS4 sin depender de llamadas síncronas.

### Qué NO debe hacer

- No llamar MS4 directamente.
- No reconciliar stock por sí misma.
- No contener reglas de venta.
- No duplicar lógica de MS4.
- No reemplazar eventos Kafka.

---

# 3. Paquete `security`

---

## `security/config/SecurityConfig.java`

**Ruta:** `com.upsjb.ms3.security.config.SecurityConfig`

### Responsabilidad

Configurar las reglas de seguridad HTTP del MS3.

Debe definir qué rutas son públicas, qué rutas requieren autenticación y cómo se manejan respuestas 401/403.

### Qué debe hacer

- Configurar seguridad stateless.
- Deshabilitar CSRF si el MS3 funciona como API stateless.
- Permitir rutas públicas de catálogo.
- Proteger rutas administrativas, inventario, auditoría y outbox.
- Registrar handlers personalizados para 401 y 403.
- Integrarse con Resource Server JWT.
- Aplicar autorización base por ruta.

Rutas públicas esperadas:

```text
GET /api/ms3/public/**
```

Rutas protegidas esperadas:

```text
/api/ms3/admin/**
/api/ms3/inventario/**
/api/ms3/catalogo/**
/api/ms3/outbox/**
/api/ms3/auditoria/**
```

### Qué consume

- `ResourceServerConfig`.
- `RestAuthenticationEntryPoint`.
- `RestAccessDeniedHandler`.
- `SecurityRoles`.
- `RoleJwtAuthenticationConverter`, indirectamente.
- Spring Security.

### Quién la usa

- Spring Security Filter Chain.
- Todas las peticiones HTTP entrantes al MS3.

### Coexistencia

- Valida seguridad general.
- No reemplaza policies.
- Las policies validan autorización contextual fina dentro de services.
- Los controllers no deben contener lógica de autorización compleja.
- El Gateway puede validar JWT, pero MS3 también debe protegerse como Resource Server.

### Qué NO debe hacer

- No hacer login.
- No emitir JWT.
- No validar stock.
- No validar permisos funcionales de inventario.
- No consultar base de datos para reglas de negocio.
- No abrir `/api/ms3/admin/**` públicamente.
- No permitir rutas internas sin token.

---

## `security/config/ResourceServerConfig.java`

**Ruta:** `com.upsjb.ms3.security.config.ResourceServerConfig`

### Responsabilidad

Configurar detalles específicos del MS3 como OAuth2 Resource Server.

Debe permitir que MS3 valide JWT emitidos por MS1 y convierta claims de rol a authorities de Spring Security.

### Qué debe hacer

- Configurar JWT Resource Server.
- Registrar `RoleJwtAuthenticationConverter`.
- Configurar validación de issuer/audience si se define por properties.
- Definir cómo extraer authorities desde claims.

### Qué consume

- `RoleJwtAuthenticationConverter`.
- `JwtClaimNames`.
- Propiedades de Spring Security OAuth2 Resource Server.
- Spring Security OAuth2.

### Quién la usa

- `SecurityConfig`.
- Spring Security durante la autenticación de cada request protegida.

### Coexistencia

- `RoleJwtAuthenticationConverter` transforma claims a authorities.
- `CurrentUserResolver` usa el contexto autenticado ya construido.
- `AuthenticatedUserContext` representa al actor interno.
- `RequestAuditContextFilter` complementa con metadata de request.

### Qué NO debe hacer

- No crear tokens.
- No renovar tokens.
- No conectarse a MS1.
- No consultar usuario en base de datos.
- No asignar roles por defecto si el JWT no los trae.
- No inventar permisos funcionales de inventario.

---

## `security/jwt/JwtClaimNames.java`

**Ruta:** `com.upsjb.ms3.security.jwt.JwtClaimNames`

### Responsabilidad

Centralizar los nombres de claims esperados en el JWT emitido por MS1.

Evita strings repetidos en convertidores, resolvers y filtros.

### Qué debe contener

Constantes como:

```text
USER_ID
USERNAME
EMAIL
ROL
ROLES
AUTHORITIES
SESSION_ID
```

### Qué consume

- Nada externo.
- Debe ser clase final con constructor privado.

### Quién la usa

- `RoleJwtAuthenticationConverter`.
- `CurrentUserResolver`.
- `AuthenticatedUserContext`.
- Services o auditoría si necesitan datos del actor, preferentemente a través de `CurrentUserResolver`.

### Coexistencia

- `SecurityRoles` centraliza los roles del sistema.
- `JwtClaimNames` centraliza nombres de claims.
- No deben mezclarse responsabilidades.

### Qué NO debe hacer

- No parsear JWT.
- No validar tokens.
- No contener valores reales de tokens.
- No contener contraseñas.
- No contener roles como valores funcionales si eso pertenece a `SecurityRoles`.

---

## `security/jwt/RoleJwtAuthenticationConverter.java`

**Ruta:** `com.upsjb.ms3.security.jwt.RoleJwtAuthenticationConverter`

### Responsabilidad

Convertir claims del JWT emitido por MS1 en authorities válidas para Spring Security.

Debe soportar múltiples formatos de claims por compatibilidad.

### Qué debe hacer

- Leer claims como `rol`, `roles` o `authorities`.
- Normalizar roles.
- Convertir `ADMIN` en `ROLE_ADMIN`.
- Convertir `EMPLEADO` en `ROLE_EMPLEADO`.
- Convertir `CLIENTE` en `ROLE_CLIENTE`.
- Evitar roles vacíos.
- No conceder roles inexistentes.

### Qué consume

- `JwtClaimNames`.
- `SecurityRoles`.
- Spring Security `Jwt`.

### Quién la usa

- `ResourceServerConfig`.
- Spring Security al autenticar requests.

### Coexistencia

- Solo transforma rol técnico.
- Las permissions finas de inventario son responsabilidad de:
  - `EmpleadoInventarioPermisoService`.
  - `EmpleadoInventarioPermisoPolicy`.
  - `ProductoPolicy`.
  - `StockPolicy`.
  - `CompraInventarioPolicy`.

### Qué NO debe hacer

- No consultar base de datos.
- No llamar MS1.
- No llamar MS2.
- No otorgar permisos de inventario.
- No crear usuarios.
- No crear roles nuevos.
- No aceptar claims vacíos como ADMIN.

---

## `security/principal/AuthenticatedUserContext.java`

**Ruta:** `com.upsjb.ms3.security.principal.AuthenticatedUserContext`

### Responsabilidad

Representar al usuario autenticado dentro del MS3.

Debe ser un objeto limpio y estable que contenga los datos mínimos del actor que ejecuta una operación.

### Qué debe contener

- `idUsuarioMs1`.
- `username`.
- `email`.
- `rolPrincipal`.
- `authorities`.
- `sessionId`, si existe.
- Métodos auxiliares como:
  - `isAdmin()`.
  - `isEmpleado()`.
  - `isCliente()`.
  - `hasRole(String role)`.

### Qué consume

- Datos extraídos del `Authentication`.
- `SecurityRoles`.

### Quién la usa

- Controllers para recibir el actor actual si se usa argument resolver.
- Services para auditoría y reglas.
- Policies para autorización.
- Audit context.
- Movimiento de inventario.
- Outbox event factory.

### Coexistencia

- `CurrentUserResolver` lo construye.
- `AuthenticatedUserArgumentResolver` lo inyecta en controllers.
- `RequestAuditContextFilter` puede usarlo para poblar auditoría.
- Policies lo consumen para decidir permisos.

### Qué NO debe hacer

- No consultar base de datos.
- No cargar empleado desde MS2.
- No validar permisos de inventario.
- No modificar SecurityContext.
- No contener lógica de negocio del dominio.

---

## `security/principal/CurrentUserResolver.java`

**Ruta:** `com.upsjb.ms3.security.principal.CurrentUserResolver`

### Responsabilidad

Resolver el usuario autenticado actual desde el contexto de Spring Security.

Debe construir un `AuthenticatedUserContext` confiable para controllers, services y auditoría.

### Qué debe hacer

- Leer `SecurityContextHolder`.
- Extraer `Authentication`.
- Validar que el request esté autenticado si la operación lo requiere.
- Extraer claims del JWT.
- Crear `AuthenticatedUserContext`.
- Lanzar `UnauthorizedException` si no hay usuario cuando es obligatorio.

### Qué consume

- `JwtClaimNames`.
- `AuthenticatedUserContext`.
- `SecurityRoles`.
- Spring Security.

### Quién la usa

- Controllers.
- Services.
- Policies, indirectamente.
- `AuthenticatedUserArgumentResolver`.
- `RequestAuditContextFilter`.

### Coexistencia

- No decide si una operación es permitida; eso lo hacen policies.
- Solo resuelve quién es el actor.
- Debe mantener consistencia con `RoleJwtAuthenticationConverter`.

### Qué NO debe hacer

- No validar reglas de inventario.
- No consultar permisos de empleado.
- No llamar MS1.
- No crear usuarios.
- No devolver usuario falso si no hay token.

---

## `security/principal/AuthenticatedUserArgumentResolver.java`

**Ruta:** `com.upsjb.ms3.security.principal.AuthenticatedUserArgumentResolver`

### Responsabilidad

Permitir inyectar `AuthenticatedUserContext` como argumento en métodos de controller.

Esto evita repetir código de extracción de usuario en cada controller.

### Qué debe hacer

- Implementar un resolver de argumentos MVC si se usa patrón de argumento personalizado.
- Detectar parámetros de tipo `AuthenticatedUserContext`.
- Delegar a `CurrentUserResolver`.
- Entregar el actor actual al controller.

### Qué consume

- `CurrentUserResolver`.
- Spring MVC argument resolver.
- `AuthenticatedUserContext`.

### Quién la usa

- Controllers protegidos.
- Spring MVC durante el binding de argumentos.

### Coexistencia

- Requiere configuración MVC si Spring Boot no lo registra automáticamente.
- Puede coexistir con anotaciones como `@AuthenticationPrincipal`, pero estandariza el acceso al actor.
- No reemplaza `CurrentUserResolver`; lo usa.

### Qué NO debe hacer

- No validar permisos.
- No consultar base de datos.
- No crear contextos falsos.
- No manejar errores de negocio.
- No modificar request.

---

## `security/roles/SecurityRoles.java`

**Ruta:** `com.upsjb.ms3.security.roles.SecurityRoles`

### Responsabilidad

Centralizar los roles reconocidos por MS3.

### Qué debe contener

Constantes como:

```text
ROLE_ADMIN
ROLE_EMPLEADO
ROLE_CLIENTE
ADMIN
EMPLEADO
CLIENTE
```

También puede contener utilidades simples para normalización si no se duplica con otro componente.

### Qué consume

- Nada externo.

### Quién la usa

- `RoleJwtAuthenticationConverter`.
- `SecurityConfig`.
- `AuthenticatedUserContext`.
- Policies.
- Tests de seguridad.

### Coexistencia

- `SecurityRoles` define roles.
- `EmpleadoInventarioPermisoHistorial` define permisos funcionales adicionales.
- No debe mezclar rol global con permiso operativo.

### Qué NO debe hacer

- No consultar JWT.
- No consultar base de datos.
- No guardar permisos de inventario.
- No crear authorities dinámicas.
- No contener rutas HTTP.

---

## `security/filter/RequestTraceFilter.java`

**Ruta:** `com.upsjb.ms3.security.filter.RequestTraceFilter`

### Responsabilidad

Garantizar trazabilidad técnica por request dentro del MS3.

Debe manejar `X-Request-Id` y `X-Correlation-Id`.

### Qué debe hacer

- Leer `X-Request-Id` si llega desde Gateway.
- Generarlo si no existe.
- Leer `X-Correlation-Id` si llega.
- Usar request ID como correlation ID si falta.
- Agregar ambos al contexto de request.
- Agregar ambos a response.
- Permitir que auditoría, logs y errores los usen.

### Qué consume

- `HeaderNames`.
- `RequestMetadataUtil`.
- `AuditContextHolder`, si se decide poblar contexto desde aquí.
- Servlet filter API o Spring filter API.

### Quién la usa

- Todos los requests HTTP.
- `RequestAuditContextFilter`.
- `GlobalExceptionHandler`.
- `AuditoriaFuncionalService`.
- Logs técnicos.

### Coexistencia

- Gateway ya puede enviar trazabilidad.
- MS3 no debe confiar en que siempre vendrá.
- Este filtro asegura trazabilidad interna aun si el request entra en pruebas locales.

### Qué NO debe hacer

- No validar JWT.
- No registrar auditoría funcional.
- No consultar base de datos.
- No leer body.
- No loguear tokens.
- No alterar datos de negocio.

---

## `security/filter/RequestAuditContextFilter.java`

**Ruta:** `com.upsjb.ms3.security.filter.RequestAuditContextFilter`

### Responsabilidad

Construir el contexto de auditoría funcional por request.

Debe recolectar metadata necesaria para registrar auditoría en operaciones críticas.

### Qué debe hacer

- Capturar IP.
- Capturar User-Agent.
- Capturar método HTTP.
- Capturar path.
- Capturar request ID.
- Capturar correlation ID.
- Capturar actor si está autenticado.
- Guardar metadata en `AuditContextHolder`.
- Limpiar el contexto al finalizar request.

### Qué consume

- `AuditContext`.
- `AuditContextHolder`.
- `RequestMetadataUtil`.
- `CurrentUserResolver`, si hay usuario autenticado.
- `HeaderNames`.

### Quién la usa

- `AuditoriaFuncionalServiceImpl`.
- Services de negocio.
- Policies en caso de auditoría de acceso denegado.
- `GlobalExceptionHandler` si registra errores funcionales.

### Coexistencia

- Complementa `RequestTraceFilter`.
- No reemplaza `AuditoriaFuncionalService`.
- Solo prepara metadata contextual.
- La auditoría real se registra en services.

### Qué NO debe hacer

- No insertar auditoría directamente por cada request.
- No registrar bodies.
- No loguear tokens.
- No decidir permisos.
- No manejar transacciones de negocio.

---

## `security/handler/RestAuthenticationEntryPoint.java`

**Ruta:** `com.upsjb.ms3.security.handler.RestAuthenticationEntryPoint`

### Responsabilidad

Responder cuando una ruta protegida se consume sin token o con token inválido.

### Qué debe hacer

- Devolver HTTP 401.
- Responder en JSON.
- Usar formato estándar de error del MS3.
- Incluir path, timestamp, requestId y código funcional.
- No revelar detalles criptográficos del token.

Código sugerido:

```text
TOKEN_AUSENTE_O_INVALIDO
```

### Qué consume

- `ErrorResponseFactory`.
- `ApiErrorCode`.
- `RequestMetadataUtil`.
- Spring Security.

### Quién la usa

- `SecurityConfig`.
- Spring Security cuando falla autenticación.

### Coexistencia

- Maneja autenticación fallida.
- `RestAccessDeniedHandler` maneja autorización fallida.
- `GlobalExceptionHandler` maneja excepciones de negocio fuera de Spring Security.

### Qué NO debe hacer

- No redirigir a login.
- No devolver HTML.
- No mostrar stacktrace.
- No indicar si el token expiró por razones sensibles si no se desea.
- No consultar base de datos.

---

## `security/handler/RestAccessDeniedHandler.java`

**Ruta:** `com.upsjb.ms3.security.handler.RestAccessDeniedHandler`

### Responsabilidad

Responder cuando el usuario está autenticado pero no tiene autoridad suficiente a nivel de seguridad HTTP.

### Qué debe hacer

- Devolver HTTP 403.
- Responder JSON uniforme.
- Usar código funcional de acceso denegado.
- Registrar metadata segura si aplica.

Código sugerido:

```text
ACCESO_DENEGADO
```

### Qué consume

- `ErrorResponseFactory`.
- `ApiErrorCode`.
- `RequestMetadataUtil`.
- Spring Security.

### Quién la usa

- `SecurityConfig`.
- Spring Security cuando falla autorización por ruta o authority.

### Coexistencia

- No reemplaza policies de dominio.
- Policies pueden lanzar `ForbiddenException` cuando falla autorización contextual.
- `GlobalExceptionHandler` manejará esas `ForbiddenException`.

### Qué NO debe hacer

- No validar permisos de inventario.
- No consultar empleado.
- No auditar de forma profunda sin service.
- No devolver detalles internos.
- No modificar respuesta si ya fue enviada.

---

## `security/handler/SecurityExceptionHandler.java`

**Ruta:** `com.upsjb.ms3.security.handler.SecurityExceptionHandler`

### Responsabilidad

Centralizar soporte técnico para construir respuestas de errores de seguridad si se necesita reutilización entre entry point y access denied handler.

### Qué debe hacer

- Construir respuestas uniformes para errores de seguridad.
- Evitar duplicación entre `RestAuthenticationEntryPoint` y `RestAccessDeniedHandler`.
- Normalizar códigos, mensajes y metadata.
- Usar `ErrorResponseFactory`.

### Qué consume

- `ErrorResponseFactory`.
- `ApiErrorCode`.
- `RequestMetadataUtil`.

### Quién la usa

- `RestAuthenticationEntryPoint`.
- `RestAccessDeniedHandler`.

### Coexistencia

- Es una clase auxiliar de seguridad.
- No reemplaza `GlobalExceptionHandler`.
- No maneja excepciones de negocio generales.

### Qué NO debe hacer

- No acceder a repositories.
- No validar reglas de catálogo.
- No registrar auditoría funcional directamente.
- No generar tokens.
- No leer cuerpos de request.

---

# 4. Paquete `controller`

---

## Reglas generales para todos los controllers

Todos los controllers del MS3 deben cumplir:

### Responsabilidad común

- Recibir HTTP.
- Validar DTOs con Bean Validation.
- Obtener usuario autenticado si aplica.
- Delegar al service correspondiente.
- Devolver `ApiResponseDto`.
- No contener lógica de negocio.
- No resolver FK.
- No generar códigos.
- No generar slug.
- No llamar repositories.
- No publicar Kafka.
- No subir a Cloudinary directamente.
- No registrar auditoría directamente salvo casos mínimos delegados.

### Qué consumen

- Services de `service.contract`.
- DTOs de request, response y filter.
- `AuthenticatedUserContext`, si aplica.
- `PageRequestDto`, si aplica.
- `ApiResponseFactory`, si se estandariza respuesta en controller.

### Quién los usa

- Angular por medio del API Gateway.
- Clientes externos autorizados si existieran.
- Tests de integración HTTP.
- Swagger/OpenAPI si se habilita.

### Coexistencia

- Controller recibe.
- Service orquesta.
- Policy autoriza.
- Validator valida.
- Mapper transforma.
- Repository persiste.
- Outbox registra evento.
- Kafka publica después.

---

## `PublicCatalogoController.java`

**Ruta:** `com.upsjb.ms3.controller.PublicCatalogoController`

### Responsabilidad

Exponer endpoints públicos generales del catálogo para usuarios anónimos, clientes o visitantes.

Debe mostrar información segura y comercial, nunca información interna.

### Operaciones esperadas

- Listar categorías públicas.
- Listar marcas públicas.
- Listar tipos de producto públicos si aplica.
- Listar filtros disponibles para catálogo.
- Obtener árbol de categorías públicas.
- Obtener datos base de navegación del catálogo.

### Qué consume

- `ProductoPublicService`.
- `ReferenceDataService`.
- `CatalogoLookupService`.
- DTOs públicos de catálogo.
- `ProductoPublicFilterDto`.

### Quién lo usa

- Angular en páginas públicas.
- Página inicial de tienda.
- Menú de categorías.
- Filtros de catálogo.
- Clientes no logueados.

### Coexistencia

- Complementa `PublicProductoController`.
- No administra productos.
- No muestra stock interno.
- No muestra costos.
- No exige JWT para consultas públicas.

### Qué NO debe hacer

- No devolver proveedores.
- No devolver kardex.
- No devolver costos.
- No devolver auditoría.
- No permitir modificar catálogo.
- No usar services admin.

---

## `PublicProductoController.java`

**Ruta:** `com.upsjb.ms3.controller.PublicProductoController`

### Responsabilidad

Exponer consulta pública de productos visibles, publicados o programados según RN.

### Operaciones esperadas

- Listar productos públicos paginados.
- Filtrar productos por categoría, marca, texto, precio, promoción o estado público.
- Obtener detalle público por slug.
- Obtener productos próximos si la RN lo permite.
- Obtener productos destacados si se implementa esa regla en service.

### Qué consume

- `ProductoPublicService`.
- `ProductoPublicFilterDto`.
- `ProductoPublicResponseDto`.
- `ProductoPublicDetailResponseDto`.
- `ProductoCatalogoCardResponseDto`.

### Quién lo usa

- Angular público.
- Catálogo online.
- Página de detalle de producto.
- Módulo de búsqueda pública.

### Coexistencia

- Usa solo service público.
- Se diferencia de `ProductoAdminController`, que muestra datos internos.
- Solo devuelve productos con condiciones públicas válidas:
  - `visible_publico = true`.
  - publicación válida.
  - estado coherente.
  - imágenes activas.
  - precio vigente si aplica.

### Qué NO debe hacer

- No devolver costo promedio.
- No devolver último costo de compra.
- No devolver proveedor.
- No devolver movimientos.
- No permitir reserva.
- No permitir compra directa.
- No devolver productos internos no publicados.

---

## `PublicPromocionController.java`

**Ruta:** `com.upsjb.ms3.controller.PublicPromocionController`

### Responsabilidad

Exponer promociones públicas vigentes o programadas según reglas de visibilidad.

### Operaciones esperadas

- Listar promociones públicas.
- Obtener detalle público de promoción.
- Listar productos/SKU asociados a una promoción pública.
- Mostrar descuentos públicos aplicables.

### Qué consume

- `PromocionService`.
- `PromocionFilterDto`.
- `PromocionPublicResponseDto`.

### Quién lo usa

- Angular público.
- Página de promociones.
- Componentes de productos en oferta.
- Landing pages de campaña.

### Coexistencia

- La lógica de vigencia se valida en `PromocionService` y `PromocionValidator`.
- No debe usar endpoints administrativos.
- MS4 consumirá promociones por Kafka para venta; este controller solo muestra información.

### Qué NO debe hacer

- No crear promociones.
- No activar promociones.
- No modificar descuentos.
- No devolver margen.
- No devolver costos.
- No mostrar promociones internas o canceladas.

---

## `ReferenceDataController.java`

**Ruta:** `com.upsjb.ms3.controller.ReferenceDataController`

### Responsabilidad

Exponer datos de referencia estáticos o semiestáticos necesarios para formularios del frontend.

### Operaciones esperadas

- Listar enums disponibles:
  - estados de producto.
  - estados de venta.
  - estados de publicación.
  - tipos de proveedor.
  - tipos de descuento.
  - monedas.
  - tipos de movimiento.
- Listar valores controlados para selects.
- Devolver catálogos simples que no requieren búsqueda avanzada.

### Qué consume

- `ReferenceDataService`.
- DTOs `SelectOptionDto`.
- Enums del dominio.

### Quién lo usa

- Angular administrativo.
- Formularios de creación/edición.
- Pantallas de filtros.
- Pantallas de inventario.

### Coexistencia

- `ReferenceDataController` devuelve opciones de enums.
- `CatalogoLookupController` devuelve registros reales de base de datos.
- No deben duplicarse.

### Qué NO debe hacer

- No devolver entidades completas.
- No consultar datos pesados.
- No resolver FK.
- No modificar datos.
- No exponer valores internos sensibles.

---

## `CatalogoLookupController.java`

**Ruta:** `com.upsjb.ms3.controller.CatalogoLookupController`

### Responsabilidad

Exponer búsquedas livianas tipo lookup para que Angular pueda seleccionar relaciones por valores reconocibles.

Permite evitar que el usuario inserte FK directamente.

### Operaciones esperadas

- Buscar tipos de producto por código/nombre.
- Buscar categorías por nombre/slug.
- Buscar marcas por nombre/slug.
- Buscar productos por código/nombre/slug.
- Buscar SKU por código/barcode/producto.
- Buscar proveedores por documento/nombre.
- Buscar almacenes por código/nombre.
- Buscar promociones por código/nombre.
- Buscar empleados con permiso de inventario si aplica.

### Qué consume

- `CatalogoLookupService`.
- `ReferenceSearchFilterDto`.
- DTOs `OptionDto`.

### Quién lo usa

- Angular admin.
- Formularios con autocompletado.
- Formularios de producto.
- Formularios de compra.
- Formularios de promoción.
- Formularios de entrada/salida/ajuste.

### Coexistencia

- Complementa `EntityReferenceService`.
- El lookup ayuda al frontend a mostrar valores reconocibles.
- El service de creación/edición resuelve la referencia real al guardar.
- No reemplaza filtros completos de listados.

### Qué NO debe hacer

- No devolver datos internos sensibles.
- No devolver listados pesados.
- No modificar información.
- No validar reglas de negocio finales.
- No exponer costos ni kardex.

---

## `TipoProductoController.java`

**Ruta:** `com.upsjb.ms3.controller.TipoProductoController`

### Responsabilidad

Administrar tipos de producto usados para clasificar productos y definir atributos esperados.

Ejemplos:

- Ropa.
- Calzado.
- Accesorio.
- Implemento deportivo.

### Operaciones esperadas

- Crear tipo de producto.
- Actualizar tipo de producto.
- Listar paginado con filtros.
- Obtener detalle.
- Activar/inactivar.
- Asociar atributos mediante service relacionado si se expone desde aquí o desde `AtributoController`.

### Qué consume

- `TipoProductoService`.
- `TipoProductoCreateRequestDto`.
- `TipoProductoUpdateRequestDto`.
- `TipoProductoFilterDto`.
- `EstadoChangeRequestDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN principalmente.
- Empleado autorizado si se decide permitir mantenimiento básico.

### Coexistencia

- `TipoProductoService` valida duplicados y estados.
- `TipoProductoAtributoService` gestiona atributos por tipo.
- `ProductoAdminService` consume tipos de producto al crear productos.
- `CatalogoLookupService` expone búsqueda liviana.

### Qué NO debe hacer

- No crear productos.
- No asignar stock.
- No resolver atributos en controller.
- No consultar repositories directamente.
- No eliminar físicamente.

---

## `CategoriaController.java`

**Ruta:** `com.upsjb.ms3.controller.CategoriaController`

### Responsabilidad

Administrar categorías jerárquicas del catálogo.

Ejemplos:

```text
Ropa deportiva
    Camisetas
    Shorts

Calzado
    Zapatillas running
```

### Operaciones esperadas

- Crear categoría.
- Actualizar categoría.
- Listar paginado con filtros.
- Obtener detalle.
- Obtener árbol administrativo.
- Activar/inactivar.
- Cambiar categoría padre.
- Cambiar orden.

### Qué consume

- `CategoriaService`.
- `CategoriaCreateRequestDto`.
- `CategoriaUpdateRequestDto`.
- `CategoriaFilterDto`.
- `EstadoChangeRequestDto`.

### Quién lo usa

- Angular administrativo.
- Formularios de producto.
- Catálogo público indirectamente mediante services públicos.

### Coexistencia

- `CategoriaService` valida jerarquía.
- `CategoriaValidator` impide ciclos.
- `ProductoAdminService` usa categoría como FK funcional.
- `CatalogoLookupController` permite buscar categorías.
- `PublicCatalogoController` expone árbol público.

### Qué NO debe hacer

- No validar jerarquía en controller.
- No permitir eliminar físicamente.
- No publicar productos.
- No manipular SKU.
- No mostrar productos internos en endpoints públicos.

---

## `MarcaController.java`

**Ruta:** `com.upsjb.ms3.controller.MarcaController`

### Responsabilidad

Administrar marcas comerciales de productos.

### Operaciones esperadas

- Crear marca.
- Actualizar marca.
- Listar paginado con filtros.
- Obtener detalle.
- Activar/inactivar.
- Buscar por slug o nombre mediante lookup.

### Qué consume

- `MarcaService`.
- `MarcaCreateRequestDto`.
- `MarcaUpdateRequestDto`.
- `MarcaFilterDto`.
- `EstadoChangeRequestDto`.

### Quién lo usa

- Angular administrativo.
- Formularios de producto.
- Catálogo público indirectamente.

### Coexistencia

- `MarcaService` valida unicidad y estado.
- `ProductoAdminService` puede resolver marca por nombre, código o slug.
- `CatalogoLookupService` expone opciones para selects/autocomplete.
- `PublicCatalogoController` puede exponer marcas públicas.

### Qué NO debe hacer

- No crear productos.
- No modificar stock.
- No exponer costos.
- No eliminar físicamente marcas con productos.
- No resolver referencias en controller.

---

## `AtributoController.java`

**Ruta:** `com.upsjb.ms3.controller.AtributoController`

### Responsabilidad

Administrar atributos dinámicos que permiten adaptar productos a ropa, calzado, accesorios y otros tipos.

Ejemplos:

- Talla.
- Color.
- Material.
- Tipo de suela.
- Superficie.
- Capacidad.
- Género.
- Deporte.

### Operaciones esperadas

- Crear atributo.
- Actualizar atributo.
- Listar paginado con filtros.
- Obtener detalle.
- Activar/inactivar.
- Asociar atributo a tipo de producto.
- Quitar asociación lógica si aplica.
- Listar atributos por tipo de producto.

### Qué consume

- `AtributoService`.
- `TipoProductoAtributoService`.
- `AtributoCreateRequestDto`.
- `AtributoUpdateRequestDto`.
- `TipoProductoAtributoAssignRequestDto`.
- `AtributoFilterDto`.

### Quién lo usa

- Angular administrativo.
- Formularios dinámicos de producto.
- Formularios dinámicos de SKU.
- `ProductoAdminService`, indirectamente.

### Coexistencia

- `AtributoValidator` valida tipo de dato.
- `TipoProductoAtributoValidator` valida asociaciones.
- `ProductoAtributoValorService` y `SkuAtributoValorService` guardan valores.
- `ProductoPublicService` solo muestra atributos públicos.

### Qué NO debe hacer

- No guardar valores de atributos de productos.
- No crear productos.
- No crear SKU.
- No validar valores específicos de producto desde controller.
- No duplicar lógica de `TipoProductoAtributoService`.

---

## `ProductoAdminController.java`

**Ruta:** `com.upsjb.ms3.controller.ProductoAdminController`

### Responsabilidad

Exponer operaciones administrativas de producto base.

Es el controller principal para gestión interna de productos.

### Operaciones esperadas

- Crear producto.
- Editar producto.
- Listar productos internos paginados y filtrados.
- Obtener detalle administrativo.
- Cambiar estado de registro.
- Cambiar estado de publicación.
- Cambiar estado de venta.
- Publicar producto.
- Programar publicación.
- Ocultar producto.
- Descontinuar producto.
- Inactivar producto.

### Qué consume

- `ProductoAdminService`.
- `ProductoCreateRequestDto`.
- `ProductoUpdateRequestDto`.
- `ProductoEstadoRegistroRequestDto`.
- `ProductoPublicacionRequestDto`.
- `ProductoVentaEstadoRequestDto`.
- `ProductoFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permiso especial para producto básico, según policy.

### Coexistencia

- `ProductoAdminService` orquesta.
- `ProductoPolicy` autoriza.
- `ProductoValidator` valida datos.
- `ProductoPublicacionValidator` valida publicación.
- `EntityReferenceService` resuelve tipo, categoría y marca.
- `CodigoGeneradorService` genera código.
- `SlugGeneratorService` genera slug.
- `EventoDominioOutboxService` registra eventos de snapshot.

### Qué NO debe hacer

- No crear SKU directamente si eso va por `ProductoSkuController`.
- No subir imágenes.
- No modificar precio.
- No modificar stock.
- No resolver FK manualmente.
- No publicar Kafka directo.
- No eliminar físicamente.

---

## `ProductoSkuController.java`

**Ruta:** `com.upsjb.ms3.controller.ProductoSkuController`

### Responsabilidad

Administrar variantes/SKU de productos.

Cada SKU representa una variante vendible o controlable en inventario.

### Operaciones esperadas

- Crear SKU.
- Editar SKU.
- Listar SKU por producto.
- Listar SKU paginados.
- Obtener detalle de SKU.
- Activar/inactivar SKU.
- Descontinuar SKU.
- Gestionar atributos de SKU si se expone aquí.

### Qué consume

- `ProductoSkuService`.
- `SkuAtributoValorService`, si se exponen valores desde aquí.
- `ProductoSkuCreateRequestDto`.
- `ProductoSkuUpdateRequestDto`.
- `SkuAtributoValorRequestDto`.
- `ProductoSkuFilterDto`.

### Quién lo usa

- Angular administrativo.
- Formularios de producto.
- Formularios de compra.
- Formularios de inventario.
- MS4 indirectamente mediante snapshots Kafka.

### Coexistencia

- `ProductoSkuService` genera `codigo_sku`.
- `ProductoSkuValidator` valida producto activo y unicidad.
- `StockService` usa SKU para stock.
- `PrecioSkuService` usa SKU para precio.
- `PromocionSkuDescuentoService` usa SKU para descuento.
- `ProductoPublicService` muestra SKU vendibles si corresponde.

### Qué NO debe hacer

- No modificar precio.
- No modificar stock.
- No crear producto base.
- No resolver producto manualmente.
- No registrar kardex.
- No publicar Kafka directo.

---

## `ProductoImagenController.java`

**Ruta:** `com.upsjb.ms3.controller.ProductoImagenController`

### Responsabilidad

Gestionar imágenes de productos y SKU usando Cloudinary.

### Operaciones esperadas

- Subir imagen de producto.
- Subir imagen de SKU.
- Listar imágenes de producto/SKU.
- Marcar imagen principal.
- Actualizar metadata de imagen.
- Inactivar imagen.
- Obtener detalle de imagen.

### Qué consume

- `ProductoImagenService`.
- `ProductoImagenUploadRequestDto`.
- `ProductoImagenUpdateRequestDto`.
- `ProductoImagenPrincipalRequestDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permiso de gestión de imágenes.

### Coexistencia

- `ProductoImagenService` orquesta validación, Cloudinary y persistencia.
- `CloudinaryService` sube o elimina recurso.
- `CloudinaryImageValidator` valida formato, tamaño y metadata.
- `ProductoImagenPolicy` valida permisos.
- `ProductoImagenMapper` transforma respuesta.
- `EventoDominioOutboxService` registra snapshot actualizado.

### Qué NO debe hacer

- No llamar Cloudinary directamente.
- No guardar metadata directamente.
- No decidir si un producto puede publicarse.
- No manipular stock.
- No devolver secretos Cloudinary.
- No eliminar físicamente registros.

---

## `PrecioSkuController.java`

**Ruta:** `com.upsjb.ms3.controller.PrecioSkuController`

### Responsabilidad

Gestionar precios versionados por SKU.

El precio es sensible porque impacta ventas futuras, pero no debe alterar ventas pasadas.

### Operaciones esperadas

- Crear nuevo precio vigente para SKU.
- Listar historial de precios.
- Obtener precio vigente.
- Filtrar precios por SKU, moneda, vigencia y fechas.

### Qué consume

- `PrecioSkuService`.
- `PrecioSkuCreateRequestDto`.
- `PrecioSkuFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN principalmente.
- MS4 indirectamente por eventos Kafka.

### Coexistencia

- `PrecioSkuService` cierra precio anterior y crea nuevo vigente.
- `PrecioSkuValidator` valida precio, moneda y vigencia.
- `PrecioSkuPolicy` restringe cambio a ADMIN.
- `EventoDominioOutboxService` registra `PrecioSnapshotActualizado`.
- MS4 consume snapshot para vender con copia local.

### Qué NO debe hacer

- No editar precio histórico directamente.
- No borrar precio.
- No modificar ventas pasadas.
- No calcular venta.
- No publicar Kafka directo.
- No resolver SKU manualmente.

---

## `PromocionController.java`

**Ruta:** `com.upsjb.ms3.controller.PromocionController`

### Responsabilidad

Administrar promociones, versiones de promoción y descuentos por SKU.

### Operaciones esperadas

- Crear promoción.
- Editar datos base de promoción.
- Crear versión de promoción.
- Cambiar estado de versión.
- Asociar SKU con descuento.
- Actualizar descuento de SKU.
- Listar promociones paginadas.
- Obtener detalle administrativo.
- Cancelar promoción o versión.
- Consultar promociones vigentes.

### Qué consume

- `PromocionService`.
- `PromocionVersionService`.
- `PromocionSkuDescuentoService`.
- `PromocionCreateRequestDto`.
- `PromocionUpdateRequestDto`.
- `PromocionVersionCreateRequestDto`.
- `PromocionVersionEstadoRequestDto`.
- `PromocionSkuDescuentoCreateRequestDto`.
- `PromocionSkuDescuentoUpdateRequestDto`.
- `PromocionFilterDto`.
- `PromocionVersionFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- MS4 indirectamente mediante eventos de promoción.

### Coexistencia

- `PromocionValidator` valida campaña.
- `PromocionVersionValidator` valida fechas y vigencia.
- `PromocionSkuDescuentoValidator` valida descuento por SKU.
- `PromocionPolicy` restringe a ADMIN.
- `PrecioSkuService` puede apoyar validación de precio base.
- `EventoDominioOutboxService` registra eventos de promoción.

### Qué NO debe hacer

- No aplicar descuento global sin SKU.
- No modificar ventas pasadas.
- No calcular facturación.
- No exponer margen en endpoint público.
- No publicar Kafka directo.

---

## `ProveedorController.java`

**Ruta:** `com.upsjb.ms3.controller.ProveedorController`

### Responsabilidad

Administrar proveedores de inventario, sean persona natural o empresa.

### Operaciones esperadas

- Crear proveedor.
- Editar proveedor.
- Listar proveedores paginados y filtrados.
- Obtener detalle.
- Activar/inactivar proveedor.
- Buscar proveedor para compras.

### Qué consume

- `ProveedorService`.
- `ProveedorCreateRequestDto`.
- `ProveedorUpdateRequestDto`.
- `ProveedorEstadoRequestDto`.
- `ProveedorFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permiso operativo si se decide.

### Coexistencia

- `CompraInventarioService` usa proveedor para compras.
- `ProveedorValidator` valida tipo, documento y RUC.
- `ProveedorPolicy` autoriza mantenimiento.
- `CatalogoLookupController` permite buscar proveedor.

### Qué NO debe hacer

- No registrar compras.
- No modificar stock.
- No consultar MS2 salvo decisión explícita futura.
- No eliminar físicamente proveedor con historial.
- No exponer proveedores al catálogo público.

---

## `AlmacenController.java`

**Ruta:** `com.upsjb.ms3.controller.AlmacenController`

### Responsabilidad

Administrar almacenes donde se controla stock.

### Operaciones esperadas

- Crear almacén.
- Editar almacén.
- Listar almacenes.
- Obtener detalle.
- Activar/inactivar almacén.
- Marcar almacén principal.
- Configurar si permite venta o compra.

### Qué consume

- `AlmacenService`.
- `AlmacenCreateRequestDto`.
- `AlmacenUpdateRequestDto`.
- `AlmacenEstadoRequestDto`.
- `AlmacenFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- Operadores de inventario autorizados.

### Coexistencia

- `StockService` controla stock por almacén.
- `CompraInventarioService` usa almacén destino.
- `ReservaStockService` usa almacén de reserva.
- `MovimientoInventarioService` registra movimientos por almacén.
- `AlmacenValidator` valida almacén principal único y estado.

### Qué NO debe hacer

- No modificar stock directamente.
- No registrar movimientos.
- No resolver permisos de inventario.
- No eliminar físicamente almacenes con stock o movimientos.

---

## `StockController.java`

**Ruta:** `com.upsjb.ms3.controller.StockController`

### Responsabilidad

Consultar estado de stock por SKU y almacén.

Debe ser principalmente consultivo; los cambios reales de stock se hacen mediante compras, reservas o movimientos.

### Operaciones esperadas

- Listar stock paginado y filtrado.
- Consultar stock por SKU.
- Consultar stock por almacén.
- Consultar stock disponible.
- Consultar productos bajo stock.
- Consultar stock para venta.

### Qué consume

- `StockService`.
- `StockSkuFilterDto`.
- `StockSkuResponseDto`.
- `StockDisponibleResponseDto`.

### Quién lo usa

- Angular administrativo.
- MS4 indirectamente si hay endpoint interno.
- Empleados autorizados.
- ADMIN.

### Coexistencia

- `StockService` lee y calcula disponibilidad.
- `ReservaStockService` modifica reservado.
- `MovimientoInventarioService` modifica físico.
- `KardexService` expone historial.
- `StockPolicy` controla acceso.

### Qué NO debe hacer

- No actualizar stock directamente.
- No registrar entradas.
- No registrar salidas.
- No registrar ajustes.
- No saltarse kardex.
- No mostrar costos a roles no autorizados.

---

## `CompraInventarioController.java`

**Ruta:** `com.upsjb.ms3.controller.CompraInventarioController`

### Responsabilidad

Gestionar compras/adquisiciones de inventario.

### Operaciones esperadas

- Crear compra en BORRADOR.
- Editar compra en BORRADOR.
- Listar compras filtradas.
- Obtener detalle.
- Confirmar compra.
- Anular compra si la RN lo permite.
- Consultar detalles de compra.

### Qué consume

- `CompraInventarioService`.
- `CompraInventarioCreateRequestDto`.
- `CompraInventarioUpdateRequestDto`.
- `CompraInventarioConfirmRequestDto`.
- `CompraInventarioAnularRequestDto`.
- `CompraInventarioFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permiso de entrada/compra si se decide.

### Coexistencia

- `CompraInventarioService` orquesta.
- `ProveedorService` resuelve proveedor.
- `ProductoSkuService` valida SKU.
- `AlmacenService` valida almacén.
- `MovimientoInventarioService` registra entrada al confirmar.
- `StockService` actualiza stock.
- `EventoDominioOutboxService` registra eventos de stock.

### Qué NO debe hacer

- No actualizar stock desde controller.
- No registrar movimiento manualmente.
- No calcular costo promedio en controller.
- No resolver FK directamente.
- No permitir confirmar compra sin detalle.

---

## `ReservaStockController.java`

**Ruta:** `com.upsjb.ms3.controller.ReservaStockController`

### Responsabilidad

Gestionar reservas de stock, especialmente para ventas o carritos de MS4.

### Operaciones esperadas

- Crear reserva de stock.
- Confirmar reserva.
- Liberar reserva.
- Consultar reserva por referencia externa.
- Listar reservas filtradas.
- Procesar solicitud de MS4 si se expone por HTTP interno.

### Qué consume

- `ReservaStockService`.
- `ReservaStockCreateRequestDto`.
- `ReservaStockConfirmRequestDto`.
- `ReservaStockLiberarRequestDto`.
- `ReservaStockMs4RequestDto`.
- `ReservaStockFilterDto`.

### Quién lo usa

- MS4 mediante endpoint interno si aplica.
- Angular administrativo para consulta.
- ADMIN.
- EMPLEADO operativo según permisos.

### Coexistencia

- `ReservaStockService` valida idempotencia.
- `StockService` valida disponibilidad.
- `MovimientoInventarioService` registra `RESERVA_VENTA`, `CONFIRMACION_VENTA` o `LIBERACION_RESERVA`.
- `EventoDominioOutboxService` publica snapshot de stock.
- `Ms4ReconciliacionService` puede consumir reservas pendientes.

### Qué NO debe hacer

- No crear venta.
- No facturar.
- No cambiar precio.
- No omitir validación de stock.
- No duplicar reserva si la referencia MS4 ya existe.

---

## `MovimientoInventarioController.java`

**Ruta:** `com.upsjb.ms3.controller.MovimientoInventarioController`

### Responsabilidad

Registrar movimientos operativos de inventario que no pertenecen directamente al flujo de compra o reserva automática.

Ejemplos:

- Entrada manual.
- Salida manual.
- Ajuste.
- Merma.
- Movimiento compensatorio.

### Operaciones esperadas

- Registrar entrada.
- Registrar salida.
- Registrar ajuste.
- Registrar movimiento compensatorio.
- Listar movimientos filtrados.
- Obtener detalle de movimiento.

### Qué consume

- `MovimientoInventarioService`.
- `EntradaInventarioRequestDto`.
- `SalidaInventarioRequestDto`.
- `AjusteInventarioRequestDto`.
- `MovimientoCompensatorioRequestDto`.
- `MovimientoInventarioFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permisos de inventario.

### Coexistencia

- `MovimientoInventarioService` ejecuta cambios.
- `StockService` actualiza stock.
- `MovimientoInventarioValidator` valida cantidades y motivo.
- `MovimientoInventarioPolicy` valida autorización.
- `AuditoriaFuncionalService` registra operación.
- `EventoDominioOutboxService` registra evento de stock/movimiento.

### Qué NO debe hacer

- No modificar stock directamente.
- No crear venta.
- No anular movimientos borrándolos.
- No saltarse movimiento compensatorio.
- No aceptar movimientos sin motivo.

---

## `KardexController.java`

**Ruta:** `com.upsjb.ms3.controller.KardexController`

### Responsabilidad

Exponer consulta del kardex de inventario.

Debe ser consultivo y permitir análisis histórico por SKU, almacén, fechas, tipo de movimiento y referencia.

### Operaciones esperadas

- Consultar kardex por SKU.
- Consultar kardex por almacén.
- Consultar kardex por fecha.
- Consultar movimientos por referencia externa.
- Exportar o preparar datos para reporte si se implementa después.

### Qué consume

- `KardexService`.
- `KardexFilterDto`.
- `KardexResponseDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- EMPLEADO con permiso de consulta de kardex.

### Coexistencia

- `KardexService` consulta `MovimientoInventarioRepository`.
- `KardexPolicy` valida acceso.
- `MovimientoInventarioService` alimenta el kardex con cada cambio de stock.
- No debe duplicar lógica de movimientos.

### Qué NO debe hacer

- No registrar movimientos.
- No modificar stock.
- No corregir kardex.
- No mostrar costos a usuarios no autorizados.
- No exponer kardex públicamente.

---

## `EmpleadoInventarioPermisoController.java`

**Ruta:** `com.upsjb.ms3.controller.EmpleadoInventarioPermisoController`

### Responsabilidad

Administrar permisos funcionales de inventario para empleados provenientes de MS2.

### Operaciones esperadas

- Registrar o actualizar snapshot de empleado MS2 si se permite desde endpoint interno.
- Consultar empleados con permisos.
- Otorgar permisos.
- Revocar permisos.
- Consultar historial de permisos.
- Listar permisos con filtros.

### Qué consume

- `EmpleadoSnapshotMs2Service`.
- `EmpleadoInventarioPermisoService`.
- `EmpleadoSnapshotMs2UpsertRequestDto`.
- `EmpleadoInventarioPermisoUpdateRequestDto`.
- `EmpleadoInventarioPermisoRevokeRequestDto`.
- `EmpleadoInventarioPermisoFilterDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- Procesos de sincronización con MS2.
- Policies de otros services indirectamente.

### Coexistencia

- `EmpleadoInventarioPermisoService` versiona permisos.
- `EmpleadoInventarioPermisoPolicy` restringe a ADMIN.
- Policies como `ProductoPolicy`, `StockPolicy`, `CompraInventarioPolicy` consultan permisos vigentes.
- `EmpleadoSnapshotMs2Service` mantiene snapshot mínimo.

### Qué NO debe hacer

- No crear empleado en MS2.
- No crear usuario en MS1.
- No asignar rol global.
- No modificar datos laborales.
- No permitir que un empleado se otorgue permisos a sí mismo.

---

## `Ms4StockSyncController.java`

**Ruta:** `com.upsjb.ms3.controller.Ms4StockSyncController`

### Responsabilidad

Exponer endpoints internos o administrativos para reconciliación de stock con MS4 cuando se requiera vía HTTP.

La sincronización principal debe ser por Kafka; este controller existe para casos controlados, recuperación o soporte operativo.

### Operaciones esperadas

- Recibir evento pendiente de reserva desde MS4, si se habilita por HTTP.
- Recibir confirmación de venta pendiente.
- Recibir liberación de stock pendiente.
- Recibir anulación de venta con impacto en stock.
- Ejecutar reconciliación puntual.
- Consultar resultado de sincronización.

### Qué consume

- `Ms4ReconciliacionService`.
- `ReservaStockService`.
- `Ms4StockEventValidator`.
- DTOs `Ms4VentaStockReservadoEventDto`, `Ms4VentaStockConfirmadoEventDto`, `Ms4VentaStockLiberadoEventDto`, `Ms4VentaAnuladaStockEventDto`.

### Quién lo usa

- MS4 internamente si se habilita HTTP.
- Operaciones administrativas de recuperación.
- Tests de integración entre MS3 y MS4.

### Coexistencia

- Kafka consumer debe ser el mecanismo principal.
- `Ms4StockCommandConsumer` procesa eventos Kafka.
- `Ms4ReconciliacionService` centraliza la lógica.
- `KafkaIdempotencyGuard` y validators evitan duplicados.
- `ReservaStockService` y `MovimientoInventarioService` aplican cambios reales.

### Qué NO debe hacer

- No ser endpoint público.
- No crear ventas.
- No facturar.
- No aceptar eventos sin idempotencia.
- No saltarse validaciones.
- No reemplazar Kafka como mecanismo principal.

---

## `AuditoriaController.java`

**Ruta:** `com.upsjb.ms3.controller.AuditoriaController`

### Responsabilidad

Exponer consulta administrativa de auditoría funcional del MS3.

### Operaciones esperadas

- Listar auditoría paginada.
- Filtrar por entidad, evento, resultado, actor, fecha.
- Obtener detalle de auditoría.
- Consultar auditoría relacionada con un registro específico.

### Qué consume

- `AuditoriaFuncionalService`.
- `AuditoriaFuncionalFilterDto`.
- `AuditoriaFuncionalResponseDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- Soporte interno.
- Auditoría operativa.

### Coexistencia

- `AuditoriaFuncionalService` registra y consulta auditoría.
- Services de negocio registran eventos funcionales.
- `RequestAuditContextFilter` prepara metadata.
- `AuditContextHolder` mantiene contexto por request.

### Qué NO debe hacer

- No crear auditoría manual de negocio.
- No modificar auditoría.
- No eliminar auditoría.
- No exponer auditoría a empleados sin autorización.
- No exponer metadata sensible innecesaria.

---

## `OutboxController.java`

**Ruta:** `com.upsjb.ms3.controller.OutboxController`

### Responsabilidad

Administrar consulta y reintento controlado de eventos Outbox.

Debe ser un endpoint administrativo protegido.

### Operaciones esperadas

- Listar eventos outbox.
- Filtrar por estado, topic, aggregate, eventType, fechas.
- Obtener detalle de evento.
- Reintentar evento fallido.
- Consultar errores de publicación.

### Qué consume

- `EventoDominioOutboxService`.
- `KafkaPublisherService`.
- `OutboxRetryRequestDto`.
- `EventoDominioOutboxFilterDto`.
- `EventoDominioOutboxResponseDto`.

### Quién lo usa

- Angular administrativo.
- ADMIN.
- Soporte técnico.
- Operaciones de recuperación Kafka.

### Coexistencia

- `EventoDominioOutboxService` gestiona estados.
- `OutboxScheduler` publica automáticamente.
- `KafkaPublisherService` publica eventos.
- `OutboxPolicy` restringe acciones.
- `AuditoriaFuncionalService` audita reintentos.

### Qué NO debe hacer

- No publicar eventos arbitrarios creados por usuario.
- No modificar payload manualmente.
- No reintentar eventos no reintentables.
- No exponer payload sensible a usuarios no autorizados.
- No reemplazar scheduler.

---
# Documentación técnica de código - MS3 Parte 2

**Microservicio:** `ms-catalogo-inventario`  
**Paquetes documentados:** `service.contract` y `service.impl`  
**Objetivo:** definir cómo debe programarse cada contrato e implementación del service layer del MS3.  
**Base funcional:** RN General Definitiva del MS3 y estructura definitiva de packages/clases.

---

# 1. Reglas generales para todos los services del MS3

## 1.1. Responsabilidad del service layer

El service layer es el centro de orquestación de casos de uso del MS3.

Un service debe coordinar:

```text
- Usuario autenticado.
- Autorización contextual mediante Policy.
- Validación funcional mediante Validator.
- Resolución de referencias funcionales mediante EntityReferenceService o resolvers compartidos.
- Generación inteligente de códigos y slug cuando aplique.
- Mappers para convertir entity/DTO.
- Repositories para persistencia.
- Specifications para listados filtrados.
- PaginationService para paginación uniforme.
- Auditoría funcional.
- Outbox para eventos Kafka.
- Integraciones externas controladas, como Cloudinary, MS2 o MS4.
```

Un service no debe comportarse como controller ni como repository.  
No debe exponer entidades JPA al controller.  
No debe permitir que el frontend inserte FK directamente como única opción.

---

## 1.2. Responsabilidad de los contratos `service.contract`

Cada interfaz de service debe declarar el contrato funcional disponible para controllers u otros services.

El contrato debe expresar:

```text
- Crear.
- Actualizar.
- Cambiar estado.
- Buscar por ID.
- Buscar detalle.
- Listar paginado con filtros.
- Lookup cuando corresponda.
- Acciones funcionales específicas.
```

Los contratos deben estar pensados para uso estable.  
La implementación puede cambiar, pero el contrato debe conservarse si el controller ya depende de él.

---

## 1.3. Responsabilidad de las implementaciones `service.impl`

Cada implementación debe:

```text
1. Resolver actor autenticado si el caso lo requiere.
2. Validar autorización con Policy.
3. Resolver FK funcionales usando referencias reconocibles.
4. Validar reglas de negocio con Validator.
5. Ejecutar operación transaccional.
6. Persistir cambios mediante Repository.
7. Mapear respuesta con Mapper.
8. Registrar auditoría funcional.
9. Registrar evento Outbox si el cambio debe sincronizar MS4.
10. Devolver respuesta clara al usuario.
```

La implementación debe trabajar con transacciones donde haya cambios de estado, stock, precio, promoción, compra, reserva o kardex.

---

## 1.4. Reglas para respuestas al usuario

Toda acción debe devolver una respuesta entendible.

Para creación:

```text
Producto creado correctamente.
Categoría creada correctamente.
SKU creado correctamente.
Proveedor registrado correctamente.
```

Para actualización:

```text
Producto actualizado correctamente.
Precio actualizado correctamente.
Promoción versionada correctamente.
```

Para eliminación lógica o inactivación:

```text
Producto inactivado correctamente.
Imagen inactivada correctamente.
Proveedor inactivado correctamente.
```

Para validación funcional:

```text
No se puede publicar el producto porque no tiene SKU activo.
No se puede registrar la salida porque el stock disponible es insuficiente.
No se puede activar la promoción porque la fecha fin es menor que la fecha inicio.
```

Para errores técnicos no controlados:

```text
Ocurrió un error interno del sistema. Intente nuevamente o contacte al administrador.
```

El detalle técnico del error debe quedar en logs/consola con:

```text
- stacktrace.
- requestId.
- correlationId.
- usuario actor.
- endpoint.
- entidad afectada.
- payload seguro si aplica.
```

No se debe enviar stacktrace al usuario.

---

## 1.5. Manejo de errores recomendado

Errores funcionales controlados:

```text
ValidationException:
    Cuando faltan datos, hay datos inválidos o regla funcional incumplida.

NotFoundException:
    Cuando no existe un recurso activo requerido.

ConflictException:
    Cuando hay duplicados, estados incompatibles o conflictos de vigencia.

ForbiddenException:
    Cuando el actor no tiene permiso contextual.

ExternalServiceException:
    Cuando falla Cloudinary, MS2, MS4 u otro servicio externo.

KafkaPublishException:
    Cuando falla publicación Kafka en proceso técnico controlado.
```

Errores técnicos inesperados:

```text
RuntimeException no prevista
SQLException no prevista
NullPointerException
Timeout no controlado
Error de conversión inesperado
```

Deben ser capturados por `GlobalExceptionHandler`, mostrar mensaje genérico al usuario y registrar detalle técnico en logs.

---

## 1.6. Regla DRY obligatoria

Los services no deben duplicar lógica transversal.

Deben consumir componentes compartidos:

```text
shared.pagination.PaginationService
shared.persistence.EntityLookupService
shared.persistence.EntityStateValidator
shared.persistence.ActiveRecordResolver
shared.persistence.SoftDeleteSupport
shared.reference.*ReferenceResolver
shared.response.ApiResponseFactory
shared.audit.AuditEventFactory
shared.audit.AuditMetadataBuilder
shared.specification.SpecificationBuilder
shared.validation.ValidationErrorCollector
shared.code.CodigoGenerator
shared.idempotency.ProcessedEventGuard
util.StringNormalizer
util.SlugUtil
util.MoneyUtil
util.StockMathUtil
util.PercentageUtil
util.DateTimeUtil
```

No se debe repetir en cada service:

```text
- armado manual de PageResponseDto.
- normalización de texto.
- validación de estado activo.
- resolución de FK por nombre/código/slug.
- construcción de respuesta estándar.
- generación de códigos.
- generación de slug.
- metadata de auditoría.
- construcción manual de errores.
```

---

## 1.7. Filtros, lookup y paginación

Todo listado administrativo debe aceptar:

```text
- filtro DTO.
- page request.
- sort controlado.
- estado activo/inactivo si aplica.
```

Todo listado público debe filtrar automáticamente:

```text
- estado = true.
- visible_publico = true.
- estado_publicacion válido.
- datos sensibles excluidos.
```

Todo lookup debe:

```text
- devolver respuestas livianas.
- aceptar búsqueda por texto.
- aceptar código, nombre, slug o identificador funcional.
- limitar resultados.
- devolver solo registros activos por defecto.
```

La resolución de FK al crear/editar debe aceptar referencias reconocibles como:

```text
- id.
- codigo.
- nombre.
- slug.
- barcode.
- codigoSku.
- codigoProducto.
- numeroDocumento.
- ruc.
```

El usuario no debe verse obligado a escribir IDs técnicos.

---

# 2. Documentación clase por clase


---

## `ReferenceDataService`

**Ruta:** `com.upsjb.ms3.service.contract.ReferenceDataService`

### Responsabilidad del contrato

Exponer datos de referencia, enums y opciones estáticas/semiestáticas del MS3 para formularios y filtros.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Listar estados de producto.
- Listar estados de publicación.
- Listar estados de venta.
- Listar estados de SKU.
- Listar monedas.
- Listar tipos de descuento.
- Listar tipos de proveedor.
- Listar tipos de movimiento.
- Listar estados de compra, reserva, promoción y outbox.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ReferenceDataController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ReferenceDataServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ReferenceDataServiceImpl`

### Responsabilidad de la implementación

Implementar `ReferenceDataService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- domain.enums.*
- `SelectOptionDto`
- `ReferenceMapper o utilitario equivalente`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No consultar tablas pesadas.
- No modificar datos.
- No mezclar lookups de BD con enums.
- No devolver valores internos sensibles.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `CatalogoLookupService`

**Ruta:** `com.upsjb.ms3.service.contract.CatalogoLookupService`

### Responsabilidad del contrato

Centralizar búsquedas livianas tipo lookup para que Angular seleccione relaciones sin escribir FK técnicos.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Buscar tipo de producto por código/nombre.
- Buscar categoría por nombre, código o slug.
- Buscar marca por nombre, código o slug.
- Buscar producto por código, nombre o slug.
- Buscar SKU por código, barcode o producto.
- Buscar proveedor por documento, RUC o nombre.
- Buscar almacén por código/nombre.
- Buscar promoción por código/nombre.
- Buscar empleado con permiso operativo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `CatalogoLookupController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `CatalogoLookupServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.CatalogoLookupServiceImpl`

### Responsabilidad de la implementación

Implementar `CatalogoLookupService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `Repositories de catálogo`
- `ReferenceSearchFilterDto`
- `ReferenceOptionMapper`
- `SpecificationBuilder`
- `PaginationService si aplica`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No devolver entidades completas.
- No resolver reglas finales de negocio.
- No modificar datos.
- No exponer costos, kardex ni auditoría.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `EntityReferenceService`

**Ruta:** `com.upsjb.ms3.service.contract.EntityReferenceService`

### Responsabilidad del contrato

Resolver referencias funcionales a entidades activas desde datos reconocibles, evitando que el frontend dependa de FK crudos.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Resolver TipoProducto por id/código/nombre.
- Resolver Categoría por id/código/slug/nombre.
- Resolver Marca por id/código/slug/nombre.
- Resolver Producto por id/código/slug.
- Resolver SKU por id/código/barcode.
- Resolver Proveedor por id/RUC/documento.
- Resolver Almacén por id/código/nombre.
- Resolver Promoción por id/código/nombre.
- Resolver EmpleadoSnapshot por idUsuarioMs1/idEmpleadoMs2/código.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Services de creación/edición que necesiten relaciones`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `EntityReferenceServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.EntityReferenceServiceImpl`

### Responsabilidad de la implementación

Implementar `EntityReferenceService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- shared.reference.*ReferenceResolver
- `ActiveRecordResolver`
- `EntityStateValidator`
- `NotFoundException`
- `ConflictException`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No mapear DTO final.
- No aplicar permisos.
- No modificar entidades.
- No reemplazar validators de negocio.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `CodigoGeneradorService`

**Ruta:** `com.upsjb.ms3.service.contract.CodigoGeneradorService`

### Responsabilidad del contrato

Generar códigos internos inteligentes y únicos para entidades que no deben ser digitadas por el usuario.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Generar código de producto.
- Generar código de SKU.
- Generar código de promoción.
- Generar código de compra.
- Generar código de reserva.
- Generar código de movimiento.
- Actualizar correlativo transaccionalmente.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Services que crean registros con código generado`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `CodigoGeneradorServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.CodigoGeneradorServiceImpl`

### Responsabilidad de la implementación

Implementar `CodigoGeneradorService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `CorrelativoCodigoRepository`
- `CodigoGenerator`
- `CodigoFormat`
- `CodigoSequenceLock`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No permitir que el usuario imponga códigos internos sin regla explícita.
- No generar slug.
- No generar códigos fuera de transacción.
- No duplicar lógica por service.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `SlugGeneratorService`

**Ruta:** `com.upsjb.ms3.service.contract.SlugGeneratorService`

### Responsabilidad del contrato

Generar slugs únicos para categorías, marcas y productos a partir de nombres legibles.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Generar slug base normalizado.
- Validar unicidad.
- Agregar sufijo incremental si ya existe.
- Regenerar slug si la RN permite cambiarlo al editar nombre.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Services de categoría, marca y producto`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `SlugGeneratorServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.SlugGeneratorServiceImpl`

### Responsabilidad de la implementación

Implementar `SlugGeneratorService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `SlugUtil`
- `Repositories para existsBySlug`
- `StringNormalizer`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No exponer slug como campo obligatorio para el usuario.
- No generar SKU.
- No cambiar slugs históricos sin política clara.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `TipoProductoService`

**Ruta:** `com.upsjb.ms3.service.contract.TipoProductoService`

### Responsabilidad del contrato

Gestionar tipos de producto usados para clasificar productos y definir atributos esperados.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear tipo de producto.
- Actualizar tipo.
- Cambiar estado lógico.
- Listar paginado con filtros.
- Buscar detalle.
- Lookup de tipos activos.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `TipoProductoController y otros services de catálogo`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `TipoProductoServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.TipoProductoServiceImpl`

### Responsabilidad de la implementación

Implementar `TipoProductoService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `TipoProductoRepository`
- `TipoProductoValidator`
- `TipoProductoPolicy`
- `TipoProductoMapper`
- `TipoProductoSpecifications`
- `PaginationService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No gestionar atributos directamente salvo delegación.
- No crear productos.
- No eliminar físicamente.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `CategoriaService`

**Ruta:** `com.upsjb.ms3.service.contract.CategoriaService`

### Responsabilidad del contrato

Gestionar categorías jerárquicas del catálogo.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear categoría.
- Actualizar categoría.
- Cambiar estado.
- Cambiar padre.
- Listar paginado.
- Obtener árbol.
- Lookup por slug/nombre/código.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `CategoriaController, PublicCatalogoController, ProductoAdminService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `CategoriaServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.CategoriaServiceImpl`

### Responsabilidad de la implementación

Implementar `CategoriaService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `CategoriaRepository`
- `CategoriaValidator`
- `CategoriaPolicy`
- `CategoriaMapper`
- `CategoriaSpecifications`
- `SlugGeneratorService`
- `PaginationService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService si impacta catálogo público`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No permitir ciclos.
- No eliminar físicamente.
- No publicar productos.
- No duplicar generación de slug.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `MarcaService`

**Ruta:** `com.upsjb.ms3.service.contract.MarcaService`

### Responsabilidad del contrato

Gestionar marcas comerciales de productos.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear marca.
- Actualizar marca.
- Cambiar estado.
- Listar paginado.
- Obtener detalle.
- Lookup por slug/nombre/código.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `MarcaController, PublicCatalogoController, ProductoAdminService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `MarcaServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.MarcaServiceImpl`

### Responsabilidad de la implementación

Implementar `MarcaService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `MarcaRepository`
- `MarcaValidator`
- `MarcaPolicy`
- `MarcaMapper`
- `MarcaSpecifications`
- `SlugGeneratorService`
- `PaginationService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService si impacta productos públicos`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear productos.
- No modificar SKU.
- No eliminar físicamente marcas con productos asociados.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `AtributoService`

**Ruta:** `com.upsjb.ms3.service.contract.AtributoService`

### Responsabilidad del contrato

Gestionar atributos dinámicos reutilizables para productos y SKU.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear atributo.
- Actualizar atributo.
- Cambiar estado.
- Listar paginado.
- Obtener detalle.
- Lookup de atributos activos.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `AtributoController, ProductoAtributoValorService, SkuAtributoValorService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `AtributoServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.AtributoServiceImpl`

### Responsabilidad de la implementación

Implementar `AtributoService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `AtributoRepository`
- `AtributoValidator`
- `AtributoPolicy`
- `AtributoMapper`
- `AtributoSpecifications`
- `PaginationService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No guardar valores de producto.
- No asociar tipo-producto salvo delegar a TipoProductoAtributoService.
- No eliminar físicamente.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `TipoProductoAtributoService`

**Ruta:** `com.upsjb.ms3.service.contract.TipoProductoAtributoService`

### Responsabilidad del contrato

Asociar atributos a tipos de producto para soportar formularios dinámicos y validación por tipo.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Asignar atributo a tipo de producto.
- Actualizar requerido/orden.
- Inactivar asociación.
- Listar atributos por tipo.
- Validar plantilla de atributos por tipo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `AtributoController, ProductoAdminService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `TipoProductoAtributoServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.TipoProductoAtributoServiceImpl`

### Responsabilidad de la implementación

Implementar `TipoProductoAtributoService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `TipoProductoAtributoRepository`
- `TipoProductoRepository`
- `AtributoRepository`
- `TipoProductoAtributoValidator`
- `AtributoPolicy`
- `TipoProductoAtributoMapper`
- `EntityReferenceService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear atributos.
- No crear productos.
- No guardar valores concretos de producto/SKU.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProductoAdminService`

**Ruta:** `com.upsjb.ms3.service.contract.ProductoAdminService`

### Responsabilidad del contrato

Orquestar la administración interna del producto base.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear producto en BORRADOR.
- Actualizar producto.
- Cambiar estado de registro.
- Cambiar estado de publicación.
- Cambiar estado de venta.
- Publicar producto.
- Programar publicación.
- Ocultar producto.
- Descontinuar producto.
- Listar productos internos paginados y filtrados.
- Obtener detalle administrativo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoAdminController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProductoAdminServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProductoAdminServiceImpl`

### Responsabilidad de la implementación

Implementar `ProductoAdminService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ProductoRepository`
- `ProductoValidator`
- `ProductoPublicacionValidator`
- `ProductoPolicy`
- `ProductoMapper`
- `ProductoSpecifications`
- `EntityReferenceService`
- `CodigoGeneradorService`
- `SlugGeneratorService`
- `PaginationService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear SKU si se usa ProductoSkuService.
- No modificar precio.
- No modificar stock.
- No subir imágenes.
- No publicar Kafka directo.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProductoPublicService`

**Ruta:** `com.upsjb.ms3.service.contract.ProductoPublicService`

### Responsabilidad del contrato

Exponer consulta pública segura de productos visibles para cliente o anónimo.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Listar productos públicos.
- Filtrar por categoría, marca, texto, precio o promoción.
- Obtener detalle público por slug.
- Listar productos próximos.
- Listar promociones visibles asociadas si aplica.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `PublicProductoController y PublicCatalogoController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProductoPublicServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProductoPublicServiceImpl`

### Responsabilidad de la implementación

Implementar `ProductoPublicService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ProductoRepository`
- `ProductoPublicSpecifications`
- `ProductoMapper`
- `PaginationService`
- `PrecioSkuService`
- `PromocionService`
- `StockService si se expone stock visible`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No devolver costos.
- No devolver proveedor.
- No devolver kardex.
- No mostrar productos internos.
- No exigir JWT para rutas públicas.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProductoSkuService`

**Ruta:** `com.upsjb.ms3.service.contract.ProductoSkuService`

### Responsabilidad del contrato

Gestionar variantes/SKU de productos.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear SKU.
- Actualizar SKU.
- Cambiar estado de SKU.
- Descontinuar SKU.
- Listar SKU paginado.
- Listar SKU por producto.
- Obtener detalle.
- Lookup por código/barcode.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoSkuController, CompraInventarioService, PrecioSkuService, PromocionSkuDescuentoService, StockService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProductoSkuServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProductoSkuServiceImpl`

### Responsabilidad de la implementación

Implementar `ProductoSkuService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ProductoSkuRepository`
- `ProductoRepository`
- `ProductoSkuValidator`
- `ProductoSkuPolicy`
- `ProductoSkuMapper`
- `ProductoSkuSpecifications`
- `EntityReferenceService`
- `CodigoGeneradorService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No modificar precio.
- No modificar stock.
- No registrar kardex.
- No duplicar código SKU manualmente.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProductoAtributoValorService`

**Ruta:** `com.upsjb.ms3.service.contract.ProductoAtributoValorService`

### Responsabilidad del contrato

Gestionar valores de atributos a nivel de producto base.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Asignar valor de atributo.
- Actualizar valor.
- Inactivar valor.
- Listar atributos del producto.
- Validar que el atributo corresponda al tipo de producto cuando aplique.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoAdminService o ProductoSkuController si se expone`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProductoAtributoValorServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProductoAtributoValorServiceImpl`

### Responsabilidad de la implementación

Implementar `ProductoAtributoValorService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ProductoAtributoValorRepository`
- `ProductoRepository`
- `AtributoRepository`
- `ProductoValidator`
- `AtributoValidator`
- `ProductoAtributoValorMapper`
- `EntityReferenceService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear atributos.
- No modificar SKU.
- No devolver atributos internos en catálogo público si visible_publico=false.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `SkuAtributoValorService`

**Ruta:** `com.upsjb.ms3.service.contract.SkuAtributoValorService`

### Responsabilidad del contrato

Gestionar valores de atributos específicos de un SKU.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Asignar atributo a SKU.
- Actualizar valor.
- Inactivar valor.
- Listar atributos del SKU.
- Validar tipo de dato del atributo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoSkuController y ProductoPublicService indirectamente`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `SkuAtributoValorServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.SkuAtributoValorServiceImpl`

### Responsabilidad de la implementación

Implementar `SkuAtributoValorService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `SkuAtributoValorRepository`
- `ProductoSkuRepository`
- `AtributoRepository`
- `ProductoSkuValidator`
- `AtributoValidator`
- `SkuAtributoValorMapper`
- `EntityReferenceService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear SKU.
- No modificar stock.
- No modificar precio.
- No exponer atributos privados en endpoints públicos.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProductoImagenService`

**Ruta:** `com.upsjb.ms3.service.contract.ProductoImagenService`

### Responsabilidad del contrato

Orquestar gestión funcional de imágenes de producto/SKU usando Cloudinary.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Subir imagen.
- Guardar metadata Cloudinary.
- Actualizar metadata funcional.
- Marcar principal.
- Listar imágenes.
- Inactivar imagen.
- Publicar evento de snapshot actualizado.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoImagenController, ProductoAdminService indirectamente`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProductoImagenServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProductoImagenServiceImpl`

### Responsabilidad de la implementación

Implementar `ProductoImagenService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `CloudinaryService`
- `ProductoImagenCloudinaryRepository`
- `ProductoImagenValidator`
- `CloudinaryImageValidator`
- `ProductoImagenPolicy`
- `ProductoImagenMapper`
- `EntityReferenceService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No guardar binarios en SQL Server.
- No llamar Cloudinary desde controller.
- No eliminar físico sin política explícita.
- No devolver secretos.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `CloudinaryService`

**Ruta:** `com.upsjb.ms3.service.contract.CloudinaryService`

### Responsabilidad del contrato

Abstraer operaciones técnicas contra Cloudinary.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Subir archivo.
- Eliminar/inactivar recurso externo si se decide.
- Obtener metadata normalizada.
- Manejar errores externos de Cloudinary.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProductoImagenServiceImpl`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `CloudinaryServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.CloudinaryServiceImpl`

### Responsabilidad de la implementación

Implementar `CloudinaryService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `CloudinaryClient`
- `CloudinaryUploadRequest`
- `CloudinaryUploadResponse`
- `CloudinaryDeleteRequest`
- `CloudinaryDeleteResponse`
- `CloudinaryProperties`
- `CloudinaryErrorMapper`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No validar permisos.
- No guardar entidades.
- No decidir imagen principal.
- No construir respuestas HTTP.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `PrecioSkuService`

**Ruta:** `com.upsjb.ms3.service.contract.PrecioSkuService`

### Responsabilidad del contrato

Gestionar precio versionado por SKU.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear nuevo precio vigente.
- Cerrar precio anterior.
- Obtener precio vigente.
- Listar historial paginado.
- Validar precio para publicación o venta.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `PrecioSkuController, ProductoPublicService, PromocionSkuDescuentoService, eventos snapshot`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `PrecioSkuServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.PrecioSkuServiceImpl`

### Responsabilidad de la implementación

Implementar `PrecioSkuService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `PrecioSkuHistorialRepository`
- `ProductoSkuRepository`
- `PrecioSkuValidator`
- `PrecioSkuPolicy`
- `PrecioSkuMapper`
- `PrecioSkuSpecifications`
- `EntityReferenceService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No editar precio histórico directamente.
- No modificar ventas pasadas.
- No publicar Kafka directo.
- No permitir cambio sin motivo.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `PromocionService`

**Ruta:** `com.upsjb.ms3.service.contract.PromocionService`

### Responsabilidad del contrato

Gestionar la campaña base de promoción.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear promoción.
- Actualizar datos base.
- Listar paginado.
- Obtener detalle.
- Inactivar promoción.
- Consultar promociones públicas/vigentes.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `PromocionController, PublicPromocionController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `PromocionServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.PromocionServiceImpl`

### Responsabilidad de la implementación

Implementar `PromocionService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `PromocionRepository`
- `PromocionValidator`
- `PromocionPolicy`
- `PromocionMapper`
- `PromocionSpecifications`
- `CodigoGeneradorService`
- `PaginationService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No definir descuentos por SKU directamente.
- No modificar precio base.
- No calcular facturación.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `PromocionVersionService`

**Ruta:** `com.upsjb.ms3.service.contract.PromocionVersionService`

### Responsabilidad del contrato

Gestionar versiones sensibles de una promoción, incluyendo vigencia, estado y visibilidad.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear versión.
- Activar versión.
- Programar versión.
- Finalizar versión.
- Cancelar versión.
- Listar versiones.
- Validar vigencia pública.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `PromocionController, PromocionService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `PromocionVersionServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.PromocionVersionServiceImpl`

### Responsabilidad de la implementación

Implementar `PromocionVersionService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `PromocionVersionRepository`
- `PromocionRepository`
- `PromocionVersionValidator`
- `PromocionPolicy`
- `PromocionVersionMapper`
- `PromocionVersionSpecifications`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No editar versiones históricas sensibles.
- No crear descuentos por SKU.
- No afectar ventas pasadas.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `PromocionSkuDescuentoService`

**Ruta:** `com.upsjb.ms3.service.contract.PromocionSkuDescuentoService`

### Responsabilidad del contrato

Gestionar descuentos versionados por SKU dentro de una versión de promoción.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Agregar SKU a promoción.
- Actualizar descuento del SKU.
- Inactivar descuento.
- Listar descuentos.
- Calcular precio final estimado.
- Validar margen estimado.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `PromocionController, ProductoPublicService, MS4 snapshots`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `PromocionSkuDescuentoServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.PromocionSkuDescuentoServiceImpl`

### Responsabilidad de la implementación

Implementar `PromocionSkuDescuentoService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `PromocionSkuDescuentoVersionRepository`
- `PromocionVersionRepository`
- `ProductoSkuRepository`
- `PrecioSkuService`
- `PromocionSkuDescuentoValidator`
- `PromocionPolicy`
- `PromocionSkuDescuentoMapper`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No aplicar descuento global uniforme.
- No modificar precio base.
- No afectar ventas pasadas.
- No permitir descuento inválido.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ProveedorService`

**Ruta:** `com.upsjb.ms3.service.contract.ProveedorService`

### Responsabilidad del contrato

Gestionar proveedores de inventario, persona natural o empresa.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear proveedor.
- Actualizar proveedor.
- Cambiar estado.
- Listar paginado.
- Obtener detalle.
- Lookup por RUC/documento/nombre.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ProveedorController, CompraInventarioService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ProveedorServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ProveedorServiceImpl`

### Responsabilidad de la implementación

Implementar `ProveedorService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ProveedorRepository`
- `ProveedorValidator`
- `ProveedorPolicy`
- `ProveedorMapper`
- `ProveedorSpecifications`
- `PaginationService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No registrar compras.
- No modificar stock.
- No eliminar físicamente proveedores con historial.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `AlmacenService`

**Ruta:** `com.upsjb.ms3.service.contract.AlmacenService`

### Responsabilidad del contrato

Gestionar almacenes donde existe stock.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear almacén.
- Actualizar almacén.
- Cambiar estado.
- Marcar principal.
- Configurar permite_venta/permite_compra.
- Listar paginado.
- Lookup por código/nombre.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `AlmacenController, StockService, CompraInventarioService, ReservaStockService, MovimientoInventarioService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `AlmacenServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.AlmacenServiceImpl`

### Responsabilidad de la implementación

Implementar `AlmacenService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `AlmacenRepository`
- `AlmacenValidator`
- `AlmacenPolicy`
- `AlmacenMapper`
- `AlmacenSpecifications`
- `PaginationService`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No modificar stock directamente.
- No registrar kardex.
- No eliminar físicamente almacenes con movimientos.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `StockService`

**Ruta:** `com.upsjb.ms3.service.contract.StockService`

### Responsabilidad del contrato

Consultar y actualizar stock físico/reservado/disponible únicamente desde flujos controlados.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Consultar stock.
- Listar stock paginado.
- Validar disponibilidad.
- Incrementar stock por entrada.
- Disminuir stock por salida.
- Reservar stock.
- Liberar stock reservado.
- Confirmar reserva descontando físico y reservado.
- Actualizar costo promedio/último costo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `StockController, CompraInventarioService, ReservaStockService, MovimientoInventarioService, ProductoPublicService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `StockServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.StockServiceImpl`

### Responsabilidad de la implementación

Implementar `StockService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `StockSkuRepository`
- `StockValidator`
- `StockPolicy`
- `StockSkuMapper`
- `StockSkuSpecifications`
- `StockMathUtil`
- `EntityReferenceService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No permitir actualización directa desde controller.
- No omitir kardex.
- No dejar stock negativo.
- No vender con stock físico en vez de disponible.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `CompraInventarioService`

**Ruta:** `com.upsjb.ms3.service.contract.CompraInventarioService`

### Responsabilidad del contrato

Orquestar compras/adquisiciones de inventario y su confirmación con impacto en stock.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear compra en BORRADOR.
- Actualizar compra en BORRADOR.
- Agregar detalles.
- Confirmar compra.
- Anular compra si procede.
- Listar paginado.
- Obtener detalle.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `CompraInventarioController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `CompraInventarioServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.CompraInventarioServiceImpl`

### Responsabilidad de la implementación

Implementar `CompraInventarioService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `CompraInventarioRepository`
- `CompraInventarioDetalleRepository`
- `CompraInventarioValidator`
- `CompraInventarioPolicy`
- `CompraInventarioMapper`
- `CompraInventarioSpecifications`
- `EntityReferenceService`
- `StockService`
- `MovimientoInventarioService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`
- `CodigoGeneradorService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No confirmar compra sin detalle.
- No actualizar stock sin movimiento.
- No modificar compra confirmada salvo anulaciones controladas.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `ReservaStockService`

**Ruta:** `com.upsjb.ms3.service.contract.ReservaStockService`

### Responsabilidad del contrato

Gestionar reservas de stock para MS4, carritos o ventas.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear reserva.
- Confirmar reserva.
- Liberar reserva.
- Vencer reserva.
- Anular reserva.
- Consultar por referencia externa.
- Listar reservas.
- Procesar eventos MS4 idempotentemente.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `ReservaStockController, Ms4ReconciliacionService, Ms4StockCommandHandler`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `ReservaStockServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.ReservaStockServiceImpl`

### Responsabilidad de la implementación

Implementar `ReservaStockService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ReservaStockRepository`
- `ReservaStockValidator`
- `ReservaStockPolicy`
- `ReservaStockMapper`
- `ReservaStockSpecifications`
- `StockService`
- `MovimientoInventarioService`
- `EventoDominioOutboxService`
- `AuditoriaFuncionalService`
- `CodigoGeneradorService`
- `ProcessedEventGuard`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear venta.
- No facturar.
- No duplicar reservas por el mismo evento MS4.
- No confirmar sin validar estado.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `MovimientoInventarioService`

**Ruta:** `com.upsjb.ms3.service.contract.MovimientoInventarioService`

### Responsabilidad del contrato

Registrar movimientos de inventario y garantizar kardex para todo cambio de stock.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Registrar entrada.
- Registrar salida.
- Registrar ajuste.
- Registrar movimiento por reserva.
- Registrar movimiento por confirmación venta.
- Registrar movimiento compensatorio.
- Listar movimientos.
- Obtener detalle.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `MovimientoInventarioController, CompraInventarioService, ReservaStockService, StockService`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `MovimientoInventarioServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.MovimientoInventarioServiceImpl`

### Responsabilidad de la implementación

Implementar `MovimientoInventarioService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `MovimientoInventarioRepository`
- `MovimientoInventarioValidator`
- `MovimientoInventarioPolicy`
- `MovimientoInventarioMapper`
- `MovimientoInventarioSpecifications`
- `StockService`
- `AuditoriaFuncionalService`
- `EventoDominioOutboxService`
- `CodigoGeneradorService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No modificar stock sin registrar movimiento.
- No eliminar kardex.
- No aceptar movimientos sin motivo.
- No duplicar eventos idempotentes.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `KardexService`

**Ruta:** `com.upsjb.ms3.service.contract.KardexService`

### Responsabilidad del contrato

Exponer consulta histórica del kardex basado en movimientos de inventario.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Consultar kardex por SKU.
- Consultar kardex por almacén.
- Filtrar por fechas, tipo, motivo, referencia y actor.
- Listar paginado.
- Obtener detalle histórico.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `KardexController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `KardexServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.KardexServiceImpl`

### Responsabilidad de la implementación

Implementar `KardexService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `MovimientoInventarioRepository`
- `KardexValidator`
- `KardexPolicy`
- `KardexMapper`
- `KardexSpecifications`
- `PaginationService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No registrar movimientos.
- No modificar stock.
- No corregir kardex directamente.
- No exponer costos a usuarios no autorizados.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `EmpleadoSnapshotMs2Service`

**Ruta:** `com.upsjb.ms3.service.contract.EmpleadoSnapshotMs2Service`

### Responsabilidad del contrato

Mantener snapshot mínimo de empleados provenientes de MS2.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear o actualizar snapshot.
- Consultar por idUsuarioMs1.
- Consultar por idEmpleadoMs2.
- Listar empleados snapshot.
- Validar empleado activo para permisos.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `EmpleadoInventarioPermisoController, EmpleadoInventarioPermisoService, Policies`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `EmpleadoSnapshotMs2ServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.EmpleadoSnapshotMs2ServiceImpl`

### Responsabilidad de la implementación

Implementar `EmpleadoSnapshotMs2Service` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `EmpleadoSnapshotMs2Repository`
- `EmpleadoSnapshotMs2Validator`
- `EmpleadoSnapshotMs2Mapper`
- `Ms2EmpleadoSnapshotClient`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear empleados en MS2.
- No modificar datos laborales oficiales.
- No crear usuarios MS1.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `EmpleadoInventarioPermisoService`

**Ruta:** `com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService`

### Responsabilidad del contrato

Versionar y consultar permisos funcionales de inventario otorgados a empleados.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Otorgar permisos.
- Revocar permisos.
- Cerrar permiso vigente anterior.
- Consultar permiso vigente.
- Listar historial.
- Validar permisos para policies.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `EmpleadoInventarioPermisoController y Policies de producto/stock/compra/kardex`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `EmpleadoInventarioPermisoServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.EmpleadoInventarioPermisoServiceImpl`

### Responsabilidad de la implementación

Implementar `EmpleadoInventarioPermisoService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `EmpleadoInventarioPermisoHistorialRepository`
- `EmpleadoSnapshotMs2Service`
- `EmpleadoInventarioPermisoValidator`
- `EmpleadoInventarioPermisoPolicy`
- `EmpleadoInventarioPermisoMapper`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No asignar rol global.
- No permitir autoasignación.
- No editar permisos históricos.
- No reemplazar MS1/MS2.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `AuditoriaFuncionalService`

**Ruta:** `com.upsjb.ms3.service.contract.AuditoriaFuncionalService`

### Responsabilidad del contrato

Registrar y consultar auditoría funcional del MS3.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Registrar evento exitoso.
- Registrar validación fallida.
- Registrar acceso denegado.
- Registrar error funcional.
- Listar auditoría paginada.
- Filtrar por entidad, actor, evento, resultado y fechas.
- Obtener detalle.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Todos los services de negocio y AuditoriaController`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `AuditoriaFuncionalServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.AuditoriaFuncionalServiceImpl`

### Responsabilidad de la implementación

Implementar `AuditoriaFuncionalService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `AuditoriaFuncionalRepository`
- `AuditoriaFuncionalMapper`
- `AuditoriaFuncionalSpecifications`
- `AuditContextHolder`
- `AuditEventFactory`
- `AuditMetadataBuilder`
- `PaginationService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No registrar información sensible innecesaria.
- No bloquear transacciones críticas si auditoría no esencial falla sin política clara.
- No modificar auditoría histórica.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `EventoDominioOutboxService`

**Ruta:** `com.upsjb.ms3.service.contract.EventoDominioOutboxService`

### Responsabilidad del contrato

Registrar, consultar y administrar eventos Outbox pendientes de publicación Kafka.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Crear evento pendiente.
- Listar eventos.
- Obtener detalle.
- Marcar publicado.
- Marcar error.
- Incrementar intentos.
- Preparar reintento administrativo.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Services de negocio, OutboxController, OutboxEventPublisher`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `EventoDominioOutboxServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.EventoDominioOutboxServiceImpl`

### Responsabilidad de la implementación

Implementar `EventoDominioOutboxService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `EventoDominioOutboxRepository`
- `EventoDominioOutboxValidator`
- `EventoDominioOutboxMapper`
- `EventoDominioOutboxSpecifications`
- `OutboxProperties`
- `AuditoriaFuncionalService`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No publicar Kafka directamente desde negocio.
- No permitir payload arbitrario de usuario.
- No perder eventos de cambios sensibles.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `KafkaPublisherService`

**Ruta:** `com.upsjb.ms3.service.contract.KafkaPublisherService`

### Responsabilidad del contrato

Publicar eventos de dominio a Kafka desde eventos Outbox ya registrados.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Publicar evento pendiente.
- Publicar lote.
- Manejar resultado de publicación.
- Registrar error técnico.
- Delegar al publisher Kafka real.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `OutboxEventPublisher, OutboxController para reintentos`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `KafkaPublisherServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.KafkaPublisherServiceImpl`

### Responsabilidad de la implementación

Implementar `KafkaPublisherService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `KafkaDomainEventPublisher`
- `KafkaTopicResolver`
- `KafkaEventKeyResolver`
- `OutboxEventSerializer`
- `EventoDominioOutboxService`
- `KafkaPublisherServiceImpl`
- `OutboxRetryPolicy`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear eventos de negocio.
- No validar reglas de dominio.
- No ser llamado directamente por controllers normales.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

## `Ms4ReconciliacionService`

**Ruta:** `com.upsjb.ms3.service.contract.Ms4ReconciliacionService`

### Responsabilidad del contrato

Procesar y reconciliar eventos/comandos de stock enviados por MS4 cuando hay ventas, reservas, confirmaciones, liberaciones o anulaciones.

Este contrato debe definir los métodos públicos que serán consumidos por controllers u otros services, evitando que el controller conozca detalles de repositories, validators, policies, mappers, outbox o integraciones externas.

### Funcionalidades esperadas

- Procesar reserva pendiente de MS4.
- Procesar confirmación de venta.
- Procesar liberación de stock.
- Procesar anulación de venta.
- Validar idempotencia.
- Aplicar cambios en stock.
- Registrar kardex.
- Publicar snapshot actualizado.
- Devolver resultado de sincronización.

### Reglas de contrato

- Debe trabajar con DTOs de request, response y filter.
- Debe aceptar filtros en todo listado relevante.
- Debe aceptar paginación en todo listado administrativo o público que pueda crecer.
- Debe exponer métodos de lookup solo si corresponde al caso de uso.
- Debe devolver respuestas pensadas para usuario final o DTOs envueltos por la capa controller.
- No debe exponer entidades JPA.
- No debe obligar al frontend a enviar FK crudos como única forma de relación.
- Debe permitir que la implementación resuelva referencias por código, nombre, slug, barcode o identificadores funcionales.

### Consumidores principales

- `Ms4StockSyncController y Ms4StockCommandHandler`.

### Coexistencia

- El controller depende del contrato, no de la implementación.
- Otros services pueden depender del contrato cuando necesiten una operación funcional completa.
- Los validators, policies y repositories no deben depender del contrato salvo casos excepcionales para evitar ciclos.
- El contrato debe mantenerse estable para no romper controllers.

---

## `Ms4ReconciliacionServiceImpl`

**Ruta:** `com.upsjb.ms3.service.impl.Ms4ReconciliacionServiceImpl`

### Responsabilidad de la implementación

Implementar `Ms4ReconciliacionService` aplicando reglas de negocio, transacciones, validaciones, autorización, auditoría, mapeo, persistencia, DRY y eventos de dominio cuando corresponda.

### Flujo recomendado por método de escritura

```text
1. Recibir DTO.
2. Obtener actor autenticado si aplica.
3. Validar autorización con Policy.
4. Resolver referencias funcionales con EntityReferenceService o resolvers compartidos.
5. Validar reglas de negocio con Validator.
6. Generar código/slug si aplica.
7. Persistir cambios con Repository.
8. Mapear respuesta con Mapper.
9. Registrar auditoría funcional.
10. Registrar evento Outbox si impacta MS4 o catálogo público.
11. Retornar respuesta clara al usuario.
```

### Flujo recomendado por método de listado

```text
1. Recibir filter DTO y PageRequestDto.
2. Normalizar filtros.
3. Aplicar estado activo por defecto si corresponde.
4. Construir Specification.
5. Validar campos sort permitidos.
6. Ejecutar repository paginado.
7. Mapear entidades a response DTO.
8. Construir PageResponseDto con PaginationService.
9. Retornar lista con mensaje de consulta exitosa.
```

### Clases que debe consumir

- `ReservaStockService`
- `StockService`
- `MovimientoInventarioService`
- `Ms4StockEventValidator`
- `ProcessedEventGuard`
- `EventoDominioOutboxService`
- `AuditoriaFuncionalService`
- `Ms4StockEventMapper`

### Respuestas esperadas

- En creación exitosa: mensaje claro indicando que el registro fue creado correctamente.
- En actualización exitosa: mensaje claro indicando que el registro fue actualizado correctamente.
- En inactivación o eliminación lógica: mensaje claro indicando que el registro fue inactivado correctamente.
- En validación fallida: mensaje específico y entendible para el usuario.
- En conflicto funcional: mensaje indicando por qué no se puede completar la acción.
- En error técnico inesperado: debe dejar que `GlobalExceptionHandler` devuelva mensaje general y registrar detalle técnico en logs.

### Coexistencia con DRY/shared

Debe reutilizar componentes compartidos para no duplicar:

- Paginación.
- Resolución de referencias.
- Validación de estado activo.
- Normalización de texto.
- Generación de código.
- Generación de slug.
- Construcción de respuestas.
- Construcción de auditoría.
- Registro de outbox.
- Idempotencia, si aplica.

### Qué NO debe hacer

- No crear ventas.
- No facturar.
- No modificar precio usado en venta.
- No aplicar dos veces el mismo evento.
- No depender de MS4 HTTP para el flujo normal Kafka.

- No debe capturar excepciones para ocultarlas sin trazabilidad.
- No debe devolver stacktrace al usuario.
- No debe construir manualmente respuestas en todos los métodos si existe `ApiResponseFactory`.
- No debe duplicar lógica que pertenece a validators o policies.
- No debe publicar Kafka directamente si corresponde usar Outbox.


---

# 3. Reglas finales para programar implementaciones

## 3.1. Respuesta funcional uniforme

Cada método que ejecute una acción debe devolver una respuesta clara.

Ejemplo conceptual:

```text
crearProducto:
    "Producto creado correctamente."

actualizarProducto:
    "Producto actualizado correctamente."

publicarProducto:
    "Producto publicado correctamente."

registrarEntrada:
    "Entrada de inventario registrada correctamente."

confirmarReserva:
    "Reserva confirmada correctamente."

error de validación:
    "No se puede publicar el producto porque no tiene precio vigente."

error técnico:
    "Ocurrió un error interno del sistema. Intente nuevamente o contacte al administrador."
```

El error técnico real debe ir a logs con requestId/correlationId.

---

## 3.2. No duplicar reglas

Si una validación aparece en más de un service, debe moverse a:

```text
- validator
- policy
- shared.validation
- shared.persistence
- shared.reference
- util
```

Ejemplo:

```text
Validar stock suficiente:
    StockValidator

Validar permiso empleado:
    EmpleadoInventarioPermisoService + Policy correspondiente

Resolver SKU por código:
    EntityReferenceService o ProductoSkuReferenceResolver

Crear PageResponseDto:
    PaginationService

Normalizar texto:
    StringNormalizer

Generar slug:
    SlugGeneratorService + SlugUtil
```

---

## 3.3. Transacciones

Deben ser transaccionales:

```text
- Crear producto con atributos iniciales.
- Crear SKU.
- Cambiar precio vigente.
- Crear versión de promoción.
- Asociar descuentos a promoción.
- Confirmar compra.
- Registrar entrada.
- Registrar salida.
- Registrar ajuste.
- Crear reserva.
- Confirmar reserva.
- Liberar reserva.
- Procesar evento MS4.
- Registrar outbox junto al cambio de negocio.
```

La publicación Kafka debe ocurrir después, mediante outbox.

---

## 3.4. Seguridad de mensajes

Mensajes al usuario:

```text
Correctos:
    "Stock insuficiente para registrar la salida."
    "No se encontró el SKU solicitado."
    "El producto no puede publicarse porque no tiene imagen principal."

Incorrectos:
    "NullPointerException en ProductoAdminServiceImpl línea 145."
    "Error SQL constraint UX_stock_sku_activo."
    "Fallo de deserialización Kafka en payload interno."
```

Los errores técnicos deben estar en logs, no en la respuesta pública.

---

## 3.5. Coherencia MS3-MS4

Los services que impactan MS4 deben registrar outbox:

```text
ProductoAdminServiceImpl
ProductoSkuServiceImpl
ProductoImagenServiceImpl
PrecioSkuServiceImpl
PromocionVersionServiceImpl
PromocionSkuDescuentoServiceImpl
StockServiceImpl
CompraInventarioServiceImpl
ReservaStockServiceImpl
MovimientoInventarioServiceImpl
Ms4ReconciliacionServiceImpl
```

Los eventos deben permitir que MS4 actualice snapshots sin consultar MS3.

---

## 3.6. Coherencia con Cloudinary

Solo estas clases deben tocar Cloudinary directa o indirectamente:

```text
ProductoImagenServiceImpl
CloudinaryServiceImpl
CloudinaryClientImpl
CloudinaryImageValidator
CloudinaryPolicy
CloudinaryProperties
CloudinaryClientConfig
```

Ningún otro service debe usar SDK Cloudinary directamente.

---

## 3.7. Coherencia con permisos de empleado

Los permisos especiales del empleado deben consultarse mediante:

```text
EmpleadoInventarioPermisoService
EmpleadoInventarioPermisoPolicy
```

No se debe repetir en cada service la consulta manual al repository de permisos.

---

## 3.8. Criterio de producción

Una implementación está lista para producción cuando:

```text
- Tiene transacciones correctas.
- Usa Policy.
- Usa Validator.
- Usa Mapper.
- Usa Repository.
- Usa Specification para listados.
- Usa PaginationService.
- Usa EntityReferenceService para FK funcionales.
- Usa AuditoriaFuncionalService.
- Usa Outbox si impacta MS4.
- Devuelve mensajes claros.
- No expone errores técnicos.
- No duplica lógica transversal.
- No rompe propiedad de dominio entre MS3 y MS4.
```

# Documentación técnica de código - MS3 Parte 3

**Microservicio:** `ms-catalogo-inventario`  
**Paquetes documentados:** `domain`, `repository`, `dto`  
**Objetivo:** documentar responsabilidad, coexistencia, forma correcta de programación y límites técnicos de entidades, enums, value objects, repositories y DTOs del MS3.

---

# 1. Reglas generales

## 1.1. `domain.entity`

Las entidades representan el modelo persistente del MS3. Deben mapear la base de datos, relaciones y restricciones funcionales, pero no deben contener lógica pesada de negocio.

Deben:
- Mapear tablas, columnas y relaciones JPA.
- Usar enums del dominio con `EnumType.STRING`.
- Heredar de `AuditableEntity` cuando aplique.
- Respetar eliminación lógica mediante `estado`.
- Mantener relaciones claras con otras entidades del MS3.
- Permitir que services, validators, mappers y repositories trabajen sobre ellas.

No deben:
- Llamar repositories.
- Llamar services.
- Llamar validators.
- Llamar policies.
- Construir respuestas HTTP.
- Publicar Kafka.
- Subir imágenes a Cloudinary.
- Resolver reglas de autorización.

## 1.2. `domain.enums`

Los enums centralizan valores controlados del dominio para evitar strings repetidos. Deben coincidir con los valores permitidos por base de datos, validators, DTOs y reglas de negocio.

Deben:
- Usarse en entities, DTOs, validators, policies y specifications.
- Mapearse con `EnumType.STRING`.
- Exponerse a Angular desde `ReferenceDataService` cuando sean opciones de formulario.
- Evitar valores duplicados en constantes sueltas.

No deben:
- Tener lógica de negocio pesada.
- Consultar base de datos.
- Depender de Spring.

## 1.3. `domain.value`

Los value objects encapsulan conceptos pequeños y reutilizables: dinero, stock, porcentaje, slug, código, documento, RUC, etc.

Deben:
- Ser inmutables.
- Validar formato o rango básico.
- Evitar duplicar validaciones pequeñas en muchos validators.
- No depender de Spring.
- No consultar base de datos.

## 1.4. `repository`

Los repositories son acceso a datos. No hacen negocio.

Deben:
- Extender `JpaRepository<Entity, Long>`.
- Extender `JpaSpecificationExecutor<Entity>` cuando haya listados filtrables.
- Exponer `findBy...AndEstadoTrue`.
- Exponer `existsBy...AndEstadoTrue`.
- Usar `Optional` en búsquedas únicas.
- Usar `@Lock` cuando haya concurrencia crítica: stock, correlativos, outbox.

No deben:
- Autorizar.
- Validar reglas de negocio completas.
- Construir mensajes para usuario.
- Publicar eventos.
- Llamar Cloudinary.

## 1.5. `dto`

Los DTOs son contratos de entrada/salida. Deben separar request, response y filter.

Deben:
- Usar Bean Validation en requests.
- Usar `EntityReferenceDto` para relaciones funcionales.
- Evitar que Angular esté obligado a enviar FK crudos.
- Usar filtros para listados.
- Usar `PageRequestDto` y `PageResponseDto` para paginación.
- No exponer entidades JPA.
- No exponer datos sensibles en DTOs públicos.

---

# 2. `domain.entity`


## `AuditableEntity.java`

### Responsabilidad
Entidad base abstracta para campos comunes de auditoría técnica y eliminación lógica.

### Coexiste con
Todas las entidades funcionales que compartan `estado`, `createdAt`, `updatedAt`.

### Cómo programarlo
Debe ser `@MappedSuperclass`; centraliza campos repetidos; evita duplicar columnas comunes.

### Qué evitar
No registra auditoría funcional; no reemplaza `AuditoriaFuncional`; no conoce usuario autenticado.


## `CorrelativoCodigo.java`

### Responsabilidad
Entidad para generación inteligente de códigos internos.

### Coexiste con
`CodigoGeneradorServiceImpl`, `CorrelativoCodigoRepository`, `shared.code`.

### Cómo programarlo
Debe soportar entidad, prefijo, último número, longitud y estado; puede usarse con lock pesimista.

### Qué evitar
No debe generar códigos fuera del service; no se expone al usuario final.


## `TipoProducto.java`

### Responsabilidad
Catálogo base para clasificar productos y definir atributos esperados.

### Coexiste con
`Producto`, `TipoProductoAtributo`, services de catálogo.

### Cómo programarlo
Debe tener código/nombre únicos activos; debe estar activo para crear productos.

### Qué evitar
No crea productos; no valida publicación.


## `Categoria.java`

### Responsabilidad
Categoría jerárquica del catálogo.

### Coexiste con
`Producto`, `CategoriaServiceImpl`, `ProductoAdminServiceImpl`, catálogo público.

### Cómo programarlo
Debe soportar padre/hijo, nivel, orden y slug generado; validator evita ciclos.

### Qué evitar
No calcula árbol pesado dentro de la entidad; no publica productos.


## `Marca.java`

### Responsabilidad
Marca comercial asociable a productos.

### Coexiste con
`Producto`, `MarcaServiceImpl`, `ProductoAdminServiceImpl`.

### Cómo programarlo
Debe tener código, nombre y slug; puede ser opcional en producto.

### Qué evitar
No maneja stock, precio ni promociones.


## `Atributo.java`

### Responsabilidad
Definición de atributo dinámico reutilizable.

### Coexiste con
`TipoProductoAtributo`, `ProductoAtributoValor`, `SkuAtributoValor`.

### Cómo programarlo
Debe guardar tipoDato, unidad, requerido, filtrable y visiblePublico.

### Qué evitar
No guarda valores; no valida valores concretos por producto.


## `TipoProductoAtributo.java`

### Responsabilidad
Asociación entre tipo de producto y atributos esperados.

### Coexiste con
`TipoProducto`, `Atributo`, `TipoProductoAtributoServiceImpl`, validators.

### Cómo programarlo
Debe evitar duplicidad activa y permitir requerido/orden.

### Qué evitar
No guarda valores de producto ni SKU.


## `Producto.java`

### Responsabilidad
Entidad principal del catálogo comercial.

### Coexiste con
`ProductoSku`, `ProductoImagenCloudinary`, precio, promoción y stock vía SKU.

### Cómo programarlo
Debe separar producto base de SKU; usar código y slug generados; manejar estados de registro/publicación/venta.

### Qué evitar
No guarda stock directo; no guarda precio directo; no aplica promociones directamente.


## `ProductoSku.java`

### Responsabilidad
Variante exacta vendible/controlable de un producto.

### Coexiste con
`StockSku`, `PrecioSkuHistorial`, `ReservaStock`, promociones, movimientos.

### Cómo programarlo
Debe ser unidad real de stock, precio y venta; código SKU generado; barcode opcional.

### Qué evitar
No guarda precio actual directo; no guarda stock total directo.


## `ProductoAtributoValor.java`

### Responsabilidad
Valor dinámico a nivel de producto base.

### Coexiste con
`Producto`, `Atributo`, `ProductoAtributoValorServiceImpl`.

### Cómo programarlo
Debe guardar un valor de tipo texto/número/boolean/fecha; validator asegura coherencia.

### Qué evitar
No decide visibilidad pública; no crea atributos.


## `SkuAtributoValor.java`

### Responsabilidad
Valor dinámico a nivel de SKU.

### Coexiste con
`ProductoSku`, `Atributo`, `SkuAtributoValorServiceImpl`.

### Cómo programarlo
Debe permitir atributos específicos de variante.

### Qué evitar
No modifica stock, precio ni producto base.


## `ProductoImagenCloudinary.java`

### Responsabilidad
Metadata de imagen almacenada en Cloudinary.

### Coexiste con
`ProductoImagenServiceImpl`, `CloudinaryServiceImpl`, catálogo público.

### Cómo programarlo
Debe guardar publicId, secureUrl, formato, dimensiones, orden, principal; no binarios.

### Qué evitar
No sube archivos; no guarda secretos; no elimina Cloudinary por sí misma.


## `Proveedor.java`

### Responsabilidad
Proveedor de inventario: persona natural o empresa.

### Coexiste con
`CompraInventario`, `ProveedorServiceImpl`, compras.

### Cómo programarlo
Debe soportar RUC/razón social o documento/nombres según tipo.

### Qué evitar
No representa clientes; no modifica stock.


## `Almacen.java`

### Responsabilidad
Ubicación física o lógica donde existe stock.

### Coexiste con
`StockSku`, compras, reservas, movimientos.

### Cómo programarlo
Debe indicar principal, permiteVenta, permiteCompra y estado.

### Qué evitar
No actualiza stock; no registra kardex.


## `StockSku.java`

### Responsabilidad
Stock físico/reservado/disponible por SKU y almacén.

### Coexiste con
`StockServiceImpl`, reservas, movimientos, catálogo público si se expone disponibilidad.

### Cómo programarlo
Debe mantener físico/reservado no negativos; disponible calculado.

### Qué evitar
No registra movimientos; no se actualiza fuera de services controlados.


## `EmpleadoSnapshotMs2.java`

### Responsabilidad
Snapshot mínimo de empleado proveniente de MS2.

### Coexiste con
Permisos de inventario, policies y services operativos.

### Cómo programarlo
Debe evitar dependencia constante de MS2; guarda idEmpleadoMs2 e idUsuarioMs1.

### Qué evitar
No crea empleados; no reemplaza MS2.


## `EmpleadoInventarioPermisoHistorial.java`

### Responsabilidad
Permisos funcionales versionados para empleados en MS3.

### Coexiste con
Policies de producto, stock, compras, kardex e imágenes.

### Cómo programarlo
Debe permitir un permiso vigente por empleado y cerrar vigencias anteriores.

### Qué evitar
No asigna roles globales; no modifica MS1/MS2.


## `PrecioSkuHistorial.java`

### Responsabilidad
Historial versionado de precio de venta por SKU.

### Coexiste con
`PrecioSkuServiceImpl`, catálogo público, promoción, MS4 snapshot.

### Cómo programarlo
Debe existir un precio vigente por SKU; conserva ventas pasadas.

### Qué evitar
No edita precio histórico; no guarda precio en SKU.


## `Promocion.java`

### Responsabilidad
Campaña promocional base.

### Coexiste con
`PromocionVersion`, services de promoción.

### Cómo programarlo
Debe tener código generado, nombre y descripción.

### Qué evitar
No guarda fechas ni descuentos por SKU.


## `PromocionVersion.java`

### Responsabilidad
Versión de promoción con fechas, estado y visibilidad.

### Coexiste con
`PromocionSkuDescuentoVersion`, catálogo público, MS4 snapshot.

### Cómo programarlo
Debe controlar vigencia, programación, activación y cancelación.

### Qué evitar
No guarda descuentos concretos por SKU.


## `PromocionSkuDescuentoVersion.java`

### Responsabilidad
Descuento específico por SKU dentro de una versión.

### Coexiste con
Promoción, SKU, precio vigente, MS4 snapshot.

### Cómo programarlo
Debe soportar porcentaje, monto fijo o precio final; descuento distinto por SKU.

### Qué evitar
No modifica precio base; no aplica descuento global.


## `CompraInventario.java`

### Responsabilidad
Cabecera de adquisición/compra de inventario.

### Coexiste con
Detalles de compra, proveedor, stock y kardex al confirmar.

### Cómo programarlo
Debe iniciar en BORRADOR y confirmar para impactar stock.

### Qué evitar
No actualiza stock sin service; no omite detalle.


## `CompraInventarioDetalle.java`

### Responsabilidad
Detalle de compra por SKU y almacén.

### Coexiste con
Compra, SKU, almacén y movimiento de entrada.

### Cómo programarlo
Debe guardar cantidad, costo unitario y costo total.

### Qué evitar
No confirma compra; no registra kardex por sí mismo.


## `ReservaStock.java`

### Responsabilidad
Reserva de stock para MS4, carrito o venta.

### Coexiste con
Stock, movimientos, reconciliación MS4.

### Cómo programarlo
Debe ser idempotente por referencia externa; maneja estados de reserva.

### Qué evitar
No crea venta; no factura.


## `MovimientoInventario.java`

### Responsabilidad
Kardex histórico de cambios de stock.

### Coexiste con
Stock, compra, reserva, MS4, auditoría.

### Cómo programarlo
Debe guardar stock anterior/nuevo, actor, referencia, tipo y motivo.

### Qué evitar
No se elimina físicamente; correcciones por movimiento compensatorio.


## `AuditoriaFuncional.java`

### Responsabilidad
Registro de auditoría funcional de operaciones críticas.

### Coexiste con
Todos los services de negocio y auditoría administrativa.

### Cómo programarlo
Debe guardar actor, evento, entidad, resultado, metadata segura y trazabilidad.

### Qué evitar
No reemplaza logs técnicos; no guarda tokens/contraseñas.


## `EventoDominioOutbox.java`

### Responsabilidad
Evento pendiente/publicado/error del patrón Outbox Kafka.

### Coexiste con
Services de negocio, OutboxPublisher, KafkaPublisherService.

### Cómo programarlo
Debe guardarse en la misma transacción del cambio de negocio.

### Qué evitar
No publica Kafka por sí sola; no acepta payload arbitrario de usuario.


---

# 3. `domain.enums`


## `EstadoRegistro.java`

### Responsabilidad
Estado genérico de registros simples cuando aplique.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoProductoRegistro.java`

### Responsabilidad
Ciclo administrativo del producto: BORRADOR, ACTIVO, INACTIVO, DESCONTINUADO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoProductoPublicacion.java`

### Responsabilidad
Estado público del producto: NO_PUBLICADO, PUBLICADO, PROGRAMADO, OCULTO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoProductoVenta.java`

### Responsabilidad
Disponibilidad comercial: NO_VENDIBLE, VENDIBLE, SOLO_VISIBLE, AGOTADO, PROXIMAMENTE.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoSku.java`

### Responsabilidad
Estado funcional del SKU: ACTIVO, INACTIVO, DESCONTINUADO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `GeneroObjetivo.java`

### Responsabilidad
Público objetivo: HOMBRE, MUJER, UNISEX, NIÑO, NIÑA, GENERAL.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoDatoAtributo.java`

### Responsabilidad
Tipo de dato para atributos dinámicos: TEXTO, NUMERO, DECIMAL, BOOLEANO, FECHA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoProveedor.java`

### Responsabilidad
Tipo de proveedor: PERSONA_NATURAL o EMPRESA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoDocumentoProveedor.java`

### Responsabilidad
Tipos de documento permitidos para proveedor.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `Moneda.java`

### Responsabilidad
Monedas permitidas: PEN, USD u otras autorizadas.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoCompraInventario.java`

### Responsabilidad
Estado de compra: BORRADOR, CONFIRMADA, ANULADA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoReservaStock.java`

### Responsabilidad
Estado de reserva: RESERVADA, CONFIRMADA, LIBERADA, VENCIDA, ANULADA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoReferenciaStock.java`

### Responsabilidad
Origen de reserva/movimiento: VENTA_MS4, CARRITO_MS4, AJUSTE_MS3.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoMovimientoInventario.java`

### Responsabilidad
Tipos de kardex: entrada, salida, reserva, confirmación, liberación, ajuste, traslado, merma.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoMovimientoInventario.java`

### Responsabilidad
Estado del movimiento: REGISTRADO, COMPENSADO, ANULADO_LOGICO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `MotivoMovimientoInventario.java`

### Responsabilidad
Motivos controlados de movimientos para evitar textos inconsistentes.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoPromocion.java`

### Responsabilidad
Estado de versión promocional: BORRADOR, PROGRAMADA, ACTIVA, FINALIZADA, CANCELADA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoDescuento.java`

### Responsabilidad
Tipo de descuento: PORCENTAJE, MONTO_FIJO, PRECIO_FINAL.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `CloudinaryResourceType.java`

### Responsabilidad
Tipo de recurso Cloudinary: image, video, raw; normalmente image.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `RolSistema.java`

### Responsabilidad
Roles reconocidos por MS3: ADMIN, EMPLEADO, CLIENTE, ANONIMO, SISTEMA.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `TipoEventoAuditoria.java`

### Responsabilidad
Eventos funcionales auditables del MS3.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EntidadAuditada.java`

### Responsabilidad
Entidades auditables: PRODUCTO, SKU, STOCK, PROMOCION, COMPRA, RESERVA, OUTBOX, etc.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `ResultadoAuditoria.java`

### Responsabilidad
Resultado de auditoría: EXITOSO, FALLIDO, DENEGADO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `EstadoPublicacionEvento.java`

### Responsabilidad
Estado outbox: PENDIENTE, PUBLICADO, ERROR.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `AggregateType.java`

### Responsabilidad
Aggregate raíz para eventos Kafka: PRODUCTO, SKU, STOCK, PRECIO, PROMOCION, MOVIMIENTO.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `ProductoEventType.java`

### Responsabilidad
Eventos Kafka de producto y SKU.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `StockEventType.java`

### Responsabilidad
Eventos Kafka de stock, reserva y movimiento.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `PrecioEventType.java`

### Responsabilidad
Eventos Kafka de precio.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `PromocionEventType.java`

### Responsabilidad
Eventos Kafka de promoción.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


## `Ms4StockEventType.java`

### Responsabilidad
Eventos/comandos entrantes desde MS4 para stock.

### Cómo programarlo
- Declarar valores cerrados y coherentes con la base de datos.
- Usar `@Enumerated(EnumType.STRING)` en entidades.
- Exponerlo mediante `ReferenceDataService` si Angular lo necesita para selects.
- Puede tener `code` y `label` si se requiere mostrar una opción amigable.

### Coexiste con
- Entities.
- DTOs.
- Validators.
- Policies.
- Specifications.
- ReferenceDataService.

### Qué evitar
- No usar `EnumType.ORDINAL`.
- No duplicar sus valores como strings sueltos.
- No poner lógica pesada de negocio dentro del enum.


---

# 4. `domain.value`


## `CodigoGeneradoValue.java`

### Responsabilidad
Representar y validar códigos generados por backend.

### Coexiste con
CodigoGeneradorServiceImpl, CodigoGenerator, entidades con código.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `SlugValue.java`

### Responsabilidad
Representar slug URL-friendly y validar formato.

### Coexiste con
SlugGeneratorServiceImpl, SlugUtil, Producto/Categoria/Marca.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `MoneyValue.java`

### Responsabilidad
Representar dinero con monto y moneda.

### Coexiste con
PrecioSkuServiceImpl, CompraInventarioServiceImpl, PromocionSkuDescuentoServiceImpl.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `StockValue.java`

### Responsabilidad
Representar cantidades de stock y reglas básicas no negativas.

### Coexiste con
StockServiceImpl, ReservaStockServiceImpl, MovimientoInventarioServiceImpl.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `PorcentajeValue.java`

### Responsabilidad
Representar porcentajes válidos para descuentos o métricas.

### Coexiste con
PromocionSkuDescuentoValidator, PercentageUtil.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `DocumentoProveedorValue.java`

### Responsabilidad
Validar documento de proveedor persona natural.

### Coexiste con
ProveedorValidator, ProveedorServiceImpl.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `RucValue.java`

### Responsabilidad
Validar RUC de proveedor empresa.

### Coexiste con
ProveedorValidator, ProveedorServiceImpl.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `CloudinaryPublicIdValue.java`

### Responsabilidad
Validar public_id de Cloudinary.

### Coexiste con
ProductoImagenServiceImpl, CloudinaryServiceImpl.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


## `NombreNormalizadoValue.java`

### Responsabilidad
Representar nombres normalizados para búsquedas y duplicados.

### Coexiste con
StringNormalizer, SlugGeneratorServiceImpl, validators.

### Cómo programarlo
- Debe ser inmutable.
- Debe tener factory estático como `of(...)` o `from(...)`.
- Debe validar reglas pequeñas y reutilizables.
- No debe depender de Spring.
- Debe evitar duplicar validaciones simples en múltiples validators.

### Qué evitar
- No consultar repositories.
- No llamar services.
- No construir respuestas HTTP.
- No reemplazar validators cuando la validación depende de BD.


---

# 5. `repository`

## Reglas generales

Cada repository debe extender `JpaRepository` y, cuando tenga listados filtrables, `JpaSpecificationExecutor`.

Debe exponer consultas funcionales por estado activo, duplicados y búsquedas por identificadores reconocibles. No debe contener lógica de negocio ni autorización.


## `CorrelativoCodigoRepository.java`

### Responsabilidad
Acceso a correlativos para generación de códigos.

### Coexiste con
CodigoGeneradorServiceImpl.

### Métodos esperados
findByEntidadAndEstadoTrue, existsByEntidadAndEstadoTrue, búsqueda con lock por entidad.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `TipoProductoRepository.java`

### Responsabilidad
Acceso a tipos de producto.

### Coexiste con
TipoProductoServiceImpl, EntityReferenceServiceImpl.

### Métodos esperados
findByCodigoAndEstadoTrue, existsByCodigoAndEstadoTrue, existsByNombreAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `CategoriaRepository.java`

### Responsabilidad
Acceso a categorías jerárquicas.

### Coexiste con
CategoriaServiceImpl, SlugGeneratorServiceImpl.

### Métodos esperados
findBySlugAndEstadoTrue, findByCodigoAndEstadoTrue, findByIdCategoriaPadreAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `MarcaRepository.java`

### Responsabilidad
Acceso a marcas.

### Coexiste con
MarcaServiceImpl, SlugGeneratorServiceImpl.

### Métodos esperados
findBySlugAndEstadoTrue, findByCodigoAndEstadoTrue, existsByNombreAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `AtributoRepository.java`

### Responsabilidad
Acceso a atributos dinámicos.

### Coexiste con
AtributoServiceImpl, TipoProductoAtributoServiceImpl.

### Métodos esperados
findByCodigoAndEstadoTrue, existsByCodigoAndEstadoTrue, existsByNombreAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `TipoProductoAtributoRepository.java`

### Responsabilidad
Acceso a asociaciones tipo-producto/atributo.

### Coexiste con
TipoProductoAtributoServiceImpl, ProductoValidator.

### Métodos esperados
findByTipoProductoAndEstadoTrue, existsByTipoProductoAndAtributoAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ProductoRepository.java`

### Responsabilidad
Acceso a producto base.

### Coexiste con
ProductoAdminServiceImpl, ProductoPublicServiceImpl.

### Métodos esperados
findByCodigoProductoAndEstadoTrue, findBySlugAndEstadoTrue, existsBySlugAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ProductoSkuRepository.java`

### Responsabilidad
Acceso a SKU.

### Coexiste con
ProductoSkuServiceImpl, StockServiceImpl, PrecioSkuServiceImpl.

### Métodos esperados
findByCodigoSkuAndEstadoTrue, findByBarcodeAndEstadoTrue, findByProductoAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ProductoAtributoValorRepository.java`

### Responsabilidad
Acceso a valores de atributos de producto.

### Coexiste con
ProductoAtributoValorServiceImpl.

### Métodos esperados
findByProductoAndEstadoTrue, existsByProductoAndAtributoAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `SkuAtributoValorRepository.java`

### Responsabilidad
Acceso a valores de atributos de SKU.

### Coexiste con
SkuAtributoValorServiceImpl.

### Métodos esperados
findBySkuAndEstadoTrue, existsBySkuAndAtributoAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ProductoImagenCloudinaryRepository.java`

### Responsabilidad
Acceso a metadata Cloudinary.

### Coexiste con
ProductoImagenServiceImpl, ProductoPublicServiceImpl.

### Métodos esperados
findByProductoAndEstadoTrue, findBySkuAndEstadoTrue, findByCloudinaryPublicIdAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ProveedorRepository.java`

### Responsabilidad
Acceso a proveedores.

### Coexiste con
ProveedorServiceImpl, CompraInventarioServiceImpl.

### Métodos esperados
findByRucAndEstadoTrue, findByTipoDocumentoAndNumeroDocumentoAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `AlmacenRepository.java`

### Responsabilidad
Acceso a almacenes.

### Coexiste con
AlmacenServiceImpl, StockServiceImpl.

### Métodos esperados
findByCodigoAndEstadoTrue, findByPrincipalTrueAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `StockSkuRepository.java`

### Responsabilidad
Acceso a stock por SKU y almacén.

### Coexiste con
StockServiceImpl, ReservaStockServiceImpl.

### Métodos esperados
findBySkuAndAlmacenAndEstadoTrue, búsqueda con lock para actualizar stock.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `EmpleadoSnapshotMs2Repository.java`

### Responsabilidad
Acceso a snapshot de empleados MS2.

### Coexiste con
EmpleadoSnapshotMs2ServiceImpl, permisos.

### Métodos esperados
findByIdUsuarioMs1AndEstadoTrue, findByIdEmpleadoMs2AndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `EmpleadoInventarioPermisoHistorialRepository.java`

### Responsabilidad
Acceso a permisos versionados.

### Coexiste con
EmpleadoInventarioPermisoServiceImpl, policies.

### Métodos esperados
findByEmpleadoSnapshotAndVigenteTrueAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `PrecioSkuHistorialRepository.java`

### Responsabilidad
Acceso a precios versionados.

### Coexiste con
PrecioSkuServiceImpl, ProductoPublicServiceImpl.

### Métodos esperados
findBySkuAndVigenteTrueAndEstadoTrue, existsBySkuAndVigenteTrueAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `PromocionRepository.java`

### Responsabilidad
Acceso a promociones base.

### Coexiste con
PromocionServiceImpl.

### Métodos esperados
findByCodigoAndEstadoTrue, existsByCodigoAndEstadoTrue, existsByNombreAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `PromocionVersionRepository.java`

### Responsabilidad
Acceso a versiones de promoción.

### Coexiste con
PromocionVersionServiceImpl, ProductoPublicServiceImpl.

### Métodos esperados
findByPromocionAndVigenteTrueAndEstadoTrue, vigentes por fecha.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `PromocionSkuDescuentoVersionRepository.java`

### Responsabilidad
Acceso a descuentos por SKU.

### Coexiste con
PromocionSkuDescuentoServiceImpl, ProductoPublicServiceImpl.

### Métodos esperados
findByPromocionVersionAndEstadoTrue, findByPromocionVersionAndSkuAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `CompraInventarioRepository.java`

### Responsabilidad
Acceso a compras.

### Coexiste con
CompraInventarioServiceImpl.

### Métodos esperados
findByCodigoCompraAndEstadoTrue, existsByCodigoCompraAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `CompraInventarioDetalleRepository.java`

### Responsabilidad
Acceso a detalles de compra.

### Coexiste con
CompraInventarioServiceImpl, MovimientoInventarioServiceImpl.

### Métodos esperados
findByCompraAndEstadoTrue, findBySkuAndEstadoTrue.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `ReservaStockRepository.java`

### Responsabilidad
Acceso a reservas de stock.

### Coexiste con
ReservaStockServiceImpl, Ms4ReconciliacionServiceImpl.

### Métodos esperados
findByCodigoReservaAndEstadoTrue, búsqueda por referencia externa y SKU/almacén.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `MovimientoInventarioRepository.java`

### Responsabilidad
Acceso a kardex/movimientos.

### Coexiste con
MovimientoInventarioServiceImpl, KardexServiceImpl.

### Métodos esperados
findByCodigoMovimientoAndEstadoTrue, existsByReferenciaTipoAndReferenciaIdExternoAndTipoMovimiento.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `AuditoriaFuncionalRepository.java`

### Responsabilidad
Acceso a auditoría funcional.

### Coexiste con
AuditoriaFuncionalServiceImpl.

### Métodos esperados
Búsqueda por specification: entidad, actor, evento, resultado, fechas.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


## `EventoDominioOutboxRepository.java`

### Responsabilidad
Acceso a eventos outbox.

### Coexiste con
EventoDominioOutboxServiceImpl, OutboxEventPublisher.

### Métodos esperados
findByEstadoPublicacionAndEstadoTrue, findByEventId, pendientes con lock, errores reintentables.

### Cómo programarlo
- Extender `JpaRepository`.
- Extender `JpaSpecificationExecutor` cuando tenga listados filtrables.
- Usar `Optional` para búsquedas únicas.
- Usar consultas con `estado = true` en operaciones funcionales.
- Usar lock cuando exista concurrencia crítica.

### Qué evitar
- No implementar reglas de negocio.
- No construir respuestas de usuario.
- No autorizar.
- No publicar Kafka.
- No llamar Cloudinary.


---

# 6. `dto.shared`


## `ApiResponseDto.java`

### Responsabilidad
Wrapper estándar para respuestas HTTP.

### Cómo programarlo
- Debe contener: success, message, data, timestamp, requestId.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `ErrorResponseDto.java`

### Responsabilidad
Respuesta estándar de error.

### Cómo programarlo
- Debe contener: code, message, path, timestamp, requestId, correlationId, fieldErrors.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `FieldErrorDto.java`

### Responsabilidad
Error de validación por campo.

### Cómo programarlo
- Debe contener: field, message, code, rejectedValue seguro.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `PageRequestDto.java`

### Responsabilidad
Entrada estándar de paginación.

### Cómo programarlo
- Debe contener: page, size, sortBy, sortDirection.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `PageResponseDto.java`

### Responsabilidad
Salida paginada estándar.

### Cómo programarlo
- Debe contener: content, page, size, totalElements, totalPages, hasNext, hasPrevious.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `SelectOptionDto.java`

### Responsabilidad
Opción genérica para selects.

### Cómo programarlo
- Debe contener: value, label, code, disabled.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `IdCodigoNombreResponseDto.java`

### Responsabilidad
Respuesta compacta de referencia.

### Cómo programarlo
- Debe contener: id, codigo, nombre.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `EntityReferenceDto.java`

### Responsabilidad
Referencia funcional para no exigir FK crudo.

### Cómo programarlo
- Debe contener: id, codigo, nombre, slug, barcode, ruc, numeroDocumento.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `EstadoChangeRequestDto.java`

### Responsabilidad
Cambio común de estado.

### Cómo programarlo
- Debe contener: estado y motivo.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `MotivoRequestDto.java`

### Responsabilidad
Acción con justificación.

### Cómo programarlo
- Debe contener: motivo obligatorio.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `DateRangeFilterDto.java`

### Responsabilidad
Filtro de rango de fecha reutilizable.

### Cómo programarlo
- Debe contener: fechaDesde y fechaHasta.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `MoneyResponseDto.java`

### Responsabilidad
Representación de dinero.

### Cómo programarlo
- Debe contener: monto, moneda, montoFormateado.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


## `StockResumenResponseDto.java`

### Responsabilidad
Resumen de stock.

### Cómo programarlo
- Debe contener: fisico, reservado, disponible, minimo, maximo, bajoStock.
- Debe ser reutilizable en varios dominios del MS3.
- No debe depender de entidades JPA.
- No debe ejecutar lógica de negocio.

### Coexiste con
Controllers, services, mappers, `ApiResponseFactory`, `PaginationService` y `GlobalExceptionHandler`.

### Qué evitar
No consultar base de datos, no resolver FK, no mezclar request y response cuando la clase crezca.


---

# 7. DTOs de referencia, catálogo, producto, precio, promoción, proveedor, inventario, empleado, auditoría, outbox y MS4

## Regla común

Los `request` deben validar entrada y usar `EntityReferenceDto` para relaciones.  
Los `response` deben devolver datos claros y seguros.  
Los `filter` deben soportar listados con búsqueda, estados, fechas y paginación.


## `EntityReferenceRequestDto.java`

### Responsabilidad
Request genérico para resolver entidades por identificador funcional.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoOptionDto.java`

### Responsabilidad
Opción liviana de tipo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaOptionDto.java`

### Responsabilidad
Opción liviana de categoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaOptionDto.java`

### Responsabilidad
Opción liviana de marca.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoOptionDto.java`

### Responsabilidad
Opción liviana de atributo.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoOptionDto.java`

### Responsabilidad
Opción liviana de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuOptionDto.java`

### Responsabilidad
Opción liviana de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorOptionDto.java`

### Responsabilidad
Opción liviana de proveedor.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenOptionDto.java`

### Responsabilidad
Opción liviana de almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionOptionDto.java`

### Responsabilidad
Opción liviana de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoInventarioOptionDto.java`

### Responsabilidad
Opción liviana de empleado con permisos/snapshot.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReferenceSearchFilterDto.java`

### Responsabilidad
Filtro común para lookups.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoCreateRequestDto.java`

### Responsabilidad
Crear tipo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoUpdateRequestDto.java`

### Responsabilidad
Actualizar tipo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoResponseDto.java`

### Responsabilidad
Respuesta resumida de tipo producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoDetailResponseDto.java`

### Responsabilidad
Detalle de tipo producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoFilterDto.java`

### Responsabilidad
Filtro de tipos de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaCreateRequestDto.java`

### Responsabilidad
Crear categoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaUpdateRequestDto.java`

### Responsabilidad
Actualizar categoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaResponseDto.java`

### Responsabilidad
Respuesta resumida de categoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaDetailResponseDto.java`

### Responsabilidad
Detalle de categoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaTreeResponseDto.java`

### Responsabilidad
Nodo de árbol de categorías.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CategoriaFilterDto.java`

### Responsabilidad
Filtro de categorías.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaCreateRequestDto.java`

### Responsabilidad
Crear marca.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaUpdateRequestDto.java`

### Responsabilidad
Actualizar marca.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaResponseDto.java`

### Responsabilidad
Respuesta resumida de marca.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaDetailResponseDto.java`

### Responsabilidad
Detalle de marca.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MarcaFilterDto.java`

### Responsabilidad
Filtro de marcas.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoCreateRequestDto.java`

### Responsabilidad
Crear atributo dinámico.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoUpdateRequestDto.java`

### Responsabilidad
Actualizar atributo dinámico.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoAtributoAssignRequestDto.java`

### Responsabilidad
Asignar atributo a tipo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoResponseDto.java`

### Responsabilidad
Respuesta resumida de atributo.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoDetailResponseDto.java`

### Responsabilidad
Detalle de atributo.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `TipoProductoAtributoResponseDto.java`

### Responsabilidad
Respuesta de asociación tipo-producto/atributo.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AtributoFilterDto.java`

### Responsabilidad
Filtro de atributos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoCreateRequestDto.java`

### Responsabilidad
Crear producto base; no debe exigir código ni slug.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoUpdateRequestDto.java`

### Responsabilidad
Actualizar producto base.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoEstadoRegistroRequestDto.java`

### Responsabilidad
Cambiar estado administrativo del producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoPublicacionRequestDto.java`

### Responsabilidad
Publicar, programar u ocultar producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoVentaEstadoRequestDto.java`

### Responsabilidad
Cambiar disponibilidad de venta.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoAtributoValorRequestDto.java`

### Responsabilidad
Enviar valor dinámico de atributo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuCreateRequestDto.java`

### Responsabilidad
Crear SKU; código lo genera backend.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuUpdateRequestDto.java`

### Responsabilidad
Actualizar SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `SkuAtributoValorRequestDto.java`

### Responsabilidad
Enviar valor dinámico de atributo de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoImagenUploadRequestDto.java`

### Responsabilidad
Subir imagen de producto/SKU mediante multipart y metadata.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoImagenUpdateRequestDto.java`

### Responsabilidad
Actualizar metadata de imagen.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoImagenPrincipalRequestDto.java`

### Responsabilidad
Marcar imagen principal.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoResponseDto.java`

### Responsabilidad
Respuesta administrativa resumida de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoDetailResponseDto.java`

### Responsabilidad
Detalle administrativo de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoPublicResponseDto.java`

### Responsabilidad
Respuesta pública de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoPublicDetailResponseDto.java`

### Responsabilidad
Detalle público de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuResponseDto.java`

### Responsabilidad
Respuesta resumida de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuDetailResponseDto.java`

### Responsabilidad
Detalle de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoAtributoValorResponseDto.java`

### Responsabilidad
Respuesta de atributo valor de producto.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `SkuAtributoValorResponseDto.java`

### Responsabilidad
Respuesta de atributo valor de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoImagenResponseDto.java`

### Responsabilidad
Respuesta de metadata Cloudinary.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoCatalogoCardResponseDto.java`

### Responsabilidad
DTO liviano para tarjetas públicas.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSnapshotResponseDto.java`

### Responsabilidad
Snapshot de producto para MS4.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoFilterDto.java`

### Responsabilidad
Filtro administrativo de productos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoPublicFilterDto.java`

### Responsabilidad
Filtro público de productos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProductoSkuFilterDto.java`

### Responsabilidad
Filtro de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PrecioSkuCreateRequestDto.java`

### Responsabilidad
Crear nuevo precio vigente.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PrecioSkuResponseDto.java`

### Responsabilidad
Respuesta de precio vigente.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PrecioSkuHistorialResponseDto.java`

### Responsabilidad
Respuesta de historial de precio.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PrecioSkuFilterDto.java`

### Responsabilidad
Filtro de precios.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionCreateRequestDto.java`

### Responsabilidad
Crear promoción base.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionUpdateRequestDto.java`

### Responsabilidad
Actualizar promoción base.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionVersionCreateRequestDto.java`

### Responsabilidad
Crear versión de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionVersionEstadoRequestDto.java`

### Responsabilidad
Cambiar estado de versión promocional.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionSkuDescuentoCreateRequestDto.java`

### Responsabilidad
Asociar SKU con descuento.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionSkuDescuentoUpdateRequestDto.java`

### Responsabilidad
Actualizar descuento de SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionResponseDto.java`

### Responsabilidad
Respuesta resumida de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionDetailResponseDto.java`

### Responsabilidad
Detalle de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionVersionResponseDto.java`

### Responsabilidad
Respuesta de versión de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionSkuDescuentoResponseDto.java`

### Responsabilidad
Respuesta de descuento por SKU.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionPublicResponseDto.java`

### Responsabilidad
Respuesta pública de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionSnapshotResponseDto.java`

### Responsabilidad
Snapshot de promoción para MS4.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionFilterDto.java`

### Responsabilidad
Filtro de promociones.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `PromocionVersionFilterDto.java`

### Responsabilidad
Filtro de versiones de promoción.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorCreateRequestDto.java`

### Responsabilidad
Crear proveedor persona natural o empresa.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorUpdateRequestDto.java`

### Responsabilidad
Actualizar proveedor.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorEstadoRequestDto.java`

### Responsabilidad
Cambiar estado de proveedor.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorResponseDto.java`

### Responsabilidad
Respuesta resumida de proveedor.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorDetailResponseDto.java`

### Responsabilidad
Detalle de proveedor.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ProveedorFilterDto.java`

### Responsabilidad
Filtro de proveedores.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenCreateRequestDto.java`

### Responsabilidad
Crear almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenUpdateRequestDto.java`

### Responsabilidad
Actualizar almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenEstadoRequestDto.java`

### Responsabilidad
Cambiar estado de almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenResponseDto.java`

### Responsabilidad
Respuesta resumida de almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenDetailResponseDto.java`

### Responsabilidad
Detalle de almacén.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AlmacenFilterDto.java`

### Responsabilidad
Filtro de almacenes.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `StockSkuResponseDto.java`

### Responsabilidad
Respuesta resumida de stock.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `StockSkuDetailResponseDto.java`

### Responsabilidad
Detalle de stock.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `StockDisponibleResponseDto.java`

### Responsabilidad
Respuesta específica de disponibilidad.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `StockSkuFilterDto.java`

### Responsabilidad
Filtro de stock.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioCreateRequestDto.java`

### Responsabilidad
Crear compra en borrador.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioDetalleRequestDto.java`

### Responsabilidad
Detalle de compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioUpdateRequestDto.java`

### Responsabilidad
Actualizar compra en borrador.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioConfirmRequestDto.java`

### Responsabilidad
Confirmar compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioAnularRequestDto.java`

### Responsabilidad
Anular compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioResponseDto.java`

### Responsabilidad
Respuesta resumida de compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioDetailResponseDto.java`

### Responsabilidad
Detalle de compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioDetalleResponseDto.java`

### Responsabilidad
Respuesta de detalle de compra.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `CompraInventarioFilterDto.java`

### Responsabilidad
Filtro de compras.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockCreateRequestDto.java`

### Responsabilidad
Crear reserva de stock.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockConfirmRequestDto.java`

### Responsabilidad
Confirmar reserva.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockLiberarRequestDto.java`

### Responsabilidad
Liberar reserva.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockMs4RequestDto.java`

### Responsabilidad
Request interno de reserva desde MS4.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockResponseDto.java`

### Responsabilidad
Respuesta de reserva.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `ReservaStockFilterDto.java`

### Responsabilidad
Filtro de reservas.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EntradaInventarioRequestDto.java`

### Responsabilidad
Registrar entrada manual.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `SalidaInventarioRequestDto.java`

### Responsabilidad
Registrar salida manual.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AjusteInventarioRequestDto.java`

### Responsabilidad
Registrar ajuste.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MovimientoCompensatorioRequestDto.java`

### Responsabilidad
Registrar movimiento compensatorio.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MovimientoInventarioResponseDto.java`

### Responsabilidad
Respuesta de movimiento.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `KardexResponseDto.java`

### Responsabilidad
Respuesta de kardex.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `MovimientoInventarioFilterDto.java`

### Responsabilidad
Filtro de movimientos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `KardexFilterDto.java`

### Responsabilidad
Filtro de kardex.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoSnapshotMs2UpsertRequestDto.java`

### Responsabilidad
Crear/actualizar snapshot de empleado MS2.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoInventarioPermisoUpdateRequestDto.java`

### Responsabilidad
Otorgar/actualizar permisos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoInventarioPermisoRevokeRequestDto.java`

### Responsabilidad
Revocar permisos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoSnapshotMs2ResponseDto.java`

### Responsabilidad
Respuesta de snapshot de empleado.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoInventarioPermisoResponseDto.java`

### Responsabilidad
Respuesta de permisos de inventario.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EmpleadoInventarioPermisoFilterDto.java`

### Responsabilidad
Filtro de permisos.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AuditoriaFuncionalResponseDto.java`

### Responsabilidad
Respuesta de auditoría funcional.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `AuditoriaFuncionalFilterDto.java`

### Responsabilidad
Filtro de auditoría.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `OutboxRetryRequestDto.java`

### Responsabilidad
Request para reintento outbox.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EventoDominioOutboxResponseDto.java`

### Responsabilidad
Respuesta de evento outbox.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `EventoDominioOutboxFilterDto.java`

### Responsabilidad
Filtro de eventos outbox.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `Ms4VentaStockReservadoEventDto.java`

### Responsabilidad
Evento/request MS4 de stock reservado.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `Ms4VentaStockConfirmadoEventDto.java`

### Responsabilidad
Evento/request MS4 de stock confirmado.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `Ms4VentaStockLiberadoEventDto.java`

### Responsabilidad
Evento/request MS4 de stock liberado.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `Ms4VentaAnuladaStockEventDto.java`

### Responsabilidad
Evento/request MS4 de venta anulada con impacto stock.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


## `Ms4StockSyncResultDto.java`

### Responsabilidad
Resultado de sincronización MS4-MS3.

### Cómo programarlo
- Debe ubicarse en `request`, `response` o `filter` según corresponda.
- Si es request, debe usar Bean Validation y referencias funcionales cuando tenga relaciones.
- Si es response, debe devolver datos claros y no exponer entidades JPA.
- Si es filter, debe permitir búsqueda, estado, fechas, tipo y paginación cuando aplique.
- Debe ser consumido por controller, service, mapper, validator o specification según su tipo.

### Qué evitar
- No consultar base de datos.
- No resolver FK.
- No ejecutar reglas de negocio.
- No exponer datos sensibles en DTOs públicos.
- No mezclar request y response.


---

# 8. Decisión final

```text
domain:
    Modelo persistente, enums y value objects.

repository:
    Acceso limpio a datos, soporte para filtros, exists, búsquedas activas y locks.

dto:
    Contratos limpios para Angular, MS4 y controllers, separando request, response y filter.
```

Regla final:

```text
Entity no hace negocio.
Repository no hace negocio.
DTO no hace negocio.
Service orquesta.
Validator valida.
Policy autoriza.
Mapper convierte.
Specification filtra.
Shared evita duplicación.
```



# Documentación técnica de código - MS3 Parte 4

**Microservicio:** `ms-catalogo-inventario`  
**Paquetes documentados:** `validator`, `policy`, `specification`, `integration`, `kafka`, `shared`, `util`  
**Objetivo:** explicar clase por clase la responsabilidad, coexistencia, forma correcta de programar y límites de cada archivo para evitar redundancia y mantener el MS3 listo para producción.

---

# 1. Reglas generales de esta parte

## 1.1. Separación estricta de responsabilidades

```text
Validator:
    Valida datos, coherencia funcional, duplicados, estados y reglas de negocio.

Policy:
    Autoriza acciones según rol, actor autenticado, permisos MS3 y contexto.

Specification:
    Construye filtros dinámicos para consultas paginadas.

Integration:
    Aísla comunicación con sistemas externos: Cloudinary, MS2, MS4.

Kafka:
    Define eventos, outbox, publicación, consumo, idempotencia y resiliencia.

Shared:
    Centraliza lógica transversal para aplicar DRY.

Util:
    Funciones puras y reutilizables sin dependencia de Spring ni dominio pesado.
```

## 1.2. Regla central DRY

No repetir en services, controllers o validators lógica que ya pertenece a:

```text
- shared.validation
- shared.persistence
- shared.reference
- shared.pagination
- shared.response
- shared.audit
- shared.idempotency
- shared.code
- util
```

Ejemplo correcto:

```text
Validar cantidad de stock:
    StockValidator + StockMathUtil

Validar permiso de empleado:
    Policy + EmpleadoInventarioPermisoService

Construir PageResponseDto:
    PaginationService

Resolver producto por slug/código/id:
    ProductoReferenceResolver o EntityReferenceService

Crear evento Kafka:
    OutboxEventFactory + EventoDominioOutboxService

Publicar Kafka:
    OutboxEventPublisher + KafkaDomainEventPublisher
```

## 1.3. Regla de errores

Las clases de validación y policy deben lanzar excepciones funcionales claras:

```text
ValidationException
ConflictException
NotFoundException
ForbiddenException
UnauthorizedException
ExternalServiceException
KafkaPublishException
```

No deben exponer errores técnicos al usuario.  
El detalle técnico queda en logs y se responde con mensaje general desde `GlobalExceptionHandler`.

## 1.4. Regla Kafka profesional

MS3 no debe publicar eventos directamente desde los services de negocio.

Flujo correcto:

```text
Service de negocio
    ↓
Guarda entidad/cambio en BD
    ↓
Registra auditoría
    ↓
Registra evento en evento_dominio_outbox
    ↓
Confirma transacción
    ↓
OutboxScheduler / OutboxEventPublisher
    ↓
KafkaDomainEventPublisher
    ↓
Kafka
    ↓
MS4 consume snapshot o evento
```

Flujo incorrecto:

```text
ProductoAdminServiceImpl → KafkaTemplate.send(...)
```

El patrón Outbox evita perder eventos si la transacción de negocio fue exitosa pero Kafka falló temporalmente.

---

# 2. `validator`

## Reglas generales para validators

Los validators validan reglas funcionales y de consistencia.  
No autorizan.  
No persisten.  
No mapean DTOs.  
No publican eventos.  
No generan respuestas HTTP.

Deben ser consumidos principalmente por `service.impl`.

Un validator puede usar repositories para validar duplicados o existencia, pero debe evitar orquestación de casos de uso completos.

---

## `TipoProductoValidator.java`

### Responsabilidad

Valida reglas de negocio del tipo de producto.

### Reglas que debe validar

- nombre obligatorio y normalizado.
- código único si se maneja manual o generado.
- no duplicar nombre activo.
- no inactivar si existen productos activos dependientes sin política definida.

### Consumidores principales

- `TipoProductoServiceImpl`.

### Clases que puede consumir

- `TipoProductoRepository`.
- `ValidationErrorCollector`.
- `StringNormalizer`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No autorizar roles.
- No crear tipo de producto.
- No construir response DTO.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `CategoriaValidator.java`

### Responsabilidad

Valida categorías jerárquicas.

### Reglas que debe validar

- nombre obligatorio.
- slug único si se recibe/genera.
- padre activo.
- no permitir que una categoría sea su propio padre.
- no permitir ciclos.
- no inactivar categoría con productos activos si la RN lo prohíbe.

### Consumidores principales

- `CategoriaServiceImpl`.

### Clases que puede consumir

- `CategoriaRepository`.
- `ValidationErrorCollector`.
- `StringNormalizer`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No construir árbol.
- No generar slug final.
- No cambiar estado.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `MarcaValidator.java`

### Responsabilidad

Valida reglas de marca.

### Reglas que debe validar

- nombre obligatorio.
- slug único.
- código único si aplica.
- no duplicar marca activa.
- no inactivar si hay productos activos según RN.

### Consumidores principales

- `MarcaServiceImpl`.

### Clases que puede consumir

- `MarcaRepository`.
- `ValidationErrorCollector`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No crear productos.
- No generar slug.
- No autorizar usuario.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `AtributoValidator.java`

### Responsabilidad

Valida atributos dinámicos.

### Reglas que debe validar

- nombre y código obligatorios.
- tipoDato válido.
- unidad coherente con tipoDato si aplica.
- no duplicar código/nombre activo.
- no cambiar tipoDato si ya existen valores incompatibles.

### Consumidores principales

- `AtributoServiceImpl`.
- `ProductoAtributoValorServiceImpl`.
- `SkuAtributoValorServiceImpl`.

### Clases que puede consumir

- `AtributoRepository`.
- `TipoDatoAtributo`.
- `ValidationErrorCollector`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No guardar valores.
- No asociar tipo producto.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `TipoProductoAtributoValidator.java`

### Responsabilidad

Valida asociación entre tipo de producto y atributo.

### Reglas que debe validar

- tipoProducto activo.
- atributo activo.
- no duplicar asociación activa.
- orden no negativo.
- coherencia de requerido.

### Consumidores principales

- `TipoProductoAtributoServiceImpl`.

### Clases que puede consumir

- `TipoProductoAtributoRepository`.
- `TipoProductoRepository`.
- `AtributoRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No crear atributo.
- No crear tipo producto.
- No guardar valores.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ProductoValidator.java`

### Responsabilidad

Valida datos base de producto.

### Reglas que debe validar

- nombre obligatorio.
- tipoProducto activo.
- categoría activa.
- marca activa si existe.
- slug único.
- códigoProducto único.
- estados coherentes.
- no descontinuar si hay reservas activas según RN.

### Consumidores principales

- `ProductoAdminServiceImpl`.

### Clases que puede consumir

- `ProductoRepository`.
- `EntityReferenceService`.
- `ValidationErrorCollector`.
- `StringNormalizer`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No publicar producto.
- No validar requisitos de publicación completos; eso va en ProductoPublicacionValidator.
- No tocar stock.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ProductoSkuValidator.java`

### Responsabilidad

Valida SKU/variante.

### Reglas que debe validar

- producto activo.
- codigoSku único.
- barcode único si existe.
- stock mínimo y máximo coherentes.
- estadoSku válido.
- no inactivar SKU con stock o reserva activa sin política definida.

### Consumidores principales

- `ProductoSkuServiceImpl`.

### Clases que puede consumir

- `ProductoSkuRepository`.
- `StockSkuRepository`.
- `ReservaStockRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No generar SKU.
- No cambiar precio.
- No modificar stock.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ProductoPublicacionValidator.java`

### Responsabilidad

Valida si un producto puede publicarse o hacerse vendible.

### Reglas que debe validar

- producto activo.
- categoría activa.
- tipoProducto activo.
- marca activa si aplica.
- al menos un SKU activo.
- precio vigente por SKU vendible.
- imagen principal si la política lo exige.
- estadoPublicacion y estadoVenta coherentes.
- fechaPublicacionFin >= fechaPublicacionInicio.

### Consumidores principales

- `ProductoAdminServiceImpl`.

### Clases que puede consumir

- `ProductoRepository`.
- `ProductoSkuRepository`.
- `PrecioSkuHistorialRepository`.
- `ProductoImagenCloudinaryRepository`.
- `AppPropertiesConfig`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No cambiar estados.
- No publicar evento.
- No modificar stock.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ProductoImagenValidator.java`

### Responsabilidad

Valida reglas funcionales de metadata de imagen.

### Reglas que debe validar

- producto activo.
- SKU activo si se asocia a SKU.
- solo una imagen principal activa por producto/SKU.
- orden no negativo.
- secureUrl obligatorio después de subir.

### Consumidores principales

- `ProductoImagenServiceImpl`.

### Clases que puede consumir

- `ProductoImagenCloudinaryRepository`.
- `ProductoRepository`.
- `ProductoSkuRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No validar tamaño/formato de archivo; eso va en CloudinaryImageValidator.
- No llamar Cloudinary.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `PrecioSkuValidator.java`

### Responsabilidad

Valida precio versionado por SKU.

### Reglas que debe validar

- SKU activo.
- precio > 0.
- moneda válida.
- fechaInicio válida.
- solo un precio vigente por SKU.
- motivo obligatorio.
- cierre correcto del precio anterior.

### Consumidores principales

- `PrecioSkuServiceImpl`.

### Clases que puede consumir

- `PrecioSkuHistorialRepository`.
- `ProductoSkuRepository`.
- `MoneyUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No modificar precio anterior.
- No publicar outbox.
- No calcular venta.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `PromocionValidator.java`

### Responsabilidad

Valida campaña base de promoción.

### Reglas que debe validar

- nombre obligatorio.
- código único.
- nombre único activo si aplica.
- estado activo para versionar.
- no inactivar si tiene versión activa sin regla definida.

### Consumidores principales

- `PromocionServiceImpl`.

### Clases que puede consumir

- `PromocionRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No validar descuento por SKU.
- No validar vigencia de versión.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `PromocionVersionValidator.java`

### Responsabilidad

Valida versión de promoción.

### Reglas que debe validar

- promoción activa.
- fechaFin >= fechaInicio.
- estadoPromocion válido.
- una versión vigente por promoción.
- no activar versiones superpuestas si la RN lo prohíbe.
- motivo obligatorio.

### Consumidores principales

- `PromocionVersionServiceImpl`.

### Clases que puede consumir

- `PromocionVersionRepository`.
- `DateTimeUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No crear descuentos.
- No modificar ventas pasadas.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `PromocionSkuDescuentoValidator.java`

### Responsabilidad

Valida descuento por SKU dentro de una promoción.

### Reglas que debe validar

- versión de promoción válida.
- SKU activo.
- tipoDescuento válido.
- valor > 0.
- porcentaje <= 100.
- precioFinal >= 0.
- no duplicar SKU en versión.
- margen negativo requiere autorización si la RN lo define.

### Consumidores principales

- `PromocionSkuDescuentoServiceImpl`.

### Clases que puede consumir

- `PromocionSkuDescuentoVersionRepository`.
- `PrecioSkuService`.
- `PercentageUtil`.
- `MoneyUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No cambiar precio base.
- No aplicar descuento global.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ProveedorValidator.java`

### Responsabilidad

Valida proveedor persona natural o empresa.

### Reglas que debe validar

- tipoProveedor obligatorio.
- si EMPRESA: RUC y razónSocial obligatorios.
- si PERSONA_NATURAL: tipoDocumento, numeroDocumento y nombres obligatorios.
- RUC/documento únicos activos.
- correo/teléfono con formato básico.

### Consumidores principales

- `ProveedorServiceImpl`.

### Clases que puede consumir

- `ProveedorRepository`.
- `RucValue`.
- `DocumentoProveedorValue`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No crear compras.
- No modificar stock.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `AlmacenValidator.java`

### Responsabilidad

Valida almacenes.

### Reglas que debe validar

- código/nombre únicos activos.
- si principal=true no debe existir otro principal activo.
- no inactivar almacén con stock/reservas/movimientos activos sin política definida.
- permiteVenta/permiteCompra coherentes.

### Consumidores principales

- `AlmacenServiceImpl`.

### Clases que puede consumir

- `AlmacenRepository`.
- `StockSkuRepository`.
- `ReservaStockRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No modificar stock.
- No registrar kardex.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `StockValidator.java`

### Responsabilidad

Valida operaciones de stock.

### Reglas que debe validar

- stock físico no negativo.
- stock reservado no negativo.
- reservado <= físico.
- salida <= disponible.
- reserva <= disponible.
- cantidad > 0.
- stockMinimo <= stockMaximo.
- almacén permiteVenta/compra según operación.

### Consumidores principales

- `StockServiceImpl`.
- `ReservaStockServiceImpl`.
- `MovimientoInventarioServiceImpl`.
- `CompraInventarioServiceImpl`.

### Clases que puede consumir

- `StockSkuRepository`.
- `StockMathUtil`.
- `AlmacenRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No actualizar stock.
- No registrar movimiento.
- No crear reserva.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `CompraInventarioValidator.java`

### Responsabilidad

Valida compra/adquisición.

### Reglas que debe validar

- proveedor activo.
- compra en BORRADOR para editar.
- detalle no vacío para confirmar.
- SKU activo.
- almacén activo y permiteCompra.
- cantidad > 0.
- costoUnitario >= 0.
- totales coherentes.

### Consumidores principales

- `CompraInventarioServiceImpl`.

### Clases que puede consumir

- `ProveedorRepository`.
- `ProductoSkuRepository`.
- `AlmacenRepository`.
- `MoneyUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No actualizar stock.
- No confirmar compra.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `ReservaStockValidator.java`

### Responsabilidad

Valida reservas de stock.

### Reglas que debe validar

- SKU activo.
- almacén activo y permiteVenta.
- cantidad > 0.
- referenciaTipo y referenciaIdExterno obligatorios.
- no duplicar reserva activa por referencia.
- estado permite confirmar/liberar.
- no confirmar reserva vencida.

### Consumidores principales

- `ReservaStockServiceImpl`.
- `Ms4ReconciliacionServiceImpl`.

### Clases que puede consumir

- `ReservaStockRepository`.
- `StockValidator`.
- `DateTimeUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No descontar stock.
- No registrar movimiento.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `MovimientoInventarioValidator.java`

### Responsabilidad

Valida movimientos de inventario.

### Reglas que debe validar

- tipoMovimiento válido.
- motivo obligatorio.
- cantidad > 0.
- stockAnterior y stockNuevo no negativos.
- referencia coherente.
- actor obligatorio.
- movimiento compensatorio referencia original.

### Consumidores principales

- `MovimientoInventarioServiceImpl`.

### Clases que puede consumir

- `MovimientoInventarioRepository`.
- `StockValidator`.
- `StockMathUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No modificar stock.
- No crear outbox.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `KardexValidator.java`

### Responsabilidad

Valida consultas de kardex.

### Reglas que debe validar

- rango de fechas válido.
- SKU/almacén activos si se filtran.
- usuario autorizado será validado por policy.
- filtros coherentes.

### Consumidores principales

- `KardexServiceImpl`.

### Clases que puede consumir

- `DateTimeUtil`.
- `ProductoSkuRepository`.
- `AlmacenRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No registrar movimiento.
- No modificar datos.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `EmpleadoSnapshotMs2Validator.java`

### Responsabilidad

Valida snapshot de empleado MS2.

### Reglas que debe validar

- idEmpleadoMs2 obligatorio.
- idUsuarioMs1 obligatorio.
- códigoEmpleado obligatorio.
- nombresCompletos obligatorio.
- no duplicar snapshot activo por empleado/usuario.
- empleadoActivo coherente.

### Consumidores principales

- `EmpleadoSnapshotMs2ServiceImpl`.

### Clases que puede consumir

- `EmpleadoSnapshotMs2Repository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No validar usuario contra MS1.
- No crear empleado en MS2.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `EmpleadoInventarioPermisoValidator.java`

### Responsabilidad

Valida permisos funcionales de inventario.

### Reglas que debe validar

- empleado snapshot activo.
- motivo obligatorio.
- fechaInicio <= fechaFin si hay fin.
- solo un permiso vigente por empleado.
- no permitir permisos vacíos si RN lo exige.

### Consumidores principales

- `EmpleadoInventarioPermisoServiceImpl`.

### Clases que puede consumir

- `EmpleadoInventarioPermisoHistorialRepository`.
- `EmpleadoSnapshotMs2Repository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No autorizar la acción; eso lo hace policy.
- No asignar rol global.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `EventoDominioOutboxValidator.java`

### Responsabilidad

Valida eventos outbox.

### Reglas que debe validar

- eventId único.
- topic obligatorio.
- eventKey obligatorio.
- payloadJson válido.
- aggregateType/eventType obligatorios.
- estadoPublicacion válido.
- evento reintentable según estado/intentos.

### Consumidores principales

- `EventoDominioOutboxServiceImpl`.
- `OutboxEventPublisher`.

### Clases que puede consumir

- `EventoDominioOutboxRepository`.
- `JsonUtil`.
- `OutboxProperties`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No publicar Kafka.
- No construir payload de negocio.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `Ms4StockEventValidator.java`

### Responsabilidad

Valida eventos/comandos recibidos desde MS4.

### Reglas que debe validar

- idempotency key obligatorio.
- referencia venta/carrito obligatoria.
- SKU/almacén/cantidad válidos.
- tipo evento válido.
- evento no aplicado previamente.
- payload mínimo suficiente para reconciliar.

### Consumidores principales

- `Ms4ReconciliacionServiceImpl`.
- `Ms4StockCommandHandler`.
- `Ms4StockSyncController`.

### Clases que puede consumir

- `KafkaIdempotencyGuard`.
- `ReservaStockRepository`.
- `MovimientoInventarioRepository`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No aplicar stock.
- No confirmar venta.
- No facturar.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

## `CloudinaryImageValidator.java`

### Responsabilidad

Valida archivo e información técnica de imagen antes/después de Cloudinary.

### Reglas que debe validar

- archivo no vacío.
- tamaño máximo.
- formato permitido.
- resourceType válido.
- secureUrl devuelto.
- publicId devuelto.
- dimensiones positivas si se informan.

### Consumidores principales

- `ProductoImagenServiceImpl`.
- `CloudinaryServiceImpl`.

### Clases que puede consumir

- `CloudinaryProperties`.
- `MimeTypeUtil`.
- `FileNameUtil`.

### Cómo programarlo

- Debe exponer métodos específicos por caso de uso, por ejemplo `validateCreate`, `validateUpdate`, `validateActive`, `validateCanPublish`.
- Debe lanzar excepciones funcionales claras.
- Debe usar `ValidationErrorCollector` si hay varias validaciones acumulables.
- Debe centralizar reglas repetidas del dominio.
- Debe permitir que el service sea más limpio y orquestador.

### Qué evitar

- No validar permisos.
- No guardar entidad.
- No llamar repository salvo que se defina explícitamente.

- No debe devolver DTOs HTTP.
- No debe autorizar roles o permisos.
- No debe publicar eventos.
- No debe ocultar errores técnicos sin trazabilidad.

---

# 3. `policy`

## Reglas generales para policies

Las policies autorizan acciones.  
No validan datos de negocio.  
No validan duplicados.  
No guardan datos.  
No publican eventos.  
No hacen mapping.

Una policy responde preguntas como:

```text
¿Este actor puede crear producto?
¿Este empleado tiene permiso para registrar salida?
¿Este usuario puede consultar auditoría?
¿MS4 puede invocar este flujo interno?
```

Las policies deben lanzar `ForbiddenException` cuando el actor no tiene permiso contextual.

---

## `TipoProductoPolicy.java`

### Responsabilidad

Autorizar mantenimiento de tipos de producto.

### Reglas de autorización

- ADMIN puede crear/editar/inactivar.
- EMPLEADO normalmente no modifica salvo permiso futuro.
- CLIENTE/ANONIMO no modifican.

### Consumidores principales

- `TipoProductoServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `SecurityRoles`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `CategoriaPolicy.java`

### Responsabilidad

Autorizar mantenimiento de categorías.

### Reglas de autorización

- ADMIN puede mantener categorías.
- EMPLEADO solo si se decide permiso de catálogo básico.
- público solo consulta árbol público vía service público.

### Consumidores principales

- `CategoriaServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `MarcaPolicy.java`

### Responsabilidad

Autorizar mantenimiento de marcas.

### Reglas de autorización

- ADMIN puede crear/editar/inactivar.
- EMPLEADO autorizado puede apoyar si RN lo permite.
- CLIENTE/ANONIMO solo consulta pública.

### Consumidores principales

- `MarcaServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `AtributoPolicy.java`

### Responsabilidad

Autorizar mantenimiento de atributos dinámicos.

### Reglas de autorización

- ADMIN administra atributos.
- EMPLEADO autorizado puede actualizar atributos operativos si tiene permiso.
- CLIENTE/ANONIMO no administra.

### Consumidores principales

- `AtributoServiceImpl`.
- `TipoProductoAtributoServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `ProductoPolicy.java`

### Responsabilidad

Autorizar operaciones administrativas sobre producto base.

### Reglas de autorización

- ADMIN puede todo.
- EMPLEADO puede crear/editar producto básico si tiene permiso.
- EMPLEADO no descontinúa ni elimina lógico complejo.
- CLIENTE/ANONIMO no modifican.

### Consumidores principales

- `ProductoAdminServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `ProductoSkuPolicy.java`

### Responsabilidad

Autorizar operaciones sobre SKU.

### Reglas de autorización

- ADMIN puede crear/editar/inactivar.
- EMPLEADO autorizado puede crear/editar básico.
- acciones destructivas o sensibles requieren ADMIN.

### Consumidores principales

- `ProductoSkuServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `ProductoImagenPolicy.java`

### Responsabilidad

Autorizar gestión de imágenes.

### Reglas de autorización

- ADMIN gestiona imágenes.
- EMPLEADO puede gestionar imágenes si tiene permiso.
- público solo consulta imágenes activas.

### Consumidores principales

- `ProductoImagenServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `PrecioSkuPolicy.java`

### Responsabilidad

Autorizar cambios de precio.

### Reglas de autorización

- solo ADMIN cambia precio.
- EMPLEADO no cambia precio aunque tenga permisos de inventario.
- MS4 no cambia precio maestro.

### Consumidores principales

- `PrecioSkuServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `PromocionPolicy.java`

### Responsabilidad

Autorizar promociones y descuentos.

### Reglas de autorización

- solo ADMIN crea/versiona/activa/cancela promociones.
- CLIENTE/ANONIMO solo consulta pública.
- EMPLEADO no gestiona promoción salvo RN explícita futura.

### Consumidores principales

- `PromocionServiceImpl`.
- `PromocionVersionServiceImpl`.
- `PromocionSkuDescuentoServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `ProveedorPolicy.java`

### Responsabilidad

Autorizar mantenimiento de proveedores.

### Reglas de autorización

- ADMIN mantiene proveedores.
- EMPLEADO autorizado puede registrar proveedor si RN lo permite.
- proveedores no son públicos.

### Consumidores principales

- `ProveedorServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `AlmacenPolicy.java`

### Responsabilidad

Autorizar mantenimiento de almacenes.

### Reglas de autorización

- ADMIN crea/edita/inactiva almacenes.
- EMPLEADO operativo puede consultar.
- acciones estructurales requieren ADMIN.

### Consumidores principales

- `AlmacenServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `StockPolicy.java`

### Responsabilidad

Autorizar consultas y operaciones de stock.

### Reglas de autorización

- ADMIN puede consultar y operar.
- EMPLEADO requiere permiso para entrada/salida/ajuste.
- CLIENTE/ANONIMO no ve stock interno.
- MS4 puede reservar/confirmar/liberar por canal interno.

### Consumidores principales

- `StockServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.
- `Ms4SyncPolicy`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `CompraInventarioPolicy.java`

### Responsabilidad

Autorizar compras/adquisiciones.

### Reglas de autorización

- ADMIN registra y confirma compras.
- EMPLEADO puede registrar entrada/compra si tiene permiso.
- confirmación puede requerir ADMIN según RN.

### Consumidores principales

- `CompraInventarioServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `ReservaStockPolicy.java`

### Responsabilidad

Autorizar reservas de stock.

### Reglas de autorización

- MS4 puede solicitar reserva por canal interno.
- ADMIN puede consultar/operar.
- EMPLEADO operativo puede reservar en flujo de venta si está autorizado.
- CLIENTE no invoca MS3 directamente para reserva.

### Consumidores principales

- `ReservaStockServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `Ms4SyncPolicy`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `MovimientoInventarioPolicy.java`

### Responsabilidad

Autorizar movimientos manuales de inventario.

### Reglas de autorización

- ADMIN puede registrar entradas/salidas/ajustes.
- EMPLEADO requiere permiso específico.
- movimientos compensatorios pueden requerir ADMIN.

### Consumidores principales

- `MovimientoInventarioServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `KardexPolicy.java`

### Responsabilidad

Autorizar consulta de kardex.

### Reglas de autorización

- ADMIN puede consultar todo.
- EMPLEADO requiere permiso consultar_kardex.
- CLIENTE/ANONIMO no consulta kardex.

### Consumidores principales

- `KardexServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `EmpleadoInventarioPermisoPolicy.java`

### Responsabilidad

Autorizar otorgamiento/revocación de permisos de inventario.

### Reglas de autorización

- solo ADMIN otorga/revoca.
- empleado no puede autoasignarse permisos.
- soporta auditoría de acceso denegado.

### Consumidores principales

- `EmpleadoInventarioPermisoServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `AuditoriaPolicy.java`

### Responsabilidad

Autorizar consulta de auditoría.

### Reglas de autorización

- solo ADMIN o rol técnico autorizado consulta auditoría.
- EMPLEADO no consulta auditoría global.
- nadie modifica auditoría.

### Consumidores principales

- `AuditoriaFuncionalServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `OutboxPolicy.java`

### Responsabilidad

Autorizar consulta/reintento de eventos Outbox.

### Reglas de autorización

- solo ADMIN técnico puede consultar/reintentar.
- no permitir payload sensible a usuarios no autorizados.
- reintentos deben auditarse.

### Consumidores principales

- `EventoDominioOutboxServiceImpl`.
- `OutboxController`.

### Clases que puede consumir

- `AuthenticatedUserContext`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `CloudinaryPolicy.java`

### Responsabilidad

Autorizar acciones sobre recursos Cloudinary.

### Reglas de autorización

- ADMIN puede subir/inactivar.
- EMPLEADO requiere permiso gestionar_imagenes.
- eliminación física externa debe ser restringida.

### Consumidores principales

- `ProductoImagenServiceImpl`.
- `CloudinaryServiceImpl`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `EmpleadoInventarioPermisoService`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

## `Ms4SyncPolicy.java`

### Responsabilidad

Autorizar sincronización interna MS4-MS3.

### Reglas de autorización

- solo canal interno confiable o actor SISTEMA.
- validar cabeceras internas si se definen.
- bloquear acceso público.
- separar HTTP interno de Kafka consumer.

### Consumidores principales

- `Ms4ReconciliacionServiceImpl`.
- `Ms4StockSyncController`.
- `Ms4StockCommandHandler`.

### Clases que puede consumir

- `AuthenticatedUserContext`.
- `HeaderNames`.
- `SystemActors`.

### Cómo programarlo

- Debe tener métodos expresivos como `canCreate`, `ensureCanUpdate`, `ensureCanRegisterOutput`, `ensureCanRetryOutbox`.
- Debe lanzar `ForbiddenException` ante denegación.
- Debe usar `AuthenticatedUserContext`.
- Debe delegar permisos de empleado a `EmpleadoInventarioPermisoService` cuando corresponda.
- Debe registrar auditoría de denegación solo si la arquitectura lo centraliza mediante service/filtro.

### Qué evitar

- No validar datos funcionales.
- No consultar duplicados.
- No guardar entidades.
- No publicar eventos.
- No mapear DTOs.
- No mezclar permisos de dominio con roles globales de MS1.

---

# 4. `specification`

## Reglas generales para specifications

Las specifications construyen filtros dinámicos para listados paginados.

Deben:

```text
- Recibir FilterDto.
- Ignorar filtros nulos o vacíos.
- Aplicar estado=true por defecto cuando corresponda.
- Permitir search normalizado.
- Permitir rangos de fecha.
- Permitir filtros por enum.
- Permitir filtros por relaciones.
- Evitar lógica de negocio compleja.
```

No deben:

```text
- Autorizar.
- Validar permisos.
- Guardar datos.
- Mapear DTOs.
- Construir PageResponseDto.
```

---

## `TipoProductoSpecifications.java`

### Responsabilidad

Filtros de tipos de producto.

### Filtros esperados

- `search`.
- `codigo`.
- `nombre`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `CategoriaSpecifications.java`

### Responsabilidad

Filtros de categorías.

### Filtros esperados

- `search`.
- `codigo`.
- `slug`.
- `idPadre`.
- `nivel`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `MarcaSpecifications.java`

### Responsabilidad

Filtros de marcas.

### Filtros esperados

- `search`.
- `codigo`.
- `nombre`.
- `slug`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `AtributoSpecifications.java`

### Responsabilidad

Filtros de atributos.

### Filtros esperados

- `search`.
- `codigo`.
- `nombre`.
- `tipoDato`.
- `requerido`.
- `filtrable`.
- `visiblePublico`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ProductoSpecifications.java`

### Responsabilidad

Filtros administrativos de producto.

### Filtros esperados

- `search`.
- `tipoProducto`.
- `categoria`.
- `marca`.
- `estadoRegistro`.
- `estadoPublicacion`.
- `estadoVenta`.
- `visiblePublico`.
- `vendible`.
- `fechaPublicacionDesde/Hasta`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ProductoPublicSpecifications.java`

### Responsabilidad

Filtros públicos de producto.

### Filtros esperados

- `search`.
- `categoriaSlug`.
- `marcaSlug`.
- `precioMin/Max si aplica`.
- `promocion`.
- `solo publicados/visibles/vendibles según RN`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ProductoSkuSpecifications.java`

### Responsabilidad

Filtros de SKU.

### Filtros esperados

- `search`.
- `codigoSku`.
- `barcode`.
- `producto`.
- `color`.
- `talla`.
- `estadoSku`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ProductoImagenSpecifications.java`

### Responsabilidad

Filtros de imágenes.

### Filtros esperados

- `producto`.
- `sku`.
- `principal`.
- `resourceType`.
- `format`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `PrecioSkuSpecifications.java`

### Responsabilidad

Filtros de precios por SKU.

### Filtros esperados

- `sku`.
- `producto`.
- `moneda`.
- `vigente`.
- `fechaInicio/fechaFin`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `PromocionSpecifications.java`

### Responsabilidad

Filtros de promociones base.

### Filtros esperados

- `search`.
- `codigo`.
- `nombre`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `PromocionVersionSpecifications.java`

### Responsabilidad

Filtros de versiones de promoción.

### Filtros esperados

- `promocion`.
- `estadoPromocion`.
- `visiblePublico`.
- `vigente`.
- `fechaInicio/fechaFin`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `PromocionSkuDescuentoSpecifications.java`

### Responsabilidad

Filtros de descuentos por SKU.

### Filtros esperados

- `promocionVersion`.
- `sku`.
- `tipoDescuento`.
- `prioridad`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ProveedorSpecifications.java`

### Responsabilidad

Filtros de proveedores.

### Filtros esperados

- `search`.
- `tipoProveedor`.
- `ruc`.
- `tipoDocumento`.
- `numeroDocumento`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `AlmacenSpecifications.java`

### Responsabilidad

Filtros de almacenes.

### Filtros esperados

- `search`.
- `codigo`.
- `nombre`.
- `principal`.
- `permiteVenta`.
- `permiteCompra`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `StockSkuSpecifications.java`

### Responsabilidad

Filtros de stock.

### Filtros esperados

- `sku`.
- `producto`.
- `almacen`.
- `stockDisponibleMin/Max`.
- `bajoStock`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `CompraInventarioSpecifications.java`

### Responsabilidad

Filtros de compras.

### Filtros esperados

- `codigoCompra`.
- `proveedor`.
- `estadoCompra`.
- `moneda`.
- `fechaCompraDesde/Hasta`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `ReservaStockSpecifications.java`

### Responsabilidad

Filtros de reservas.

### Filtros esperados

- `codigoReserva`.
- `sku`.
- `almacen`.
- `referenciaTipo`.
- `referenciaIdExterno`.
- `estadoReserva`.
- `reservadoAtDesde/Hasta`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `MovimientoInventarioSpecifications.java`

### Responsabilidad

Filtros de movimientos de inventario.

### Filtros esperados

- `codigoMovimiento`.
- `sku`.
- `almacen`.
- `tipoMovimiento`.
- `motivoMovimiento`.
- `referencia`.
- `actor`.
- `createdAtDesde/Hasta`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `KardexSpecifications.java`

### Responsabilidad

Filtros de kardex basados en MovimientoInventario.

### Filtros esperados

- `sku`.
- `almacen`.
- `tipoMovimiento`.
- `referencia`.
- `fechaDesde/Hasta`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `EmpleadoSnapshotMs2Specifications.java`

### Responsabilidad

Filtros de snapshots de empleado.

### Filtros esperados

- `idEmpleadoMs2`.
- `idUsuarioMs1`.
- `codigoEmpleado`.
- `nombres`.
- `empleadoActivo`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `EmpleadoInventarioPermisoSpecifications.java`

### Responsabilidad

Filtros de permisos de inventario.

### Filtros esperados

- `empleado`.
- `idUsuarioMs1`.
- `vigente`.
- `fechaInicio/fechaFin`.
- `permiso específico`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `AuditoriaFuncionalSpecifications.java`

### Responsabilidad

Filtros de auditoría.

### Filtros esperados

- `tipoEvento`.
- `entidad`.
- `resultado`.
- `actor`.
- `requestId`.
- `correlationId`.
- `eventAtDesde/Hasta`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

## `EventoDominioOutboxSpecifications.java`

### Responsabilidad

Filtros de outbox.

### Filtros esperados

- `estadoPublicacion`.
- `topic`.
- `aggregateType`.
- `aggregateId`.
- `eventType`.
- `createdAtDesde/Hasta`.
- `publishedAtDesde/Hasta`.
- `estado`.

### Cómo programarlo

- Debe exponer métodos estáticos o un builder claro que devuelva `Specification<Entity>`.
- Debe usar `SpecificationUtils` para no repetir lógica de equals, like, ranges y booleans.
- Debe normalizar búsquedas con `TextSearchUtil` o `StringNormalizer`.
- Debe aplicar `estado = true` por defecto si el filter no solicita explícitamente inactivos.
- Debe permitir composición fluida de filtros.
- Debe estar alineada con el FilterDto correspondiente.

### Coexiste con

- FilterDto correspondiente.
- Repository con `JpaSpecificationExecutor`.
- ServiceImpl que construye el listado.
- PaginationService.
- SortFieldValidator.

### Qué evitar

- No validar permisos.
- No lanzar mensajes de usuario salvo error técnico de filtro inválido si se decide.
- No hacer joins innecesarios si no hay filtro relacionado.
- No retornar DTOs.
- No aplicar reglas de negocio que correspondan a Validator.

---

# 5. `integration`

## Reglas generales para integration

Las clases de integración aíslan dependencias externas.

Deben:

```text
- Encapsular detalles HTTP/SDK.
- Convertir errores externos a excepciones propias.
- Evitar que services conozcan detalles técnicos del proveedor.
- Ser reemplazables en tests.
- Usar properties de config.
```

No deben contener reglas de negocio del dominio.

---

## `cloudinary/CloudinaryClient.java`

### Responsabilidad

Contrato técnico para operaciones con Cloudinary.

### Funciones o datos principales

- upload.
- delete/invalidate si aplica.

### Coexiste con

- `CloudinaryServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No decidir permisos.
- No guardar BD.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryClientImpl.java`

### Responsabilidad

Implementación del cliente Cloudinary usando SDK o HTTP.

### Funciones o datos principales

- subir archivo.
- eliminar recurso.
- mapear respuesta.

### Coexiste con

- `CloudinaryClientConfig`.
- `CloudinaryProperties`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No ser usado por controller.
- No exponer secretos.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryUploadRequest.java`

### Responsabilidad

DTO interno de integración para subir archivo.

### Funciones o datos principales

- archivo.
- folder.
- publicId opcional.
- resourceType.
- tags/metadata si aplica.

### Coexiste con

- `CloudinaryServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No usar como DTO HTTP público.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryUploadResponse.java`

### Responsabilidad

DTO interno con respuesta normalizada de Cloudinary.

### Funciones o datos principales

- assetId.
- publicId.
- secureUrl.
- format.
- bytes.
- width.
- height.

### Coexiste con

- `ProductoImagenServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No incluir API secret.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryDeleteRequest.java`

### Responsabilidad

DTO interno para eliminar o invalidar recurso Cloudinary.

### Funciones o datos principales

- publicId.
- resourceType.
- invalidate.

### Coexiste con

- `CloudinaryServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No usar sin policy.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryDeleteResponse.java`

### Responsabilidad

DTO interno de resultado de eliminación/inactivación externa.

### Funciones o datos principales

- publicId.
- result.
- success.

### Coexiste con

- `CloudinaryServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No ocultar fallo externo.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryException.java`

### Responsabilidad

Excepción específica de integración Cloudinary.

### Funciones o datos principales

- errores SDK.
- timeout.
- respuesta inválida.

### Coexiste con

- `CloudinaryErrorMapper`.
- `GlobalExceptionHandler`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No usar para validaciones funcionales de imagen.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `cloudinary/CloudinaryErrorMapper.java`

### Responsabilidad

Mapea errores de Cloudinary a excepciones propias.

### Funciones o datos principales

- timeout.
- credenciales inválidas.
- payload inválido.
- servicio no disponible.

### Coexiste con

- `CloudinaryClientImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No devolver stacktrace al usuario.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms2/Ms2EmpleadoSnapshotClient.java`

### Responsabilidad

Contrato para consultar/sincronizar datos mínimos de empleado desde MS2.

### Funciones o datos principales

- buscar empleado por idUsuarioMs1.
- buscar empleado por idEmpleadoMs2.
- obtener snapshot.

### Coexiste con

- `EmpleadoSnapshotMs2ServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No reemplazar MS2.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms2/Ms2EmpleadoSnapshotClientImpl.java`

### Responsabilidad

Implementación HTTP o cliente interno hacia MS2.

### Funciones o datos principales

- consumir endpoint MS2.
- manejar timeout.
- mapear respuesta.

### Coexiste con

- `Ms2IntegrationProperties`.
- `Ms2ClientErrorMapper`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No llamar desde cada request si snapshot local basta.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms2/Ms2ClientException.java`

### Responsabilidad

Excepción de integración con MS2.

### Funciones o datos principales

- MS2 caído.
- timeout.
- respuesta inválida.

### Coexiste con

- `Ms2ClientErrorMapper`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No mezclar con NotFound interno de MS3.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms2/Ms2ClientErrorMapper.java`

### Responsabilidad

Mapea errores del cliente MS2 a excepciones controladas.

### Funciones o datos principales

- 404 externo.
- 401/403 externo.
- 5xx.
- timeout.

### Coexiste con

- `Ms2EmpleadoSnapshotClientImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No filtrar datos sensibles.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms4/Ms4StockSyncClient.java`

### Responsabilidad

Contrato de integración HTTP opcional con MS4 para reconciliación.

### Funciones o datos principales

- consultar pendientes.
- notificar resultado si se define.

### Coexiste con

- `Ms4ReconciliacionServiceImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No ser flujo principal si Kafka está disponible.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms4/Ms4StockSyncClientImpl.java`

### Responsabilidad

Implementación HTTP hacia MS4.

### Funciones o datos principales

- llamadas internas.
- timeouts.
- mapeo de error.

### Coexiste con

- `Ms4IntegrationProperties`.
- `Ms4ClientErrorMapper`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No crear ventas.
- No facturar.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms4/Ms4ClientException.java`

### Responsabilidad

Excepción de integración con MS4.

### Funciones o datos principales

- MS4 caído.
- timeout.
- respuesta inválida.

### Coexiste con

- `Ms4ClientErrorMapper`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No reemplazar errores de Kafka.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

## `ms4/Ms4ClientErrorMapper.java`

### Responsabilidad

Mapea errores HTTP de MS4.

### Funciones o datos principales

- 4xx.
- 5xx.
- timeout.
- payload inválido.

### Coexiste con

- `Ms4StockSyncClientImpl`.

### Cómo programarlo

- Debe estar aislado de controllers.
- Debe convertir respuestas externas a modelos internos.
- Debe manejar timeout y errores de proveedor.
- Debe lanzar excepciones de integración controladas.
- Debe registrar logs técnicos con requestId/correlationId cuando sea posible.
- Debe permitir pruebas con mocks.

### Qué evitar

- No ocultar trazabilidad.

- No aplicar reglas de negocio del MS3.
- No autorizar usuario final.
- No persistir entidades directamente.
- No devolver errores técnicos crudos al usuario.

---

# 6. `kafka`

## 6.1. Reglas generales de Kafka en MS3

Kafka permite que MS4 tenga snapshots locales y pueda operar aunque MS3 esté caído.

MS3 produce eventos de:

```text
- Producto.
- SKU.
- Imagen.
- Precio.
- Promoción.
- Stock.
- Movimiento de inventario.
```

MS3 consume eventos/comandos de MS4 relacionados con:

```text
- Reserva pendiente.
- Confirmación de venta.
- Liberación de stock.
- Anulación de venta con impacto en stock.
```

## 6.2. Regla Outbox

Los services de negocio no publican directo a Kafka.  
Primero registran `EventoDominioOutbox`.

Esto garantiza:

```text
- Transacción de negocio + evento pendiente en la misma BD.
- Reintentos si Kafka falla.
- Auditoría de publicación.
- Recuperación ante caída.
```

---

# 6.3. `kafka.event`

## `DomainEventEnvelope.java`

### Responsabilidad

Envoltorio estándar de todos los eventos Kafka del MS3.

### Campos principales

- `eventId`.
- `eventType`.
- `aggregateType`.
- `aggregateId`.
- `occurredAt`.
- `producer`.
- `schemaVersion`.
- `correlationId`.
- `payload`.

### Coexiste con

- `OutboxEventFactory`.
- `OutboxEventSerializer`.
- `KafkaDomainEventPublisher`.
- `MS4 consumers`.

### Cómo programarlo

- Debe ser estable y versionado.
- Debe permitir idempotencia.
- Debe incluir metadata de trazabilidad.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir entidades JPA.
- No incluir datos sensibles innecesarios.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `ProductoSnapshotEvent.java`

### Responsabilidad

Evento específico de snapshot de producto.

### Campos principales

- `envelope`.
- `ProductoSnapshotPayload`.

### Coexiste con

- `ProductoAdminServiceImpl`.
- `OutboxEventFactory`.

### Cómo programarlo

- Debe representar cambios de producto base.
- Debe ser consumible por MS4 sin consultar MS3.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir costos internos.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `ProductoSnapshotPayload.java`

### Responsabilidad

Payload con datos de producto para MS4.

### Campos principales

- `idProducto`.
- `codigoProducto`.
- `nombre`.
- `slug`.
- `categoría`.
- `marca`.
- `estadoPublicacion`.
- `estadoVenta`.
- `visiblePublico`.
- `vendible`.

### Coexiste con

- `ProductoSnapshotEvent`.
- `ProductoMapper`.

### Cómo programarlo

- Debe contener datos mínimos de catálogo.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir kardex.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `ProductoSkuSnapshotPayload.java`

### Responsabilidad

Payload de SKU asociado a producto.

### Campos principales

- `idSku`.
- `codigoSku`.
- `barcode`.
- `color`.
- `talla`.
- `estadoSku`.

### Coexiste con

- `ProductoSnapshotPayload`.
- `ProductoSkuServiceImpl`.

### Cómo programarlo

- Debe permitir que MS4 venda por SKU.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir stock si va en StockSnapshotPayload.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `ProductoImagenSnapshotPayload.java`

### Responsabilidad

Payload de imagen principal o imágenes públicas.

### Campos principales

- `publicId`.
- `secureUrl`.
- `principal`.
- `orden`.
- `altText`.

### Coexiste con

- `ProductoSnapshotPayload`.
- `ProductoImagenServiceImpl`.

### Cómo programarlo

- Debe usar secureUrl.
- Debe excluir secretos Cloudinary.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir API key/secret.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `PrecioSnapshotEvent.java`

### Responsabilidad

Evento de cambio de precio vigente.

### Campos principales

- `envelope`.
- `PrecioSnapshotPayload`.

### Coexiste con

- `PrecioSkuServiceImpl`.
- `OutboxEventFactory`.

### Cómo programarlo

- Debe emitirse al cambiar precio vigente.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No modificar ventas pasadas.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `PrecioSnapshotPayload.java`

### Responsabilidad

Payload de precio vigente por SKU.

### Campos principales

- `idSku`.
- `codigoSku`.
- `precioVenta`.
- `moneda`.
- `fechaInicio`.
- `vigente`.

### Coexiste con

- `PrecioSnapshotEvent`.
- `PrecioSkuMapper`.

### Cómo programarlo

- Debe permitir que MS4 actualice precio local.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir historial completo si no es necesario.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `PromocionSnapshotEvent.java`

### Responsabilidad

Evento de promoción vigente/programada/cancelada.

### Campos principales

- `envelope`.
- `PromocionSnapshotPayload`.

### Coexiste con

- `PromocionVersionServiceImpl`.
- `PromocionSkuDescuentoServiceImpl`.

### Cómo programarlo

- Debe informar cambios promocionales a MS4.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No recalcular ventas anteriores.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `PromocionSnapshotPayload.java`

### Responsabilidad

Payload de promoción.

### Campos principales

- `idPromocion`.
- `codigo`.
- `nombre`.
- `fechaInicio`.
- `fechaFin`.
- `estadoPromocion`.
- `visiblePublico`.
- `descuentos`.

### Coexiste con

- `PromocionSnapshotEvent`.

### Cómo programarlo

- Debe incluir descuentos por SKU.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir margen si no es necesario para MS4.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `PromocionSkuDescuentoPayload.java`

### Responsabilidad

Payload de descuento por SKU.

### Campos principales

- `idSku`.
- `codigoSku`.
- `tipoDescuento`.
- `valorDescuento`.
- `precioFinalEstimado`.
- `prioridad`.

### Coexiste con

- `PromocionSnapshotPayload`.

### Cómo programarlo

- Debe permitir descuentos distintos por SKU.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No aplicar descuento global.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `StockSnapshotEvent.java`

### Responsabilidad

Evento de stock actualizado.

### Campos principales

- `envelope`.
- `StockSnapshotPayload`.

### Coexiste con

- `StockServiceImpl`.
- `ReservaStockServiceImpl`.
- `MovimientoInventarioServiceImpl`.

### Cómo programarlo

- Debe emitirse ante cambios de stock relevante.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No depender de consulta posterior a MS3.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `StockSnapshotPayload.java`

### Responsabilidad

Payload de stock por SKU/almacén.

### Campos principales

- `idSku`.
- `codigoSku`.
- `idAlmacen`.
- `stockFisico`.
- `stockReservado`.
- `stockDisponible`.
- `updatedAt`.

### Coexiste con

- `StockSnapshotEvent`.

### Cómo programarlo

- Debe permitir que MS4 actualice stock local.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir costos si MS4 no los requiere.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `MovimientoInventarioEvent.java`

### Responsabilidad

Evento de movimiento/kardex registrado.

### Campos principales

- `envelope`.
- `MovimientoInventarioPayload`.

### Coexiste con

- `MovimientoInventarioServiceImpl`.

### Cómo programarlo

- Debe servir para auditoría o integración avanzada.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No reemplazar StockSnapshotEvent.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `MovimientoInventarioPayload.java`

### Responsabilidad

Payload de movimiento de inventario.

### Campos principales

- `codigoMovimiento`.
- `tipoMovimiento`.
- `cantidad`.
- `stockAnterior`.
- `stockNuevo`.
- `referencia`.
- `actor`.
- `createdAt`.

### Coexiste con

- `MovimientoInventarioEvent`.

### Cómo programarlo

- Debe explicar el cambio de stock.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir datos sensibles del usuario.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `Ms4StockCommandEvent.java`

### Responsabilidad

Evento/comando recibido desde MS4.

### Campos principales

- `envelope`.
- `Ms4StockCommandPayload`.

### Coexiste con

- `Ms4StockCommandConsumer`.

### Cómo programarlo

- Debe representar intención de reserva/confirmación/liberación/anulación.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No debe ser tratado sin idempotencia.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

## `Ms4StockCommandPayload.java`

### Responsabilidad

Payload de comando de stock enviado por MS4.

### Campos principales

- `idVentaMs4`.
- `idCarritoMs4`.
- `idSku`.
- `idAlmacen`.
- `cantidad`.
- `tipoOperacion`.
- `idempotencyKey`.

### Coexiste con

- `Ms4StockCommandHandler`.
- `Ms4StockEventValidator`.

### Cómo programarlo

- Debe contener datos suficientes para reconciliar.

- Debe ser serializable a JSON.
- Debe ser estable por versión.
- Debe evitar acoplarse a entidades JPA.
- Debe incluir metadata suficiente para trazabilidad.

### Qué evitar

- No incluir datos de facturación innecesarios.

- No usar entities como payload.
- No incluir secretos.
- No depender de relaciones lazy.

---

# 6.4. `kafka.outbox`

## `OutboxEventFactory.java`

### Responsabilidad

Construye eventos outbox desde cambios de dominio.

### Funciones principales

- crear evento producto.
- crear evento precio.
- crear evento promoción.
- crear evento stock.
- crear evento movimiento.

### Coexiste con

- `EventoDominioOutboxServiceImpl`.
- `Services de negocio`.

### Cómo programarlo

- No persiste por sí solo.
- No publica Kafka.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxEventSerializer.java`

### Responsabilidad

Serializa eventos a JSON seguro.

### Funciones principales

- serialize.
- validateJson.

### Coexiste con

- `OutboxEventFactory`.
- `KafkaDomainEventPublisher`.

### Cómo programarlo

- Debe usar JsonUtil.
- Debe fallar explícitamente si payload inválido.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxEventPublisher.java`

### Responsabilidad

Lee eventos pendientes de outbox y coordina publicación.

### Funciones principales

- publicar lote.
- bloquear eventos.
- marcar resultado.

### Coexiste con

- `OutboxScheduler`.
- `KafkaPublisherServiceImpl`.

### Cómo programarlo

- Debe ser transaccional por lote o evento.
- Debe manejar errores sin perder eventos.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxScheduler.java`

### Responsabilidad

Scheduler que dispara publicación de outbox.

### Funciones principales

- ejecutar cada intervalo.
- respetar enabled=false.
- evitar ejecución concurrente.

### Coexiste con

- `OutboxEventPublisher`.
- `OutboxProperties`.

### Cómo programarlo

- Debe ser apagable por properties.
- No debe crear eventos.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxRetryPolicy.java`

### Responsabilidad

Define si un evento puede reintentarse.

### Funciones principales

- maxAttempts.
- estado permitido.
- backoff si aplica.

### Coexiste con

- `OutboxEventPublisher`.
- `KafkaPublisherServiceImpl`.
- `OutboxController`.

### Cómo programarlo

- No publica.
- No cambia BD directamente.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxPublishResult.java`

### Responsabilidad

Resultado técnico de publicación.

### Funciones principales

- success.
- errorMessage.
- publishedAt.
- attempts.

### Coexiste con

- `KafkaPublisherServiceImpl`.
- `OutboxEventPublisher`.

### Cómo programarlo

- Debe ser DTO interno.
- No exponer completo al usuario.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

## `OutboxLockService.java`

### Responsabilidad

Bloquea eventos pendientes para evitar doble publicación concurrente.

### Funciones principales

- lock por batch.
- unlock.
- lock timeout.

### Coexiste con

- `OutboxEventPublisher`.
- `EventoDominioOutboxRepository`.

### Cómo programarlo

- Debe prevenir múltiples nodos publicando lo mismo.
- No publicar Kafka.

- Debe trabajar con `EventoDominioOutbox`.
- Debe conservar trazabilidad de errores.
- Debe permitir reintentos seguros.
- Debe respetar `OutboxProperties`.

### Qué evitar

- No mezclar reglas de negocio de producto, stock o precio.
- No aceptar payload arbitrario del usuario.
- No borrar eventos publicados.
- No perder errores técnicos.

---

# 6.5. `kafka.producer`

## `KafkaDomainEventPublisher.java`

### Responsabilidad

Publicador técnico final hacia Kafka.

### Funciones principales

- enviar topic/key/payload.
- recibir confirmación.
- manejar error.

### Coexiste con

- `KafkaPublisherServiceImpl`.

### Cómo programarlo

- Debe envolver KafkaTemplate.
- Debe devolver resultado controlado.
- Debe loguear error técnico.

- Debe estar aislado de services de negocio.
- Debe manejar errores de publicación de forma controlada.
- Debe respetar trazabilidad con requestId/correlationId.

### Qué evitar

- No construir payload de negocio.
- No consultar BD de dominio.
- No decidir si una operación debe emitir evento.
- No ser invocado directamente por controllers CRUD.

---

## `KafkaEventKeyResolver.java`

### Responsabilidad

Resuelve la key Kafka para particionamiento e idempotencia.

### Funciones principales

- por producto.
- por SKU.
- por stock.
- por promoción.
- por venta/referencia.

### Coexiste con

- `OutboxEventFactory`.
- `KafkaPublisherServiceImpl`.

### Cómo programarlo

- Debe ser determinístico.
- Debe evitar keys aleatorias para aggregate.

- Debe estar aislado de services de negocio.
- Debe manejar errores de publicación de forma controlada.
- Debe respetar trazabilidad con requestId/correlationId.

### Qué evitar

- No construir payload de negocio.
- No consultar BD de dominio.
- No decidir si una operación debe emitir evento.
- No ser invocado directamente por controllers CRUD.

---

## `KafkaTopicResolver.java`

### Responsabilidad

Resuelve topic según eventType/aggregateType.

### Funciones principales

- producto.
- stock.
- precio.
- promoción.
- movimiento.

### Coexiste con

- `OutboxEventFactory`.
- `KafkaPublisherServiceImpl`.

### Cómo programarlo

- Debe usar KafkaTopicProperties.
- No hardcodear topics en services.

- Debe estar aislado de services de negocio.
- Debe manejar errores de publicación de forma controlada.
- Debe respetar trazabilidad con requestId/correlationId.

### Qué evitar

- No construir payload de negocio.
- No consultar BD de dominio.
- No decidir si una operación debe emitir evento.
- No ser invocado directamente por controllers CRUD.

---

# 6.6. `kafka.consumer`

## `Ms4StockCommandConsumer.java`

### Responsabilidad

Listener Kafka que recibe comandos/eventos de stock desde MS4.

### Funciones principales

- escuchar topic MS4.
- deserializar evento.
- delegar handler.
- manejar ack según estrategia.

### Coexiste con

- `Ms4StockCommandHandler`.
- `KafkaConsumerErrorHandler`.

### Cómo programarlo

- No aplicar negocio directamente.
- Debe ser delgado y resiliente.

- Debe soportar reintentos de Kafka.
- Debe diferenciar errores funcionales de errores técnicos.
- Debe garantizar que un evento duplicado no descuente stock dos veces.
- Debe conservar logs con correlationId si viene en el evento.

### Qué evitar

- No crear ventas.
- No facturar.
- No modificar precio usado por MS4.
- No procesar eventos sin validar payload mínimo.
- No aplicar operaciones sin idempotencia.

---

## `Ms4StockCommandHandler.java`

### Responsabilidad

Procesa funcionalmente eventos MS4 ya deserializados.

### Funciones principales

- validar evento.
- verificar idempotencia.
- delegar a Ms4ReconciliacionService.
- registrar resultado.

### Coexiste con

- `Ms4ReconciliacionServiceImpl`.
- `Ms4StockEventValidator`.
- `KafkaIdempotencyGuard`.

### Cómo programarlo

- Debe ser idempotente.
- Debe evitar duplicar stock.

- Debe soportar reintentos de Kafka.
- Debe diferenciar errores funcionales de errores técnicos.
- Debe garantizar que un evento duplicado no descuente stock dos veces.
- Debe conservar logs con correlationId si viene en el evento.

### Qué evitar

- No crear ventas.
- No facturar.
- No modificar precio usado por MS4.
- No procesar eventos sin validar payload mínimo.
- No aplicar operaciones sin idempotencia.

---

## `KafkaConsumerErrorHandler.java`

### Responsabilidad

Maneja errores de consumo Kafka.

### Funciones principales

- errores de deserialización.
- errores funcionales.
- errores técnicos.
- DLQ si se implementa.

### Coexiste con

- `Ms4StockCommandConsumer`.

### Cómo programarlo

- Debe loguear con trazabilidad.
- No debe ocultar fallas críticas.

- Debe soportar reintentos de Kafka.
- Debe diferenciar errores funcionales de errores técnicos.
- Debe garantizar que un evento duplicado no descuente stock dos veces.
- Debe conservar logs con correlationId si viene en el evento.

### Qué evitar

- No crear ventas.
- No facturar.
- No modificar precio usado por MS4.
- No procesar eventos sin validar payload mínimo.
- No aplicar operaciones sin idempotencia.

---

## `KafkaIdempotencyGuard.java`

### Responsabilidad

Evita procesar dos veces el mismo evento MS4.

### Funciones principales

- resolver idempotencyKey.
- verificar procesado.
- marcar procesado si aplica.
- detectar duplicado.

### Coexiste con

- `Ms4StockCommandHandler`.
- `Ms4StockEventValidator`.
- `ProcessedEventGuard`.

### Cómo programarlo

- Debe apoyarse en reservas/movimientos/referencias.
- Debe ser transaccional donde aplique.

- Debe soportar reintentos de Kafka.
- Debe diferenciar errores funcionales de errores técnicos.
- Debe garantizar que un evento duplicado no descuente stock dos veces.
- Debe conservar logs con correlationId si viene en el evento.

### Qué evitar

- No crear ventas.
- No facturar.
- No modificar precio usado por MS4.
- No procesar eventos sin validar payload mínimo.
- No aplicar operaciones sin idempotencia.

---

# 7. `shared`

## Reglas generales de shared

`shared` es donde más se aplica DRY.

Debe contener lógica transversal usada por varias partes del MS3, sin pertenecer a un solo agregado.

---

## `audit/AuditContext.java`

### Responsabilidad

Objeto de contexto de auditoría del request.

### Funciones o datos principales

- actor.
- ip.
- userAgent.
- requestId.
- correlationId.
- path.

### Coexiste con

- `RequestAuditContextFilter`.
- `AuditoriaFuncionalServiceImpl`.

### Cómo programarlo

- Debe ser simple.
- No persistir por sí mismo.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `audit/AuditContextHolder.java`

### Responsabilidad

ThreadLocal o mecanismo equivalente para guardar AuditContext por request.

### Funciones o datos principales

- set.
- get.
- clear.

### Coexiste con

- `RequestAuditContextFilter`.
- `AuditoriaFuncionalServiceImpl`.

### Cómo programarlo

- Debe limpiarse siempre.
- No filtrar memoria.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `audit/AuditEventFactory.java`

### Responsabilidad

Factory para construir eventos de auditoría uniformes.

### Funciones o datos principales

- success.
- failure.
- denied.

### Coexiste con

- `Services`.
- `AuditoriaFuncionalServiceImpl`.

### Cómo programarlo

- Evita duplicar armado de auditoría.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `audit/AuditMetadataBuilder.java`

### Responsabilidad

Builder para metadata JSON de auditoría.

### Funciones o datos principales

- agregar valores seguros.
- ocultar sensibles.
- serializar metadata.

### Coexiste con

- `AuditEventFactory`.

### Cómo programarlo

- No incluir tokens/contraseñas.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `audit/AuditResult.java`

### Responsabilidad

Modelo interno de resultado de auditoría.

### Funciones o datos principales

- resultado.
- mensaje.
- metadata.

### Coexiste con

- `AuditoriaFuncionalServiceImpl`.

### Cómo programarlo

- No reemplazar ResponseDto.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `constants/ApiPaths.java`

### Responsabilidad

Constantes de rutas base del MS3.

### Funciones o datos principales

- PUBLIC.
- ADMIN.
- INVENTARIO.
- CATALOGO.
- OUTBOX.

### Coexiste con

- `Controllers`.
- `SecurityConfig`.

### Cómo programarlo

- No duplicar strings de rutas.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `constants/HeaderNames.java`

### Responsabilidad

Constantes de headers.

### Funciones o datos principales

- X-Request-Id.
- X-Correlation-Id.
- Authorization.
- User-Agent.

### Coexiste con

- `Filters`.
- `Integration clients`.

### Cómo programarlo

- No hardcodear headers.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `constants/SystemActors.java`

### Responsabilidad

Constantes de actores técnicos.

### Funciones o datos principales

- SISTEMA.
- MS3_OUTBOX.
- MS4_SYNC.

### Coexiste con

- `Audit`.
- `Kafka`.
- `Ms4SyncPolicy`.

### Cómo programarlo

- No mezclar con roles humanos.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `constants/Ms3Constants.java`

### Responsabilidad

Constantes generales del MS3.

### Funciones o datos principales

- service name.
- version.
- default limits.

### Coexiste con

- `Varios componentes`.

### Cómo programarlo

- No meter reglas que deberían ser properties.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `constants/TopicNames.java`

### Responsabilidad

Constantes fallback de nombres de topic.

### Funciones o datos principales

- topics default.

### Coexiste con

- `KafkaTopicProperties`.
- `KafkaTopicResolver`.

### Cómo programarlo

- Preferir properties para ambiente.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/BusinessException.java`

### Responsabilidad

Excepción base funcional.

### Funciones o datos principales

- code.
- message.
- context.

### Coexiste con

- `GlobalExceptionHandler`.

### Cómo programarlo

- No usar para errores técnicos inesperados.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/ValidationException.java`

### Responsabilidad

Error de validación funcional.

### Funciones o datos principales

- fieldErrors.
- message.

### Coexiste con

- `Validators`.

### Cómo programarlo

- No usar para 500.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/NotFoundException.java`

### Responsabilidad

Recurso no encontrado.

### Funciones o datos principales

- entity.
- reference.

### Coexiste con

- `Resolvers`.
- `Services`.

### Cómo programarlo

- No devolver stacktrace.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/ConflictException.java`

### Responsabilidad

Conflicto funcional.

### Funciones o datos principales

- duplicate.
- state conflict.
- version conflict.

### Coexiste con

- `Validators`.
- `Services`.

### Cómo programarlo

- No usar para permisos.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/ForbiddenException.java`

### Responsabilidad

Acceso denegado funcional.

### Funciones o datos principales

- actor.
- operation.

### Coexiste con

- `Policies`.

### Cómo programarlo

- No usar para token inválido.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/UnauthorizedException.java`

### Responsabilidad

No autenticado.

### Funciones o datos principales

- missing auth.

### Coexiste con

- `CurrentUserResolver`.

### Cómo programarlo

- No usar para permisos contextuales.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/ExternalServiceException.java`

### Responsabilidad

Falla externa general.

### Funciones o datos principales

- serviceName.
- status.
- message.

### Coexiste con

- `Integration`.

### Cómo programarlo

- No exponer detalle sensible.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/CloudinaryIntegrationException.java`

### Responsabilidad

Falla específica Cloudinary.

### Funciones o datos principales

- publicId.
- operation.
- cause.

### Coexiste con

- `CloudinaryErrorMapper`.

### Cómo programarlo

- No mostrar API secret.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/KafkaPublishException.java`

### Responsabilidad

Falla de publicación Kafka.

### Funciones o datos principales

- topic.
- eventId.
- cause.

### Coexiste con

- `KafkaPublisherServiceImpl`.

### Cómo programarlo

- No perder evento outbox.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `exception/GlobalExceptionHandler.java`

### Responsabilidad

Manejador global de errores HTTP.

### Funciones o datos principales

- mapear excepciones.
- respuesta segura.
- log técnico.

### Coexiste con

- `Controllers`.

### Cómo programarlo

- No exponer stacktrace.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `pagination/PaginationService.java`

### Responsabilidad

Servicio DRY para construir paginación.

### Funciones o datos principales

- toPageable.
- toPageResponse.

### Coexiste con

- `Services listados`.

### Cómo programarlo

- No repetir armado de PageResponseDto.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `pagination/PaginationMapper.java`

### Responsabilidad

Mapea Page<T> a PageResponseDto.

### Funciones o datos principales

- content.
- metadata.

### Coexiste con

- `PaginationService`.

### Cómo programarlo

- No mezclar con mappers de dominio.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `pagination/SortNormalizer.java`

### Responsabilidad

Normaliza sortBy/sortDirection.

### Funciones o datos principales

- sort default.
- ASC/DESC.

### Coexiste con

- `PaginationService`.

### Cómo programarlo

- No permitir sort arbitrario inseguro.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `pagination/SortFieldValidator.java`

### Responsabilidad

Valida campos permitidos de ordenamiento.

### Funciones o datos principales

- allowedFields.

### Coexiste con

- `Services`.
- `PaginationService`.

### Cómo programarlo

- Evita errores y abuso.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `persistence/EntityLookupService.java`

### Responsabilidad

Utilidad genérica para buscar entidades.

### Funciones o datos principales

- find active.
- not found.

### Coexiste con

- `ReferenceResolvers`.

### Cómo programarlo

- No reemplazar repositories.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `persistence/EntityStateValidator.java`

### Responsabilidad

Valida estado activo/inactivo de entidades.

### Funciones o datos principales

- ensureActive.
- ensureInactiveAllowed.

### Coexiste con

- `Services`.
- `Validators`.

### Cómo programarlo

- No duplicar if estado.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `persistence/ActiveRecordResolver.java`

### Responsabilidad

Resuelve registros activos por referencia.

### Funciones o datos principales

- id/codigo/slug.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No aplicar negocio.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `persistence/SoftDeleteSupport.java`

### Responsabilidad

Soporte para eliminación lógica.

### Funciones o datos principales

- markInactive.
- restore if allowed.

### Coexiste con

- `Services`.

### Cómo programarlo

- No borrar físico.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/EntityDisplayResolver.java`

### Responsabilidad

Construye nombres visibles de entidades para respuestas y errores.

### Funciones o datos principales

- display producto.
- display proveedor.

### Coexiste con

- `Mappers`.
- `Errors`.

### Cómo programarlo

- No consultar pesado.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/ReferenceOptionMapper.java`

### Responsabilidad

Mapea entidad a option DTO.

### Funciones o datos principales

- toOption.

### Coexiste con

- `CatalogoLookupService`.

### Cómo programarlo

- No mezclar con mappers detalle.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/TipoProductoReferenceResolver.java`

### Responsabilidad

Resuelve TipoProducto por id/código/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No validar negocio.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/CategoriaReferenceResolver.java`

### Responsabilidad

Resuelve Categoria por id/código/slug/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No generar slug.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/MarcaReferenceResolver.java`

### Responsabilidad

Resuelve Marca por id/código/slug/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No crear marca.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/AtributoReferenceResolver.java`

### Responsabilidad

Resuelve Atributo por id/código/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No validar valor.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/ProductoReferenceResolver.java`

### Responsabilidad

Resuelve Producto por id/código/slug.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No publicar producto.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/ProductoSkuReferenceResolver.java`

### Responsabilidad

Resuelve SKU por id/código/barcode.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No validar stock.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/ProveedorReferenceResolver.java`

### Responsabilidad

Resuelve Proveedor por id/RUC/documento/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No crear proveedor.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/AlmacenReferenceResolver.java`

### Responsabilidad

Resuelve Almacen por id/código/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No validar stock.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/PromocionReferenceResolver.java`

### Responsabilidad

Resuelve Promocion por id/código/nombre.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No activar promoción.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `reference/EmpleadoInventarioReferenceResolver.java`

### Responsabilidad

Resuelve snapshot empleado MS2 por idUsuario/idEmpleado/código.

### Funciones o datos principales

- resolve active.

### Coexiste con

- `EntityReferenceService`.

### Cómo programarlo

- No crear empleado.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `response/ApiResponseFactory.java`

### Responsabilidad

Factory de respuestas exitosas estándar.

### Funciones o datos principales

- created.
- updated.
- deleted.
- listed.
- detail.

### Coexiste con

- `Controllers`.

### Cómo programarlo

- Evita mensajes duplicados.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `response/ErrorResponseFactory.java`

### Responsabilidad

Factory de respuestas de error estándar.

### Funciones o datos principales

- from exception.
- field errors.

### Coexiste con

- `GlobalExceptionHandler`.
- `Security handlers`.

### Cómo programarlo

- No exponer stacktrace.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `response/ApiErrorCode.java`

### Responsabilidad

Códigos de error funcionales.

### Funciones o datos principales

- PRODUCTO_NO_ENCONTRADO.
- STOCK_INSUFICIENTE.

### Coexiste con

- `Exceptions`.
- `GlobalExceptionHandler`.

### Cómo programarlo

- No duplicar strings.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `specification/SpecificationBuilder.java`

### Responsabilidad

Builder genérico para specifications.

### Funciones o datos principales

- and if not null.
- like.
- equals.
- range.

### Coexiste con

- `Specifications`.

### Cómo programarlo

- Evita repetición.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `specification/SpecificationUtils.java`

### Responsabilidad

Utilidades JPA Specification.

### Funciones o datos principales

- likeNormalized.
- dateBetween.
- enumEquals.
- active.

### Coexiste con

- `Specifications`.

### Cómo programarlo

- No contener reglas de negocio.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `specification/DateRangeCriteria.java`

### Responsabilidad

Value/filter compartido para rango de fechas.

### Funciones o datos principales

- from.
- to.

### Coexiste con

- `FilterDtos`.
- `Specifications`.

### Cómo programarlo

- Validar coherencia.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `specification/NumericRangeCriteria.java`

### Responsabilidad

Rango numérico compartido.

### Funciones o datos principales

- min.
- max.

### Coexiste con

- `Stock/Precio filters`.

### Cómo programarlo

- No usar para dinero sin moneda si importa.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `specification/BooleanCriteria.java`

### Responsabilidad

Criterio booleano reusable.

### Funciones o datos principales

- value.
- explicit.

### Coexiste con

- `Specifications`.

### Cómo programarlo

- Distinguir null de false.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `validation/ValidationErrorCollector.java`

### Responsabilidad

Acumula errores de validación.

### Funciones o datos principales

- add.
- throwIfAny.

### Coexiste con

- `Validators`.

### Cómo programarlo

- Evita lanzar de a uno si se quiere listar varios.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `validation/BusinessRuleValidator.java`

### Responsabilidad

Contrato/base para validators de reglas.

### Funciones o datos principales

- validate.

### Coexiste con

- `Validators`.

### Cómo programarlo

- No acoplar a controller.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `validation/RequiredFieldValidator.java`

### Responsabilidad

Validaciones comunes de requeridos.

### Funciones o datos principales

- notBlank.
- notNull.
- notEmpty.

### Coexiste con

- `Validators`.

### Cómo programarlo

- No repetir if null.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `code/CodigoGenerator.java`

### Responsabilidad

Genera texto de código según prefijo/número/longitud.

### Funciones o datos principales

- format.

### Coexiste con

- `CodigoGeneradorServiceImpl`.

### Cómo programarlo

- No consultar BD.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `code/CodigoFormat.java`

### Responsabilidad

Modelo de formato de código.

### Funciones o datos principales

- prefijo.
- longitud.
- separator.

### Coexiste con

- `CodigoGenerator`.

### Cómo programarlo

- No generar correlativo.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `code/CodigoSequenceLock.java`

### Responsabilidad

Soporte de bloqueo lógico/técnico para correlativos.

### Funciones o datos principales

- lock.
- release if needed.

### Coexiste con

- `CodigoGeneradorServiceImpl`.

### Cómo programarlo

- No reemplazar transacción BD.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `idempotency/IdempotencyKeyResolver.java`

### Responsabilidad

Construye clave idempotente.

### Funciones o datos principales

- from MS4 event.
- from reference.

### Coexiste con

- `KafkaIdempotencyGuard`.
- `ProcessedEventGuard`.

### Cómo programarlo

- Debe ser determinístico.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `idempotency/ProcessedEventGuard.java`

### Responsabilidad

Verifica si una operación/evento ya fue aplicado.

### Funciones o datos principales

- isProcessed.
- markProcessed.

### Coexiste con

- `Ms4ReconciliacionServiceImpl`.
- `KafkaIdempotencyGuard`.

### Cómo programarlo

- No duplicar salida.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

## `idempotency/DuplicateEventDecision.java`

### Responsabilidad

Resultado de decisión ante evento duplicado.

### Funciones o datos principales

- processed.
- ignored.
- shouldRetry.

### Coexiste con

- `KafkaIdempotencyGuard`.

### Cómo programarlo

- DTO interno.

- Debe ser reutilizable.
- Debe reducir duplicación.
- Debe mantenerse pequeño y claro.
- Debe tener nombres explícitos.

### Qué evitar

- No mezclar lógica específica de un agregado si solo aplica a una entidad.
- No crear dependencias circulares.
- No consultar servicios de alto nivel desde utilidades compartidas.
- No exponer datos sensibles.

---

# 8. `util`

## Reglas generales para util

Las clases `util` deben ser puras, estáticas o de bajo acoplamiento.  
No deben depender de Spring salvo necesidad fuerte.  
No deben consultar base de datos.  
No deben lanzar excepciones funcionales complejas si eso pertenece a validators.

---

## `StringNormalizer.java`

### Responsabilidad

Normaliza texto para búsquedas, duplicados y comparaciones.

### Funciones esperadas

- trim.
- lower/upper.
- remover dobles espacios.
- quitar tildes si se decide.

### Coexiste con

- `Validators`.
- `Specifications`.
- `SlugUtil`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `SlugUtil.java`

### Responsabilidad

Genera slug base a partir de texto.

### Funciones esperadas

- normalizar.
- reemplazar espacios por guiones.
- eliminar caracteres inválidos.

### Coexiste con

- `SlugGeneratorServiceImpl`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `CodeFormatUtil.java`

### Responsabilidad

Formatea códigos internos.

### Funciones esperadas

- prefijo.
- padding.
- separadores.

### Coexiste con

- `CodigoGenerator`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `TextSearchUtil.java`

### Responsabilidad

Prepara texto para filtros `LIKE`.

### Funciones esperadas

- escape like.
- normalizar search.
- tokens si aplica.

### Coexiste con

- `Specifications`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `DateTimeUtil.java`

### Responsabilidad

Utilidades de fecha/hora.

### Funciones esperadas

- nowUtc.
- validateRange.
- startOfDay/endOfDay.

### Coexiste con

- `Validators`.
- `Specifications`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `JsonUtil.java`

### Responsabilidad

Serialización/deserialización JSON.

### Funciones esperadas

- toJson.
- fromJson.
- isValidJson.

### Coexiste con

- `OutboxEventSerializer`.
- `AuditMetadataBuilder`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `RequestMetadataUtil.java`

### Responsabilidad

Extrae metadata segura del request.

### Funciones esperadas

- ip.
- userAgent.
- requestId.
- correlationId.

### Coexiste con

- `Filters`.
- `GlobalExceptionHandler`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `BigDecimalUtil.java`

### Responsabilidad

Operaciones seguras con BigDecimal.

### Funciones esperadas

- scale.
- compare.
- zeroIfNull.

### Coexiste con

- `MoneyUtil`.
- `PercentageUtil`.
- `StockMathUtil`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `MoneyUtil.java`

### Responsabilidad

Operaciones de dinero.

### Funciones esperadas

- validar positivo.
- redondeo.
- subtotal/total.

### Coexiste con

- `PrecioSkuValidator`.
- `CompraInventarioValidator`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `StockMathUtil.java`

### Responsabilidad

Operaciones de stock.

### Funciones esperadas

- disponible=fisico-reservado.
- sumar entrada.
- restar salida.
- reservar.
- liberar.

### Coexiste con

- `StockValidator`.
- `StockServiceImpl`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `PercentageUtil.java`

### Responsabilidad

Operaciones de porcentaje.

### Funciones esperadas

- validar 0-100.
- aplicar descuento.
- calcular precio final.

### Coexiste con

- `PromocionSkuDescuentoValidator`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `FileNameUtil.java`

### Responsabilidad

Utilidades de nombre de archivo.

### Funciones esperadas

- extensión.
- nombre seguro.
- normalizar.

### Coexiste con

- `CloudinaryImageValidator`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

## `MimeTypeUtil.java`

### Responsabilidad

Utilidades de MIME type.

### Funciones esperadas

- validar imagen.
- mapear extensión.
- formatos permitidos.

### Coexiste con

- `CloudinaryImageValidator`.

### Cómo programarlo

- Debe ser determinístico.
- Debe tener métodos pequeños.
- Debe ser fácil de probar unitariamente.
- Debe evitar dependencia de Spring.
- Debe documentar reglas de formato cuando sean críticas.

### Qué evitar

- No consultar base de datos.
- No llamar services.
- No capturar errores para ocultarlos.
- No duplicar lógica existente en value objects si ya fue encapsulada.
- No contener reglas de autorización.

---

# 9. Decisión final de esta parte

Esta parte del MS3 sostiene la calidad técnica del proyecto.

```text
validator:
    protege reglas de negocio y consistencia.

policy:
    protege permisos y autorización contextual.

specification:
    permite listados profesionales con filtros dinámicos y paginación.

integration:
    aísla sistemas externos.

kafka:
    permite resiliencia, sincronización, outbox e idempotencia con MS4.

shared:
    concentra DRY transversal.

util:
    concentra funciones puras y reutilizables.
```

Regla final:

```text
Service orquesta.
Validator valida.
Policy autoriza.
Specification filtra.
Integration comunica.
Kafka sincroniza.
Shared evita duplicación.
Util normaliza/calcula/formatea.
```
Con esta separación, el MS3 puede programarse de manera mantenible, profesional y preparada para producción.


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

