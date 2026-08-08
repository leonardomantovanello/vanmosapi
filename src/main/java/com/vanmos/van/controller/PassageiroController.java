package com.vanmos.van.controller;

import com.vanmos.van.dto.AlterarSenhaRequest;
import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.CadastrarPassageiroPeloMotoristaRequest;
import com.vanmos.van.dto.EsqueciSenhaRequest;
import com.vanmos.van.dto.RedefinirSenhaComTokenRequest;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.service.AlunoService;
import com.vanmos.van.model.service.EmailService;
import com.vanmos.van.model.service.PassageiroService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import com.vanmos.van.security.RandomPasswordGenerator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passageiros")
public class PassageiroController {

    private static final Logger log = LoggerFactory.getLogger(PassageiroController.class);

    @Autowired private PassageiroService      passageiroService;
    @Autowired private AlunoService           alunoService;
    @Autowired private JwtUtil                jwtUtil;
    @Autowired private OwnershipValidator     ownership;
    @Autowired private EmailService           emailService;
    @Autowired private RandomPasswordGenerator passwordGenerator;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> cadastrar(@Valid @RequestBody Passageiro passageiro) {
        passageiro.setAtivo(false);
        Passageiro resultado = passageiroService.save(passageiro);
        resultado.setSenha(null);
        return ResponseEntity.status(201).body(
            ApiResponse.created("Cadastro realizado. Aguarde ativação.", resultado)
        );
        // IllegalArgumentException (CPF/email duplicado, CPF inválido) e demais
        // são capturadas pelo GlobalExceptionHandler → HTTP 400
    }

    /**
     * Cadastro de passageiro feito pelo motorista, pelo painel (ver
     * SecurityConfig: exige ROLE_MOTORISTA ou ROLE_ADMIN). Cria duas coisas
     * ligadas entre si — ver javadoc de CadastrarPassageiroPeloMotoristaRequest:
     *  1. A conta Passageiro do responsável — sem senha no corpo (gerada aqui
     *     e enviada por e-mail), ativo=true direto (sem aprovação de admin,
     *     já que o motorista conhece o responsável pessoalmente).
     *  2. O Aluno vinculado a essa conta E ao motorista logado — é isso que
     *     faz o aluno aparecer no dashboard do app (ver AlunoController,
     *     que filtra a listagem por motoristaId).
     */
    @PostMapping("/cadastrar-motorista")
    public ResponseEntity<ApiResponse<?>> cadastrarPeloMotorista(
            @Valid @RequestBody CadastrarPassageiroPeloMotoristaRequest request) {

        Passageiro passageiro = new Passageiro();
        passageiro.setNome(request.nomeResponsavel());
        // "" (campo opcional deixado em branco) falha o @Pattern de CPF da
        // entidade — mesma causa do bug do telefone do aluno logo abaixo.
        passageiro.setCpf(vazioParaNull(request.cpfResponsavel()));
        passageiro.setEmail(request.emailResponsavel());
        passageiro.setIdade(request.idadeResponsavel());
        passageiro.setGenero(request.generoResponsavel());
        passageiro.setTipo("PASSAGEIRO");
        passageiro.setAtivo(true);

        String senhaGerada = passwordGenerator.gerar();
        passageiro.setSenha(senhaGerada);

        // save() valida CPF/e-mail duplicado e hasheia a senha antes de persistir
        Passageiro responsavelSalvo = passageiroService.save(passageiro);

        Long motoristaId = ownership.getCurrentUserId(jwtUtil);
        Aluno aluno = new Aluno();
        aluno.setNome(request.nomeAluno());
        // Normaliza pra dígitos e trata vazio como ausente — o campo aceita
        // qualquer formatação digitada (com ou sem DDD entre parênteses,
        // hífen, espaço), e turno vazio (usuário não selecionou) não deve
        // virar "" e falhar a validação de @Pattern na entidade.
        aluno.setTelefoneResponsavel(semFormatacao(request.telefoneResponsavel()));
        aluno.setEnderecoEmbarque(request.enderecoEmbarque());
        aluno.setEnderecoDesembarque(request.enderecoDesembarque());
        aluno.setEscola(request.escola());
        aluno.setTurno(vazioParaNull(request.turno()));
        aluno.setValor(request.valor());
        aluno.setAtivo(true);
        aluno.setResponsavelId(responsavelSalvo.getId());
        aluno.setMotoristaId(motoristaId);
        alunoService.save(aluno);

        // O cadastro (responsável + aluno) já está persistido nesse ponto —
        // uma falha de e-mail (SMTP fora do ar, credencial errada, etc.) não
        // pode derrubar a resposta com 500 e deixar o motorista achando que
        // nada foi salvo. Loga o erro e avisa na mensagem, mas responde 201.
        boolean emailEnviado = true;
        try {
            emailService.enviarSenhaGerada(responsavelSalvo.getEmail(), responsavelSalvo.getNome(), senhaGerada);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de senha gerada para passageiro id={}", responsavelSalvo.getId(), e);
            emailEnviado = false;
        }

        responsavelSalvo.setSenha(null);
        String mensagem = emailEnviado
                ? "Passageiro cadastrado. Senha enviada por e-mail."
                : "Passageiro cadastrado, mas não foi possível enviar o e-mail com a senha. Contate o suporte para reenviar.";
        return ResponseEntity.status(201).body(
            ApiResponse.created(mensagem, responsavelSalvo)
        );
    }

    private String semFormatacao(String valor) {
        if (valor == null) return null;
        String digitos = valor.replaceAll("[^0-9]", "");
        return digitos.isBlank() ? null : digitos;
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<Passageiro>>> listarTodos() {
        List<Passageiro> lista = passageiroService.findAll();
        lista.forEach(p -> p.setSenha(null));
        return ResponseEntity.ok(ApiResponse.ok("Passageiros listados.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Passageiro>> buscarPorId(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        Passageiro p = passageiroService.findById(id)
                .orElseThrow(() -> new com.vanmos.van.exception.ResourceNotFoundException("Passageiro", id));
        p.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Passageiro encontrado.", p));
    }

    // Sem @Valid: é uma edição parcial pelo admin (nome/email/cpf/idade/genero),
    // sem senha nem tipo no corpo — as regras de @NotBlank da entidade (pensadas
    // para o cadastro completo) rejeitariam qualquer edição feita por aqui.
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Passageiro>> atualizar(
            @PathVariable Long id, @RequestBody Passageiro passageiro) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        Passageiro atualizado = passageiroService.update(id, passageiro);
        atualizado.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Passageiro atualizado.", atualizado));
    }

    // Troca de senha self-service — exige a senha atual (ver
    // PassageiroService#alterarSenha), diferente do PUT /{id} de edição de
    // perfil, que não pede confirmação nenhuma.
    @PutMapping("/{id}/senha")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(
            @PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest request) {

        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        passageiroService.alterarSenha(id, request.senhaAtual(), request.novaSenha());
        return ResponseEntity.ok(ApiResponse.noContent("Senha alterada com sucesso."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "passageiro");

        passageiroService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Passageiro removido."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/ativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> ativarPerfil(@PathVariable Long id) {
        passageiroService.updateCodStatus(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário ativado com sucesso."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/inativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> inativarPerfil(@PathVariable Long id) {
        passageiroService.inativarCadastro(id);
        return ResponseEntity.ok(ApiResponse.noContent("Usuário inativado com sucesso."));
    }

    /**
     * "Esqueci minha senha", etapa 1 — gera um token de uso único e envia um
     * link de redefinição por e-mail (ver redefinirSenha abaixo pra etapa 2).
     * Sempre responde a mesma mensagem genérica, exista ou não o e-mail,
     * pra não revelar pra quem tenta se um endereço está cadastrado
     * (evita user enumeration).
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<ApiResponse<Void>> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        passageiroService.buscarPorEmail(request.email()).ifPresent(passageiro -> {
            String token = passageiroService.gerarTokenRedefinicaoSenha(passageiro.getId());
            try {
                emailService.enviarLinkRedefinicaoSenha(passageiro.getEmail(), passageiro.getNome(), token);
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de link de redefinição para passageiro id={}", passageiro.getId(), e);
            }
        });

        return ResponseEntity.ok(ApiResponse.noContent(
            "Se esse e-mail estiver cadastrado, enviamos um link de redefinição para ele."
        ));
    }

    /**
     * "Esqueci minha senha", etapa 2 — troca a senha usando o token recebido
     * por e-mail (ver PassageiroService#redefinirSenhaComToken: token
     * inválido/expirado/já usado vira ValidationException → HTTP 422).
     */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<ApiResponse<Void>> redefinirSenha(@Valid @RequestBody RedefinirSenhaComTokenRequest request) {
        passageiroService.redefinirSenhaComToken(request.token(), request.novaSenha());
        return ResponseEntity.ok(ApiResponse.noContent("Senha redefinida com sucesso."));
    }

    @GetMapping("/verificar-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verificarEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok("Verificação concluída.",
            Map.of("disponivel", !passageiroService.emailJaCadastrado(email))
        ));
    }
}
