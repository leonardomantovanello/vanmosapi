package com.vanmos.van.exception;

/**
 * Exceção lançada quando dados de entrada falham na validação de negócio.
 * Mapeada para HTTP 422 no GlobalExceptionHandler.
 *
 * Diferente do HTTP 400 (Bad Request) que é para erros de formato/sintaxe,
 * o 422 (Unprocessable Entity) indica que o formato está correto mas
 * a regra de negócio não foi satisfeita (ex: CPF inválido, idade negativa).
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
