package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.ReprovarCadastroRequest;
import com.vanmos.van.dto.RevisaoCadastroDTO;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.service.EmailService;
import com.vanmos.van.model.service.PassageiroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Fluxo público de aprovação/reprovação de cadastro de motorista por
 * e-mail — substitui a ativação manual pelo antigo painel de admin. Sem
 * autenticação: a prova de identidade de quem age aqui é o token de uso
 * único que só chega em quem tem acesso à caixa de e-mail do suporte (ver
 * EmailService#enviarSolicitacaoAprovacaoCadastro).
 *
 * Fica em /api/passageiros/aprovacao (não /api/motoristas/**, que já tem
 * regras do dead-code Motorista no SecurityConfig).
 */
@RestController
@RequestMapping("/api/passageiros/aprovacao")
public class CadastroAprovacaoController {

    private static final Logger log = LoggerFactory.getLogger(CadastroAprovacaoController.class);

    @Autowired private PassageiroService passageiroService;
    @Autowired private EmailService emailService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<RevisaoCadastroDTO>> buscar(@PathVariable String token) {
        Passageiro passageiro = passageiroService.buscarParaRevisaoPorToken(token);
        return ResponseEntity.ok(ApiResponse.ok("Cadastro encontrado.", RevisaoCadastroDTO.from(passageiro)));
    }

    @PostMapping("/{token}/aprovar")
    public ResponseEntity<ApiResponse<Void>> aprovar(@PathVariable String token) {
        Passageiro passageiro = passageiroService.aprovarCadastro(token);

        // Confirmação por e-mail é best-effort — a aprovação já está
        // persistida nesse ponto, uma falha de SMTP não pode desfazê-la.
        try {
            emailService.enviarConfirmacaoAprovacao(passageiro.getEmail(), passageiro.getNome());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação de aprovação para passageiro id={}", passageiro.getId(), e);
        }

        return ResponseEntity.ok(ApiResponse.noContent("Cadastro aprovado com sucesso."));
    }

    @PostMapping("/{token}/reprovar")
    public ResponseEntity<ApiResponse<Void>> reprovar(
            @PathVariable String token,
            @RequestBody(required = false) ReprovarCadastroRequest request) {

        String motivo = request != null ? request.motivo() : null;
        Passageiro passageiro = passageiroService.reprovarCadastro(token, motivo);

        try {
            emailService.enviarNotificacaoReprovacao(passageiro.getEmail(), passageiro.getNome(), motivo);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de notificação de reprovação para passageiro id={}", passageiro.getId(), e);
        }

        return ResponseEntity.ok(ApiResponse.noContent("Cadastro reprovado."));
    }
}
