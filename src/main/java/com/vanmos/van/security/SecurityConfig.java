package com.vanmos.van.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuração central de segurança da aplicação.
 *
 * DECISÕES DE DESIGN:
 *  - Sessão STATELESS: o Spring não cria HttpSession. Toda autenticação
 *    é feita via JWT em cada requisição.
 *  - BCrypt com strength 12: bom equilíbrio entre segurança e performance.
 *    Cada hash leva ~250ms, tornando ataques de força bruta extremamente lentos.
 *  - Rotas públicas: apenas login, cadastro e refresh token.
 *  - Todas as demais rotas exigem autenticação válida.
 *
 * INTEGRAÇÃO: Esta classe substitui o CorsConfiguration.java existente.
 * Remova ou desative o arquivo CorsConfiguration.java para evitar conflito.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8686}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With,X-Refresh-Request}")
    private String allowedHeaders;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt strength 12: ~250ms por hash — seguro contra brute force
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            /*
             * CSRF fica desabilitado porque a API e stateless e nao usa cookie
             * como autenticador automatico: access tokens chegam no header
             * Authorization e o refresh token chega explicitamente no body.
             *
             * Se access/refresh tokens migrarem para cookies HttpOnly enviados
             * automaticamente pelo browser, habilite protecao CSRF para endpoints
             * mutaveis ou combine SameSite=Strict/Lax com token anti-CSRF.
             */
            .csrf(csrf -> csrf.disable())

            // Headers de segurança — API JSON pura, mas nada impede que a resposta
            // seja aberta dentro de um <iframe>/carregada como script em outro contexto.
            .headers(headers -> headers
                    .contentTypeOptions(contentTypeOptions -> {})
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .referrerPolicy(referrer -> referrer
                            .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000))
            )

            // Configuração CORS centralizada aqui (substitui CorsConfiguration.java)
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(csvToList(allowedOrigins));
                // Apenas métodos necessários para uma API REST — sem TRACE nem CONNECT
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(csvToList(allowedHeaders));
                config.setAllowCredentials(true);
                return config;
            }))

            // API sem estado — nunca cria sessão HTTP
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Resposta padronizada para requisições não autenticadas (401)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        new ObjectMapper().writeValue(response.getOutputStream(), Map.of(
                                "sucesso", false,
                                "mensagem", "Token ausente ou inválido. Faça login novamente."
                        ));
                    })
                    // Resposta padronizada para acesso negado por role (403)
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        new ObjectMapper().writeValue(response.getOutputStream(), Map.of(
                                "sucesso", false,
                                "mensagem", "Acesso negado. Você não tem permissão para este recurso."
                        ));
                    })
            )

            // Definição de quais rotas são públicas e quais exigem autenticação
            .authorizeHttpRequests(auth -> auth
                    // Rotas públicas — não exigem token
                    // IMPORTANTE: "/api/cadastro" aqui é só o cadastro público (POST). Um
                    // requestMatcher sem HttpMethod casa com QUALQUER verbo — listar GET
                    // ficava acidentalmente público também até essa correção, pois essa
                    // regra é avaliada antes da regra ROLE_ADMIN mais específica abaixo.
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/login", "/api/login-admin",
                            "/api/cadastro", "/api/auth/refresh", "/api/motoristas-admin/login")
                    .permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET,
                            "/api/cadastro/verificar-email", "/api/motoristas-admin/publico")
                    .permitAll()

                    // Rotas exclusivas de administrador
                    .requestMatchers(
                            "/api/motoristas-admin/**",
                            "/api/cadastro/*/ativar",
                            "/api/cadastro/*/inativar"
                    ).hasRole("ADMIN")

                    // Listagem geral de cadastros: apenas ADMIN
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/cadastro")
                    .hasRole("ADMIN")

                    // Motoristas: leitura para ADMIN e MOTORISTA; escrita apenas ADMIN
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/motoristas", "/api/motoristas/**")
                    .hasAnyRole("ADMIN", "MOTORISTA")

                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/motoristas")
                    .hasRole("ADMIN")

                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/motoristas/**")
                    .hasRole("ADMIN")

                    // Vans: leitura para ADMIN e MOTORISTA; escrita apenas ADMIN
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/vans", "/api/vans/**")
                    .hasAnyRole("ADMIN", "MOTORISTA")

                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/vans")
                    .hasRole("ADMIN")

                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/vans/**")
                    .hasRole("ADMIN")

                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/vans/**")
                    .hasRole("ADMIN")

                    // Alunos: ADMIN e MOTORISTA gerenciam; RESPONSAVEL apenas lê
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/alunos", "/api/alunos/**")
                    .hasAnyRole("ADMIN", "MOTORISTA", "RESPONSAVEL")

                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/alunos")
                    .hasAnyRole("ADMIN", "MOTORISTA")

                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/alunos/**")
                    .hasAnyRole("ADMIN", "MOTORISTA")

                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/alunos/**")
                    .hasAnyRole("ADMIN", "MOTORISTA")

                    // Responsáveis: cada um acessa o próprio; ADMIN acessa todos
                    .requestMatchers("/api/responsaveis", "/api/responsaveis/**")
                    .hasAnyRole("ADMIN", "RESPONSAVEL")

                    // Todas as demais rotas exigem qualquer autenticação válida
                    .anyRequest().authenticated()
            );

        // Registra o rate limiter ANTES do filtro JWT (bloqueia antes de processar o token)
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        // Registra o filtro JWT antes do filtro padrão de autenticação do Spring
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private List<String> csvToList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }
}
