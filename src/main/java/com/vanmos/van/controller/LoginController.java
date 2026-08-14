package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.entity.StatusCadastro;
import com.vanmos.van.model.service.MotoristaService;
import com.vanmos.van.model.service.PassageiroService;
import com.vanmos.van.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Login unificado — uma tela só, sem escolher "sou motorista/sou
 * passageiro". Tenta achar em passageiros (responsável/passageiro) primeiro;
 * se não achar, tenta em motorista. A mensagem de "credenciais inválidas"
 * nunca revela em qual das duas tabelas (ou em nenhuma) a busca bateu —
 * evita enumeração de usuários.
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
    private PassageiroService passageiroService;

    @Autowired
    private MotoristaService motoristaService;

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

        Passageiro passageiro = passageiroService.findByEmailOrCpf(emailOuCpf.trim());
        if (passageiro != null) {
            return autenticarPassageiro(passageiro, senha);
        }

        Motorista motorista = motoristaService.findByEmailOuCpf(emailOuCpf.trim());
        if (motorista != null) {
            return autenticarMotorista(motorista, senha);
        }

        // Mensagem genérica para não revelar se o usuário existe ou não (user enumeration)
        return ResponseEntity.status(401).body(Map.of(
                "sucesso", false,
                "mensagem", "Credenciais inválidas"
        ));
    }

    private ResponseEntity<?> autenticarPassageiro(Passageiro usuario, String senha) {
        if (!usuario.getAtivo()) {
            return ResponseEntity.status(403).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Conta inativa. Entre em contato com o administrador."
            ));
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        // Passageiro agora só guarda responsável/passageiro (motorista mora
        // em `motorista`, ver V18) — role é sempre RESPONSAVEL aqui. Mantido
        // como leitura de getTipo() em vez de constante fixa por segurança:
        // se algum dia sobrar uma linha tipo=MOTORISTA nessa tabela, ela não
        // vira RESPONSAVEL por engano.
        String role = "MOTORISTA".equals(usuario.getTipo()) ? "MOTORISTA" : "RESPONSAVEL";

        String accessToken  = jwtUtil.generateAccessToken(usuario.getEmail(), role, usuario.getId());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        Map<String, Object> usuarioInfo = new HashMap<>();
        usuarioInfo.put("id",    usuario.getId());
        usuarioInfo.put("nome",  usuario.getNome());
        usuarioInfo.put("email", usuario.getEmail());
        usuarioInfo.put("tipo",  usuario.getTipo());

        return ResponseEntity.ok(Map.of(
                "sucesso",       true,
                "mensagem",      "Login realizado com sucesso",
                "accessToken",   accessToken,
                "refreshToken",  refreshToken,
                "usuario",       usuarioInfo
        ));
    }

    private ResponseEntity<?> autenticarMotorista(Motorista usuario, String senha) {
        if (!usuario.isAtivo()) {
            // Mensagem ciente do status do fluxo de aprovação de motorista
            // (ver MotoristaAprovacaoController).
            String mensagemInativo = "Conta inativa. Entre em contato com o administrador.";
            if (usuario.getStatusCadastro() == StatusCadastro.PENDENTE) {
                mensagemInativo = "Seu cadastro está em análise. Você receberá um e-mail assim que for aprovado.";
            } else if (usuario.getStatusCadastro() == StatusCadastro.REPROVADO) {
                mensagemInativo = "Seu cadastro não foi aprovado"
                        + (usuario.getMotivoReprovacao() == null || usuario.getMotivoReprovacao().isBlank()
                                ? "." : ": " + usuario.getMotivoReprovacao() + ".")
                        + " Entre em contato com o suporte para mais informações.";
            }
            return ResponseEntity.status(403).body(Map.of(
                    "sucesso", false,
                    "mensagem", mensagemInativo
            ));
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            return ResponseEntity.status(401).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Credenciais inválidas"
            ));
        }

        String accessToken  = jwtUtil.generateAccessToken(usuario.getEmail(), "MOTORISTA", usuario.getId());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        Map<String, Object> usuarioInfo = new HashMap<>();
        usuarioInfo.put("id",    usuario.getId());
        usuarioInfo.put("nome",  usuario.getNome());
        usuarioInfo.put("email", usuario.getEmail());
        usuarioInfo.put("tipo",  "MOTORISTA");

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
