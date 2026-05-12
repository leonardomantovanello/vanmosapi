package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import com.vanmos.van.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de login para usuários (responsáveis).
 *
 * MUDANÇAS APLICADAS:
 *  1. BCrypt: senha comparada com passwordEncoder.matches() — nunca em plaintext.
 *  2. JWT: retorna access token (15 min) + refresh token (7 dias) no login.
 *  3. NPE corrigido: usuario == null verificado antes de acessar qualquer campo.
 *  4. Mensagem genérica: "Credenciais inválidas" para usuário não encontrado E
 *     senha errada — evita enumeração de usuários (user enumeration attack).
 *  5. @CrossOrigin removido — CORS centralizado no SecurityConfig.
 *
 * ENDPOINT DE REFRESH: POST /api/auth/refresh (ver AuthController)
 */
@RestController
@RequestMapping("/api/login")
// @CrossOrigin REMOVIDO — gerenciado centralmente pelo SecurityConfig
public class LoginController {

    @Autowired
    private CadastroService cadastroService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String emailOuCpf = loginData.get("emailOuCpf");
        String senha       = loginData.get("senha");

        if (emailOuCpf == null || emailOuCpf.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Email/CPF e senha são obrigatórios"
            ));
        }

        // Busca o usuário por email ou CPF via query no banco (não mais findAll em memória)
        Cadastro usuario = cadastroService.findByEmailOrCpf(emailOuCpf.trim());

        // Mensagem genérica para não revelar se o usuário existe ou não (user enumeration)
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        if (!usuario.getAtivo()) {
            return ResponseEntity.status(403).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Conta inativa. Entre em contato com o administrador."
            ));
        }

        // BCrypt: compara a senha digitada com o hash armazenado no banco
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        // Gera os tokens JWT
        String accessToken  = jwtUtil.generateAccessToken(usuario.getEmail(), "RESPONSAVEL", usuario.getId());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        Map<String, Object> usuarioInfo = new HashMap<>();
        usuarioInfo.put("id",    usuario.getId());
        usuarioInfo.put("nome",  usuario.getNome());
        usuarioInfo.put("email", usuario.getEmail());

        return ResponseEntity.ok(Map.of(
                "sucesso",       true,
                "mensagem",      "Login realizado com sucesso",
                "accessToken",   accessToken,
                "refreshToken",  refreshToken,
                "usuario",       usuarioInfo
        ));
    }

    /**
     * Logout stateless: o frontend descarta os tokens.
     * Para invalidação server-side real, implemente uma blacklist de tokens com Redis.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of(
                "sucesso",  true,
                "mensagem", "Logout realizado com sucesso. Descarte os tokens no cliente."
        ));
    }
}
