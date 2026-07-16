package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.service.MotoristaService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    @Autowired private MotoristaService   motoristaService;
    @Autowired private OwnershipValidator ownership;
    @Autowired private JwtUtil            jwtUtil;

    // Lista completa com PII (cpf, cnh, telefone) de todos os motoristas: só ADMIN.
    @GetMapping
    public ResponseEntity<ApiResponse<List<Motorista>>> listarTodos() {
        ownership.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok("Motoristas listados.", motoristaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Motorista>> buscarPorId(@PathVariable Long id) {
        // Ponto 2: motorista só vê o próprio perfil; ADMIN vê qualquer um
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        Motorista m = motoristaService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Motorista", id));
        return ResponseEntity.ok(ApiResponse.ok("Motorista encontrado.", m));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Motorista>> criar(@Valid @RequestBody Motorista motorista) {
        ownership.requireAdmin();
        return ResponseEntity.status(201).body(
            ApiResponse.created("Motorista criado.", motoristaService.save(motorista))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Motorista>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Motorista motorista) {

        // Ponto 2: motorista só edita o próprio perfil
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        return ResponseEntity.ok(
            ApiResponse.ok("Motorista atualizado.", motoristaService.update(id, motorista))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        ownership.requireAdmin();
        motoristaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista removido."));
    }
}
