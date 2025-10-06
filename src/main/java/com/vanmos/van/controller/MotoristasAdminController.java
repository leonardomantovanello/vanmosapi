package com.vanmos.van.controller;

import com.vanmos.van.model.entity.MotoristasAdmin;
import com.vanmos.van.model.service.MotoristasAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/motoristas-admin")
@CrossOrigin(origins = "*")
public class MotoristasAdminController {

    @Autowired
    private MotoristasAdminService motoristasAdminService;

    @GetMapping
    public List<MotoristasAdmin> listarTodos() {
        System.out.println("Listando todos os motoristas...");
        List<MotoristasAdmin> lista = motoristasAdminService.findAll();
        System.out.println("Total de motoristas encontrados: " + lista.size());
        return lista;
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "API funcionando");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public Optional<MotoristasAdmin> buscarPorId(@PathVariable Long id) {
        return motoristasAdminService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody MotoristasAdmin motoristasAdmin) {
        try {
            System.out.println("Recebendo dados:");
            System.out.println("Nome: " + motoristasAdmin.getNomeCompleto());
            System.out.println("Gmail: " + motoristasAdmin.getGmail());
            System.out.println("CPF: " + motoristasAdmin.getCpf());
            System.out.println("CNH: " + motoristasAdmin.getCnh());
            System.out.println("Placa: " + motoristasAdmin.getPlacaVan());
            System.out.println("Modelo: " + motoristasAdmin.getModeloVan());
            
            MotoristasAdmin novoMotorista = motoristasAdminService.save(motoristasAdmin);
            System.out.println("Motorista salvo com ID: " + novoMotorista.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Motorista admin criado com sucesso");
            response.put("motorista", novoMotorista);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao criar motorista admin: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody MotoristasAdmin motoristasAdmin) {
        try {
            if (!motoristasAdminService.existsById(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Motorista admin não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            MotoristasAdmin motoristaAtualizado = motoristasAdminService.update(id, motoristasAdmin);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Motorista admin atualizado com sucesso");
            response.put("motorista", motoristaAtualizado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao atualizar motorista admin: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            if (!motoristasAdminService.existsById(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Motorista admin não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            motoristasAdminService.deleteById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Motorista admin deletado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao deletar motorista admin: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
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
            
            MotoristasAdmin motorista = motoristasAdminService.findByGmailOrCpf(emailOuCpf.trim());
            
            if (motorista != null && motorista.getSenha() != null) {
                if (!motorista.isAtivo()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", false);
                    response.put("mensagem", "Motorista inativo. Entre em contato com o administrador");
                    return ResponseEntity.badRequest().body(response);
                }
                if (motorista.getSenha().trim().equals(senha.trim())) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("mensagem", "Login realizado com sucesso");
                    Map<String, Object> motoristaInfo = new HashMap<>();
                    motoristaInfo.put("id", motorista.getId());
                    motoristaInfo.put("nomeCompleto", motorista.getNomeCompleto());
                    motoristaInfo.put("gmail", motorista.getGmail());
                    motoristaInfo.put("placaVan", motorista.getPlacaVan());
                    motoristaInfo.put("modeloVan", motorista.getModeloVan());
                    response.put("motorista", motoristaInfo);
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", false);
                    response.put("mensagem", "Senha incorreta");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Motorista admin não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PutMapping("/{id}/ativar")
    public ResponseEntity<?> ativar(@PathVariable Long id) {
        try {
            MotoristasAdmin motorista = motoristasAdminService.ativar(id);
            if (motorista != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("mensagem", "Motorista ativado com sucesso");
                response.put("motorista", motorista);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Motorista não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao ativar motorista: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PutMapping("/{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable Long id) {
        try {
            MotoristasAdmin motorista = motoristasAdminService.inativar(id);
            if (motorista != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("mensagem", "Motorista inativado com sucesso");
                response.put("motorista", motorista);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Motorista não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao inativar motorista: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}