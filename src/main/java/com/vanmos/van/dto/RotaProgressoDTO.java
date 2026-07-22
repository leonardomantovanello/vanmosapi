package com.vanmos.van.dto;

// Um único formato pras duas visões de /api/rotas/progresso (ver
// RotaController): pro MOTORISTA, alunoAtualNome vem preenchido e
// suaOrdem/vocEhAtual/vocEhOProximo ficam null; pro RESPONSAVEL é o
// contrário — nunca expõe o nome/endereço de OUTRO aluno da rota, só a
// posição relativa do próprio filho.
public record RotaProgressoDTO(
        boolean ativo,
        Integer ordemAtual,
        int totalParadas,
        String alunoAtualNome,
        Integer suaOrdem,
        Boolean vocEhAtual,
        Boolean vocEhOProximo
) {
}
