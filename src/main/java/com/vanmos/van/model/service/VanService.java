package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Van;
import com.vanmos.van.model.repository.VanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VanService {
    
    @Autowired
    private VanRepository vanRepository;
    
    public List<Van> findAll() {
        return vanRepository.findAll();
    }
    
    public Optional<Van> findById(Long id) {
        return vanRepository.findById(id);
    }
    
    public Van save(Van van) {
        return vanRepository.save(van);
    }
    
    public void deleteById(Long id) {
        vanRepository.deleteById(id);
    }
    
    public Van update(Long id, Van van) {
        van.setId(id);
        return vanRepository.save(van);
    }
    
    public boolean existsById(Long id) {
        return vanRepository.existsById(id);
    }
    
    public long count() {
        return vanRepository.count();
    }
}