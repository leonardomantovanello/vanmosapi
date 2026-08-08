package com.vanmos.van.model.service;

import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.entity.PasswordResetToken;
import com.vanmos.van.model.repository.PassageiroRepository;
import com.vanmos.van.model.repository.PasswordResetTokenRepository;
import com.vanmos.van.security.PasswordResetTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PassageiroService {

    // Validade do link de redefinição de senha enviado por e-mail.
    private static final long TOKEN_REDEFINICAO_VALIDADE_MINUTOS = 30;

    @Autowired
    private PassageiroRepository passageiroRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordResetTokenGenerator passwordResetTokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Passageiro> findAll() {
        return passageiroRepository.findAll();
    }

    public Optional<Passageiro> findById(Long id) {
        return passageiroRepository.findById(id);
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

    public boolean emailJaCadastrado(String email) {
        return passageiroRepository.findByEmailIgnoreCase(email.trim()).isPresent();
    }

    public Optional<Passageiro> buscarPorEmail(String email) {
        return passageiroRepository.findByEmailIgnoreCase(email.trim());
    }

    @Transactional
    public Passageiro save(Passageiro passageiro) {
        validarCpf(passageiro);
        verificarCpfDuplicado(passageiro);
        verificarEmailDuplicado(passageiro);
        passageiro.setSenha(passwordEncoder.encode(passageiro.getSenha()));
        return passageiroRepository.save(passageiro);
    }

    private void validarCpf(Passageiro passageiro) {
        if (passageiro.getCpf() == null || passageiro.getCpf().isBlank()) return;
        if (!cpfValido(passageiro.getCpf())) {
            throw new IllegalArgumentException("CPF incorreto");
        }
    }

    private void verificarCpfDuplicado(Passageiro passageiro) {
        if (passageiro.getCpf() == null || passageiro.getCpf().isBlank()) return;
        String cpfDigitos = passageiro.getCpf().replaceAll("[^0-9]", "");
        passageiroRepository.findByCpfDigits(cpfDigitos).ifPresent(existente -> {
            if (!existente.getId().equals(passageiro.getId())) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
        });
    }

    // Cadastro só cadastra — se email já existe, rejeita sempre.
    // Login é responsabilidade exclusiva do endpoint /api/login.
    private void verificarEmailDuplicado(Passageiro passageiro) {
        if (passageiro.getEmail() == null) return;
        if (passageiroRepository.findByEmailIgnoreCase(passageiro.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
    }

    public void deleteById(Long id) {
        passageiroRepository.deleteById(id);
    }

    @Transactional
    public Passageiro update(Long id, Passageiro passageiro) {
        Passageiro existente = passageiroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Passageiro não encontrado: " + id));

        if (passageiro.getNome() != null && !passageiro.getNome().isBlank()) {
            existente.setNome(passageiro.getNome());
        }
        existente.setIdade(passageiro.getIdade());
        existente.setGenero(passageiro.getGenero());

        if (passageiro.getCpf() != null && !passageiro.getCpf().isBlank()) {
            if (!cpfValido(passageiro.getCpf())) {
                throw new IllegalArgumentException("CPF incorreto");
            }
            String cpfDigitos = passageiro.getCpf().replaceAll("[^0-9]", "");
            passageiroRepository.findByCpfDigits(cpfDigitos).ifPresent(outro -> {
                if (!outro.getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado");
            });
            existente.setCpf(passageiro.getCpf());
        }

        if (passageiro.getEmail() != null && !passageiro.getEmail().isBlank()) {
            passageiroRepository.findByEmailIgnoreCase(passageiro.getEmail().trim()).ifPresent(outro -> {
                if (!outro.getId().equals(id)) throw new IllegalArgumentException("E-mail já cadastrado");
            });
            existente.setEmail(passageiro.getEmail());
        }

        if (passageiro.getSenha() != null && !passageiro.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(passageiro.getSenha()));
        }
        // Diferente dos campos acima, null aqui é um valor válido ("sem
        // foto") — não "não veio no request". O app sempre reenvia o avatar
        // atual inteiro a cada save (ver profileService.ts), então
        // sobrescrever sem guarda de blank é seguro e correto.
        existente.setAvatarBase64(passageiro.getAvatarBase64());
        return passageiroRepository.save(existente);
    }

    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Passageiro existente = passageiroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Passageiro não encontrado: " + id));

        if (!passwordEncoder.matches(senhaAtual, existente.getSenha())) {
            throw new com.vanmos.van.exception.ValidationException("Senha atual incorreta");
        }

        existente.setSenha(passwordEncoder.encode(novaSenha));
        passageiroRepository.save(existente);
    }

    // Redefinição via "esqueci minha senha" — diferente de alterarSenha()
    // acima, não exige a senha atual: a prova de identidade aqui é o token
    // de uso único que só chega em quem tem acesso à caixa de e-mail
    // cadastrada (ver gerarTokenRedefinicaoSenha/redefinirSenhaComToken).
    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        Passageiro existente = passageiroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Passageiro não encontrado: " + id));
        existente.setSenha(passwordEncoder.encode(novaSenha));
        passageiroRepository.save(existente);
    }

    // Gera o token de uso único do link enviado por e-mail — cada pedido
    // cria uma linha nova (não invalida pedidos anteriores; se o usuário
    // clicar um link antigo antes de um novo, ele ainda funciona até expirar
    // ou ser usado, o que for primeiro).
    @Transactional
    public String gerarTokenRedefinicaoSenha(Long passageiroId) {
        String token = passwordResetTokenGenerator.gerar();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setPassageiroId(passageiroId);
        resetToken.setExpiraEm(LocalDateTime.now().plusMinutes(TOKEN_REDEFINICAO_VALIDADE_MINUTOS));
        resetToken.setUsado(false);
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    // Valida o token (existe, não expirou, não foi usado) e troca a senha —
    // marca o token como usado no mesmo passo pra não poder ser reaproveitado.
    @Transactional
    public void redefinirSenhaComToken(String token, String novaSenha) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Link de redefinição inválido ou já usado."));

        if (resetToken.isUsado()) {
            throw new ValidationException("Link de redefinição inválido ou já usado.");
        }
        if (resetToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Link de redefinição expirado. Solicite um novo.");
        }

        redefinirSenha(resetToken.getPassageiroId(), novaSenha);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public Passageiro updateCodStatus(Long id) {
        Passageiro passageiro = passageiroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Passageiro não encontrado: " + id));
        passageiro.setAtivo(true);
        return passageiroRepository.save(passageiro);
    }

    @Transactional
    public Passageiro inativarCadastro(Long id) {
        Passageiro passageiro = passageiroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Passageiro não encontrado: " + id));
        passageiro.setAtivo(false);
        return passageiroRepository.save(passageiro);
    }

    public boolean existsById(Long id) {
        return passageiroRepository.existsById(id);
    }

    public long count() {
        return passageiroRepository.count();
    }

    public void deleteAll() {
        passageiroRepository.deleteAll();
    }

    public Passageiro findByEmailOrCpf(String emailOuCpf) {
        String cpfLimpo = emailOuCpf.replaceAll("[^0-9]", "");
        return passageiroRepository.findByEmailIgnoreCaseOrCpfDigits(emailOuCpf.trim(), cpfLimpo)
                .orElse(null);
    }
}
