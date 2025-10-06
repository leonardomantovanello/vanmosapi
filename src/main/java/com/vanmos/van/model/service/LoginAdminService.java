package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.LoginAdmin;
import com.vanmos.van.model.repository.LoginAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoginAdminService {
    
    @Autowired
    private LoginAdminRepository loginAdminRepository;
    
    public List<LoginAdmin> findAll() {
        return loginAdminRepository.findAll();
    }
    
    public Optional<LoginAdmin> findById(Long id) {
        return loginAdminRepository.findById(id);
    }
    
    public LoginAdmin save(LoginAdmin loginAdmin) {
        return loginAdminRepository.save(loginAdmin);
    }
    
    public void deleteById(Long id) {
        loginAdminRepository.deleteById(id);
    }
    
    public LoginAdmin update(Long id, LoginAdmin loginAdmin) {
        loginAdmin.setId(id);
        return loginAdminRepository.save(loginAdmin);
    }
    
    public boolean existsById(Long id) {
        return loginAdminRepository.existsById(id);
    }
    
    public long count() {
        return loginAdminRepository.count();
    }
    
    public LoginAdmin findByEmailOuCpfAndSenha(String emailOuCpf, String senha) {
        return loginAdminRepository.findByEmailOuCpfAndSenha(emailOuCpf, senha);
    }
}