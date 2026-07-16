package com.vanmos.van.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarcarFaltaRequest(
        @NotNull(message = "Data é obrigatória") LocalDate data,
        String justificativa
) {
}
