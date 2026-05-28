Revisé el código unificado del MS3. El proyecto corresponde a `C:\Users\WinterOS\IdeaProjects\CalidadSoftwareMS3`, puerto `8083`, dominio catálogo/inventario, y ya tiene Springdoc/OpenAPI habilitado por properties. El punto a corregir es el mismo patrón que ocurrió con MS2: en `SecurityConfig` está liberado `/v3/api-docs/**`, pero conviene liberar también `/v3/api-docs`, `/v3/api-docs.yaml` y `/v3/api-docs.yml` para evitar problemas al generar documentación.

## 1. Reemplaza `SecurityConfig.java`

Ruta:

```txt
src/main/java/com/upsjb/ms3/security/config/SecurityConfig.java
```

Código completo actualizado:

```java
// ruta: src/main/java/com/upsjb/ms3/security/config/SecurityConfig.java
package com.upsjb.ms3.security.config;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.security.filter.InternalServiceAuthenticationFilter;
import com.upsjb.ms3.security.filter.RequestAuditContextFilter;
import com.upsjb.ms3.security.filter.RequestTraceFilter;
import com.upsjb.ms3.security.handler.RestAccessDeniedHandler;
import com.upsjb.ms3.security.handler.RestAuthenticationEntryPoint;
import com.upsjb.ms3.security.jwt.RoleJwtAuthenticationConverter;
import com.upsjb.ms3.security.principal.AuthenticatedUserArgumentResolver;
import com.upsjb.ms3.security.roles.SecurityRoles;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.yml"
    };

    private static final String[] ACTUATOR_PUBLIC_WHITELIST = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    };

    private final AppPropertiesConfig appProperties;
    private final RoleJwtAuthenticationConverter roleJwtAuthenticationConverter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RequestTraceFilter requestTraceFilter;
    private final RequestAuditContextFilter requestAuditContextFilter;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    /*
     * Cadena exclusiva para endpoints internos entre microservicios.
     *
     * Estos endpoints no deben depender de JWT de usuario ADMIN.
     * Se protegen con X-Internal-Service-Key y reciben ROLE_INTERNAL_SERVICE.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/internal/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAuthority(SecurityRoles.ROLE_INTERNAL_SERVICE)
                )
                .addFilterBefore(requestTraceFilter, SecurityContextHolderFilter.class)
                .addFilterBefore(internalServiceAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(requestAuditContextFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    /*
     * Cadena principal para rutas públicas, administrativas y operativas del MS3.
     *
     * MS3 valida JWT emitidos por MS1 aunque el Gateway también valide.
     * El Gateway filtra entrada general; MS3 conserva seguridad propia.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /*
                         * Swagger/OpenAPI queda libre únicamente para documentación técnica local.
                         *
                         * Rutas liberadas:
                         * - /swagger-ui.html
                         * - /swagger-ui/**
                         * - /v3/api-docs
                         * - /v3/api-docs/**
                         * - /v3/api-docs.yaml
                         * - /v3/api-docs.yml
                         *
                         * No libera endpoints funcionales del dominio.
                         */
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/ms3/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/catalogo/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/productos/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/promociones/public/**").permitAll()

                        .requestMatchers(ACTUATOR_PUBLIC_WHITELIST).permitAll()

                        .requestMatchers("/api/ms3/admin/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/outbox/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/auditoria/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/catalogo/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_EMPLEADO)

                        .requestMatchers("/api/ms3/inventario/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_EMPLEADO)

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(roleJwtAuthenticationConverter))
                )
                .addFilterBefore(requestTraceFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(requestAuditContextFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        AppPropertiesConfig.Cors corsProperties = appProperties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPathPattern(), configuration);

        return source;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }
}
```

Después reinicia MS3:

```powershell
cd C:\Users\WinterOS\IdeaProjects\CalidadSoftwareMS3
.\mvnw.cmd spring-boot:run
```

Prueba que OpenAPI esté libre:

```powershell
curl.exe -i -H "Accept: application/json" http://localhost:8083/v3/api-docs
```

Debe responder `HTTP/1.1 200`.

---

## 2. Generar documentación única `.md` para MS3

Con MS3 levantado en otra ventana, pega este comando completo en PowerShell. El archivo final quedará en:

```txt
C:\Users\WinterOS\Desktop\MS\ms3-frontend-context.md
```

Comando completo:

```powershell
Set-Location "C:\Users\WinterOS\IdeaProjects\CalidadSoftwareMS3"

$ScriptPath = "C:\Users\WinterOS\Desktop\MS\generar-ms3-contexto-frontend.ps1"

New-Item -ItemType Directory -Force -Path "C:\Users\WinterOS\Desktop\MS" | Out-Null

@'
$ErrorActionPreference = "Stop"

$ProjectRoot = "C:\Users\WinterOS\IdeaProjects\CalidadSoftwareMS3"
$OutputFile = "C:\Users\WinterOS\Desktop\MS\ms3-frontend-context.md"
$OpenApiUrl = "http://localhost:8083/v3/api-docs"

if (-not (Test-Path (Join-Path $ProjectRoot "pom.xml"))) {
    throw "No existe pom.xml en $ProjectRoot. Verifica la ruta del proyecto MS3."
}

New-Item -ItemType Directory -Force -Path (Split-Path $OutputFile) | Out-Null

function Add-Line {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$Line = ""
    )

    [void]$Builder.AppendLine($Line)
}

function Add-Section {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$Title
    )

    Add-Line $Builder ""
    Add-Line $Builder "## $Title"
    Add-Line $Builder ""
}

function Sanitize-Content {
    param(
        [string]$Content
    )

    if ($null -eq $Content) {
        return ""
    }

    $sanitizedLines = New-Object System.Collections.Generic.List[string]

    foreach ($line in ($Content -split "`r?`n")) {
        if ($line -match "(?i)(password|passwd|pwd|secret|token|private[-_.]?key|api[-_.]?key|internal[-_.]?service[-_.]?key|cloudinary.*api[-_.]?secret|cloudinary.*api[-_.]?key)\s*[:=]") {
            $parts = $line -split "[:=]", 2

            if ($parts.Count -eq 2) {
                $sanitizedLines.Add($parts[0] + "=***MASKED***")
            } else {
                $sanitizedLines.Add("***MASKED***")
            }
        } else {
            $sanitizedLines.Add($line)
        }
    }

    return ($sanitizedLines -join "`r`n")
}

function Add-CodeFile {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$FilePath,
        [string]$Language = "java",
        [bool]$Sanitize = $false
    )

    if (-not (Test-Path $FilePath)) {
        return
    }

    $relativePath = $FilePath.Replace($ProjectRoot + "\", "")
    $content = Get-Content -Path $FilePath -Raw -Encoding UTF8

    if ($Sanitize) {
        $content = Sanitize-Content $content
    }

    Add-Line $Builder ""
    Add-Line $Builder "### $relativePath"
    Add-Line $Builder ""
    Add-Line $Builder ("~~~" + $Language)
    Add-Line $Builder $content
    Add-Line $Builder "~~~"
}

$SelectedFiles = New-Object System.Collections.Generic.List[string]

$DirectoriesToExtract = @(
    "src\main\java\com\upsjb\ms3\controller",
    "src\main\java\com\upsjb\ms3\dto",
    "src\main\java\com\upsjb\ms3\domain\enums",
    "src\main\java\com\upsjb\ms3\domain\value",
    "src\main\java\com\upsjb\ms3\service\contract",
    "src\main\java\com\upsjb\ms3\security\config",
    "src\main\java\com\upsjb\ms3\security\principal",
    "src\main\java\com\upsjb\ms3\security\roles",
    "src\main\java\com\upsjb\ms3\security\handler",
    "src\main\java\com\upsjb\ms3\security\jwt",
    "src\main\java\com\upsjb\ms3\config",
    "src\main\java\com\upsjb\ms3\shared\constants",
    "src\main\java\com\upsjb\ms3\shared\response",
    "src\main\java\com\upsjb\ms3\shared\exception",
    "src\main\java\com\upsjb\ms3\exception",
    "src\main\java\com\upsjb\ms3\validator",
    "src\main\java\com\upsjb\ms3\validation",
    "src\main\java\com\upsjb\ms3\policy",
    "src\main\java\com\upsjb\ms3\mapper",
    "src\main\java\com\upsjb\ms3\kafka\event"
)

foreach ($dir in $DirectoriesToExtract) {
    $fullDir = Join-Path $ProjectRoot $dir

    if (Test-Path $fullDir) {
        Get-ChildItem -Path $fullDir -Recurse -File -Filter "*.java" | ForEach-Object {
            $SelectedFiles.Add($_.FullName)
        }
    }
}

$SelectedFiles = $SelectedFiles | Sort-Object -Unique

try {
    $OpenApiResponse = Invoke-WebRequest `
        -Uri $OpenApiUrl `
        -UseBasicParsing `
        -TimeoutSec 30 `
        -Headers @{ Accept = "application/json" }

    $OpenApiRaw = $OpenApiResponse.Content

    if ($OpenApiRaw -notmatch '"openapi"') {
        throw "La respuesta no parece ser un contrato OpenAPI valido."
    }

    try {
        $OpenApiContent = ($OpenApiRaw | ConvertFrom-Json | ConvertTo-Json -Depth 100)
    } catch {
        $OpenApiContent = $OpenApiRaw
    }
} catch {
    throw "No se pudo descargar OpenAPI desde $OpenApiUrl. Verifica que MS3 este levantado, que /v3/api-docs responda 200 y que Swagger este liberado en SecurityConfig."
}

$Builder = New-Object System.Text.StringBuilder

Add-Line $Builder "# MS3 - Contrato oficial de integración frontend"
Add-Line $Builder ""
Add-Line $Builder "Cumple estrictamente este contrato. Programa el frontend Angular consumiendo únicamente las rutas, DTOs, parámetros, validaciones y respuestas declaradas aquí."
Add-Line $Builder ""
Add-Line $Builder "No inventes endpoints."
Add-Line $Builder "No inventes campos."
Add-Line $Builder "No inventes payloads."
Add-Line $Builder "No consumas rutas que no estén en el contrato OpenAPI."
Add-Line $Builder "No envíes parámetros internos del backend."
Add-Line $Builder "No consumas endpoints internos desde Angular."
Add-Line $Builder ""
Add-Line $Builder "Proyecto backend origen:"
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder $ProjectRoot
Add-Line $Builder "~~~"

Add-Section $Builder "Reglas obligatorias de implementación"

Add-Line $Builder "1. Implementa Angular consumiendo MS3 mediante API Gateway."
Add-Line $Builder "2. Usa rutas relativas en desarrollo."
Add-Line $Builder "3. Usa el contrato OpenAPI como fuente principal para endpoints, DTOs, schemas, parámetros y validaciones."
Add-Line $Builder "4. Usa las clases backend incluidas como fuente complementaria para reglas que OpenAPI no expresa completamente."
Add-Line $Builder "5. No generes servicios Angular para endpoints inexistentes."
Add-Line $Builder "6. No agregues propiedades TypeScript que no existan en los schemas o DTOs incluidos."
Add-Line $Builder "7. No mandes `actor` desde Angular si aparece en OpenAPI."
Add-Line $Builder "8. No llames directamente al puerto interno de MS3 desde Angular."
Add-Line $Builder "9. No llames `/api/internal/**` desde Angular."
Add-Line $Builder "10. No envíes `X-Internal-Service-Key` desde Angular."
Add-Line $Builder "11. Usa interceptores para JWT."
Add-Line $Builder "12. Aplica guards por rol."
Add-Line $Builder "13. Maneja errores HTTP de forma centralizada."
Add-Line $Builder "14. Usa formularios reactivos con validaciones equivalentes a las del backend."
Add-Line $Builder "15. Usa paginación solo cuando el endpoint reciba `PageRequestDto` o devuelva `PageResponseDto`."
Add-Line $Builder "16. Usa enums exactamente como están declarados."
Add-Line $Builder "17. No modifiques stock desde pantallas consultivas."
Add-Line $Builder "18. No expongas costos en pantallas públicas."
Add-Line $Builder "19. No expongas Cloudinary API key, API secret ni configuración sensible."
Add-Line $Builder "20. No consumas Kafka ni Outbox desde el frontend salvo endpoints administrativos documentados."

Add-Section $Builder "Arquitectura de consumo"

Add-Line $Builder "El frontend Angular es monolito y consume el backend por API Gateway."
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "Angular"
Add-Line $Builder "  -> API Gateway http://localhost:8080"
Add-Line $Builder "      -> MS3 http://localhost:8083"
Add-Line $Builder "~~~"
Add-Line $Builder ""
Add-Line $Builder "En Angular usa:"
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "/api/ms3/..."
Add-Line $Builder "~~~"
Add-Line $Builder ""
Add-Line $Builder "No uses:"
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "http://localhost:8083/..."
Add-Line $Builder "~~~"

Add-Section $Builder "Autenticación y autorización"

Add-Line $Builder "MS3 funciona como OAuth2 Resource Server."
Add-Line $Builder "MS3 valida JWT emitidos por MS1."
Add-Line $Builder "El frontend debe enviar el access token en cada endpoint protegido."
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "Authorization: Bearer <access_token>"
Add-Line $Builder "~~~"
Add-Line $Builder ""
Add-Line $Builder "Roles funcionales:"
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "ADMIN"
Add-Line $Builder "EMPLEADO"
Add-Line $Builder "CLIENTE"
Add-Line $Builder "INTERNAL_SERVICE"
Add-Line $Builder "~~~"
Add-Line $Builder ""
Add-Line $Builder "Reglas:"
Add-Line $Builder ""
Add-Line $Builder "- `/api/ms3/public/**` es público para lectura."
Add-Line $Builder "- `/api/ms3/admin/**` requiere ADMIN."
Add-Line $Builder "- `/api/ms3/outbox/**` requiere ADMIN."
Add-Line $Builder "- `/api/ms3/auditoria/**` requiere ADMIN."
Add-Line $Builder "- `/api/ms3/catalogo/**` requiere ADMIN o EMPLEADO, salvo rutas públicas explícitas."
Add-Line $Builder "- `/api/ms3/inventario/**` requiere ADMIN o EMPLEADO."
Add-Line $Builder "- `/api/internal/**` no es frontend; requiere X-Internal-Service-Key entre microservicios."

Add-Section $Builder "Regla crítica sobre actor"

Add-Line $Builder "Si en OpenAPI aparece un parámetro llamado `actor`, ignóralo en Angular."
Add-Line $Builder ""
Add-Line $Builder "No enviar:"
Add-Line $Builder ""
Add-Line $Builder "~~~txt"
Add-Line $Builder "?actor=..."
Add-Line $Builder "~~~"
Add-Line $Builder ""
Add-Line $Builder "`actor` representa `AuthenticatedUserContext` y se resuelve en backend desde el JWT."

Add-Section $Builder "Respuesta estándar"

Add-Line $Builder "El backend responde con `ApiResponseDto<T>`."
Add-Line $Builder ""
Add-Line $Builder "Estructura base:"
Add-Line $Builder ""
Add-Line $Builder "~~~json"
Add-Line $Builder "{"
Add-Line $Builder '  "success": true,'
Add-Line $Builder '  "message": "Operacion realizada correctamente.",'
Add-Line $Builder '  "data": {},'
Add-Line $Builder '  "timestamp": "2026-05-26T00:00:00Z",'
Add-Line $Builder '  "requestId": "uuid-o-correlation-id"'
Add-Line $Builder "}"
Add-Line $Builder "~~~"

Add-Section $Builder "Manejo obligatorio de errores"

Add-Line $Builder "Implementa manejo centralizado para:"
Add-Line $Builder ""
Add-Line $Builder "- 400: validación de formulario o request inválido."
Add-Line $Builder "- 401: token ausente, inválido o expirado. Limpiar sesión y redirigir a login."
Add-Line $Builder "- 403: usuario autenticado sin permisos. Mostrar acceso denegado."
Add-Line $Builder "- 404: recurso no encontrado."
Add-Line $Builder "- 409: conflicto funcional o regla de negocio."
Add-Line $Builder "- 413: archivo multipart excede el límite permitido."
Add-Line $Builder "- 415: tipo de archivo no permitido."
Add-Line $Builder "- 500: error interno."

Add-Section $Builder "Responsabilidad funcional de MS3"

Add-Line $Builder "MS3 gestiona:"
Add-Line $Builder ""
Add-Line $Builder "- Catálogo."
Add-Line $Builder "- Tipos de producto."
Add-Line $Builder "- Categorías."
Add-Line $Builder "- Marcas."
Add-Line $Builder "- Productos base."
Add-Line $Builder "- SKU."
Add-Line $Builder "- Atributos dinámicos."
Add-Line $Builder "- Imágenes Cloudinary de productos y SKU."
Add-Line $Builder "- Precios versionados por SKU."
Add-Line $Builder "- Promociones versionadas."
Add-Line $Builder "- Descuentos por SKU."
Add-Line $Builder "- Proveedores."
Add-Line $Builder "- Compras de inventario."
Add-Line $Builder "- Almacenes."
Add-Line $Builder "- Stock."
Add-Line $Builder "- Reservas de stock."
Add-Line $Builder "- Movimientos de inventario."
Add-Line $Builder "- Kardex."
Add-Line $Builder "- Auditoría funcional."
Add-Line $Builder "- Outbox Kafka."
Add-Line $Builder "- Snapshots mínimos de empleados MS2 para permisos de inventario."
Add-Line $Builder "- Sincronización interna de stock con MS4."
Add-Line $Builder ""
Add-Line $Builder "MS3 no gestiona:"
Add-Line $Builder ""
Add-Line $Builder "- Login."
Add-Line $Builder "- Passwords."
Add-Line $Builder "- Refresh tokens."
Add-Line $Builder "- Emisión de JWT."
Add-Line $Builder "- Usuarios oficiales."
Add-Line $Builder "- Personas, clientes o empleados oficiales."
Add-Line $Builder "- Ventas."
Add-Line $Builder "- Pagos."
Add-Line $Builder "- Boletas."
Add-Line $Builder "- Facturación."
Add-Line $Builder ""
Add-Line $Builder "Eso pertenece a MS1, MS2 o MS4 según corresponda."

Add-Section $Builder "Reglas funcionales obligatorias"

Add-Line $Builder "Catálogo:"
Add-Line $Builder ""
Add-Line $Builder "- Producto base no equivale a SKU vendible."
Add-Line $Builder "- SKU representa la variante vendible."
Add-Line $Builder "- Producto puede estar en borrador, publicado, programado, oculto o despublicado según estados definidos."
Add-Line $Builder "- Publicar producto puede requerir SKU activo, precio vigente e imagen principal."
Add-Line $Builder "- Endpoints públicos no exponen costos, proveedores, kardex, stock interno, auditoría ni movimientos."
Add-Line $Builder ""
Add-Line $Builder "SKU:"
Add-Line $Builder ""
Add-Line $Builder "- Crear o actualizar SKU no modifica precio ni stock."
Add-Line $Builder "- Inactivar o descontinuar SKU debe respetar reglas del backend."
Add-Line $Builder "- Los atributos dinámicos se envían según los DTOs documentados."
Add-Line $Builder ""
Add-Line $Builder "Imágenes:"
Add-Line $Builder ""
Add-Line $Builder "- Subida de imagen usa multipart/form-data."
Add-Line $Builder "- Angular envía archivo y metadata."
Add-Line $Builder "- Backend gestiona Cloudinary."
Add-Line $Builder "- Angular nunca maneja credenciales de Cloudinary."
Add-Line $Builder ""
Add-Line $Builder "Precios:"
Add-Line $Builder ""
Add-Line $Builder "- Los precios son versionados."
Add-Line $Builder "- Registrar nuevo precio no edita historial anterior."
Add-Line $Builder "- Precio vigente se consulta por SKU o referencia funcional."
Add-Line $Builder ""
Add-Line $Builder "Promociones:"
Add-Line $Builder ""
Add-Line $Builder "- Promoción base, versión de promoción y descuento por SKU son conceptos distintos."
Add-Line $Builder "- Descuentos por SKU se crean dentro de versiones."
Add-Line $Builder "- El cálculo de descuento no persiste cambios cuando el endpoint indica calcular."
Add-Line $Builder ""
Add-Line $Builder "Inventario:"
Add-Line $Builder ""
Add-Line $Builder "- Stock es consultivo."
Add-Line $Builder "- No actualizar stock manualmente desde pantallas de consulta."
Add-Line $Builder "- Reservar stock aumenta stock reservado."
Add-Line $Builder "- Confirmar reserva descuenta físico y reservado según reglas del service."
Add-Line $Builder "- Liberar reserva devuelve reservado a disponible."
Add-Line $Builder "- Todo movimiento relevante debe reflejarse en kardex."
Add-Line $Builder "- Costos solo deben mostrarse si el endpoint y autorización lo permiten."
Add-Line $Builder ""
Add-Line $Builder "Empleados inventario:"
Add-Line $Builder ""
Add-Line $Builder "- EMPLEADO requiere permisos funcionales para operar inventario."
Add-Line $Builder "- ADMIN administra permisos."
Add-Line $Builder "- Frontend debe consultar permisos antes de mostrar acciones operativas sensibles."
Add-Line $Builder ""
Add-Line $Builder "Outbox y Kafka:"
Add-Line $Builder ""
Add-Line $Builder "- Outbox es administrativo."
Add-Line $Builder "- Angular no publica Kafka directamente."
Add-Line $Builder "- Los endpoints outbox solicitan acciones al backend."
Add-Line $Builder ""
Add-Line $Builder "Endpoints internos:"
Add-Line $Builder ""
Add-Line $Builder "- `/api/internal/**` no debe consumirse desde Angular."
Add-Line $Builder "- Son rutas entre microservicios y requieren `X-Internal-Service-Key`."

Add-Section $Builder "Servicios Angular esperados"

Add-Line $Builder "Organiza el consumo HTTP en servicios separados por dominio:"
Add-Line $Builder ""
Add-Line $Builder "- Ms3PublicCatalogoService."
Add-Line $Builder "- Ms3PublicProductoService."
Add-Line $Builder "- Ms3PublicPromocionService."
Add-Line $Builder "- Ms3ReferenceDataService."
Add-Line $Builder "- Ms3CatalogoLookupService."
Add-Line $Builder "- Ms3TipoProductoService."
Add-Line $Builder "- Ms3CategoriaService."
Add-Line $Builder "- Ms3MarcaService."
Add-Line $Builder "- Ms3AtributoService."
Add-Line $Builder "- Ms3ProductoAdminService."
Add-Line $Builder "- Ms3ProductoSkuService."
Add-Line $Builder "- Ms3ProductoImagenService."
Add-Line $Builder "- Ms3PrecioSkuService."
Add-Line $Builder "- Ms3PromocionService."
Add-Line $Builder "- Ms3ProveedorService."
Add-Line $Builder "- Ms3AlmacenService."
Add-Line $Builder "- Ms3StockService."
Add-Line $Builder "- Ms3ReservaStockService."
Add-Line $Builder "- Ms3MovimientoInventarioService."
Add-Line $Builder "- Ms3KardexService."
Add-Line $Builder "- Ms3CompraInventarioService."
Add-Line $Builder "- Ms3EmpleadoInventarioPermisoService."
Add-Line $Builder "- Ms3AuditoriaService."
Add-Line $Builder "- Ms3OutboxService."
Add-Line $Builder ""
Add-Line $Builder "Genera interfaces TypeScript desde `components.schemas` del OpenAPI."
Add-Line $Builder "Genera formularios Angular usando `required`, `minLength`, `maxLength`, `minimum`, `maximum`, `email`, `enum`, `format` y las validaciones declaradas en DTOs."
Add-Line $Builder "Para archivos usa `FormData` y respeta `multipart/form-data`."

Add-Section $Builder "Contrato OpenAPI"

Add-Line $Builder "Usa este bloque como contrato principal."
Add-Line $Builder ""
Add-Line $Builder "~~~json"
Add-Line $Builder $OpenApiContent
Add-Line $Builder "~~~"

Add-Section $Builder "Configuración del backend"

Add-CodeFile $Builder (Join-Path $ProjectRoot "pom.xml") "xml" $false
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application.properties") "properties" $true
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application.yml") "yaml" $true
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application.yaml") "yaml" $true
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application-local.properties") "properties" $true
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application-local.yml") "yaml" $true
Add-CodeFile $Builder (Join-Path $ProjectRoot "src\main\resources\application-local.yaml") "yaml" $true

Add-Section $Builder "Clases backend incluidas"

foreach ($file in $SelectedFiles) {
    Add-Line $Builder ("- " + $file.Replace($ProjectRoot + "\", ""))
}

Add-Section $Builder "Código backend seleccionado"

foreach ($file in $SelectedFiles) {
    Add-CodeFile $Builder $file "java" $false
}

if (Test-Path $OutputFile) {
    Remove-Item $OutputFile -Force
}

Set-Content -Path $OutputFile -Value $Builder.ToString() -Encoding UTF8

$fileInfo = Get-Item $OutputFile

Write-Host ""
Write-Host "ARCHIVO GENERADO:"
Write-Host $OutputFile
Write-Host ""
Write-Host "TAMANIO BYTES:"
Write-Host $fileInfo.Length
Write-Host ""
Write-Host "CLASES JAVA INCLUIDAS:"
Write-Host $SelectedFiles.Count
Write-Host ""
Write-Host "OPENAPI:"
Write-Host $OpenApiUrl
'@ | Set-Content -Path $ScriptPath -Encoding UTF8

powershell -NoProfile -ExecutionPolicy Bypass -File $ScriptPath

Remove-Item $ScriptPath -Force
```

Verifica:

```powershell
Test-Path "C:\Users\WinterOS\Desktop\MS\ms3-frontend-context.md"
```

Debe devolver:

```txt
True
```

Abre el archivo:

```powershell
notepad "C:\Users\WinterOS\Desktop\MS\ms3-frontend-context.md"
```

El único archivo que debes compartir después es:

```txt
C:\Users\WinterOS\Desktop\MS\ms3-frontend-context.md
```
