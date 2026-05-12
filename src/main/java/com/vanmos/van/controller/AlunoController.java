package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Todas as respostas usam ApiResponse<T> — formato único e previsível.
 *
 * PROPRIEDADE DE RECURSO (ponto 2):
 *  Alunos pertencem a um motorista. A verificação completa de propriedade
 *  requer uma FK motorista_id na tabela alunos (melhoria futura de schema).
 *  Por ora, apenas ADMINs e MOTORISTAs autenticados acessam este recurso
 *  (garantido pelo SecurityConfig — .anyRequest().authenticated()).
 *  Quando a FK for adicionada, injete OwnershipValidator e valide aqui.
 */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Aluno>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Alunos listados.", alunoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Aluno>> buscarPorId(@PathVariable Long id) {
        Aluno aluno = alunoService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Aluno", id));
        return ResponseEntity.ok(ApiResponse.ok("Aluno encontrado.", aluno));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Aluno>> criar(@Valid @RequestBody Aluno aluno) {
        return ResponseEntity.status(201).body(
            ApiResponse.created("Aluno criado com sucesso.", alunoService.save(aluno))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Aluno>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Aluno aluno) {
        return ResponseEntity.ok(
            ApiResponse.ok("Aluno atualizado.", alunoService.update(id, aluno))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        alunoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Aluno removido."));
    }
}
