package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.EnviarMensagemRequest;
import com.vanmos.van.exception.ForbiddenException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.Mensagem;
import com.vanmos.van.model.service.AlunoService;
import com.vanmos.van.model.service.MensagemService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat entre motorista e responsável, escopado por aluno — antes só existia
 * em memória no app (MOCK_MESSAGES), sem persistência e sem separação real
 * por conversa. Cada aluno tem exatamente uma thread, visível para o
 * motorista que o cadastrou e para o responsável vinculado a ele.
 */
@RestController
@RequestMapping("/api/mensagens")
public class MensagemController {

    @Autowired private MensagemService mensagemService;
    @Autowired private AlunoService    alunoService;
    @Autowired private JwtUtil         jwtUtil;
    @Autowired private OwnershipValidator ownership;

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<List<Mensagem>>> listar(@PathVariable Long alunoId) {
        Aluno aluno = buscarAlunoComAcesso(alunoId);
        return ResponseEntity.ok(ApiResponse.ok("Mensagens listadas.", mensagemService.listarPorAluno(aluno.getId())));
    }

    @PostMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<Mensagem>> enviar(
            @PathVariable Long alunoId, @Valid @RequestBody EnviarMensagemRequest request) {

        Aluno aluno = buscarAlunoComAcesso(alunoId);
        String remetenteTipo = ownership.getCurrentRole();
        Long remetenteId = ownership.getCurrentUserId(jwtUtil);

        Mensagem enviada = mensagemService.enviar(aluno.getId(), remetenteTipo, remetenteId, request.texto());
        return ResponseEntity.status(201).body(ApiResponse.created("Mensagem enviada.", enviada));
    }

    // Só o motorista do aluno, o responsável do aluno, ou ADMIN podem ver/mandar
    // mensagens nessa conversa — mesmo padrão de ownership do AlunoController.
    private Aluno buscarAlunoComAcesso(Long alunoId) {
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
            throw new ForbiddenException("Você não tem permissão para acessar esta conversa.");
        }
        return aluno;
    }
}
