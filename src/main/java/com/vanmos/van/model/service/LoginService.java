package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Login;
import com.vanmos.van.model.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoginService {
    
    @Autowired
    private LoginRepository loginRepository;
    
    public List<Login> findAll() {
        return loginRepository.findAll();
    }
    
    public Optional<Login> findById(Long id) {
        return loginRepository.findById(id);
    }
    
    public Login save(Login login) {
        return loginRepository.save(login);
    }
    
    public void deleteById(Long id) {
        loginRepository.deleteById(id);
    }
    
    public Login update(Long id, Login login) {
        login.setId(id);
        return loginRepository.save(login);
    }
    
    public boolean existsById(Long id) {
        return loginRepository.existsById(id);
    }
    
    public long count() {
        return loginRepository.count();
    }
}