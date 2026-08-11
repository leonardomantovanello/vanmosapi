package com.vanmos.van.dto;

import com.vanmos.van.model.entity.Passageiro;

import java.time.LocalDateTime;

/**
 * Dados expostos na página pública de revisão de cadastro (ver
 * CadastroAprovacaoController) — sem senha, id ou status, que não são
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
    public static RevisaoCadastroDTO from(Passageiro p) {
        return new RevisaoCadastroDTO(
                p.getNome(), p.getCpf(), p.getRg(), p.getCnh(), p.getTelefone(), p.getEmail(),
                p.getIdade(), p.getGenero(), p.getRgDocumentoBase64(), p.getCnhDocumentoBase64(), p.getCriadoEm()
        );
    }
}
