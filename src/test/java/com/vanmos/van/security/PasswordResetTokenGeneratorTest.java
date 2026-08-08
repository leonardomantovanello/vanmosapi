package com.vanmos.van.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * O token do link de "esqueci minha senha" precisa ser seguro pra ir numa
 * URL (sem caracteres que quebrem query string) e imprevisível o bastante
 * pra ninguém adivinhar o token de outra pessoa.
 */
class PasswordResetTokenGeneratorTest {

    private final PasswordResetTokenGenerator generator = new PasswordResetTokenGenerator();

    @Test
    void tokenDeveSerUrlSafe() {
        // Base64 padrão usa +, / e = — todos problemáticos numa query string
        // se não forem escapados. O gerador usa a variante URL-safe sem padding.
        String token = generator.gerar();

        assertFalse(token.contains("+"), "token não deveria conter '+': " + token);
        assertFalse(token.contains("/"), "token não deveria conter '/': " + token);
        assertFalse(token.contains("="), "token não deveria conter '=' (padding): " + token);
    }

    @Test
    void tokensGeradosDevemSerUnicos() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(generator.gerar());
        }

        assertEquals(1000, tokens.size(), "não deveria haver colisão em 1000 gerações");
    }
}
