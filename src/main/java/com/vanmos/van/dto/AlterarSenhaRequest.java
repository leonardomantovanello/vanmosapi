package com.vanmos.van.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Corpo aceito por PUT /api/passageiros/{id}/senha.
 *
 * A senha atual é conferida contra o hash salvo (ver
 * PassageiroService#alterarSenha) antes de qualquer troca — não basta estar
 * autenticado, quem troca precisa provar que conhece a senha em uso.
 */
public record AlterarSenhaRequest(
        @NotBlank(message = "Senha atual é obrigatória") String senhaAtual,

        @NotBlank(message = "Nova senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "A nova senha deve ter pelo menos 8 caracteres, incluindo maiúscula, minúscula e número"
        )
        String novaSenha
) {
}
