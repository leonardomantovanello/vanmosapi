package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.model.entity.Van;
import com.vanmos.van.model.service.VanService;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vans")
public class VanController {

    @Autowired private VanService         vanService;
    @Autowired private OwnershipValidator ownership;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Van>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok("Vans listadas.", vanService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Van>> buscarPorId(@PathVariable Long id) {
        Van van = vanService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Van", id));
        return ResponseEntity.ok(ApiResponse.ok("Van encontrada.", van));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Van>> criar(@Valid @RequestBody Van van) {
        // Apenas ADMIN cadastra vans
        ownership.requireAdmin();
        return ResponseEntity.status(201).body(
            ApiResponse.created("Van cadastrada.", vanService.save(van))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Van>> atualizar(
            @PathVariable Long id, @Valid @RequestBody Van van) {
        // Apenas ADMIN edita vans
        ownership.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok("Van atualizada.", vanService.update(id, van)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        ownership.requireAdmin();
        vanService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Van removida."));
    }
}
