package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Login;
import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.LoginService;
import com.vanmos.van.model.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private LoginService loginService;
    
    @Autowired
    private CadastroService cadastroService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Login login) {
        try {
            System.out.println("Login attempt - Email/CPF: " + login.getEmailOuCpf());
            System.out.println("Login attempt - Senha: " + login.getSenha());
            
            // Buscar usuário por email ou CPF
            Cadastro usuario = cadastroService.findByEmailOrCpf(login.getEmailOuCpf());
            
            if (usuario != null) {
                System.out.println("Usuário encontrado - Email: " + usuario.getEmail());
                System.out.println("Usuário encontrado - CPF: " + usuario.getCpf());
                System.out.println("Senha no banco: '" + usuario.getSenha() + "'");
                System.out.println("Senha enviada: '" + login.getSenha() + "'");
                System.out.println("Tamanho senha banco: " + usuario.getSenha().length());
                System.out.println("Tamanho senha enviada: " + login.getSenha().length());
                System.out.println("Senhas são iguais: " + usuario.getSenha().equals(login.getSenha()));
                
                // Tentar com trim para remover espaços
                String senhaBanco = usuario.getSenha().trim();
                String senhaEnviada = login.getSenha().trim();
                System.out.println("Comparando com trim: " + senhaBanco.equals(senhaEnviada));
                
                if (senhaBanco.equals(senhaEnviada)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("mensagem", "Login realizado com sucesso");
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", false);
                    response.put("mensagem", "Senha incorreta");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                System.out.println("Usuário não encontrado");
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Usuário não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public String logout() {
        return "Logout realizado com sucesso";
    }
    
    @GetMapping
    public List<Login> listarTodos() {
        return loginService.findAll();
    }
    
    @GetMapping("/{id}")
    public Optional<Login> buscarPorId(@PathVariable Long id) {
        return loginService.findById(id);
    }
    
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        loginService.deleteById(id);
    }
}