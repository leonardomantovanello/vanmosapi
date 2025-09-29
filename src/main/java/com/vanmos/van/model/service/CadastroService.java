package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Cadastro;
import com.vanmos.van.model.repository.CadastroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CadastroService {
    
    @Autowired
    private CadastroRepository cadastroRepository;
    
    public List<Cadastro> findAll() {
        return cadastroRepository.findAll();
    }
    
    public Optional<Cadastro> findById(Long id) {
        return cadastroRepository.findById(id);
    }
    
    public Cadastro save(Cadastro cadastro) {
        return cadastroRepository.save(cadastro);
    }
    
    public void deleteById(Long id) {
        cadastroRepository.deleteById(id);
    }
    
    public Cadastro update(Long id, Cadastro cadastro) {
        cadastro.setId(id);
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
        List<Cadastro> usuarios = cadastroRepository.findAll();
        String cpfLimpo = emailOuCpf.replaceAll("[^0-9]", ""); // Remove máscara do CPF
        
        for (Cadastro usuario : usuarios) {
            // Buscar por email
            if (usuario.getEmail() != null && usuario.getEmail().equals(emailOuCpf)) {
                return usuario;
            }
            // Buscar por CPF (com e sem máscara)
            if (usuario.getCpf() != null) {
                String cpfUsuario = usuario.getCpf().replaceAll("[^0-9]", "");
                if (cpfUsuario.equals(cpfLimpo)) {
                    return usuario;
                }
            }
        }
        return null;
    }
}