package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.MarcarFaltaRequest;
import com.vanmos.van.exception.ForbiddenException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.Falta;
import com.vanmos.van.model.service.AlunoService;
import com.vanmos.van.model.service.FaltaService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controle de faltas por aluno — só o motorista (ou ADMIN) registra/edita/
 * remove; o responsável só visualiza (ver validarAcessoEscrita). Um dia sem
 * registro aqui é considerado normal — não existe um estado "presente"
 * salvo explicitamente, só a ausência de falta.
 */
@RestController
@RequestMapping("/api/faltas")
public class FaltaController {

    @Autowired private FaltaService    faltaService;
    @Autowired private AlunoService    alunoService;
    @Autowired private JwtUtil         jwtUtil;
    @Autowired private OwnershipValidator ownership;

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<List<Falta>>> listar(@PathVariable Long alunoId) {
        Aluno aluno = buscarAlunoComAcessoLeitura(alunoId);
        return ResponseEntity.ok(ApiResponse.ok("Faltas listadas.", faltaService.listarPorAluno(aluno.getId())));
    }

    @PostMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<Falta>> marcar(
            @PathVariable Long alunoId, @Valid @RequestBody MarcarFaltaRequest request) {

        Aluno aluno = buscarAlunoComAcessoEscrita(alunoId);
        Long motoristaId = ownership.getCurrentUserId(jwtUtil);
        Falta falta = faltaService.marcar(aluno.getId(), request.data(), request.justificativa(), motoristaId);
        return ResponseEntity.status(201).body(ApiResponse.created("Falta registrada.", falta));
    }

    @DeleteMapping("/aluno/{alunoId}/data/{data}")
    public ResponseEntity<ApiResponse<Void>> desmarcar(
            @PathVariable Long alunoId,
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate data) {

        Aluno aluno = buscarAlunoComAcessoEscrita(alunoId);
        faltaService.desmarcar(aluno.getId(), data);
        return ResponseEntity.ok(ApiResponse.noContent("Falta removida."));
    }

    // Motorista do aluno, responsável do aluno, ou ADMIN podem ver as faltas.
    private Aluno buscarAlunoComAcessoLeitura(Long alunoId) {
        Aluno aluno = alunoService.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", alunoId));

        if (ownership.isAdmin()) return aluno;

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        String role = ownership.getCurrentRole();
        boolean ehOMotoristaDoAluno = "MOTORISTA".equals(role)
                && aluno.getMotoristaId() != null && aluno.getMotoristaId().equals(currentUserId);
        boolean ehOResponsavelDoAluno = "RESPONSAVEL".equals(role)
                && aluno.getResponsavelId() != null && aluno.getResponsavelId().equals(currentUserId);

        if (!ehOMotoristaDoAluno && !ehOResponsavelDoAluno) {
            throw new ForbiddenException("Você não tem permissão para ver as faltas deste aluno.");
        }
        return aluno;
    }

    // Só o motorista do aluno (ou ADMIN) pode registrar/remover faltas —
    // responsável tem acesso só de leitura, nunca de escrita.
    private Aluno buscarAlunoComAcessoEscrita(Long alunoId) {
        Aluno aluno = alunoService.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", alunoId));

        if (ownership.isAdmin()) return aluno;

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        boolean ehOMotoristaDoAluno = "MOTORISTA".equals(ownership.getCurrentRole())
                && aluno.getMotoristaId() != null && aluno.getMotoristaId().equals(currentUserId);

        if (!ehOMotoristaDoAluno) {
            throw new ForbiddenException("Apenas o motorista deste aluno pode registrar faltas.");
        }
        return aluno;
    }
}
