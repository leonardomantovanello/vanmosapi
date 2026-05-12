package com.vanmos.van.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Middleware JWT — intercepta TODAS as requisições uma única vez (OncePerRequestFilter).
 *
 * FLUXO:
 *  1. Lê o header "Authorization: Bearer <token>"
 *  2. Valida que é um access token legítimo via JwtUtil
 *  3. Extrai subject (email/CPF) e role do token
 *  4. Registra a autenticação no SecurityContext do Spring
 *  5. Se o token for inválido/ausente, a requisição continua sem autenticação
 *     e o SecurityConfig decide se a rota é pública ou protegida
 *
 * INTEGRAÇÃO: Este filtro é registrado em SecurityConfig antes do
 * UsernamePasswordAuthenticationFilter do Spring Security.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Só processa se o header existir e começar com "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // remove "Bearer "

            if (jwtUtil.isValidAccessToken(token)) {
                String subject = jwtUtil.extractSubject(token);
                String role    = jwtUtil.extractRole(token);

                // Cria a autenticação com a role do usuário (ex: ROLE_ADMIN, ROLE_MOTORISTA)
                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var authentication = new UsernamePasswordAuthenticationToken(
                        subject, null, List.of(authority)
                );

                // Registra no contexto de segurança — a partir daqui o Spring sabe quem é o usuário
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // Token inválido: não seta autenticação, SecurityConfig vai bloquear rotas protegidas
        }

        filterChain.doFilter(request, response);
    }
}
