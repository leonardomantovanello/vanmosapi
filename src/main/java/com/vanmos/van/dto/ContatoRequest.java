package com.vanmos.van.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo aceito por POST /api/contato — formulário público "Contate-nos" do
 * site. Sem autenticação (qualquer visitante pode enviar), protegido apenas
 * pelo RateLimitFilter global (ver SecurityConfig).
 */
public record ContatoRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido") String email,
        String telefone,
        @NotBlank(message = "Assunto é obrigatório") String assunto,
        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 5000, message = "Mensagem muito longa") String mensagem
) {
}
