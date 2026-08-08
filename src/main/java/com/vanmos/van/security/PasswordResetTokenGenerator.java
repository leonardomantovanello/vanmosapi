package com.vanmos.van.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera o token do link de "esqueci minha senha" — diferente de
 * RandomPasswordGenerator (que gera senha pra uma pessoa digitar), este vai
 * direto na query string de um link clicável, então usa o alfabeto Base64
 * URL-safe inteiro (sem preocupação de ambiguidade visual) com bem mais
 * entropia (32 bytes) já que precisa resistir a tentativas de adivinhação.
 */
@Component
public class PasswordResetTokenGenerator {

    private static final int TAMANHO_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        byte[] bytes = new byte[TAMANHO_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
