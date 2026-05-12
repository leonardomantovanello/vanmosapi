package com.vanmos.van.controller;

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
 *  3. Este endpoint valida o refresh token e emite um novo access token.
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Refresh token é obrigatório"
            ));
        }

        // Valida que é um refresh token legítimo e não expirado
        if (!jwtUtil.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Refresh token inválido ou expirado. Faça login novamente."
            ));
        }

        String subject = jwtUtil.extractSubject(refreshToken);

        // Emite novo access token — role padrão RESPONSAVEL para refresh via este endpoint
        // Para múltiplas roles, armazene a role no refresh token ou consulte o banco
        String novoAccessToken = jwtUtil.generateAccessToken(subject, "RESPONSAVEL", null);

        return ResponseEntity.ok(Map.of(
                "sucesso",      true,
                "accessToken",  novoAccessToken
        ));
    }
}
