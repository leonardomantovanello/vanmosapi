package com.vanmos.van.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitário central para geração e validação de tokens JWT.
 *
 * ESTRATÉGIA DE TOKENS:
 *  - Access Token:  vida curta (15 min). Enviado no header Authorization: Bearer <token>.
 *                   Usado para autenticar cada requisição protegida.
 *  - Refresh Token: vida longa (7 dias). Armazenado em cookie HttpOnly no frontend.
 *                   Usado APENAS para emitir um novo access token sem pedir login novamente.
 *
 * CLAIM "type" diferencia os dois tokens para evitar que um refresh token
 * seja aceito como access token em rotas protegidas.
 */
@Component
public class JwtUtil {

    // Chave secreta lida do application.properties (injetada via variável de ambiente JWT_SECRET)
    @Value("${jwt.secret}")
    private String secret;

    // Expiração do access token em ms (padrão: 15 minutos)
    @Value("${jwt.expiration.ms}")
    private long accessExpirationMs;

    // Expiração do refresh token em ms (padrão: 7 dias)
    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpirationMs;

    // Constrói a chave HMAC-SHA512 a partir do secret configurado
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um access token JWT.
     *
     * @param subject  identificador do usuário (email ou CPF)
     * @param role     papel do usuário: "RESPONSAVEL", "MOTORISTA" ou "ADMIN"
     * @param userId   ID do usuário no banco de dados
     */
    public String generateAccessToken(String subject, String role, Long userId) {
        return buildToken(subject, role, userId, accessExpirationMs, "access");
    }

    /**
     * Gera um refresh token JWT.
     * Contém apenas o subject e o type — sem role nem userId para minimizar exposição.
     */
    public String generateRefreshToken(String subject) {
        return buildToken(subject, null, null, refreshExpirationMs, "refresh");
    }

    private String buildToken(String subject, String role, Long userId, long expirationMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(subject)
                .claim("type", type)        // diferencia access de refresh
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey());

        if (role != null)   builder.claim("role", role);
        if (userId != null) builder.claim("userId", userId);

        return builder.compact();
    }

    /**
     * Extrai todas as claims de um token.
     * Lança JwtException se o token for inválido ou expirado.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    /**
     * Valida se o token é um access token válido e não expirado.
     * Retorna false para refresh tokens mesmo que sejam válidos —
     * isso impede que um refresh token seja usado para autenticar rotas.
     */
    public boolean isValidAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "access".equals(claims.get("type", String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida se o token é um refresh token válido e não expirado.
     */
    public boolean isValidRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type", String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
