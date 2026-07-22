package com.vanmos.van.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarParadaRequest(
        @NotNull(message = "Aluno é obrigatório") Long alunoId
) {
}
