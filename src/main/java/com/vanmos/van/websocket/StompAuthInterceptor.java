package com.vanmos.van.websocket;

import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.service.AlunoService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.StompPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Autenticação e autorização da camada STOMP/WebSocket — o WebSocketConfig
 * deixa o handshake HTTP em /ws público (SecurityConfig), então toda a
 * proteção acontece aqui, mensagem a mensagem:
 *
 *  CONNECT   — valida o JWT enviado no header STOMP "Authorization" (igual
 *              o JwtAuthFilter faz pra HTTP) e associa um StompPrincipal à
 *              sessão. Sem token válido, a conexão é recusada.
 *  SUBSCRIBE — valida que o usuário conectado tem permissão sobre o tópico
 *              pedido, replicando a mesma regra de ownership que já existe
 *              em MensagemController/AlunoController pros endpoints REST
 *              equivalentes. Sem essa checagem, qualquer usuário autenticado
 *              poderia assinar o chat de qualquer outra família
 *              (IDOR).
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthInterceptor.class);

    private static final Pattern MENSAGENS_TOPIC = Pattern.compile("^/topic/mensagens/aluno/(\\d+)$");

    @Autowired private JwtUtil jwtUtil;
    @Autowired private AlunoService alunoService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            StompPrincipal principal = authenticate(accessor);
            accessor.setUser(principal);
            log.info("[ws] CONNECT autenticado: userId={} role={}", principal.getUserId(), principal.getRole());
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            log.info("[ws] SUBSCRIBE pedido: destino={} user={}", accessor.getDestination(), accessor.getUser());
            authorizeSubscription(accessor);
            log.info("[ws] SUBSCRIBE autorizado: destino={}", accessor.getDestination());
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            log.info("[ws] SEND: destino={} user={}", accessor.getDestination(), accessor.getUser());
        }

        return message;
    }

    private StompPrincipal authenticate(StompHeaderAccessor accessor) {
        String authHeader = firstHeader(accessor, "Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[ws] CONNECT recusado: header Authorization ausente");
            throw new MessagingException("Token ausente. Conexão recusada.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValidAccessToken(token)) {
            log.warn("[ws] CONNECT recusado: token inválido/expirado");
            throw new MessagingException("Token inválido ou expirado. Conexão recusada.");
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String subject = jwtUtil.extractSubject(token);
        return new StompPrincipal(userId, role, subject);
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessagingException("Destino de inscrição ausente.");
        }

        StompPrincipal principal = (StompPrincipal) accessor.getUser();
        if (principal == null) {
            throw new MessagingException("Sessão não autenticada.");
        }

        Matcher mensagensMatch = MENSAGENS_TOPIC.matcher(destination);
        if (mensagensMatch.matches()) {
            authorizeMensagensTopic(principal, Long.valueOf(mensagensMatch.group(1)));
            return;
        }

        // Nenhum outro tópico é reconhecido — nega por padrão em vez de
        // permitir qualquer destino não mapeado.
        throw new MessagingException("Inscrição não permitida para este destino.");
    }

    private void authorizeMensagensTopic(StompPrincipal principal, Long alunoId) {
        if ("ADMIN".equals(principal.getRole())) return;

        Aluno aluno = alunoService.findById(alunoId).orElse(null);
        if (aluno == null) {
            throw new MessagingException("Aluno não encontrado.");
        }

        boolean ehOMotoristaDoAluno = "MOTORISTA".equals(principal.getRole())
                && aluno.getMotoristaId() != null && aluno.getMotoristaId().equals(principal.getUserId());
        boolean ehOResponsavelDoAluno = "RESPONSAVEL".equals(principal.getRole())
                && aluno.getResponsavelId() != null && aluno.getResponsavelId().equals(principal.getUserId());

        if (!ehOMotoristaDoAluno && !ehOResponsavelDoAluno) {
            throw new MessagingException("Você não tem permissão para acessar esta conversa.");
        }
    }

    private String firstHeader(StompHeaderAccessor accessor, String name) {
        return accessor.getFirstNativeHeader(name);
    }
}
