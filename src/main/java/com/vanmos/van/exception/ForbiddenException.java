package com.vanmos.van.exception;

/**
 * Lançada quando um usuário autenticado tenta modificar um recurso
 * que não lhe pertence. Mapeada para HTTP 403 no GlobalExceptionHandler.
 *
 * DIFERENÇA entre 401 e 403:
 *  401 Unauthorized → usuário não está autenticado (sem token ou token inválido)
 *  403 Forbidden    → usuário está autenticado mas não tem permissão sobre ESTE recurso
 *
 * USO: throw new ForbiddenException("Você não tem permissão para alterar este cadastro.");
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
