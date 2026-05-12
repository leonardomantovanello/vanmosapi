package com.vanmos.van.exception;

/**
 * Exceção lançada quando há conflito de dados únicos (email, CPF, placa, RENAVAM).
 * Mapeada para HTTP 409 no GlobalExceptionHandler.
 *
 * USO: throw new DuplicateResourceException("E-mail já cadastrado.");
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
