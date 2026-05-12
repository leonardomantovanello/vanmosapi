package com.vanmos.van.exception;

import com.vanmos.van.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * Handler global de exceções — intercepta TODAS as exceções antes de chegarem ao cliente.
 *
 * MAPEAMENTO COMPLETO:
 *  NoHandlerFoundException          → 404  rota não existe (middleware de 404)
 *  HttpMessageNotReadableException  → 400  body JSON malformado
 *  MethodArgumentNotValidException  → 400  Bean Validation falhou (@Valid)
 *  ResourceNotFoundException        → 404  recurso não encontrado no banco
 *  ForbiddenException               → 403  recurso não pertence ao usuário
 *  DuplicateResourceException       → 409  conflito de dados únicos
 *  ValidationException              → 422  regra de negócio violada
 *  IllegalArgumentException         → 400  argumento inválido
 *  DataIntegrityViolationException  → 409  constraint do banco violada
 *  EntityNotFoundException (JPA)    → 404  entidade JPA não encontrada
 *  Exception (fallback)             → 500  erro interno — nunca expõe detalhes
 *
 * TODOS os retornos usam ApiResponse<T> — formato único e previsível para o frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // Ponto 1: Middleware de 404 para rotas inexistentes
    // Requer: spring.mvc.throw-exception-if-no-handler-found=true
    //         spring.web.resources.add-mappings=false
    //         (adicionados no application.properties)
    // -------------------------------------------------------------------------

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoRoute(NoHandlerFoundException ex) {
        String metodo = ex.getHttpMethod().replaceAll("[\r\n]", "_");
        String url     = ex.getRequestURL().replaceAll("[\r\n]", "_");
        log.warn("Rota não encontrada: {} {}", metodo, url);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(404, "Rota não encontrada.")
        );
    }

    // -------------------------------------------------------------------------
    // Body JSON malformado (ex: vírgula faltando, tipo errado)
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(HttpMessageNotReadableException ex) {
        // Sanitiza mensagem antes de logar — previne log injection (CWE-117)
        String msg = ex.getMessage() != null ? ex.getMessage().replaceAll("[\r\n]", "_") : "null";
        log.warn("JSON malformado: {}", msg);
        return ResponseEntity.badRequest().body(
            ApiResponse.error(400, "O corpo da requisição está malformado ou com tipo de dado incorreto.")
        );
    }

    // -------------------------------------------------------------------------
    // Bean Validation (@Valid nos controllers)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBeanValidation(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(
            ApiResponse.validationError("Dados inválidos. Corrija os campos indicados.", erros)
        );
    }

    // -------------------------------------------------------------------------
    // Exceções de negócio
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(404, ex.getMessage())
        );
    }

    /** Ponto 2: recurso não pertence ao usuário autenticado → 403 */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error(403, ex.getMessage())
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.error(409, ex.getMessage())
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiResponse.error(422, ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
            ApiResponse.error(400, ex.getMessage())
        );
    }

    // -------------------------------------------------------------------------
    // Violações de constraint do banco — detalhe técnico fica só no log
    // -------------------------------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage() != null
                ? ex.getMostSpecificCause().getMessage().replaceAll("[\r\n]", "_") : "null";
        log.error("Violação de integridade no banco: {}", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.error(409, "Operação viola uma regra de integridade dos dados. Verifique campos únicos.")
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().replaceAll("[\r\n]", "_") : "null";
        log.warn("EntityNotFoundException: {}", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(404, "Recurso não encontrado.")
        );
    }

    // -------------------------------------------------------------------------
    // Fallback genérico — NUNCA expõe stack trace ou mensagem interna ao cliente
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().replaceAll("[\r\n]", "_") : "null";
        log.error("Erro interno não tratado: {}", msg, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(500, "Ocorreu um erro interno. Tente novamente ou contate o suporte.")
        );
    }
}
