package com.vanmos.van.controller;

import com.vanmos.van.model.entity.LoginAdmin;
import com.vanmos.van.model.service.LoginAdminService;
import com.vanmos.van.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de login para administradores.
 *
 * MUDANÇAS APLICADAS:
 *  1. GET /api/login-admin e GET /api/login-admin/{id} REMOVIDOS —
 *     expunham lista de admins com senhas em plaintext para qualquer pessoa.
 *  2. BCrypt: senha comparada com passwordEncoder.matches().
 *  3. JWT: retorna access token com role "ADMIN" + refresh token.
 *  4. findByEmailOuCpfAndSenha SUBSTITUÍDO por findByEmailOuCpf —
 *     a senha não é mais passada para o banco; a comparação é feita em Java com BCrypt.
 *  5. @CrossOrigin removido — CORS centralizado no SecurityConfig.
 */
@RestController
@RequestMapping("/api/login-admin")
// @CrossOrigin REMOVIDO — gerenciado centralmente pelo SecurityConfig
public class LoginAdminController {

    @Autowired
    private LoginAdminService loginAdminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /api/login-admin     — REMOVIDO (expunha todos os admins com senhas)
    // GET /api/login-admin/{id} — REMOVIDO (expunha admin individual com senha)

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String emailOuCpf = loginData.get("email_ou_cpf");
        String senha       = loginData.get("senha");

        if (emailOuCpf == null || emailOuCpf.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Email/CPF e senha são obrigatórios"
            ));
        }

        // Busca apenas por email/CPF — a senha NÃO vai para o banco
        LoginAdmin admin = loginAdminService.findByEmailOuCpf(emailOuCpf.trim());

        // Mensagem genérica para evitar user enumeration
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        // BCrypt: compara a senha digitada com o hash armazenado
        if (!passwordEncoder.matches(senha, admin.getSenha())) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        // Token com role ADMIN — SecurityConfig usa essa role para proteger rotas administrativas
        String accessToken  = jwtUtil.generateAccessToken(admin.getEmailOuCpf(), "ADMIN", admin.getId());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getEmailOuCpf());

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id",    admin.getId());
        adminInfo.put("nome",  "Administrador");
        adminInfo.put("email", admin.getEmailOuCpf());

        return ResponseEntity.ok(Map.of(
                "sucesso",      true,
                "mensagem",     "Login admin realizado com sucesso",
                "accessToken",  accessToken,
                "refreshToken", refreshToken,
                "usuario",      adminInfo
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of(
                "sucesso",  true,
                "mensagem", "Logout realizado com sucesso. Descarte os tokens no cliente."
        ));
    }
}
