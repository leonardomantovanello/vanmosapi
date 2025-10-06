package com.vanmos.van.controller;

import com.vanmos.van.model.entity.LoginAdmin;
import com.vanmos.van.model.service.LoginAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/login-admin")
@CrossOrigin(origins = "*")
public class LoginAdminController {

    @Autowired
    private LoginAdminService loginAdminService;

    @GetMapping
    public List<LoginAdmin> listarTodos() {
        return loginAdminService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<LoginAdmin> buscarPorId(@PathVariable Long id) {
        return loginAdminService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String emailOuCpf = loginData.get("email_ou_cpf");
            String senha = loginData.get("senha");
            
            if (emailOuCpf == null || senha == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Email/CPF e senha são obrigatórios");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Buscar no banco de dados
            LoginAdmin loginAdmin = loginAdminService.findByEmailOuCpfAndSenha(emailOuCpf.trim(), senha.trim());
            
            if (loginAdmin != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("mensagem", "Login admin realizado com sucesso");
                Map<String, Object> usuarioInfo = new HashMap<>();
                usuarioInfo.put("id", loginAdmin.getId());
                usuarioInfo.put("nome", "Administrador");
                usuarioInfo.put("email", loginAdmin.getEmailOuCpf());
                response.put("usuario", usuarioInfo);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Credenciais inválidas");
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
        response.put("mensagem", "Logout admin realizado com sucesso");
        return ResponseEntity.ok(response);
    }
}