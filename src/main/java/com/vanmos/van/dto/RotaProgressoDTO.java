package com.vanmos.van.dto;

// Um único formato pras duas visões de /api/rotas/progresso (ver
// RotaController): pro MOTORISTA, alunoAtualNome vem preenchido e
// suaOrdem/vocEhAtual/vocEhOProximo ficam null; pro RESPONSAVEL é o
// contrário — nunca expõe o nome/endereço de OUTRO aluno da rota, só a
// posição relativa do próprio filho.
//
// ordemAtual/suaOrdem/totalParadas refletem a posição dentro da rota "de
// hoje" (rota padrão menos os alunos com falta registrada pra hoje — ver
// RotaProgressoService#filtrarAtivasHoje), não o campo "ordem" bruto salvo
// em RotaParada. Se o próprio filho do responsável está ausente hoje,
// suaOrdem vem null (mesmo sinal já usado pra "não está na rota").
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
