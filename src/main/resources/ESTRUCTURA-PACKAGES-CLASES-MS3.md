Listo. Estos son los **3 `application.properties` completos y actualizados** para **MS2, MS3 y MS4**, con Kafka Docker local, topics reales, Outbox real y topics técnicos `dev.*` para los probes sin persistencia. Mantengo los secretos tal como los venías usando. Base consolidada desde los properties/código que compartiste de MS4/MS2 y el contexto actual de MS3.

No mezcles los tres en un solo archivo. Cada bloque va en su microservicio correspondiente.

---

# 1. MS2 — `src/main/resources/application.properties`

```properties
# =========================================================
# MS2 - PERSONAS CLIENTES EMPLEADOS - LOCAL DETRAS DEL API GATEWAY
# =========================================================
spring.application.name=ms-personas-clientes-empleados
server.port=8082

# Necesario para operar correctamente detrás del API Gateway.
server.forward-headers-strategy=framework

# =========================================================
# SQL SERVER LOCAL
# =========================================================
spring.datasource.url=jdbc:sqlserver://192.168.18.7:1433;databaseName=bd_ms2;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=admin
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# =========================================================
# JPA / HIBERNATE
# =========================================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.flyway.enabled=false

# =========================================================
# APP
# =========================================================
app.name=MS Personas Clientes Empleados
app.environment=local
app.gateway-url=http://localhost:8080
app.frontend-url=http://localhost:4200
app.locale=es-PE
app.timezone=America/Lima

# =========================================================
# MAIL FUNCIONAL MS2
# MS2 solo envia correos funcionales.
# Seguridad, password reset, codigos y login sospechoso pertenecen a MS1.
# =========================================================
app.mail.enabled=false
app.mail.from-email=tresdynx@gmail.com
app.mail.from-name=MS Personas Clientes Empleados
app.mail.support-email=tresdynx@gmail.com

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tresdynx@gmail.com
spring.mail.password=unoy evse kuws vdrn
spring.mail.protocol=smtp
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=8000
spring.mail.properties.mail.smtp.writetimeout=8000
spring.mail.properties.mail.debug=false

# =========================================================
# SECURITY - RESOURCE SERVER JWT
# MS2 valida JWT emitidos por MS1.
# MS2 no emite tokens, no maneja login, no maneja refresh token.
# =========================================================
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks

# No usar private key en MS2.
# MS2 valida con JWKS publico expuesto por MS1.
# spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/ms1-public.pem

# =========================================================
# MS1 INTEGRATION
# =========================================================
app.ms1.enabled=true

ms1.enabled=true
ms1.base-url=http://localhost:8081

# Endpoint real de administracion de usuarios en MS1.
# MS1 expone UsuarioController en /api/usuarios.
# Se usa para crear usuario EMPLEADO desde MS2.
ms1.create-user-path=/api/usuarios

ms1.timeout=5s

# Validacion online de sesion MS1.
# Si true, MS2 valida contra MS1 que la sesion del JWT siga activa.
ms1.session-validation-enabled=true
ms1.validate-session-path=/api/internal/sesiones/validate

# Seguridad interna MS2 -> MS1 para /api/internal/**
# Debe coincidir exactamente con internal.security.service-key en MS1.
ms1.internal-service-key-header=X-Internal-Service-Key
ms1.internal-service-key=local-ms2-to-ms1-internal-key-change-me

# Validaciones funcionales del JWT emitido por MS1.
# Debe coincidir con security.jwt.issuer y security.jwt.audiences de MS1.
ms1.jwt-issuer=http://localhost:8080
ms1.required-audiences[0]=ms-personas-clientes-empleados
ms1.required-audiences[1]=api-gateway
ms1.access-token-type=access
ms1.require-user-id-claim=true
ms1.require-session-claim=true
ms1.require-role-claim=true
ms1.require-token-type-claim=true

# =========================================================
# KAFKA - MS2
# MS2 produce snapshots funcionales hacia MS4.
# MS2 no consume eventos funcionales Kafka.
# Kafka local corre en Docker Desktop: kafka-local -> localhost:9092
# =========================================================
app.kafka.enabled=true

# Topics producidos por MS2.
app.kafka.topics.cliente-snapshot=ms2.cliente.snapshot.v1
app.kafka.topics.empleado-snapshot=ms2.empleado.snapshot.v1

# Broker Kafka local.
spring.kafka.bootstrap-servers=localhost:9092

# Kafka Admin.
# Permite que Spring declare topics si agregaste KafkaTopicAdminConfig con NewTopic.
spring.kafka.admin.auto-create=true
spring.kafka.admin.fail-fast=false

# Producer Kafka confiable para Outbox.
spring.kafka.producer.client-id=ms2-outbox-producer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.max.in.flight.requests.per.connection=5
spring.kafka.producer.properties.delivery.timeout.ms=120000
spring.kafka.producer.properties.request.timeout.ms=30000
spring.kafka.producer.properties.linger.ms=5
spring.kafka.producer.properties.batch.size=16384
spring.kafka.producer.properties.retry.backoff.ms=1000
spring.kafka.producer.properties.compression.type=none

# =========================================================
# KAFKA CONSUMER - SOLO PARA PROBE TECNICO MS2
# MS2 oficialmente produce snapshots.
# Este consumer solo escucha ACK de prueba desde MS4.
# No persiste en BD, no usa Outbox, no ejecuta reglas de negocio.
# =========================================================
spring.kafka.consumer.client-id=ms2-probe-consumer-client
spring.kafka.consumer.group-id=ms2-probe-consumer
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.properties.max.poll.records=10

spring.kafka.listener.ack-mode=manual
spring.kafka.listener.missing-topics-fatal=false

# =========================================================
# OUTBOX KAFKA
# El service de negocio guarda cambio + evento pendiente en la misma transaccion.
# El scheduler publica despues hacia Kafka.
# =========================================================
outbox.enabled=true
outbox.batch-size=25
outbox.max-attempts=5

# Se deja en milisegundos para compatibilidad con:
# @Scheduled(fixedDelayString = "${outbox.fixed-delay:10000}")
outbox.fixed-delay=10000

outbox.lock-timeout=30s
outbox.publish-timeout=10s

# =========================================================
# KAFKA PROBE - MS2 -> MS4
# Prueba tecnica de comunicacion Kafka sin BD.
# MS2 publica a MS4 y espera ACK desde MS4.
# =========================================================
app.kafka.probe.enabled=true
app.kafka.probe.run-on-startup=true
app.kafka.probe.initial-delay-ms=8000
app.kafka.probe.retry-delay-ms=10000
app.kafka.probe.max-attempts=6
app.kafka.probe.fail-on-timeout=false
app.kafka.probe.send-timeout-ms=10000
app.kafka.probe.service-name=ms-personas-clientes-empleados
app.kafka.probe.target-ms4=ms-ventas-facturacion

# MS2 emite probe hacia MS4.
app.kafka.probe.topics.ms2-to-ms4=dev.ms2.ms4.probe.v1

# MS4 responde ACK hacia MS2.
app.kafka.probe.topics.ms4-to-ms2-ack=dev.ms4.ms2.probe-ack.v1

# =========================================================
# AUDITORIA / PAGINACION
# =========================================================
app.audit.enabled=true
app.pagination.default-size=20
app.pagination.max-size=100

# =========================================================
# ACTUATOR
# =========================================================
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized

# =========================================================
# OPENAPI / SWAGGER
# Cada microservicio expone su propio contrato OpenAPI.
# En local queda habilitado.
# =========================================================
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha

# =========================================================
# LOGGING LOCAL
# =========================================================
logging.level.root=INFO
logging.level.com.upsjb.ms2=INFO
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=INFO
logging.level.org.hibernate.orm.jdbc.bind=INFO
logging.level.org.springframework.kafka=INFO
logging.level.org.apache.kafka=INFO
```

---

# 2. MS3 — `src/main/resources/application.properties`

```properties
# =========================================================
# MS3 - CATALOGO INVENTARIO - LOCAL DETRAS DEL API GATEWAY
# =========================================================
spring.application.name=ms-catalogo-inventario
server.port=8083

# Necesario para operar correctamente detras del API Gateway.
server.forward-headers-strategy=framework

# =========================================================
# SQL SERVER LOCAL
# =========================================================
spring.datasource.url=jdbc:sqlserver://192.168.18.7:1433;databaseName=bd_ms3;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=admin
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# =========================================================
# JPA / HIBERNATE
# En local usamos update para avanzar rapido.
# En produccion deberia ser validate + Flyway habilitado.
# =========================================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.open-in-view=false

spring.flyway.enabled=false
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# =========================================================
# APP
# =========================================================
app.name=MS Catalogo Inventario
app.version=1.0.0
app.description=Microservicio de catalogo, productos, SKU, precios, promociones, inventario, stock, kardex, Cloudinary y eventos outbox Kafka.
app.environment=local
app.gateway-url=http://localhost:8080
app.frontend-url=http://localhost:4200
app.locale=es-PE
app.timezone=America/Lima
app.zone-id=UTC

# =========================================================
# MULTIPART - IMAGENES PRODUCTO / SKU
# =========================================================
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB

# =========================================================
# SECURITY - RESOURCE SERVER JWT
# MS3 valida JWT emitidos por MS1.
# MS3 no emite tokens, no maneja login, no maneja refresh token.
# =========================================================
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks

# Alternativa si no usas JWKS:
# spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/ms1-public.pem

# =========================================================
# MS1 INTEGRATION
# MS3 consulta MS1 solo para validaciones internas, sesion o contexto.
# =========================================================
app.ms1.enabled=true

ms1.enabled=true
ms1.base-url=http://localhost:8081
ms1.timeout=5s

# Validacion online de sesion MS1.
# Por ahora queda en false porque MS3 valida JWT localmente con JWKS.
ms1.session-validation-enabled=false
ms1.validate-session-path=/api/internal/sesiones/validate

# Seguridad interna MS3 -> MS1 para /api/internal/**
# Debe coincidir con internal.security.service-key en MS1 si MS1 permite a MS3.
ms1.internal-service-key-header=X-Internal-Service-Key
ms1.internal-service-key=local-ms2-to-ms1-internal-key-change-me

# Validaciones funcionales del JWT emitido por MS1.
ms1.jwt-issuer=http://localhost:8080
ms1.required-audiences[0]=ms-catalogo-inventario
ms1.required-audiences[1]=api-gateway
ms1.access-token-type=access
ms1.require-user-id-claim=true
ms1.require-session-claim=true
ms1.require-role-claim=true
ms1.require-token-type-claim=true

# Claims esperados del JWT.
ms1.claims.user-id=id_usuario_ms1
ms1.claims.session-id=sid
ms1.claims.token-type=typ
ms1.claims.roles=roles
ms1.claims.authorities=authorities
ms1.claims.role=rol

# =========================================================
# MS2 INTEGRATION
# MS3 consume snapshots o datos minimos de empleados desde MS2.
# MS3 no administra personas, clientes ni empleados oficiales.
# =========================================================
app.ms2.enabled=true

ms2.enabled=true
ms2.base-url=http://localhost:8082
ms2.timeout=5s

# Endpoints internos sugeridos para consultar empleado/contexto funcional.
ms2.empleado-snapshot-path=/api/internal/empleados/snapshot
ms2.empleado-by-usuario-path=/api/internal/empleados/by-usuario

# Seguridad interna MS3 -> MS2.
ms2.internal-service-key-header=X-Internal-Service-Key
ms2.internal-service-key=local-ms3-to-ms2-internal-key-change-me

# =========================================================
# MS4 INTEGRATION
# MS3 y MS4 pueden actuar como productores segun contexto.
# MS4 usa snapshots de MS3 para poder vender incluso si MS3 cae.
# Kafka es el flujo principal; HTTP queda como apoyo tecnico/controlado.
# =========================================================
app.ms4.enabled=true

ms4.enabled=true
ms4.base-url=http://localhost:8084
ms4.timeout=5s

# Endpoints internos sugeridos de reconciliacion HTTP opcional.
ms4.stock-sync-path=/api/internal/ms4/stock-sync
ms4.pending-stock-events-path=/api/internal/ms4/stock-events/pending

# Seguridad interna MS3 -> MS4 y MS4 -> MS3.
ms4.internal-service-key-header=X-Internal-Service-Key
ms4.internal-service-key=local-ms3-ms4-internal-key-change-me

# Seguridad interna propia de MS3 para endpoints /api/internal/**
internal.security.enabled=true
internal.security.header-name=X-Internal-Service-Key
internal.security.service-key=local-ms3-ms4-internal-key-change-me

# =========================================================
# CLOUDINARY
# MS3 solo guarda metadata en SQL Server.
# Los binarios se suben a Cloudinary.
# =========================================================
cloudinary.enabled=true
cloudinary.cloud-name=bj-sport-local-cloud
cloudinary.api-key=bj-sport-local-api-key
cloudinary.api-secret=bj-sport-local-api-secret
cloudinary.secure=true
cloudinary.folder-root=ms3/catalogo-inventario
cloudinary.product-folder=productos
cloudinary.sku-folder=sku
cloudinary.max-file-size-bytes=10485760
cloudinary.allowed-content-types[0]=image/jpeg
cloudinary.allowed-content-types[1]=image/png
cloudinary.allowed-content-types[2]=image/webp
cloudinary.allowed-content-types[3]=image/jpg
cloudinary.default-resource-type=image
cloudinary.invalidate-on-delete=true
cloudinary.upload-timeout=15s

# =========================================================
# KAFKA - MS3
# MS3 produce snapshots hacia MS4.
# MS3 consume comandos de stock desde MS4.
# Kafka local corre en Docker Desktop: kafka-local -> localhost:9092
# =========================================================
app.kafka.enabled=true

spring.kafka.bootstrap-servers=localhost:9092

# Kafka Admin.
spring.kafka.admin.auto-create=true
spring.kafka.admin.fail-fast=false

# Producer confiable para Outbox.
spring.kafka.producer.client-id=ms3-outbox-producer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.max.in.flight.requests.per.connection=5
spring.kafka.producer.properties.delivery.timeout.ms=120000
spring.kafka.producer.properties.request.timeout.ms=30000
spring.kafka.producer.properties.linger.ms=5
spring.kafka.producer.properties.batch.size=16384
spring.kafka.producer.properties.retry.backoff.ms=1000
spring.kafka.producer.properties.compression.type=none

# Consumer para comandos/eventos de MS4.
spring.kafka.consumer.client-id=ms3-stock-command-consumer-client
spring.kafka.consumer.group-id=ms3-stock-command-consumer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.properties.max.poll.records=25

spring.kafka.listener.ack-mode=manual
spring.kafka.listener.missing-topics-fatal=false

# Topics producidos por MS3 hacia MS4.
app.kafka.topics.producto-snapshot=ms3.producto.snapshot.v1
app.kafka.topics.precio-snapshot=ms3.precio.snapshot.v1
app.kafka.topics.promocion-snapshot=ms3.promocion.snapshot.v1
app.kafka.topics.stock-snapshot=ms3.stock.snapshot.v1
app.kafka.topics.movimiento-inventario=ms3.movimiento-inventario.v1

# Topics consumidos desde MS4.
app.kafka.topics.ms4-stock-command=ms4.stock.command.v1
app.kafka.topics.ms4-stock-reconciliation=ms4.stock.reconciliation.v1

# Topic tecnico.
app.kafka.topics.dead-letter=ms3.dead-letter.v1

# =========================================================
# OUTBOX KAFKA
# Patron Outbox para no perder eventos Kafka.
# El service de negocio guarda el cambio + evento pendiente en la misma transaccion.
# El scheduler publica despues.
# =========================================================
outbox.enabled=true
outbox.batch-size=25
outbox.max-attempts=5
outbox.fixed-delay=10s
outbox.lock-timeout=30s
outbox.publish-timeout=10s
outbox.publisher-id=ms3-local-outbox-publisher
outbox.retry-errors=true
outbox.delete-published=false

# =========================================================
# KAFKA PROBE - MS3 <-> MS4
# Prueba tecnica de comunicacion Kafka sin BD.
# No usa Outbox, no usa repositories, no ejecuta reglas de negocio.
# =========================================================
app.kafka.probe.enabled=true
app.kafka.probe.run-on-startup=true
app.kafka.probe.initial-delay-ms=10000
app.kafka.probe.retry-delay-ms=10000
app.kafka.probe.max-attempts=6
app.kafka.probe.fail-on-timeout=false
app.kafka.probe.send-timeout-ms=10000
app.kafka.probe.consumer-group=ms3-probe-consumer
app.kafka.probe.service-name=ms-catalogo-inventario
app.kafka.probe.target-ms4=ms-ventas-facturacion

# MS3 emite probe hacia MS4.
app.kafka.probe.topics.ms3-to-ms4=dev.ms3.ms4.probe.v1

# MS4 responde ACK hacia MS3.
app.kafka.probe.topics.ms4-to-ms3-ack=dev.ms4.ms3.probe-ack.v1

# MS4 emite probe hacia MS3.
app.kafka.probe.topics.ms4-to-ms3=dev.ms4.ms3.probe.v1

# MS3 responde ACK hacia MS4.
app.kafka.probe.topics.ms3-to-ms4-ack=dev.ms3.ms4.probe-ack.v1

# =========================================================
# INVENTARIO / STOCK
# =========================================================
app.inventory.default-reservation-minutes=15
app.inventory.allow-negative-stock=false
app.inventory.low-stock-enabled=true
app.inventory.kardex-required=true
app.inventory.require-movement-reason=true
app.inventory.allow-manual-stock-update=false

# =========================================================
# PRODUCTO / CATALOGO
# =========================================================
app.catalog.auto-generate-product-code=true
app.catalog.auto-generate-sku-code=true
app.catalog.auto-generate-slug=true
app.catalog.require-main-image-to-publish=true
app.catalog.require-active-sku-to-publish=true
app.catalog.require-current-price-to-publish=true
app.catalog.public-show-programmed-products=true
app.catalog.programmed-products-selectable=false

# =========================================================
# PROMOCIONES / PRECIOS
# =========================================================
app.pricing.default-currency=PEN
app.pricing.price-history-required=true
app.pricing.require-price-change-reason=true

app.promotion.require-date-range=true
app.promotion.allow-global-discount=false
app.promotion.discount-per-sku-required=true
app.promotion.allow-negative-margin=false

# =========================================================
# PERMISOS EMPLEADO INVENTARIO
# El admin puede habilitar al empleado para apoyar en catalogo/inventario.
# No reemplaza roles globales de MS1.
# =========================================================
app.employee-inventory-permissions.enabled=true
app.employee-inventory-permissions.require-active-employee=true
app.employee-inventory-permissions.versioned=true

# =========================================================
# AUDITORIA / PAGINACION
# =========================================================
app.audit.enabled=true
app.audit.include-request-metadata=true
app.audit.include-response-summary=false
app.audit.mask-sensitive-data=true

app.pagination.default-size=20
app.pagination.max-size=100
app.pagination.default-sort-direction=DESC

# =========================================================
# CACHE
# =========================================================
spring.cache.type=caffeine

app.cache.names[0]=reference-data
app.cache.names[1]=catalogo-lookup
app.cache.names[2]=producto-publico
app.cache.names[3]=categoria-tree
app.cache.names[4]=marca-lookup
app.cache.names[5]=tipo-producto-lookup
app.cache.names[6]=empleado-inventario-permiso
app.cache.expire-after-write-minutes=10
app.cache.maximum-size=10000
app.cache.record-stats=true
app.cache.allow-null-values=false

# =========================================================
# CORS
# Idealmente CORS se centraliza en Gateway.
# MS3 conserva CORS solo para pruebas tecnicas directas.
# =========================================================
app.cors.allowed-origins[0]=http://localhost:4200
app.cors.allowed-origins[1]=http://127.0.0.1:4200
app.cors.allowed-origins[2]=http://localhost:8080
app.cors.allowed-methods[0]=GET
app.cors.allowed-methods[1]=POST
app.cors.allowed-methods[2]=PUT
app.cors.allowed-methods[3]=PATCH
app.cors.allowed-methods[4]=DELETE
app.cors.allowed-methods[5]=OPTIONS
app.cors.allowed-headers[0]=Authorization
app.cors.allowed-headers[1]=Content-Type
app.cors.allowed-headers[2]=Accept
app.cors.allowed-headers[3]=Origin
app.cors.allowed-headers[4]=X-Requested-With
app.cors.allowed-headers[5]=X-Request-Id
app.cors.allowed-headers[6]=X-Correlation-Id
app.cors.allowed-headers[7]=X-Forwarded-For
app.cors.allowed-headers[8]=X-Forwarded-Proto
app.cors.allowed-headers[9]=X-Forwarded-Host
app.cors.allowed-headers[10]=X-Forwarded-Port
app.cors.allowed-headers[11]=X-Real-IP
app.cors.allowed-headers[12]=X-Gateway-Source
app.cors.allowed-headers[13]=X-Internal-Service-Key
app.cors.exposed-headers[0]=Authorization
app.cors.exposed-headers[1]=X-Request-Id
app.cors.exposed-headers[2]=X-Correlation-Id
app.cors.allow-credentials=false
app.cors.max-age-seconds=3600
app.cors.path-pattern=/**

# =========================================================
# OPENAPI / SWAGGER
# Cada microservicio expone su propio contrato OpenAPI.
# En local queda habilitado.
# =========================================================
app.open-api.title=MS Catalogo Inventario API
app.open-api.description=API local del microservicio de catalogo, productos, SKU, precios, promociones, inventario, stock, kardex, Cloudinary y eventos Outbox Kafka.
app.open-api.contact-name=Equipo MS3
app.open-api.contact-email=tresdynx@gmail.com
app.open-api.contact-url=http://localhost:4200

app.open-api.servers[0].url=http://localhost:8080
app.open-api.servers[0].description=API Gateway local
app.open-api.servers[1].url=http://localhost:8083
app.open-api.servers[1].description=MS3 directo local solo para pruebas tecnicas

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha

# =========================================================
# JPA AUDIT
# =========================================================
app.jpa.system-auditor=SYSTEM
app.jpa.auditor-max-length=120

# =========================================================
# ACTUATOR
# =========================================================
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when_authorized
management.endpoint.health.probes.enabled=true
management.health.mail.enabled=false

# =========================================================
# LOGGING LOCAL
# =========================================================
logging.level.root=INFO
logging.level.com.upsjb.ms3=INFO
logging.level.org.springframework.security=INFO
logging.level.org.springframework.security.oauth2=INFO
logging.level.org.springframework.web=INFO
logging.level.org.springframework.kafka=INFO
logging.level.org.apache.kafka=INFO
logging.level.org.hibernate.SQL=OFF
logging.level.org.hibernate.orm.jdbc.bind=OFF
logging.level.org.springframework.transaction=INFO
logging.level.org.springframework.orm.jpa=INFO
```

---

# 3. MS4 — `src/main/resources/application.properties`

```properties
# =========================================================
# MS4 - VENTAS FACTURACION - LOCAL DETRAS DEL API GATEWAY
# =========================================================
spring.application.name=ms-ventas-facturacion
server.port=8084

# Necesario para operar correctamente detras del API Gateway.
server.forward-headers-strategy=framework

# =========================================================
# SQL SERVER LOCAL
# =========================================================
spring.datasource.url=jdbc:sqlserver://192.168.18.7:1433;databaseName=bd_ms4;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=admin
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# =========================================================
# JPA / HIBERNATE
# En local usamos update para avanzar rapido.
# En produccion deberia ser validate + Flyway habilitado.
# =========================================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.open-in-view=false

spring.flyway.enabled=false
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# =========================================================
# APP
# =========================================================
app.name=MS Ventas Facturacion
app.version=1.0.0
app.description=Microservicio de ventas, pagos, boletas, caja, reportes, auditoria funcional, snapshots MS2/MS3, Stripe Sandbox, correo outbox y comandos de stock hacia MS3 por Kafka.
app.environment=local
app.gateway-url=http://localhost:8080
app.frontend-url=http://localhost:4200
app.locale=es-PE
app.timezone=America/Lima
app.zone-id=America/Lima

# =========================================================
# MULTIPART - ASSETS VISUALES
# MS4 no sube PDFs de boleta.
# Solo assets visuales: logo, marca y recursos de plantilla.
# =========================================================
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB

# =========================================================
# SECURITY - RESOURCE SERVER JWT
# MS4 valida JWT emitidos por MS1.
# MS4 no emite tokens, no maneja login, no maneja refresh token.
# =========================================================
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks

# Alternativa si no usas JWKS:
# spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/ms1-public.pem

# =========================================================
# SECURITY - VALIDACION FUNCIONAL JWT MS4
# Debe coincidir con los audiences emitidos por MS1.
# En MS1 existe: security.jwt.audiences[4]=ms-ventas-facturacion
# =========================================================
ms4.security.jwt.enabled=true
ms4.security.jwt.required-issuer=http://localhost:8080
ms4.security.jwt.required-audience=ms-ventas-facturacion
ms4.security.jwt.require-email=true
ms4.security.jwt.require-token-type=true
ms4.security.jwt.allowed-roles[0]=ADMIN
ms4.security.jwt.allowed-roles[1]=EMPLEADO
ms4.security.jwt.allowed-roles[2]=CLIENTE
ms4.security.jwt.accepted-token-types[0]=access
ms4.security.jwt.accepted-token-types[1]=access_token
ms4.security.jwt.accepted-token-types[2]=bearer

# =========================================================
# INTERNAL SECURITY - MS3 <-> MS4
# Protege endpoints internos: /api/internal/ms4/**
# Esta clave debe coincidir con ms4.internal-service-key en MS3.
# =========================================================
ms4.security.internal.enabled=true
ms4.security.internal.header-name=X-Internal-Service-Key
ms4.security.internal.service-key=local-ms3-ms4-internal-key-change-me

# =========================================================
# MS1 INTEGRATION
# MS4 no autentica usuarios. Solo puede consultar soporte interno si existe contrato.
# El codigo actual usa la misma ms4.security.internal.service-key para clientes internos.
# =========================================================
ms4.integration.ms1.base-url=http://localhost:8081
ms4.integration.ms1.admin-contacts-path=/api/internal/ms1/admins/contactos

# =========================================================
# MS3 INTEGRATION HTTP INTERNA
# Kafka es el flujo principal.
# HTTP queda como apoyo tecnico/controlado para reconciliacion.
# =========================================================
ms4.integration.ms3.base-url=http://localhost:8083
ms4.integration.ms3.pending-events-path=/api/internal/ms3/stock-sync/ms4-events
ms4.integration.ms3.reconcile-path=/api/internal/ms3/stock-sync/reconcile/{idempotencyKey}

# =========================================================
# STRIPE SANDBOX
# MS4 usa Stripe solamente en Sandbox/Test.
# No usar sk_live_* ni pk_live_*.
# =========================================================
stripe.enabled=true
stripe.mode=sandbox
stripe.secret-key=sk_test_bj_sport_local_secret_key_change_me
stripe.publishable-key=pk_test_bj_sport_local_publishable_key_change_me
stripe.webhook-secret=whsec_bj_sport_local_webhook_secret_change_me
stripe.currency=pen

# =========================================================
# CLOUDINARY
# MS4 solo guarda assets visuales.
# Los PDFs de boleta se generan en vivo y no se almacenan.
# Se usan los mismos secretos locales/revocados compartidos en MS3.
# =========================================================
cloudinary.enabled=true
cloudinary.cloud-name=bj-sport-local-cloud
cloudinary.api-key=bj-sport-local-api-key
cloudinary.api-secret=bj-sport-local-api-secret
cloudinary.folder-base=ms4/assets

# =========================================================
# MAIL / SMTP
# MS4 envia boletas por correo mediante correo_outbox.
# Se usan los mismos secretos locales/revocados de MS1/MS2.
# =========================================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tresdynx@gmail.com
spring.mail.password=unoy evse kuws vdrn
spring.mail.protocol=smtp
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=8000
spring.mail.properties.mail.smtp.writetimeout=8000
spring.mail.properties.mail.debug=false

# =========================================================
# CORREO OUTBOX
# El envio de boletas no debe ejecutarse directo desde VentaService.
# Debe registrarse pendiente y procesarse por scheduler/processor.
# =========================================================
ms4.correo-outbox.enabled=true
ms4.correo-outbox.batch-size=25
ms4.correo-outbox.max-attempts=5
ms4.correo-outbox.lock-ttl=30s
ms4.correo-outbox.processor-id=ms4-local-correo-processor
ms4.correo-outbox.default-from=tresdynx@gmail.com
ms4.correo-outbox.admin-alert-email=tresdynx@gmail.com

# =========================================================
# KAFKA - MS4
# MS4 consume snapshots de MS2/MS3.
# MS4 produce comandos de stock hacia MS3.
# Kafka local corre en Docker Desktop: kafka-local -> localhost:9092
# =========================================================
spring.kafka.bootstrap-servers=localhost:9092

# Kafka Admin.
spring.kafka.admin.auto-create=true
spring.kafka.admin.fail-fast=false

# Consumer snapshots/eventos.
spring.kafka.consumer.client-id=ms4-snapshot-consumer-client
spring.kafka.consumer.group-id=ms4-snapshot-consumer
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.properties.max.poll.records=25

spring.kafka.listener.ack-mode=manual
spring.kafka.listener.missing-topics-fatal=false

# Producer confiable para Outbox.
spring.kafka.producer.client-id=ms4-outbox-producer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.max.in.flight.requests.per.connection=5
spring.kafka.producer.properties.delivery.timeout.ms=120000
spring.kafka.producer.properties.request.timeout.ms=30000
spring.kafka.producer.properties.linger.ms=5
spring.kafka.producer.properties.batch.size=16384
spring.kafka.producer.properties.retry.backoff.ms=1000
spring.kafka.producer.properties.compression.type=none

# =========================================================
# TOPICS KAFKA CONSUMIDOS DESDE MS2
# =========================================================
ms4.kafka.topics.ms2-cliente-snapshot=ms2.cliente.snapshot.v1
ms4.kafka.topics.ms2-empleado-snapshot=ms2.empleado.snapshot.v1

# =========================================================
# TOPICS KAFKA CONSUMIDOS DESDE MS3
# =========================================================
ms4.kafka.topics.ms3-producto-snapshot=ms3.producto.snapshot.v1
ms4.kafka.topics.ms3-precio-snapshot=ms3.precio.snapshot.v1
ms4.kafka.topics.ms3-promocion-snapshot=ms3.promocion.snapshot.v1
ms4.kafka.topics.ms3-stock-snapshot=ms3.stock.snapshot.v1
ms4.kafka.topics.ms3-movimiento-inventario=ms3.movimiento-inventario.v1

# =========================================================
# TOPICS KAFKA PRODUCIDOS POR MS4 HACIA MS3
# =========================================================
ms4.kafka.topics.ms4-stock-command=ms4.stock.command.v1
ms4.kafka.topics.ms4-stock-reconciliation=ms4.stock.reconciliation.v1

# Topic tecnico.
ms4.kafka.topics.dead-letter=ms4.dead-letter.v1

# =========================================================
# OUTBOX KAFKA
# El service de negocio guarda cambio + evento pendiente en la misma transaccion.
# El scheduler publica despues.
# =========================================================
ms4.outbox.enabled=true
ms4.outbox.batch-size=25
ms4.outbox.max-attempts=5
ms4.outbox.lock-ttl=30s
ms4.outbox.publish-timeout=10s
ms4.outbox.fixed-delay-ms=5000
ms4.outbox.publisher-id=ms4-local-outbox-publisher

# =========================================================
# KAFKA PROBE - MS4 <-> MS2 / MS3
# Prueba tecnica de comunicacion Kafka sin BD.
# No usa Outbox, no usa repositories, no ejecuta reglas de negocio.
# =========================================================
app.kafka.probe.enabled=true
app.kafka.probe.run-on-startup=true
app.kafka.probe.initial-delay-ms=12000
app.kafka.probe.retry-delay-ms=10000
app.kafka.probe.max-attempts=6
app.kafka.probe.fail-on-timeout=false
app.kafka.probe.send-timeout-ms=10000
app.kafka.probe.consumer-group=ms4-probe-consumer
app.kafka.probe.service-name=ms-ventas-facturacion
app.kafka.probe.target-ms2=ms-personas-clientes-empleados
app.kafka.probe.target-ms3=ms-catalogo-inventario

# MS2 emite probe hacia MS4.
app.kafka.probe.topics.ms2-to-ms4=dev.ms2.ms4.probe.v1

# MS4 responde ACK hacia MS2.
app.kafka.probe.topics.ms4-to-ms2-ack=dev.ms4.ms2.probe-ack.v1

# MS3 emite probe hacia MS4.
app.kafka.probe.topics.ms3-to-ms4=dev.ms3.ms4.probe.v1

# MS4 responde ACK hacia MS3.
app.kafka.probe.topics.ms4-to-ms3-ack=dev.ms4.ms3.probe-ack.v1

# MS4 emite probe hacia MS3.
app.kafka.probe.topics.ms4-to-ms3=dev.ms4.ms3.probe.v1

# MS3 responde ACK hacia MS4.
app.kafka.probe.topics.ms3-to-ms4-ack=dev.ms3.ms4.probe-ack.v1

# =========================================================
# VENTAS
# MS4 trabaja con venta directa.
# No carrito, no checkout como dominio oficial en esta version.
# =========================================================
app.sales.enabled=true
app.sales.currency=PEN
app.sales.default-online-channel=ONLINE
app.sales.default-physical-channel=TIENDA
app.sales.require-client=true
app.sales.freeze-price-promotion-tax=true
app.sales.confirm-online-sale-only-by-valid-stripe-webhook=true
app.sales.physical-sale-requires-open-cashbox=true
app.sales.allow-negative-total=false
app.sales.allow-zero-total=false

# =========================================================
# STOCK / INVENTARIO MS4 -> MS3
# MS4 no modifica stock oficial directamente.
# Publica comandos hacia MS3 mediante Outbox.
# =========================================================
app.stock-command.enabled=true
app.stock-command.reserve-online-sale=true
app.stock-command.confirm-after-payment=true
app.stock-command.release-on-cancel=true
app.stock-command.reconcile-with-ms3=true
app.stock-command.idempotency-required=true

# =========================================================
# BOLETA
# MS4 implementa BOLETA operativa.
# No factura, no nota de credito, no nota de debito, no SUNAT/OSE/XML.
# PDF se genera en vivo con Thymeleaf/OpenHTMLToPDF.
# =========================================================
app.boleta.enabled=true
app.boleta.tipo-comprobante=BOLETA
app.boleta.default-serie=B001
app.boleta.default-moneda=PEN
app.boleta.pdf-generated-live=true
app.boleta.persist-pdf=false
app.boleta.upload-pdf-to-cloudinary=false
app.boleta.template-html=boleta/boleta
app.boleta.mail-template-html=mail/boleta-compra
app.boleta.allow-email-resend=true

# =========================================================
# CAJA
# =========================================================
app.cashbox.enabled=true
app.cashbox.require-open-for-physical-sale=true
app.cashbox.default-currency=PEN
app.cashbox.allow-one-open-cashbox-per-employee=true
app.cashbox.require-closing-balance=true
app.cashbox.allow-adjustments=true

# =========================================================
# REPORTES
# =========================================================
app.reports.enabled=true
app.reports.default-currency=PEN
app.reports.employee-only-own-sales=true
app.reports.admin-global-reports=true

# =========================================================
# CONTINGENCIA
# MS4 puede operar con snapshots locales si MS2/MS3 estan caidos.
# La reconciliacion posterior con MS3 debe ser controlada.
# =========================================================
app.contingency.enabled=true
app.contingency.requires-admin-approval=true
app.contingency.stock-reconciliation-required=true
app.contingency.audit-required=true

# =========================================================
# CONFIGURACION EMPRESA / TRIBUTARIA LOCAL
# Estos valores iniciales pueden servir para seed/config local.
# La gestion oficial queda en entidades/configuracion MS4.
# =========================================================
app.company.default-ruc=00000000000
app.company.default-razon-social=BJ SPORT LOCAL
app.company.default-nombre-comercial=BJ SPORT
app.company.default-direccion-fiscal=Lima, Peru
app.company.default-correo=tresdynx@gmail.com
app.company.default-telefono=999999999

app.tax.default-igv-percent=18.00
app.tax.default-moneda=PEN
app.tax.default-afecta-igv=true

# =========================================================
# AUDITORIA / PAGINACION
# =========================================================
app.audit.enabled=true
app.audit.include-request-metadata=true
app.audit.include-response-summary=false
app.audit.mask-sensitive-data=true

app.pagination.default-size=20
app.pagination.max-size=100
app.pagination.default-sort-direction=DESC

# =========================================================
# CACHE
# =========================================================
spring.cache.type=caffeine

app.cache.names[0]=cliente-snapshot
app.cache.names[1]=empleado-snapshot
app.cache.names[2]=producto-venta
app.cache.names[3]=sku-venta
app.cache.names[4]=precio-vigente
app.cache.names[5]=promocion-vigente
app.cache.names[6]=stock-venta
app.cache.names[7]=configuracion-empresa
app.cache.names[8]=configuracion-tributaria
app.cache.names[9]=serie-boleta
app.cache.expire-after-write-minutes=10
app.cache.maximum-size=10000
app.cache.record-stats=true
app.cache.allow-null-values=false

# =========================================================
# CORS
# Idealmente CORS se centraliza en Gateway.
# MS4 conserva CORS solo para pruebas tecnicas directas.
# =========================================================
ms4.cors.enabled=true
ms4.cors.allowed-origins[0]=http://localhost:4200
ms4.cors.allowed-origins[1]=http://127.0.0.1:4200
ms4.cors.allowed-origins[2]=http://localhost:8080
ms4.cors.allowed-methods[0]=GET
ms4.cors.allowed-methods[1]=POST
ms4.cors.allowed-methods[2]=PUT
ms4.cors.allowed-methods[3]=PATCH
ms4.cors.allowed-methods[4]=DELETE
ms4.cors.allowed-methods[5]=OPTIONS
ms4.cors.allowed-headers[0]=Authorization
ms4.cors.allowed-headers[1]=Content-Type
ms4.cors.allowed-headers[2]=Accept
ms4.cors.allowed-headers[3]=Origin
ms4.cors.allowed-headers[4]=X-Requested-With
ms4.cors.allowed-headers[5]=X-Request-Id
ms4.cors.allowed-headers[6]=X-Correlation-Id
ms4.cors.allowed-headers[7]=X-Forwarded-For
ms4.cors.allowed-headers[8]=X-Forwarded-Proto
ms4.cors.allowed-headers[9]=X-Forwarded-Host
ms4.cors.allowed-headers[10]=X-Forwarded-Port
ms4.cors.allowed-headers[11]=X-Real-IP
ms4.cors.allowed-headers[12]=X-Gateway-Source
ms4.cors.allowed-headers[13]=X-Internal-Service-Key
ms4.cors.allowed-headers[14]=Stripe-Signature
ms4.cors.exposed-headers[0]=Authorization
ms4.cors.exposed-headers[1]=X-Request-Id
ms4.cors.exposed-headers[2]=X-Correlation-Id
ms4.cors.exposed-headers[3]=Content-Disposition
ms4.cors.allow-credentials=false
ms4.cors.max-age=3600

# =========================================================
# OPENAPI / SWAGGER
# Cada microservicio expone su propio contrato OpenAPI.
# En local queda habilitado.
# =========================================================
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha

# =========================================================
# JPA AUDIT
# =========================================================
app.jpa.system-auditor=SYSTEM
app.jpa.auditor-max-length=120

# =========================================================
# ACTUATOR
# =========================================================
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when_authorized
management.endpoint.health.probes.enabled=true
management.health.mail.enabled=false

management.info.env.enabled=true
info.app.name=ms-ventas-facturacion
info.app.description=Microservicio de ventas, pagos, boletas, caja, snapshots y stock commands
info.app.version=1.0.0-local

management.metrics.tags.application=ms-ventas-facturacion

# =========================================================
# JACKSON
# =========================================================
spring.jackson.time-zone=America/Lima
spring.jackson.default-property-inclusion=non_null

# =========================================================
# LOGGING LOCAL
# =========================================================
logging.level.root=INFO
logging.level.com.upsjb.ms4=INFO
logging.level.org.springframework.security=INFO
logging.level.org.springframework.security.oauth2=INFO
logging.level.org.springframework.web=INFO
logging.level.org.springframework.kafka=INFO
logging.level.org.apache.kafka=INFO
logging.level.org.hibernate.SQL=OFF
logging.level.org.hibernate.orm.jdbc.bind=OFF
logging.level.org.springframework.transaction=INFO
logging.level.org.springframework.orm.jpa=INFO
logging.level.com.stripe=INFO
logging.level.com.cloudinary=INFO
```

Con esto los tres quedan alineados:

```text
MS2 -> Kafka real:
ms2.cliente.snapshot.v1
ms2.empleado.snapshot.v1

MS3 -> Kafka real:
ms3.producto.snapshot.v1
ms3.precio.snapshot.v1
ms3.promocion.snapshot.v1
ms3.stock.snapshot.v1
ms3.movimiento-inventario.v1

MS4 -> Kafka real:
ms4.stock.command.v1
ms4.stock.reconciliation.v1

Probe técnico:
dev.ms2.ms4.probe.v1
dev.ms4.ms2.probe-ack.v1
dev.ms3.ms4.probe.v1
dev.ms4.ms3.probe-ack.v1
dev.ms4.ms3.probe.v1
dev.ms3.ms4.probe-ack.v1
```
