package com.vanmos.van.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

/**
 * Infraestrutura de tempo real (chat e localização) via STOMP sobre
 * WebSocket. Broker simples em memória — suficiente pra escala de um TCC;
 * se precisar de múltiplas instâncias no futuro, troca por um broker
 * externo (ex. RabbitMQ) com enableStompBrokerRelay, igual o comentário do
 * RateLimitFilter já aponta pra Redis no rate limiting.
 *
 * O endpoint /ws fica público no SecurityConfig — a autenticação real
 * acontece por mensagem STOMP no StompAuthInterceptor (ver classe), não no
 * handshake HTTP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private StompAuthInterceptor stompAuthInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8686}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(csvToArray(allowedOrigins))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }

    private String[] csvToArray(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toArray(String[]::new);
    }
}
