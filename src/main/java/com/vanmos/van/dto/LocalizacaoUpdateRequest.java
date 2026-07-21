package com.vanmos.van.dto;

/**
 * Payload que o motorista publica em /app/localizacao. Sem @Valid porque
 * mensagens STOMP não passam pelo GlobalExceptionHandler (esse é só pra
 * requisições HTTP) — LocalizacaoController valida manualmente.
 */
public record LocalizacaoUpdateRequest(Double lat, Double lng) {
}
