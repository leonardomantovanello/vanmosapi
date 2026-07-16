package com.vanmos.van.controller;

import com.vanmos.van.model.repository.PassageiroRepository;
import com.vanmos.van.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável pela renovação de tokens JWT.
 *
 * FLUXO DE REFRESH TOKEN:
 *  1. Access token expira após 15 minutos.
 *  2. Frontend detecta resposta 401 e chama POST /api/auth/refresh
 *     enviando o refresh token no body.
 *  3. Este endpoint valida o refresh token, busca o usuário no banco
 *     para recuperar a role e o userId corretos, e emite um novo access token.
 *  4. O refresh token em si NÃO é renovado — expira em 7 dias e força novo login.
 *
 * SEGURANÇA:
 *  - O refresh token deve ser armazenado em cookie HttpOnly no frontend
 *    (não em localStorage) para evitar acesso via JavaScript (XSS).
 *  - Este endpoint é público (ver SecurityConfig) pois o usuário não tem
 *    access token válido quando chega aqui.
 *
 * MELHORIA FUTURA: Implemente refresh token rotation — cada uso do refresh
 * token gera um novo refresh token e invalida o anterior (requer Redis).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PassageiroRepository passageiroRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestHeader(name = "X-Refresh-Request", required = false) String refreshRequestHeader,
            @RequestBody(required = false) Map<String, String> body) {

        String refreshTokenFromBody = body != null ? body.get("refreshToken") : null;
        boolean cookieRefreshRequested = refreshTokenCookie != null
                && !refreshTokenCookie.isBlank()
                && "true".equalsIgnoreCase(refreshRequestHeader);
        String refreshToken = cookieRefreshRequested ? refreshTokenCookie : refreshTokenFromBody;

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Refresh token é obrigatório"
            ));
        }

        if (!jwtUtil.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Refresh token inválido ou expirado. Faça login novamente."
            ));
        }

        String subject = jwtUtil.extractSubject(refreshToken);

        // Busca o usuário no banco para recuperar role e userId corretos.
        // O subject é o email do usuário (definido no login).
        return passageiroRepository.findByEmailIgnoreCase(subject)
                .map(usuario -> {
                    // Role depende do tipo de cadastro (MOTORISTA ou PASSAGEIRO) —
                    // mesma lógica usada no login (ver LoginController).
                    String role = "MOTORISTA".equals(usuario.getTipo()) ? "MOTORISTA" : "RESPONSAVEL";
                    String novoAccessToken = jwtUtil.generateAccessToken(subject, role, usuario.getId());
                    return ResponseEntity.ok(Map.of(
                            "sucesso",      true,
                            "accessToken",  novoAccessToken
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of(
                        "sucesso", false,
                        "mensagem", "Usuário não encontrado. Faça login novamente."
                )));
    }
}
