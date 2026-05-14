package com.upsjb.ms3.security.config;

import com.upsjb.ms3.config.AppPropertiesConfig;
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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final AppPropertiesConfig appProperties;
    private final RoleJwtAuthenticationConverter roleJwtAuthenticationConverter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RequestTraceFilter requestTraceFilter;
    private final RequestAuditContextFilter requestAuditContextFilter;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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

                        .requestMatchers(HttpMethod.GET, "/api/ms3/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/catalogo/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/productos/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/promociones/public/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()

                        .requestMatchers("/api/ms3/admin/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/outbox/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/auditoria/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/internal/**")
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