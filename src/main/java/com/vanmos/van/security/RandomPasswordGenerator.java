package com.vanmos.van.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Gera senhas temporárias para contas criadas por terceiros (ex: motorista
 * cadastrando um passageiro) — a pessoa nunca escolhe/digita essa senha, ela
 * chega pronta por e-mail, então só precisa satisfazer a política de senha
 * da entidade (mínimo 8 caracteres) e ser imprevisível.
 */
@Component
public class RandomPasswordGenerator {

    private static final String MAIUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // sem I/O — confundem em e-mail
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz"; // sem l
    private static final String NUMEROS = "23456789"; // sem 0/1
    private static final String TODOS = MAIUSCULAS + MINUSCULAS + NUMEROS;
    private static final int TAMANHO = 10;

    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        char[] senha = new char[TAMANHO];
        // Garante ao menos um de cada categoria antes de preencher o resto aleatoriamente
        senha[0] = MAIUSCULAS.charAt(random.nextInt(MAIUSCULAS.length()));
        senha[1] = MINUSCULAS.charAt(random.nextInt(MINUSCULAS.length()));
        senha[2] = NUMEROS.charAt(random.nextInt(NUMEROS.length()));

        for (int i = 3; i < TAMANHO; i++) {
            senha[i] = TODOS.charAt(random.nextInt(TODOS.length()));
        }

        // Embaralha para as posições fixas (0,1,2) não seguirem sempre o mesmo padrão
        for (int i = senha.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = senha[i];
            senha[i] = senha[j];
            senha[j] = tmp;
        }

        return new String(senha);
    }
}
