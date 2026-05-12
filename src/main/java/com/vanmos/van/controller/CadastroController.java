package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import com.vanmos.van.model.service.LoginSucessoException;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    @Autowired private CadastroService    cadastroService;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private JwtUtil            jwtUtil;
    @Autowired private OwnershipValidator ownership;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> cadastrar(@Valid @RequestBody Cadastro cadastro) {
        cadastro.setAtivo(false);
        cadastro.setSenha(passwordEncoder.encode(cadastro.getSenha()));

        try {
            Cadastro resultado = cadastroService.save(cadastro);
            // Nunca retornar o hash da senha na resposta
            resultado.setSenha(null);
            return ResponseEntity.status(201).body(
                ApiResponse.created("Cadastro realizado. Aguarde ativação.", resultado)
            );
        } catch (LoginSucessoException e) {
            // Usuário já existe com os mesmos dados — emite tokens de login
            Cadastro usuario = e.getUsuario();
            String accessToken  = jwtUtil.generateAccessToken(usuario.getEmail(), "RESPONSAVEL", usuario.getId());
            String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());
            return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso.", Map.of(
                "id",           usuario.getId(),
                "nome",         usuario.getNome(),
                "email",        usuario.getEmail(),
                "accessToken",  accessToken,
                "refreshToken", refreshToken
            )));
        }
        // IllegalArgumentException e demais são capturadas pelo GlobalExceptionHandler
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<Cadastro>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Cadastros listados.", cadastroService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cadastro>> buscarPorId(@PathVariable Long id) {
        // Ponto 2: usuário só pode ver o próprio cadastro; ADMIN vê qualquer um
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "cadastro");

        Cadastro c = cadastroService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Cadastro", id));
        c.setSenha(null); // nunca expor hash
        return ResponseEntity.ok(ApiResponse.ok("Cadastro encontrado.", c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Cadastro>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Cadastro cadastro) {

        // Ponto 2: valida que o recurso pertence ao usuário autenticado
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "cadastro");

        if (cadastro.getSenha() != null && !cadastro.getSenha().isBlank()) {
            cadastro.setSenha(passwordEncoder.encode(cadastro.getSenha()));
        }
        Cadastro atualizado = cadastroService.update(id, cadastro);
        atualizado.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Cadastro atualizado.", atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "cadastro");

        cadastroService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Cadastro removido."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @PutMapping("/{id}/ativar")
    public ResponseEntity<ApiResponse<Void>> ativarPerfil(@PathVariable Long id) {
        cadastroService.updateCodStatus(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário ativado com sucesso."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @PutMapping("/{id}/inativar")
    public ResponseEntity<ApiResponse<Void>> inativarPerfil(@PathVariable Long id) {
        cadastroService.inativarCadastro(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário inativado com sucesso."));
    }

    @GetMapping("/verificar-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verificarEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok("Verificação concluída.",
            Map.of("disponivel", !cadastroService.emailJaCadastrado(email))
        ));
    }
}
