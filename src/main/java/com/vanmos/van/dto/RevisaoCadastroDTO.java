package com.vanmos.van.dto;

import com.vanmos.van.model.entity.Motorista;

import java.time.LocalDateTime;

/**
 * Dados expostos na página pública de revisão de cadastro (ver
 * MotoristaAprovacaoController) — sem senha, id ou status, que não são
 * necessários pro suporte decidir e não devem vazar por um link de e-mail.
 */
public record RevisaoCadastroDTO(
        String nome,
        String cpf,
        String rg,
        String cnh,
        String telefone,
        String email,
        Integer idade,
        String genero,
        String rgDocumentoBase64,
        String cnhDocumentoBase64,
        LocalDateTime criadoEm
) {
    public static RevisaoCadastroDTO from(Motorista m) {
        return new RevisaoCadastroDTO(
                m.getNome(), m.getCpf(), m.getRg(), m.getCnh(), m.getTelefone(), m.getEmail(),
                m.getIdade(), m.getGenero(), m.getRgDocumentoBase64(), m.getCnhDocumentoBase64(), m.getCriadoEm()
        );
    }
}
