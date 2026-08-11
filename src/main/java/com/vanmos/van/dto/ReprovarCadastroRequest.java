package com.vanmos.van.dto;

/**
 * Corpo aceito por POST /api/passageiros/aprovacao/{token}/reprovar —
 * motivo é opcional (o suporte pode reprovar sem justificar).
 */
public record ReprovarCadastroRequest(String motivo) {
}
