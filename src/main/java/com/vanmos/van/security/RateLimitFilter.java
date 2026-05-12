package com.vanmos.van.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Middleware de Rate Limiting para os endpoints de login.
 *
 * ESTRATÉGIA (Token Bucket):
 *  - Cada IP tem seu próprio bucket com 5 tentativas de login.
 *  - O bucket se reabastece completamente a cada 1 minuto.
 *  - Ou seja: máximo de 5 tentativas por minuto por IP.
 *  - Se exceder, retorna HTTP 429 Too Many Requests.
 *
 * PROTEÇÃO CONTRA BRUTE FORCE:
 *  Um atacante tentando 1000 senhas levaria 200 minutos (~3h20) apenas
 *  para um único IP, tornando ataques automatizados inviáveis.
 *
 * INTEGRAÇÃO: Registrado em SecurityConfig antes do JwtAuthFilter.
 * Aplica-se apenas às rotas de login (ver método shouldNotFilter).
 *
 * NOTA PARA PRODUÇÃO: Substitua o ConcurrentHashMap em memória por
 * Bucket4j + Redis para funcionar corretamente em múltiplas instâncias.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Mapa IP -> Bucket (em memória; use Redis em produção com múltiplas instâncias)
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cria ou reutiliza o bucket para o IP informado
    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, key -> {
            // 5 tokens, reabastece 5 tokens a cada 1 minuto
            Bandwidth limit = Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofMinutes(1))
                    .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Respeita o header X-Forwarded-For para IPs atrás de proxy/load balancer
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            // Tentativa permitida — continua o fluxo normal
            filterChain.doFilter(request, response);
        } else {
            // Limite excedido — bloqueia e informa quantos segundos aguardar
            long segundosParaReabastecer = bucket.getAvailableTokens() == 0 ? 60 : 0;

            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(segundosParaReabastecer));

            Map<String, Object> body = Map.of(
                    "sucesso", false,
                    "mensagem", "Muitas tentativas de login. Aguarde " + segundosParaReabastecer + " segundos.",
                    "retryAfterSeconds", segundosParaReabastecer
            );
            objectMapper.writeValue(response.getOutputStream(), body);
        }
    }

    /**
     * Aplica o rate limit APENAS nas rotas de login.
     * Todas as outras rotas passam direto sem consumir tokens.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/api/login")
                && !path.equals("/api/login-admin")
                && !path.equals("/api/motoristas-admin/login");
    }
}
