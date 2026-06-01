package com.vanmos.van.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt strength 12: ~250ms por hash — seguro contra brute force
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF — desnecessário em APIs stateless com JWT
            .csrf(csrf -> csrf.disable())

            // Configuração CORS centralizada aqui (substitui CorsConfiguration.java)
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of(
                        "http://localhost:5173",
                        "http://localhost:8686"
                        // Adicione a URL de produção do frontend aqui via variável de ambiente
                ));
                // Apenas métodos necessários para uma API REST — sem TRACE nem CONNECT
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(java.util.List.of("*"));
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
                    .requestMatchers(
                            "/api/login",
                            "/api/login-admin",
                            "/api/cadastro",
                            "/api/auth/refresh",
                            "/api/motoristas-admin/login"
                    ).permitAll()

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
}
