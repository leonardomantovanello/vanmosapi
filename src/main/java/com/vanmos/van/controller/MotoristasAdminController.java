package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.MotoristaPublicoDTO;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.MotoristasAdmin;
import com.vanmos.van.model.service.MotoristasAdminService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MUDANÇAS APLICADAS nesta reescrita:
 *  1. Todos os System.out.println() removidos — violavam LGPD (CPF, CNH nos logs)
 *  2. @CrossOrigin(origins="*") removido — CORS centralizado no SecurityConfig
 *  3. try/catch manuais removidos — GlobalExceptionHandler trata tudo
 *  4. Respostas padronizadas com ApiResponse<T>
 *  5. Login com BCrypt (passwordEncoder.matches) em vez de .equals() em plaintext
 *  6. Verificação de propriedade: motorista só edita o próprio perfil (ponto 2)
 *  7. Senha nunca retornada nas respostas
 */
@RestController
@RequestMapping("/api/motoristas-admin")
public class MotoristasAdminController {

    @Autowired private MotoristasAdminService motoristasAdminService;
    @Autowired private JwtUtil                jwtUtil;
    @Autowired private PasswordEncoder        passwordEncoder;
    @Autowired private OwnershipValidator     ownership;

    // Protegido por ROLE_ADMIN no SecurityConfig
    @GetMapping
    public ResponseEntity<ApiResponse<List<MotoristasAdmin>>> listarTodos() {
        List<MotoristasAdmin> lista = motoristasAdminService.findAll();
        // Remove senhas da resposta
        lista.forEach(m -> m.setSenha(null));
        return ResponseEntity.ok(ApiResponse.ok("Motoristas listados.", lista));
    }

    // Rota pública (ver SecurityConfig) — só campos sem PII, para a página
    // pública "Nossos Motoristas". Não usar a listagem completa acima aqui:
    // ela expõe cpf/cnh/gmail e exige ROLE_ADMIN de propósito.
    @GetMapping("/publico")
    public ResponseEntity<ApiResponse<List<MotoristaPublicoDTO>>> listarPublico() {
        List<MotoristaPublicoDTO> lista = motoristasAdminService.findAtivos().stream()
                .map(MotoristaPublicoDTO::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Motoristas listados.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MotoristasAdmin>> buscarPorId(@PathVariable Long id) {
        // Ponto 2: motorista só vê o próprio perfil; ADMIN vê qualquer um
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        MotoristasAdmin m = motoristasAdminService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista", id));
        m.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Motorista encontrado.", m));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @PostMapping
    public ResponseEntity<ApiResponse<MotoristasAdmin>> criar(@Valid @RequestBody MotoristasAdmin motorista) {
        // Hash da senha antes de salvar
        if (motorista.getSenha() != null && !motorista.getSenha().isBlank()) {
            motorista.setSenha(passwordEncoder.encode(motorista.getSenha()));
        }
        MotoristasAdmin salvo = motoristasAdminService.save(motorista);
        salvo.setSenha(null);
        return ResponseEntity.status(201).body(
            ApiResponse.created("Motorista cadastrado.", salvo)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MotoristasAdmin>> atualizar(
            @PathVariable Long id, @Valid @RequestBody MotoristasAdmin motorista) {

        // Ponto 2: motorista só edita o próprio perfil
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);
        ownership.validateOwnership(id, currentUserId, "motorista");

        if (motorista.getSenha() != null && !motorista.getSenha().isBlank()) {
            motorista.setSenha(passwordEncoder.encode(motorista.getSenha()));
        }
        MotoristasAdmin atualizado = motoristasAdminService.update(id, motorista);
        atualizado.setSenha(null);
        return ResponseEntity.ok(ApiResponse.ok("Motorista atualizado.", atualizado));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        if (!motoristasAdminService.existsById(id)) {
            throw new ResourceNotFoundException("Motorista", id);
        }
        motoristasAdminService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista removido."));
    }

    // Endpoint de login — público (ver SecurityConfig: /api/motoristas-admin/login)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody Map<String, String> loginData) {

        String emailOuCpf = loginData.get("emailOuCpf");
        String senha       = loginData.get("senha");

        if (emailOuCpf == null || emailOuCpf.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Email/CPF e senha são obrigatórios.")
            );
        }

        MotoristasAdmin motorista = motoristasAdminService.findByGmailOrCpf(emailOuCpf.trim());

        // Mensagem genérica — evita user enumeration
        if (motorista == null || motorista.getSenha() == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Credenciais inválidas."));
        }

        if (!motorista.isAtivo()) {
            return ResponseEntity.status(403).body(
                ApiResponse.error(403, "Conta inativa. Entre em contato com o administrador.")
            );
        }

        // BCrypt — substitui a comparação em plaintext que existia antes
        if (!passwordEncoder.matches(senha, motorista.getSenha())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Credenciais inválidas."));
        }

        String accessToken  = jwtUtil.generateAccessToken(motorista.getGmail(), "MOTORISTA", motorista.getId());
        String refreshToken = jwtUtil.generateRefreshToken(motorista.getGmail());

        Map<String, Object> dados = Map.of(
            "id",           motorista.getId(),
            "nomeCompleto", motorista.getNomeCompleto(),
            "gmail",        motorista.getGmail(),
            "placaVan",     motorista.getPlacaVan(),
            "modeloVan",    motorista.getModeloVan(),
            "accessToken",  accessToken,
            "refreshToken", refreshToken
        );
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso.", dados));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/ativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> ativar(@PathVariable Long id) {
        MotoristasAdmin m = motoristasAdminService.ativar(id);
        if (m == null) throw new ResourceNotFoundException("Motorista", id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista ativado."));
    }

    // Protegido por ROLE_ADMIN no SecurityConfig
    @RequestMapping(value = "/{id}/inativar", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable Long id) {
        MotoristasAdmin m = motoristasAdminService.inativar(id);
        if (m == null) throw new ResourceNotFoundException("Motorista", id);
        return ResponseEntity.ok(ApiResponse.noContent("Motorista inativado."));
    }
}
