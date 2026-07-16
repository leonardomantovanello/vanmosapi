package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.MotoristasAdmin;
import com.vanmos.van.model.repository.MotoristasAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    public MotoristasAdmin update(Long id, MotoristasAdmin motoristasAdmin) {
        motoristasAdmin.setId(id);
        return motoristasAdminRepository.save(motoristasAdmin);
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