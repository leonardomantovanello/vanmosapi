package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Responsavel;
import com.vanmos.van.model.repository.ResponsavelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResponsavelService {
    
    @Autowired
    private ResponsavelRepository responsavelRepository;
    
    public List<Responsavel> findAll() {
        return responsavelRepository.findAll();
    }
    
    public Optional<Responsavel> findById(Long id) {
        return responsavelRepository.findById(id);
    }
    
    public Responsavel save(Responsavel responsavel) {
        return responsavelRepository.save(responsavel);
    }
    
    public void deleteById(Long id) {
        responsavelRepository.deleteById(id);
    }
    
    public Responsavel update(Long id, Responsavel responsavel) {
        responsavel.setId(id);
        return responsavelRepository.save(responsavel);
    }
    
    public boolean existsById(Long id) {
        return responsavelRepository.existsById(id);
    }
    
    public long count() {
        return responsavelRepository.count();
    }
}