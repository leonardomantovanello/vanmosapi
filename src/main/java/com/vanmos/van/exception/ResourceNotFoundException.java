package com.vanmos.van.exception;

/**
 * Exceção lançada quando um recurso não é encontrado no banco.
 * Mapeada para HTTP 404 no GlobalExceptionHandler.
 *
 * USO: throw new ResourceNotFoundException("Aluno", id);
 * MENSAGEM PÚBLICA: "Aluno com id 42 não encontrado."
 * — nunca expõe detalhes de tabela, coluna ou SQL.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resource;
    private final Object identifier;

    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " com id " + identifier + " não encontrado.");
        this.resource   = resource;
        this.identifier = identifier;
    }

    public String getResource()    { return resource; }
    public Object getIdentifier()  { return identifier; }
}
