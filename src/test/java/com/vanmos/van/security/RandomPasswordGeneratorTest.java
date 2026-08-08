package com.vanmos.van.security;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere que a senha gerada automaticamente (enviada por e-mail em vez de
 * escolhida pela pessoa) sempre satisfaz a política de senha da entidade
 * Passageiro (@Size(min=8) + validação de maiúscula/minúscula/número usada
 * no front) — e nunca contém os caracteres ambíguos que o gerador promete evitar.
 */
class RandomPasswordGeneratorTest {

    private final RandomPasswordGenerator generator = new RandomPasswordGenerator();

    @RepeatedTest(20) // aleatório: repete pra não passar só por sorte numa única geração
    void senhaGeradaDeveSatisfazerPoliticaDeSenha() {
        String senha = generator.gerar();

        assertEquals(10, senha.length());
        assertTrue(senha.chars().anyMatch(Character::isUpperCase), "deveria ter maiúscula: " + senha);
        assertTrue(senha.chars().anyMatch(Character::isLowerCase), "deveria ter minúscula: " + senha);
        assertTrue(senha.chars().anyMatch(Character::isDigit), "deveria ter número: " + senha);
    }

    @RepeatedTest(20)
    void senhaGeradaNaoDeveConterCaracteresAmbiguos() {
        // I/O/0/1/l são propositalmente excluídos (comentário do gerador) por
        // confundirem em e-mail — ex.: "I" maiúsculo parece "l" minúsculo.
        String senha = generator.gerar();
        assertFalse(senha.matches(".*[IO01l].*"), "não deveria conter I, O, 0, 1 ou l: " + senha);
    }

    @Test
    void duasSenhasGeradasNaoDevemSerIguais() {
        String senha1 = generator.gerar();
        String senha2 = generator.gerar();
        assertNotEquals(senha1, senha2);
    }
}
