package com.vanmos.van.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Corpo aceito por POST /api/passageiros/esqueci-senha — formulário público
 * "Esqueci minha senha" do site. Sem autenticação (é para quem não consegue
 * logar), protegido pelo mesmo RateLimitFilter dos endpoints de login.
 */
public record EsqueciSenhaRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {
}
