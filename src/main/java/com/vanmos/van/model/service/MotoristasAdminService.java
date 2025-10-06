package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.MotoristasAdmin;
import com.vanmos.van.model.repository.MotoristasAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MotoristasAdminService {
    
    @Autowired
    private MotoristasAdminRepository motoristasAdminRepository;
    
    public List<MotoristasAdmin> findAll() {
        return motoristasAdminRepository.findAll();
    }
    
    public Optional<MotoristasAdmin> findById(Long id) {
        return motoristasAdminRepository.findById(id);
    }
    
    public MotoristasAdmin save(MotoristasAdmin motoristasAdmin) {
        System.out.println("Service: Salvando motorista - " + motoristasAdmin.getNomeCompleto());
        MotoristasAdmin saved = motoristasAdminRepository.save(motoristasAdmin);
        System.out.println("Service: Motorista salvo com ID - " + saved.getId());
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
    
    public MotoristasAdmin ativar(Long id) {
        Optional<MotoristasAdmin> motorista = findById(id);
        if (motorista.isPresent()) {
            motorista.get().setAtivo(true);
            return save(motorista.get());
        }
        return null;
    }
    
    public MotoristasAdmin inativar(Long id) {
        Optional<MotoristasAdmin> motorista = findById(id);
        if (motorista.isPresent()) {
            motorista.get().setAtivo(false);
            return save(motorista.get());
        }
        return null;
    }
}