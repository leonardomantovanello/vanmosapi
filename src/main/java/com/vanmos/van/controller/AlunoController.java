package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.service.AlunoService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Todas as respostas usam ApiResponse<T> — formato único e previsível.
 *
 * PROPRIEDADE DE RECURSO (ponto 2):
 *  Alunos têm responsavel_id (FK para cadastro.id — quem loga como RESPONSAVEL).
 *  RESPONSAVEL só lista/vê os próprios alunos. ADMIN/MOTORISTA veem todos —
 *  ainda não existe FK motorista_id/van_id para restringir por rota do motorista
 *  (melhoria futura de schema).
 */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    @Autowired private AlunoService       alunoService;
    @Autowired private OwnershipValidator ownership;
    @Autowired private JwtUtil            jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Aluno>>> listarTodos() {
        if ("RESPONSAVEL".equals(ownership.getCurrentRole())) {
            Long currentUserId = ownership.getCurrentUserId(jwtUtil);
            return ResponseEntity.ok(ApiResponse.ok("Alunos listados.", alunoService.findByResponsavelId(currentUserId)));
        }
        return ResponseEntity.ok(ApiResponse.ok("Alunos listados.", alunoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Aluno>> buscarPorId(@PathVariable Long id) {
        Aluno aluno = alunoService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Aluno", id));

        if ("RESPONSAVEL".equals(ownership.getCurrentRole())) {
            Long currentUserId = ownership.getCurrentUserId(jwtUtil);
            ownership.validateOwnership(aluno.getResponsavelId(), currentUserId, "aluno");
        }
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
