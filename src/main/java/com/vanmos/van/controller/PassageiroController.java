package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.service.PassageiroService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passageiros")
public class PassageiroController {

    @Autowired private PassageiroService  passageiroService;
    @Autowired private JwtUtil            jwtUtil;
    @Autowired private OwnershipValidator ownership;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> cadastrar(@Valid @RequestBody Passageiro passageiro) {
        passageiro.setAtivo(false);
        Passageiro resultado = passageiroService.save(passageiro);
        resultado.setSenha(null);
        return ResponseEntity.status(201).body(
            ApiResponse.created("Cadastro realizado. Aguarde ativação.", resultado)
        );
        // IllegalArgumentException (CPF/email duplicado, CPF inválido) e demais
        // são capturadas pelo GlobalExceptionHandler → HTTP 400
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<Passageiro>>> listarTodos() {
        List<Passageiro> lista = passageiroService.findAll();
        lista.forEach(p -> p.setSenha(null));
        return ResponseEntity.ok(ApiResponse.ok("Passageiros listados.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Passageiro>> buscarPorId(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        Passageiro p = passageiroService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Passageiro", id));
        p.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Passageiro encontrado.", p));
    }

    // Sem @Valid: é uma edição parcial pelo admin (nome/email/cpf/idade/genero),
    // sem senha nem tipo no corpo — as regras de @NotBlank da entidade (pensadas
    // para o cadastro completo) rejeitariam qualquer edição feita por aqui.
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Passageiro>> atualizar(
            @PathVariable Long id, @RequestBody Passageiro passageiro) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        Passageiro atualizado = passageiroService.update(id, passageiro);
        atualizado.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Passageiro atualizado.", atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        passageiroService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Passageiro removido."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/ativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> ativarPerfil(@PathVariable Long id) {
        passageiroService.updateCodStatus(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário ativado com sucesso."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/inativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> inativarPerfil(@PathVariable Long id) {
        passageiroService.inativarCadastro(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário inativado com sucesso."));
    }

    @GetMapping("/verificar-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verificarEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok("Verificação concluída.",
            Map.of("disponivel", !passageiroService.emailJaCadastrado(email))
        ));
    }
}
