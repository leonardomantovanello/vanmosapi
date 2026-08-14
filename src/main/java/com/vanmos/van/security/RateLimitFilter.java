package com.vanmos.van.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
 * Aplica-se às rotas de login e outros endpoints públicos sensíveis
 * (cadastro, aprovação de motorista por token, esqueci-senha, contato —
 * ver método shouldNotFilter para a lista completa).
 *
 * NOTA PARA PRODUÇÃO: Substitua o ConcurrentHashMap em memória por
 * Bucket4j + Redis para funcionar corretamente em múltiplas instâncias.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Mapa IP -> Bucket (em memória; use Redis em produção com múltiplas instâncias)
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.security.trusted-proxies:127.0.0.1,0:0:0:0:0:0:0:1,::1}")
    private String trustedProxies;

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

        String ip = resolveClientIp(request);

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
     * Aplica o rate limit nas rotas de login e em outros endpoints públicos
     * que disparam ação sensível (ex.: esqueci-senha e contato enviam
     * e-mail — sem limite, dá pra floodar a caixa de entrada de qualquer
     * cadastrado, ou a da própria VanMos no caso do formulário de contato).
     * Também cobre o autocadastro (POST /api/passageiros e /api/motoristas —
     * sem isso dava pra automatizar tentativas de cadastro/abuso sem
     * fricção nenhuma) e o fluxo de aprovação de motorista por token
     * (GET/POST /api/motoristas/aprovacao/**), já que o path muda por
     * requisição (token na URL), por isso o startsWith em vez de equals.
     * Todas as outras rotas passam direto sem consumir tokens — em
     * especial os pollings autenticados do app (progresso da rota, faltas
     * de hoje), que já rodam perto do limite de 5/min sozinhos e
     * quebrariam se caíssem numa lista "protegida por padrão".
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
        boolean isCadastroPassageiroPublico = isPost && path.equals("/api/passageiros");
        boolean isCadastroMotoristaPublico = isPost && path.equals("/api/motoristas");
        boolean isAprovacaoPassageiro = path.startsWith("/api/passageiros/aprovacao/");
        boolean isAprovacaoMotorista = path.startsWith("/api/motoristas/aprovacao/");

        return !path.equals("/api/login")
                && !path.equals("/api/login-admin")
                && !path.equals("/api/passageiros/esqueci-senha")
                && !path.equals("/api/passageiros/redefinir-senha")
                && !path.equals("/api/motoristas/esqueci-senha")
                && !path.equals("/api/motoristas/redefinir-senha")
                && !path.equals("/api/contato")
                && !isCadastroPassageiroPublico
                && !isCadastroMotoristaPublico
                && !isAprovacaoPassageiro
                && !isAprovacaoMotorista;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (!isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }

        String forwardedClientIp = extractFirstValidForwardedIp(request.getHeader("X-Forwarded-For"));
        return forwardedClientIp != null ? forwardedClientIp : remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return false;
        }

        Set<String> trusted = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(proxy -> !proxy.isBlank())
                .collect(Collectors.toSet());

        return trusted.contains(remoteAddr);
    }

    private String extractFirstValidForwardedIp(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }

        String candidate = forwardedFor.split(",")[0].trim();
        if (!candidate.matches("[0-9a-fA-F:.]{2,45}")) {
            return null;
        }

        try {
            InetAddress.getByName(candidate);
            return candidate;
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
