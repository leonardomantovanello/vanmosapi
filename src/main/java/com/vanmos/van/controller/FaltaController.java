package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.MarcarFaltaRequest;
import com.vanmos.van.exception.ForbiddenException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.exception.ValidationException;
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
 * Controle de faltas por aluno — só o responsável (ou ADMIN) registra/edita/
 * remove; o motorista só visualiza (ver buscarAlunoComAcessoEscrita e o
 * endpoint /hoje, usado pra etiqueta "Faltou hoje" e notificação local no
 * app do motorista). Um dia sem registro aqui é considerado normal — não
 * existe um estado "presente" salvo explicitamente, só a ausência de falta.
 * Marcar falta pra hoje remove a parada da corrida do motorista (ver
 * RotaProgressoService), evitando uma viagem desnecessária.
 */
@RestController
@RequestMapping("/api/faltas")
public class FaltaController {

    @Autowired private FaltaService    faltaService;
    @Autowired private AlunoService    alunoService;
    @Autowired private JwtUtil         jwtUtil;
    @Autowired private OwnershipValidator ownership;

    // Faltas de hoje entre os alunos do motorista logado — usado pra
    // etiqueta "Faltou hoje" na lista de passageiros e pra detectar novas
    // ausências e disparar notificação local (ver driver-home.tsx no app).
    @GetMapping("/hoje")
    public ResponseEntity<ApiResponse<List<Falta>>> hoje() {
        if (!"MOTORISTA".equals(ownership.getCurrentRole())) {
            throw new ForbiddenException("Apenas o motorista pode consultar as faltas de hoje da sua rota.");
        }
        Long motoristaId = ownership.getCurrentUserId(jwtUtil);
        List<Long> ids = alunoService.findByMotoristaId(motoristaId).stream().map(Aluno::getId).toList();
        return ResponseEntity.ok(ApiResponse.ok("Faltas de hoje.", faltaService.listarPorAlunosEData(ids, LocalDate.now())));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<List<Falta>>> listar(@PathVariable Long alunoId) {
        Aluno aluno = buscarAlunoComAcessoLeitura(alunoId);
        return ResponseEntity.ok(ApiResponse.ok("Faltas listadas.", faltaService.listarPorAluno(aluno.getId())));
    }

    @PostMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<Falta>> marcar(
            @PathVariable Long alunoId, @Valid @RequestBody MarcarFaltaRequest request) {

        Aluno aluno = buscarAlunoComAcessoEscrita(alunoId);

        // Só faz sentido avisar ausência de hoje em diante — um dia que já
        // passou não pode mais ser removido da corrida. ADMIN tem bypass
        // pra correções excepcionais (mesmo padrão de outras validações
        // condicionais no projeto).
        if (!ownership.isAdmin() && request.data().isBefore(LocalDate.now())) {
            throw new ValidationException("Não é possível marcar falta para um dia que já passou.");
        }

        Long responsavelId = ownership.getCurrentUserId(jwtUtil);
        Falta falta = faltaService.marcar(aluno.getId(), request.data(), request.justificativa(), responsavelId);
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

    // Só o responsável do aluno (ou ADMIN) pode registrar/remover faltas —
    // motorista tem acesso só de leitura, nunca de escrita.
    private Aluno buscarAlunoComAcessoEscrita(Long alunoId) {
        Aluno aluno = alunoService.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", alunoId));

        if (ownership.isAdmin()) return aluno;

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        boolean ehOResponsavelDoAluno = "RESPONSAVEL".equals(ownership.getCurrentRole())
                && aluno.getResponsavelId() != null && aluno.getResponsavelId().equals(currentUserId);

        if (!ehOResponsavelDoAluno) {
            throw new ForbiddenException("Apenas o responsável deste aluno pode registrar faltas.");
        }
        return aluno;
    }
}
