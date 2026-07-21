package com.vanmos.van.security;

import java.security.Principal;

/**
 * Identidade do usuário autenticado numa sessão STOMP/WebSocket — o
 * equivalente ao Authentication que o JwtAuthFilter registra no
 * SecurityContext para requisições HTTP comuns, mas para o handshake
 * STOMP (que não passa pela cadeia de filtros do Spring Security por
 * requisição, já que a conexão fica aberta). Ver StompAuthInterceptor.
 */
public class StompPrincipal implements Principal {

    private final Long userId;
    private final String role;
    private final String subject;

    public StompPrincipal(Long userId, String role, String subject) {
        this.userId = userId;
        this.role = role;
        this.subject = subject;
    }

    public Long getUserId() { return userId; }
    public String getRole() { return role; }

    // Principal#getName precisa ser único por conexão — usado internamente
    // pelo Spring pra rotear mensagens user-specific (não usamos esse
    // recurso aqui, mas o contrato exige um valor não-nulo).
    @Override
    public String getName() { return subject; }
}
