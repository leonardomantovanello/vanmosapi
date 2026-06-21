package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.repository.CadastroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CadastroService {

    @Autowired
    private CadastroRepository cadastroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cadastro> findAll() {
        return cadastroRepository.findAll();
    }

    public Optional<Cadastro> findById(Long id) {
        return cadastroRepository.findById(id);
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
        return cadastroRepository.findByEmailIgnoreCase(email.trim()).isPresent();
    }

    @Transactional
    public Cadastro save(Cadastro cadastro) {
        validarCpf(cadastro);
        verificarCpfDuplicado(cadastro);
        verificarEmailDuplicado(cadastro);
        cadastro.setSenha(passwordEncoder.encode(cadastro.getSenha()));
        return cadastroRepository.save(cadastro);
    }

    private void validarCpf(Cadastro cadastro) {
        if (cadastro.getCpf() != null && !cpfValido(cadastro.getCpf())) {
            throw new IllegalArgumentException("CPF incorreto");
        }
    }

    private void verificarCpfDuplicado(Cadastro cadastro) {
        if (cadastro.getCpf() == null || cadastro.getCpf().isBlank()) return;
        String cpfDigitos = cadastro.getCpf().replaceAll("[^0-9]", "");
        cadastroRepository.findByCpfDigits(cpfDigitos).ifPresent(existente -> {
            if (!existente.getId().equals(cadastro.getId())) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
        });
    }

    // Cadastro só cadastra — se email já existe, rejeita sempre.
    // Login é responsabilidade exclusiva do endpoint /api/login.
    private void verificarEmailDuplicado(Cadastro cadastro) {
        if (cadastro.getEmail() == null) return;
        if (cadastroRepository.findByEmailIgnoreCase(cadastro.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
    }

    public void deleteById(Long id) {
        cadastroRepository.deleteById(id);
    }

    @Transactional
    public Cadastro update(Long id, Cadastro cadastro) {
        Cadastro existente = cadastroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cadastro não encontrado: " + id));
        existente.setNome(cadastro.getNome());
        existente.setIdade(cadastro.getIdade());
        existente.setGenero(cadastro.getGenero());
        if (cadastro.getSenha() != null && !cadastro.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(cadastro.getSenha()));
        }
        return cadastroRepository.save(existente);
    }

    @Transactional
    public Cadastro updateCodStatus(Long id) {
        Cadastro cadastro = cadastroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cadastro não encontrado: " + id));
        cadastro.setAtivo(true);
        return cadastroRepository.save(cadastro);
    }

    @Transactional
    public Cadastro inativarCadastro(Long id) {
        Cadastro cadastro = cadastroRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cadastro não encontrado: " + id));
        cadastro.setAtivo(false);
        return cadastroRepository.save(cadastro);
    }

    public boolean existsById(Long id) {
        return cadastroRepository.existsById(id);
    }

    public long count() {
        return cadastroRepository.count();
    }

    public void deleteAll() {
        cadastroRepository.deleteAll();
    }

    public Cadastro findByEmailOrCpf(String emailOuCpf) {
        String cpfLimpo = emailOuCpf.replaceAll("[^0-9]", "");
        return cadastroRepository.findByEmailIgnoreCaseOrCpfDigits(emailOuCpf.trim(), cpfLimpo)
                .orElse(null);
    }
}
