package com.vanmos.van.security;

import com.vanmos.van.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Componente reutilizável para verificar propriedade de recurso (ponto 2).
 *
 * PROBLEMA RESOLVIDO — IDOR (Insecure Direct Object Reference):
 *  Sem esta verificação, um usuário autenticado com id=10 poderia fazer:
 *    PUT /api/cadastro/99  → alterando dados de outro usuário
 *    DELETE /api/alunos/5  → deletando aluno de outro motorista
 *  O Spring Security garante que o usuário está autenticado, mas NÃO
 *  verifica se o recurso pertence a ele. Essa é nossa responsabilidade.
 *
 * ESTRATÉGIA POR TIPO DE USUÁRIO:
 *
 *  RESPONSAVEL (role=RESPONSAVEL):
 *   - Pode ler/editar apenas seu próprio cadastro (id do token == id da URL)
 *   - Pode ler/editar apenas alunos vinculados a ele
 *
 *  MOTORISTA (role=MOTORISTA):
 *   - Pode ler/editar apenas seu próprio perfil de motorista
 *   - Pode ler/editar alunos da sua van
 *
 *  ADMIN (role=ADMIN):
 *   - Pode operar sobre qualquer recurso — sem restrição de propriedade
 *
 * INTEGRAÇÃO:
 *  Injete OwnershipValidator nos controllers e chame antes de qualquer
 *  operação de escrita (PUT, DELETE). Lança ForbiddenException se negado,
 *  capturada pelo GlobalExceptionHandler → HTTP 403.
 */
@Component
public class OwnershipValidator {

    /**
     * Extrai o usuário autenticado do SecurityContext.
     * O subject é o email/CPF definido no JWT durante o login.
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Retorna a role do usuário autenticado (ex: "RESPONSAVEL", "MOTORISTA", "ADMIN").
     */
    public String getCurrentRole() {
        return getAuthentication().getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }

    /**
     * Retorna o subject (email/CPF) do usuário autenticado.
     */
    public String getCurrentSubject() {
        return getAuthentication().getName();
    }

    /**
     * Verifica se o usuário autenticado é ADMIN.
     * ADMINs passam por todas as verificações de propriedade.
     */
    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    /**
     * Valida que o ID do recurso na URL corresponde ao ID do usuário autenticado.
     *
     * CASO DE USO PRINCIPAL: PUT /api/cadastro/{id}
     *  O userId extraído do JWT deve ser igual ao {id} da URL.
     *  Impede que usuário A altere o cadastro do usuário B.
     *
     * @param resourceId  ID do recurso na URL (ex: PathVariable)
     * @param ownerUserId ID do dono do recurso (extraído do banco ou do JWT)
     * @param resourceName nome legível para a mensagem de erro (ex: "cadastro")
     */
    public void validateOwnership(Long resourceId, Long ownerUserId, String resourceName) {
        if (isAdmin()) return; // ADMIN pode tudo

        if (ownerUserId == null || !ownerUserId.equals(resourceId)) {
            throw new ForbiddenException(
                "Você não tem permissão para modificar este " + resourceName + "."
            );
        }
    }

    /**
     * Valida que o subject (email/CPF) do token corresponde ao subject do recurso.
     *
     * CASO DE USO: quando o identificador do recurso é email/CPF, não um Long.
     *
     * @param resourceSubject email ou CPF do dono do recurso (vindo do banco)
     * @param resourceName    nome legível para a mensagem de erro
     */
    public void validateOwnershipBySubject(String resourceSubject, String resourceName) {
        if (isAdmin()) return;

        String currentSubject = getCurrentSubject();
        if (resourceSubject == null || !resourceSubject.equalsIgnoreCase(currentSubject)) {
            throw new ForbiddenException(
                "Você não tem permissão para modificar este " + resourceName + "."
            );
        }
    }

    /**
     * Lança ForbiddenException se o usuário NÃO for ADMIN.
     * Usado em operações exclusivamente administrativas.
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Esta operação requer privilégios de administrador.");
        }
    }

    /**
     * Extrai o userId do JWT via JwtUtil.
     * Atalho para uso nos controllers sem injetar JwtUtil diretamente.
     */
    public Long getCurrentUserId(JwtUtil jwtUtil) {
        // O token já foi validado pelo JwtAuthFilter — podemos extrair com segurança
        String authHeader = org.springframework.web.context.request.RequestContextHolder
                .getRequestAttributes() == null ? null :
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder
                                .getRequestAttributes())
                        .getRequest()
                        .getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(authHeader.substring(7));
        }
        return null;
    }
}
