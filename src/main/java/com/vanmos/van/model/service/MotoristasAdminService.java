package com.vanmos.van.model.service;

import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.MotoristasAdmin;
import com.vanmos.van.model.repository.MotoristasAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MotoristasAdminService {

    private static final Logger log = LoggerFactory.getLogger(MotoristasAdminService.class);

    @Autowired
    private MotoristasAdminRepository motoristasAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<MotoristasAdmin> findAll() {
        return motoristasAdminRepository.findAll();
    }

    public List<MotoristasAdmin> findAtivos() {
        return motoristasAdminRepository.findByAtivoTrue();
    }
    
    public Optional<MotoristasAdmin> findById(Long id) {
        return motoristasAdminRepository.findById(id);
    }
    
    public MotoristasAdmin save(MotoristasAdmin motoristasAdmin) {
        log.debug("Salvando motorista id={}", motoristasAdmin.getId());
        MotoristasAdmin saved = motoristasAdminRepository.save(motoristasAdmin);
        log.debug("Motorista salvo com id={}", saved.getId());
        return saved;
    }
    
    public void deleteById(Long id) {
        motoristasAdminRepository.deleteById(id);
    }
    
    // Merge parcial — NUNCA um save() cego do objeto recebido: GET /{id} tira
    // a senha da resposta (ver MotoristasAdminController), então um PUT de
    // edição de perfil sempre chega aqui com senha null/branco. Um save()
    // direto sobrescreveria a coluna com null e travaria o login do
    // motorista. ativo também não é editável por aqui de propósito — é
    // responsabilidade exclusiva de ativar()/inativar() (ROLE_ADMIN).
    public MotoristasAdmin update(Long id, MotoristasAdmin motorista) {
        MotoristasAdmin existente = motoristasAdminRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));

        if (motorista.getNomeCompleto() != null && !motorista.getNomeCompleto().isBlank()) {
            existente.setNomeCompleto(motorista.getNomeCompleto());
        }
        if (motorista.getGmail() != null && !motorista.getGmail().isBlank()) {
            existente.setGmail(motorista.getGmail());
        }
        if (motorista.getCpf() != null && !motorista.getCpf().isBlank()) {
            existente.setCpf(motorista.getCpf());
        }
        if (motorista.getCnh() != null && !motorista.getCnh().isBlank()) {
            existente.setCnh(motorista.getCnh());
        }
        if (motorista.getModeloVan() != null && !motorista.getModeloVan().isBlank()) {
            existente.setModeloVan(motorista.getModeloVan());
        }
        if (motorista.getPlacaVan() != null && !motorista.getPlacaVan().isBlank()) {
            existente.setPlacaVan(motorista.getPlacaVan());
        }
        if (motorista.getSenha() != null && !motorista.getSenha().isBlank()) {
            existente.setSenha(motorista.getSenha());
        }
        // Igual ao Passageiro: null aqui é "sem foto", um estado válido, não
        // "não veio no request" — o app sempre reenvia o avatar atual
        // inteiro a cada save (ver profileService.ts).
        existente.setAvatarBase64(motorista.getAvatarBase64());
        return motoristasAdminRepository.save(existente);
    }

    // Espelha PassageiroService#alterarSenha — motorista não tinha essa
    // rota antes (só dava pra trocar senha via cadastro/reset por ADMIN).
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        MotoristasAdmin existente = motoristasAdminRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Motorista não encontrado: " + id));

        if (!passwordEncoder.matches(senhaAtual, existente.getSenha())) {
            throw new ValidationException("Senha atual incorreta");
        }

        existente.setSenha(passwordEncoder.encode(novaSenha));
        motoristasAdminRepository.save(existente);
    }

    public boolean existsById(Long id) {
        return motoristasAdminRepository.existsById(id);
    }
    
    public long count() {
        return motoristasAdminRepository.count();
    }
    
    public MotoristasAdmin findByGmailOrCpf(String emailOuCpf) {
        return motoristasAdminRepository.findByGmailOrCpf(emailOuCpf).orElse(null);
    }
    
    // Método privado extraído para eliminar duplicação entre ativar() e inativar()
    private MotoristasAdmin alterarStatus(Long id, boolean ativo) {
        Optional<MotoristasAdmin> motorista = findById(id);
        if (motorista.isPresent()) {
            motorista.get().setAtivo(ativo);
            return save(motorista.get());
        }
        return null;
    }

    public MotoristasAdmin ativar(Long id) {
        return alterarStatus(id, true);
    }

    public MotoristasAdmin inativar(Long id) {
        return alterarStatus(id, false);
    }
}