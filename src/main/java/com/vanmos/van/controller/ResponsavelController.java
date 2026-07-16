package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Responsavel;
import com.vanmos.van.model.service.ResponsavelService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

    @Autowired private ResponsavelService responsavelService;
    @Autowired private OwnershipValidator  ownership;
    @Autowired private JwtUtil             jwtUtil;

    // Lista completa com PII de todos os responsáveis: só ADMIN.
    @GetMapping
    public ResponseEntity<ApiResponse<List<Responsavel>>> listarTodos() {
        ownership.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok("Responsáveis listados.", responsavelService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Responsavel>> buscarPorId(@PathVariable Long id) {
        // Ponto 2: responsável só vê o próprio registro; ADMIN vê qualquer um
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "responsável");

        Responsavel r = responsavelService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Responsavel", id));
        return ResponseEntity.ok(ApiResponse.ok("Responsável encontrado.", r));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Responsavel>> criar(@Valid @RequestBody Responsavel responsavel) {
        return ResponseEntity.status(201).body(
            ApiResponse.created("Responsável criado.", responsavelService.save(responsavel))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Responsavel>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Responsavel responsavel) {

        // Ponto 2: responsável só pode editar o próprio registro
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "responsável");

        return ResponseEntity.ok(
            ApiResponse.ok("Responsável atualizado.", responsavelService.update(id, responsavel))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        // Apenas ADMIN pode deletar responsáveis
        ownership.requireAdmin();
        responsavelService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Responsável removido."));
    }
}
