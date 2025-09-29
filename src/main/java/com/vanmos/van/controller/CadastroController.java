package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cadastro")
@CrossOrigin(origins = "*")
public class CadastroController {

    @Autowired
    private CadastroService cadastroService;

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Cadastro cadastro) {
        try {
            Cadastro resultado = cadastroService.save(cadastro);
            return ResponseEntity.ok(resultado);
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
    
    @DeleteMapping
    public ResponseEntity<String> limparTudo() {
        cadastroService.deleteAll();
        return ResponseEntity.ok("Banco de dados limpo com sucesso!");
    }
}