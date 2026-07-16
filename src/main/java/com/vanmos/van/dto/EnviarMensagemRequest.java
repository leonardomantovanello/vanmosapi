package com.vanmos.van.dto;

import jakarta.validation.constraints.NotBlank;

public record EnviarMensagemRequest(@NotBlank(message = "Texto é obrigatório") String texto) {
}
