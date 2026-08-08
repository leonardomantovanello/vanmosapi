package com.vanmos.van.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Corpo aceito por POST /api/passageiros/redefinir-senha — segunda etapa do
 * fluxo "esqueci minha senha", chamada a partir do link recebido por e-mail.
 * O token prova a posse do e-mail (ver PassageiroService#gerarTokenRedefinicaoSenha);
 * diferente de AlterarSenhaRequest, não há senha atual pra confirmar.
 */
public record RedefinirSenhaComTokenRequest(
        @NotBlank(message = "Token é obrigatório") String token,

        @NotBlank(message = "Nova senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "A nova senha deve ter pelo menos 8 caracteres, incluindo maiúscula, minúscula e número"
        )
        String novaSenha
) {
}
