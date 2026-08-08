package com.vanmos.van.model.service;

import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.Passageiro;
import com.vanmos.van.model.repository.PassageiroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes de unidade com Mockito — repository e encoder são mocks, nada toca
 * o banco de verdade. Foco nas regras de negócio que já vêm com comentários
 * de segurança no código-fonte: CPF/e-mail duplicado e troca de senha exigindo
 * a senha atual.
 */
@ExtendWith(MockitoExtension.class)
class PassageiroServiceTest {

    @Mock
    private PassageiroRepository passageiroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PassageiroService passageiroService;

    private Passageiro novoPassageiro;

    @BeforeEach
    void setUp() {
        novoPassageiro = new Passageiro();
        novoPassageiro.setNome("Maria Silva");
        novoPassageiro.setEmail("maria@exemplo.com");
        novoPassageiro.setSenha("SenhaForte1");
        novoPassageiro.setTipo("PASSAGEIRO");
    }

    @Test
    void cadastroComCpfDeDigitosRepetidosDeveSerRejeitado() {
        // "111.111.111-11" passa no formato (@Pattern da entidade) mas é
        // estruturalmente inválido — cpfValido() rejeita antes de qualquer query.
        novoPassageiro.setCpf("111.111.111-11");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passageiroService.save(novoPassageiro));
        assertEquals("CPF incorreto", ex.getMessage());
    }

    @Test
    void cadastroComCpfComDigitoVerificadorErradoDeveSerRejeitado() {
        // "111.444.777-35" é um CPF válido — troquei o último dígito.
        novoPassageiro.setCpf("111.444.777-36");

        assertThrows(IllegalArgumentException.class, () -> passageiroService.save(novoPassageiro));
    }

    @Test
    void cadastroComEmailJaCadastradoDeveSerRejeitado() {
        when(passageiroRepository.findByEmailIgnoreCase("maria@exemplo.com"))
                .thenReturn(Optional.of(new Passageiro()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passageiroService.save(novoPassageiro));
        assertEquals("E-mail já cadastrado", ex.getMessage());
    }

    @Test
    void cadastroValidoDeveHashearASenhaAntesDeSalvar() {
        when(passageiroRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SenhaForte1")).thenReturn("$2a$hash-fake");
        when(passageiroRepository.save(any(Passageiro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Passageiro salvo = passageiroService.save(novoPassageiro);

        assertEquals("$2a$hash-fake", salvo.getSenha());
        verify(passageiroRepository).save(novoPassageiro);
    }

    @Test
    void alterarSenhaComSenhaAtualErradaDeveSerRejeitada() {
        Passageiro existente = new Passageiro();
        existente.setSenha("hash-da-senha-atual");
        when(passageiroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("senhaErrada", "hash-da-senha-atual")).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> passageiroService.alterarSenha(1L, "senhaErrada", "NovaSenha1"));

        verify(passageiroRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void alterarSenhaComSenhaAtualCorretaDeveAtualizarOHash() {
        Passageiro existente = new Passageiro();
        existente.setSenha("hash-da-senha-atual");
        when(passageiroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("SenhaCorreta1", "hash-da-senha-atual")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("hash-da-senha-nova");

        passageiroService.alterarSenha(1L, "SenhaCorreta1", "NovaSenha1");

        assertEquals("hash-da-senha-nova", existente.getSenha());
        verify(passageiroRepository).save(existente);
    }
}
