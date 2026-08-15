package com.vanmos.van.model.service;

import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.CadastroAprovacaoToken;
import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.entity.MotoristaPasswordResetToken;
import com.vanmos.van.model.entity.StatusCadastro;
import com.vanmos.van.model.repository.CadastroAprovacaoTokenRepository;
import com.vanmos.van.model.repository.MotoristaPasswordResetTokenRepository;
import com.vanmos.van.model.repository.MotoristaRepository;
import com.vanmos.van.model.repository.PassageiroRepository;
import com.vanmos.van.security.PasswordResetTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conta de motorista — login, cadastro/aprovação por e-mail, redefinição de
 * senha. Espelha PassageiroService 1:1 (mesmos nomes de método, mesma
 * lógica), migrado de lá quando motorista ganhou tabela própria (V14-V16).
 */
@Service
public class MotoristaService {

    private static final long TOKEN_REDEFINICAO_VALIDADE_MINUTOS = 30;
    private static final long TOKEN_APROVACAO_VALIDADE_DIAS = 7;

    @Autowired
    private MotoristaRepository motoristaRepository;

    // Só pra checar duplicidade de e-mail entre as duas tabelas de conta
    // (ver verificarEmailDuplicado) — espelha o mesmo check em
    // PassageiroService, na direção oposta.
    @Autowired
    private PassageiroRepository passageiroRepository;

    @Autowired
    private MotoristaPasswordResetTokenRepository motoristaPasswordResetTokenRepository;

    @Autowired
    private CadastroAprovacaoTokenRepository cadastroAprovacaoTokenRepository;

    @Autowired
    private PasswordResetTokenGenerator passwordResetTokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Motorista> findAll() {
        return motoristaRepository.findAll();
    }

    public Optional<Motorista> findById(Long id) {
        return motoristaRepository.findById(id);
    }

    private boolean cpfValido(String cpf) {
        String c = cpf.replaceAll("[^0-9]", "");
        if (c.length() != 11 || c.chars().distinct().count() == 1) return false;
        int s1 = 0, s2 = 0;
        for (int i = 0; i < 9; i++) s1 += (c.charAt(i) - '0') * (10 - i);
        int d1 = (s1 * 10) % 11; if (d1 == 10) d1 = 0;
        for (int i = 0; i < 10; i++) s2 += (c.charAt(i) - '0') * (11 - i);
        int d2 = (s2 * 10) % 11; if (d2 == 10) d2 = 0;
        return d1 == (c.charAt(9) - '0') && d2 == (c.charAt(10) - '0');
    }

    public Optional<Motorista> buscarPorEmail(String email) {
        return motoristaRepository.findByEmailIgnoreCase(email.trim());
    }

    public Motorista findByEmailOuCpf(String emailOuCpf) {
        String cpfLimpo = emailOuCpf.replaceAll("[^0-9]", "");
        return motoristaRepository.findByEmailIgnoreCaseOrCpfDigits(emailOuCpf.trim(), cpfLimpo)
                .orElse(null);
    }

    // Sempre um cadastro novo — setId(null) evita mass assignment (mesma
    // defesa aplicada em PassageiroService#save): um "id" no corpo do POST
    // público faria o Spring Data JPA chamar merge() em vez de persist(),
    // sobrescrevendo silenciosamente o cadastro de outro motorista.
    @Transactional
    public Motorista save(Motorista motorista) {
        motorista.setId(null);
        validarCpf(motorista);
        verificarCpfDuplicado(motorista);
        verificarEmailDuplicado(motorista);
        validarCamposObrigatorios(motorista);
        motorista.setSenha(passwordEncoder.encode(motorista.getSenha()));
        return motoristaRepository.save(motorista);
    }

    private void validarCamposObrigatorios(Motorista motorista) {
        List<String> faltando = new ArrayList<>();
        if (isBlank(motorista.getTelefone())) faltando.add("telefone");
        if (isBlank(motorista.getRg())) faltando.add("RG");
        if (isBlank(motorista.getCnh())) faltando.add("CNH");
        if (isBlank(motorista.getRgDocumentoBase64())) faltando.add("documento do RG");
        if (isBlank(motorista.getCnhDocumentoBase64())) faltando.add("documento da CNH");

        if (!faltando.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campos obrigatórios para cadastro de motorista: " + String.join(", ", faltando));
        }
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private void validarCpf(Motorista motorista) {
        if (motorista.getCpf() == null || motorista.getCpf().isBlank()) return;
        if (!cpfValido(motorista.getCpf())) {
            throw new IllegalArgumentException("CPF incorreto");
        }
    }

    private void verificarCpfDuplicado(Motorista motorista) {
        if (motorista.getCpf() == null || motorista.getCpf().isBlank()) return;
        String cpfDigitos = motorista.getCpf().replaceAll("[^0-9]", "");
        motoristaRepository.findByCpfDigits(cpfDigitos).ifPresent(existente -> {
            if (!existente.getId().equals(motorista.getId())) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
        });
    }

    // Checa nas duas tabelas — ver comentário equivalente em
    // PassageiroService#verificarEmailDuplicado.
    private void verificarEmailDuplicado(Motorista motorista) {
        if (motorista.getEmail() == null) return;
        String email = motorista.getEmail().trim();
        if (motoristaRepository.findByEmailIgnoreCase(email).isPresent()
                || passageiroRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
    }

    public void deleteById(Long id) {
        motoristaRepository.deleteById(id);
    }

    @Transactional
    public Motorista update(Long id, Motorista motorista) {
        Motorista existente = motoristaRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));

        if (motorista.getNome() != null && !motorista.getNome().isBlank()) {
            existente.setNome(motorista.getNome());
        }
        if (motorista.getTelefone() != null && !motorista.getTelefone().isBlank()) {
            existente.setTelefone(motorista.getTelefone());
        }
        if (motorista.getIdade() != null) {
            existente.setIdade(motorista.getIdade());
        }
        if (motorista.getGenero() != null && !motorista.getGenero().isBlank()) {
            existente.setGenero(motorista.getGenero());
        }
        if (motorista.getRg() != null && !motorista.getRg().isBlank()) {
            existente.setRg(motorista.getRg());
        }
        if (motorista.getModeloVan() != null && !motorista.getModeloVan().isBlank()) {
            existente.setModeloVan(motorista.getModeloVan());
        }
        if (motorista.getPlacaVan() != null && !motorista.getPlacaVan().isBlank()) {
            existente.setPlacaVan(motorista.getPlacaVan());
        }

        if (motorista.getCpf() != null && !motorista.getCpf().isBlank()) {
            if (!cpfValido(motorista.getCpf())) {
                throw new IllegalArgumentException("CPF incorreto");
            }
            String cpfDigitos = motorista.getCpf().replaceAll("[^0-9]", "");
            motoristaRepository.findByCpfDigits(cpfDigitos).ifPresent(outro -> {
                if (!outro.getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado");
            });
            existente.setCpf(motorista.getCpf());
        }

        if (motorista.getCnh() != null && !motorista.getCnh().isBlank()) {
            existente.setCnh(motorista.getCnh());
        }

        if (motorista.getEmail() != null && !motorista.getEmail().isBlank()) {
            String email = motorista.getEmail().trim();
            motoristaRepository.findByEmailIgnoreCase(email).ifPresent(outro -> {
                if (!outro.getId().equals(id)) throw new IllegalArgumentException("E-mail já cadastrado");
            });
            if (passageiroRepository.findByEmailIgnoreCase(email).isPresent()) {
                throw new IllegalArgumentException("E-mail já cadastrado");
            }
            existente.setEmail(motorista.getEmail());
        }

        // null aqui é um valor válido ("sem foto"), não "não veio no
        // request" — mesmo padrão de Passageiro#update.
        existente.setAvatarBase64(motorista.getAvatarBase64());
        return motoristaRepository.save(existente);
    }

    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Motorista existente = motoristaRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));

        if (!passwordEncoder.matches(senhaAtual, existente.getSenha())) {
            throw new ValidationException("Senha atual incorreta");
        }

        existente.setSenha(passwordEncoder.encode(novaSenha));
        motoristaRepository.save(existente);
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        Motorista existente = motoristaRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));
        existente.setSenha(passwordEncoder.encode(novaSenha));
        motoristaRepository.save(existente);
    }

    @Transactional
    public String gerarTokenRedefinicaoSenha(Long motoristaId) {
        String token = passwordResetTokenGenerator.gerar();

        MotoristaPasswordResetToken resetToken = new MotoristaPasswordResetToken();
        resetToken.setToken(token);
        resetToken.setMotoristaId(motoristaId);
        resetToken.setExpiraEm(LocalDateTime.now().plusMinutes(TOKEN_REDEFINICAO_VALIDADE_MINUTOS));
        resetToken.setUsado(false);
        motoristaPasswordResetTokenRepository.save(resetToken);

        return token;
    }

    @Transactional
    public void redefinirSenhaComToken(String token, String novaSenha) {
        MotoristaPasswordResetToken resetToken = motoristaPasswordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Link de redefinição inválido ou já usado."));

        if (resetToken.isUsado()) {
            throw new ValidationException("Link de redefinição inválido ou já usado.");
        }
        if (resetToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Link de redefinição expirado. Solicite um novo.");
        }

        redefinirSenha(resetToken.getMotoristaId(), novaSenha);

        resetToken.setUsado(true);
        motoristaPasswordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public String gerarTokenAprovacaoCadastro(Long motoristaId) {
        String token = passwordResetTokenGenerator.gerar();

        CadastroAprovacaoToken aprovacaoToken = new CadastroAprovacaoToken();
        aprovacaoToken.setToken(token);
        aprovacaoToken.setMotoristaId(motoristaId);
        aprovacaoToken.setExpiraEm(LocalDateTime.now().plusDays(TOKEN_APROVACAO_VALIDADE_DIAS));
        aprovacaoToken.setUsado(false);
        cadastroAprovacaoTokenRepository.save(aprovacaoToken);

        return token;
    }

    public Motorista buscarParaRevisaoPorToken(String token) {
        CadastroAprovacaoToken aprovacaoToken = validarTokenAprovacao(token, false);
        return motoristaRepository.findById(aprovacaoToken.getMotoristaId())
                .orElseThrow(() -> new ValidationException("Cadastro não encontrado para este link."));
    }

    @Transactional
    public Motorista aprovarCadastro(String token) {
        CadastroAprovacaoToken aprovacaoToken = validarTokenAprovacao(token, true);
        Motorista motorista = motoristaRepository.findById(aprovacaoToken.getMotoristaId())
                .orElseThrow(() -> new ValidationException("Cadastro não encontrado para este link."));

        motorista.setStatusCadastro(StatusCadastro.APROVADO);
        motorista.setAtivo(true);
        return motoristaRepository.save(motorista);
    }

    @Transactional
    public Motorista reprovarCadastro(String token, String motivo) {
        CadastroAprovacaoToken aprovacaoToken = validarTokenAprovacao(token, true);
        Motorista motorista = motoristaRepository.findById(aprovacaoToken.getMotoristaId())
                .orElseThrow(() -> new ValidationException("Cadastro não encontrado para este link."));

        motorista.setStatusCadastro(StatusCadastro.REPROVADO);
        motorista.setAtivo(false);
        motorista.setMotivoReprovacao(isBlank(motivo) ? null : motivo);
        return motoristaRepository.save(motorista);
    }

    private CadastroAprovacaoToken validarTokenAprovacao(String token, boolean marcarUsado) {
        CadastroAprovacaoToken aprovacaoToken = cadastroAprovacaoTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Link de aprovação inválido ou já utilizado."));

        if (aprovacaoToken.isUsado()) {
            throw new ValidationException("Este cadastro já foi revisado anteriormente.");
        }
        if (aprovacaoToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Link de aprovação expirado. Contate o suporte técnico.");
        }

        if (marcarUsado) {
            aprovacaoToken.setUsado(true);
            cadastroAprovacaoTokenRepository.save(aprovacaoToken);
        }
        return aprovacaoToken;
    }

    @Transactional
    public Motorista ativar(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));
        motorista.setAtivo(true);
        return motoristaRepository.save(motorista);
    }

    @Transactional
    public Motorista inativar(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));
        motorista.setAtivo(false);
        return motoristaRepository.save(motorista);
    }
}
