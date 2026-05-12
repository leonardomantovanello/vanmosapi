package com.vanmos.van.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Envelope de resposta padronizado para TODAS as operações da API.
 *
 * PROBLEMA RESOLVIDO (ponto 3):
 *  Antes, cada controller retornava um formato diferente:
 *   - Alguns retornavam a entidade diretamente
 *   - Outros retornavam Map<String, Object> montado manualmente
 *   - Erros tinham campos diferentes ("erro", "mensagem", "message")
 *
 * CONTRATO GARANTIDO — toda resposta segue este formato:
 *  {
 *    "sucesso":   true | false,
 *    "status":    200 | 201 | 400 | 401 | 403 | 404 | 409 | 422 | 500,
 *    "mensagem":  "texto legível para o usuário",
 *    "dados":     { ... } | [ ... ] | null,   (ausente se null)
 *    "erros":     [ "campo: motivo", ... ],   (ausente se null)
 *    "timestamp": "2025-01-01T00:00:00Z"
 *  }
 *
 * @param <T> tipo do payload em "dados"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean sucesso;
    private final int     status;
    private final String  mensagem;
    private final T       dados;
    private final List<String> erros;
    private final String  timestamp;

    private ApiResponse(boolean sucesso, int status, String mensagem, T dados, List<String> erros) {
        this.sucesso   = sucesso;
        this.status    = status;
        this.mensagem  = mensagem;
        this.dados     = dados;
        this.erros     = erros;
        this.timestamp = Instant.now().toString();
    }

    // --- Factories de sucesso ---

    public static <T> ApiResponse<T> ok(String mensagem, T dados) {
        return new ApiResponse<>(true, 200, mensagem, dados, null);
    }

    public static <T> ApiResponse<T> created(String mensagem, T dados) {
        return new ApiResponse<>(true, 201, mensagem, dados, null);
    }

    public static <T> ApiResponse<T> noContent(String mensagem) {
        return new ApiResponse<>(true, 200, mensagem, null, null);
    }

    // --- Factories de erro (usadas pelo GlobalExceptionHandler) ---

    public static <T> ApiResponse<T> error(int status, String mensagem) {
        return new ApiResponse<>(false, status, mensagem, null, null);
    }

    public static <T> ApiResponse<T> validationError(String mensagem, List<String> erros) {
        return new ApiResponse<>(false, 400, mensagem, null, erros);
    }

    // --- Getters ---

    public boolean isSucesso()     { return sucesso; }
    public int     getStatus()     { return status; }
    public String  getMensagem()   { return mensagem; }
    public T       getDados()      { return dados; }
    public List<String> getErros() { return erros; }
    public String  getTimestamp()  { return timestamp; }
}
