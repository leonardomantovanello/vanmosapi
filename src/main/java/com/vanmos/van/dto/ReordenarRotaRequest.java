package com.vanmos.van.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// `ids` deve conter o id de TODA parada atual do motorista, na nova ordem
// desejada — ver RotaParadaService.reordenar.
public record ReordenarRotaRequest(
        @NotEmpty(message = "Informe a lista de paradas na nova ordem") List<Long> ids
) {
}
