package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {
    
    @Autowired
    private AlunoRepository alunoRepository;
    
    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }
    
    public Optional<Aluno> findById(Long id) {
        return alunoRepository.findById(id);
    }
    
    public Aluno save(Aluno aluno) {
        return alunoRepository.save(aluno);
    }
    
    public void deleteById(Long id) {
        alunoRepository.deleteById(id);
    }
    
    public Aluno update(Long id, Aluno aluno) {
        aluno.setId(id);
        return alunoRepository.save(aluno);
    }
    
    public boolean existsById(Long id) {
        return alunoRepository.existsById(id);
    }
    
    public long count() {
        return alunoRepository.count();
    }
}