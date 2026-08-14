package com.vanmos.van.controller;

import com.vanmos.van.dto.AlterarSenhaRequest;
import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.EsqueciSenhaRequest;
import com.vanmos.van.dto.MotoristaPublicoDTO;
import com.vanmos.van.dto.RedefinirSenhaComTokenRequest;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.entity.StatusCadastro;
import com.vanmos.van.model.service.EmailService;
import com.vanmos.van.model.service.MotoristaService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Conta de motorista — autocadastro (com aprovação por e-mail, ver
 * MotoristaAprovacaoController), perfil, troca/redefinição de senha.
 * Migrado de PassageiroController quando motorista ganhou tabela própria
 * (antes vivia em Passageiro/tipo=MOTORISTA — ver V14-V16).
 */
@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private static final Logger log = LoggerFactory.getLogger(MotoristaController.class);

    @Autowired private MotoristaService    motoristaService;
    @Autowired private JwtUtil             jwtUtil;
    @Autowired private OwnershipValidator  ownership;
    @Autowired private EmailService        emailService;

    // Público — autocadastro. Fica PENDENTE até o suporte aprovar por
    // e-mail (ver MotoristaAprovacaoController); não consegue logar antes
    // disso (ver LoginController, que agora também consulta esta tabela).
    @PostMapping
    public ResponseEntity<ApiResponse<?>> cadastrar(@Valid @RequestBody Motorista motorista) {
        motorista.setAtivo(false);
        motorista.setStatusCadastro(StatusCadastro.PENDENTE);

        Motorista resultado = motoristaService.save(motorista);

        // Mesmo cuidado de ordem já usado em PassageiroController#cadastrar:
        // resultado.setSenha(null) só depois de TODAS as chamadas
        // @Transactional abaixo, pra não deixar a entidade "suja" antes de
        // um flush do Hibernate (senha=null violaria validação em outro lugar).
        boolean emailEnviado = true;
        try {
            String token = motoristaService.gerarTokenAprovacaoCadastro(resultado.getId());
            emailService.enviarSolicitacaoAprovacaoCadastro(resultado, token);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de solicitação de aprovação para motorista id={}", resultado.getId(), e);
            emailEnviado = false;
        }
        String mensagem = emailEnviado
                ? "Cadastro realizado! Seus dados foram enviados para análise. Você receberá um e-mail assim que for aprovado."
                : "Cadastro realizado, mas houve uma falha ao notificar o suporte para análise. Entre em contato para agilizar a aprovação.";
        resultado.setSenha(null);

        return ResponseEntity.status(201).body(
            ApiResponse.created(mensagem, resultado)
        );
        // IllegalArgumentException (CPF/email duplicado, CPF inválido, campos
        // obrigatórios faltando) capturada pelo GlobalExceptionHandler → HTTP 400
    }

    // Público — lista de motoristas ativos pra página "Nossos Motoristas".
    @GetMapping("/publico")
    public ResponseEntity<ApiResponse<List<MotoristaPublicoDTO>>> listarPublico() {
        List<MotoristaPublicoDTO> lista = motoristaService.findAll().stream()
                .filter(Motorista::isAtivo)
                .map(MotoristaPublicoDTO::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Motoristas listados.", lista));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<Motorista>>> listarTodos() {
        List<Motorista> lista = motoristaService.findAll();
        lista.forEach(m -> m.setSenha(null));
        return ResponseEntity.ok(ApiResponse.ok("Motoristas listados.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Motorista>> buscarPorId(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        Motorista m = motoristaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista", id));
        m.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Motorista encontrado.", m));
    }

    // Sem @Valid: edição parcial de perfil, sem senha/status no corpo.
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Motorista>> atualizar(
            @PathVariable Long id, @RequestBody Motorista motorista) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        Motorista atualizado = motoristaService.update(id, motorista);
        atualizado.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Motorista atualizado.", atualizado));
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(
            @PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest request) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        motoristaService.alterarSenha(id, request.senhaAtual(), request.novaSenha());
        return ResponseEntity.ok(ApiResponse.noContent("Senha alterada com sucesso."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        motoristaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista removido."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig — escotilha de emergência,
    // mesmo padrão de PassageiroController.
    @RequestMapping(value = "/{id}/ativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> ativarPerfil(@PathVariable Long id) {
        motoristaService.ativar(id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista ativado com sucesso."));
    }

    @RequestMapping(value = "/{id}/inativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> inativarPerfil(@PathVariable Long id) {
        motoristaService.inativar(id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista inativado com sucesso."));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<ApiResponse<Void>> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        motoristaService.buscarPorEmail(request.email()).ifPresent(motorista -> {
            String token = motoristaService.gerarTokenRedefinicaoSenha(motorista.getId());
            try {
                emailService.enviarLinkRedefinicaoSenha(motorista.getEmail(), motorista.getNome(), token);
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de link de redefinição para motorista id={}", motorista.getId(), e);
            }
        });

        return ResponseEntity.ok(ApiResponse.noContent(
            "Se esse e-mail estiver cadastrado, enviamos um link de redefinição para ele."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<ApiResponse<Void>> redefinirSenha(@Valid @RequestBody RedefinirSenhaComTokenRequest request) {
        motoristaService.redefinirSenhaComToken(request.token(), request.novaSenha());
        return ResponseEntity.ok(ApiResponse.noContent("Senha redefinida com sucesso."));
    }
}
