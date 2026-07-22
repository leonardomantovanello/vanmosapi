package com.vanmos.van.dto;

// Junta RotaParada com os dados do Aluno que ela referencia, pra evitar que
// o app precise cruzar /api/rotas com /api/alunos manualmente.
public record RotaParadaDTO(
        Long id,
        Long alunoId,
        String nome,
        String enderecoEmbarque,
        String enderecoDesembarque,
        String escola,
        String turno,
        int ordem
) {
}
