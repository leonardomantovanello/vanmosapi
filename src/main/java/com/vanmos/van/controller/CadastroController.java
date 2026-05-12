package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import com.vanmos.van.model.service.LoginSucessoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cadastro")
@CrossOrigin(origins = "*")
public class CadastroController {

    @Autowired
    private CadastroService cadastroService;

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Cadastro cadastro) {
        try {
            cadastro.setAtivo(false);
            Cadastro resultado = cadastroService.save(cadastro);
            return ResponseEntity.ok(resultado);
        } catch (LoginSucessoException e) {
            Cadastro usuario = e.getUsuario();
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Login realizado com sucesso");
            Map<String, Object> usuarioInfo = new HashMap<>();
            usuarioInfo.put("id", usuario.getId());
            usuarioInfo.put("nome", usuario.getNome());
            usuarioInfo.put("email", usuario.getEmail());
            response.put("usuario", usuarioInfo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("erro", e.getMessage());
            return ResponseEntity.status(409).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @GetMapping
    public List<Cadastro> listarTodos() {
        return cadastroService.findAll();
    }
    
    @GetMapping("/{id}")
    public Optional<Cadastro> buscarPorId(@PathVariable Long id) {
        return cadastroService.findById(id);
    }
    
    @PutMapping("/{id}")
    public Cadastro atualizar(@PathVariable Long id, @RequestBody Cadastro cadastro) {
        return cadastroService.update(id, cadastro);
    }
    
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        cadastroService.deleteById(id);
    }

    @PutMapping("/{id}/ativar")
    public void ativarperfil(@PathVariable Long id) {

        System.out.println("ID recebido: " + id);
        cadastroService.updateCodStatus(id);
    }

    @PutMapping("/{id}/inativar")
    public void inativarperfil(@PathVariable Long id) {

        System.out.println("ID recebido para inativar: " + id);
        cadastroService.inativarCadastro(id);
    }
    
    @DeleteMapping
    public ResponseEntity<String> limparTudo() {
        cadastroService.deleteAll();
        return ResponseEntity.ok("Banco de dados limpo com sucesso!");
    }
    
    @GetMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("disponivel", !cadastroService.emailJaCadastrado(email));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teste-login/{emailOuCpf}")
    public ResponseEntity<?> testeLogin(@PathVariable String emailOuCpf) {
        try {
            Cadastro usuario = cadastroService.findByEmailOrCpf(emailOuCpf);
            if (usuario != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("encontrado", true);
                response.put("id", usuario.getId());
                response.put("nome", usuario.getNome());
                response.put("email", usuario.getEmail());
                response.put("cpf", usuario.getCpf());
                response.put("senha", "[OCULTA - Tamanho: " + (usuario.getSenha() != null ? usuario.getSenha().length() : 0) + "]");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("encontrado", false);
                response.put("mensagem", "Usuário não encontrado");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}