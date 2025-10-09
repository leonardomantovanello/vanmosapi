package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private CadastroService cadastroService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String emailOuCpf = loginData.get("emailOuCpf");
            String senha = loginData.get("senha");
            

            
            if (emailOuCpf == null || senha == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Email/CPF e senha são obrigatórios");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Buscar usuário por email ou CPF
            Cadastro usuario = cadastroService.findByEmailOrCpf(emailOuCpf.trim());
            

            
            if (usuario != null && usuario.getSenha() != null && usuario.getAtivo() != false) {
                // Comparar senhas (removendo espaços em branco)
                if (usuario.getSenha().trim().equals(senha.trim())) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("mensagem", "Login realizado com sucesso");
                    Map<String, Object> usuarioInfo = new HashMap<>();
                    usuarioInfo.put("id", usuario.getId());
                    usuarioInfo.put("nome", usuario.getNome());
                    usuarioInfo.put("email", usuario.getEmail());
                    response.put("usuario", usuarioInfo);
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", false);
                    response.put("mensagem", "Senha incorreta");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {

                if (usuario.getAtivo()==false) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", false);
                    response.put("mensagem", "Usuário inativo");
                    return ResponseEntity.badRequest().body(response);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Usuário não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("mensagem", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }
}