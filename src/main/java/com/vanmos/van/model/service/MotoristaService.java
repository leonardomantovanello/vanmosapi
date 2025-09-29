package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.repository.MotoristaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MotoristaService {
    
    @Autowired
    private MotoristaRepository motoristaRepository;
    
    public List<Motorista> findAll() {
        return motoristaRepository.findAll();
    }
    
    public Optional<Motorista> findById(Long id) {
        return motoristaRepository.findById(id);
    }
    
    public Motorista save(Motorista motorista) {
        return motoristaRepository.save(motorista);
    }
    
    public void deleteById(Long id) {
        motoristaRepository.deleteById(id);
    }
    
    public Motorista update(Long id, Motorista motorista) {
        motorista.setId(id);
        return motoristaRepository.save(motorista);
    }
    
    public boolean existsById(Long id) {
        return motoristaRepository.existsById(id);
    }
    
    public long count() {
        return motoristaRepository.count();
    }
}