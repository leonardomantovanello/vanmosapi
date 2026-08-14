package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.ReprovarCadastroRequest;
import com.vanmos.van.dto.RevisaoCadastroDTO;
import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.service.EmailService;
import com.vanmos.van.model.service.MotoristaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Fluxo público de aprovação/reprovação de cadastro de motorista por
 * e-mail. Sem autenticação: a prova de identidade de quem age aqui é o
 * token de uso único que só chega em quem tem acesso à caixa de e-mail do
 * suporte (ver EmailService#enviarSolicitacaoAprovacaoCadastro).
 * Substitui CadastroAprovacaoController (removido — vivia em
 * /api/passageiros/aprovacao, quando motorista ainda era Passageiro).
 */
@RestController
@RequestMapping("/api/motoristas/aprovacao")
public class MotoristaAprovacaoController {

    private static final Logger log = LoggerFactory.getLogger(MotoristaAprovacaoController.class);

    @Autowired private MotoristaService motoristaService;
    @Autowired private EmailService emailService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<RevisaoCadastroDTO>> buscar(@PathVariable String token) {
        Motorista motorista = motoristaService.buscarParaRevisaoPorToken(token);
        return ResponseEntity.ok(ApiResponse.ok("Cadastro encontrado.", RevisaoCadastroDTO.from(motorista)));
    }

    @PostMapping("/{token}/aprovar")
    public ResponseEntity<ApiResponse<Void>> aprovar(@PathVariable String token) {
        Motorista motorista = motoristaService.aprovarCadastro(token);

        // Confirmação por e-mail é best-effort — a aprovação já está
        // persistida nesse ponto, uma falha de SMTP não pode desfazê-la.
        try {
            emailService.enviarConfirmacaoAprovacao(motorista.getEmail(), motorista.getNome());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação de aprovação para motorista id={}", motorista.getId(), e);
        }

        return ResponseEntity.ok(ApiResponse.noContent("Cadastro aprovado com sucesso."));
    }

    @PostMapping("/{token}/reprovar")
    public ResponseEntity<ApiResponse<Void>> reprovar(
            @PathVariable String token,
            @RequestBody(required = false) ReprovarCadastroRequest request) {

        String motivo = request != null ? request.motivo() : null;
        Motorista motorista = motoristaService.reprovarCadastro(token, motivo);

        try {
            emailService.enviarNotificacaoReprovacao(motorista.getEmail(), motorista.getNome(), motivo);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de notificação de reprovação para motorista id={}", motorista.getId(), e);
        }

        return ResponseEntity.ok(ApiResponse.noContent("Cadastro reprovado."));
    }
}
