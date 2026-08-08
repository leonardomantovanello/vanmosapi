package com.vanmos.van.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Corpo aceito por POST /api/passageiros/cadastrar-motorista.
 *
 * Cria DUAS coisas de uma vez, ligadas entre si:
 *  1. Uma conta Passageiro (tipo=PASSAGEIRO) pro responsável — é quem
 *     efetivamente loga no app. Sem senha aqui: ela é gerada pelo backend e
 *     enviada por e-mail (ver EmailService), nunca escolhida pelo motorista.
 *  2. Um Aluno vinculado a essa conta (responsavelId) E ao motorista logado
 *     (motoristaId) — é o que aparece na lista "Meus Passageiros" do
 *     motorista e no dashboard do app.
 */
public record CadastrarPassageiroPeloMotoristaRequest(
        // Dados do responsável (conta de login)
        @NotBlank(message = "Nome do responsável é obrigatório") String nomeResponsavel,
        String cpfResponsavel,
        @NotBlank(message = "E-mail do responsável é obrigatório")
        @Email(message = "E-mail inválido") String emailResponsavel,
        Integer idadeResponsavel,
        String generoResponsavel,

        // Dados do aluno
        @NotBlank(message = "Nome do aluno é obrigatório") String nomeAluno,
        String telefoneResponsavel,
        String enderecoEmbarque,
        String enderecoDesembarque,
        String escola,
        String turno,

        // Mensalidade cobrada pelo aluno — opcional (pode ser preenchida
        // depois via edição), soma dos ativos vira a Receita Mensal do dashboard.
        @DecimalMin(value = "0.0", inclusive = true, message = "Valor mensal não pode ser negativo")
        BigDecimal valor
) {
}
