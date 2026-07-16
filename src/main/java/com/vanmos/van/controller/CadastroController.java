package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    @Autowired private CadastroService    cadastroService;
    @Autowired private JwtUtil            jwtUtil;
    @Autowired private OwnershipValidator ownership;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> cadastrar(@Valid @RequestBody Cadastro cadastro) {
        cadastro.setAtivo(false);
        Cadastro resultado = cadastroService.save(cadastro);
        resultado.setSenha(null);
        return ResponseEntity.status(201).body(
            ApiResponse.created("Cadastro realizado. Aguarde ativação.", resultado)
        );
        // IllegalArgumentException (CPF/email duplicado, CPF inválido) e demais
        // são capturadas pelo GlobalExceptionHandler → HTTP 400
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<Cadastro>>> listarTodos() {
        List<Cadastro> lista = cadastroService.findAll();
        lista.forEach(c -> c.setSenha(null));
        return ResponseEntity.ok(ApiResponse.ok("Cadastros listados.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cadastro>> buscarPorId(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "cadastro");

        Cadastro c = cadastroService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Cadastro", id));
        c.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Cadastro encontrado.", c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Cadastro>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Cadastro cadastro) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "cadastro");

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
    @RequestMapping(value = "/{id}/ativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> ativarPerfil(@PathVariable Long id) {
        cadastroService.updateCodStatus(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário ativado com sucesso."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/inativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
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
