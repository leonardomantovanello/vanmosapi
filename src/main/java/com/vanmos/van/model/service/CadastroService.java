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
    
    private boolean cpfValido(String cpf) {
        String c = cpf.replaceAll("[^0-9]", "");
        if (c.length() != 11 || c.chars().distinct().count() == 1) return false;
        int s1 = 0, s2 = 0;
        for (int i = 0; i < 9; i++) s1 += (c.charAt(i) - '0') * (10 - i);
        int d1 = (s1 * 10) % 11; if (d1 == 10) d1 = 0;
        for (int i = 0; i < 10; i++) s2 += (c.charAt(i) - '0') * (11 - i);
        int d2 = (s2 * 10) % 11; if (d2 == 10) d2 = 0;
        return d1 == (c.charAt(9) - '0') && d2 == (c.charAt(10) - '0');
    }

    public boolean emailJaCadastrado(String email) {
        return cadastroRepository.findByEmailIgnoreCase(email.trim()).isPresent();
    }

    public Cadastro save(Cadastro cadastro) {
        if (cadastro.getCpf() != null && !cpfValido(cadastro.getCpf())) {
            throw new IllegalArgumentException("CPF incorreto");
        }
        if (cadastro.getEmail() != null) {
            Optional<Cadastro> existenteOpt = cadastroRepository.findByEmailIgnoreCase(cadastro.getEmail().trim());
            if (existenteOpt.isPresent()) {
                Cadastro existente = existenteOpt.get();
                String cpfNovo = cadastro.getCpf() != null ? cadastro.getCpf().replaceAll("[^0-9]", "") : "";
                String cpfExistente = existente.getCpf() != null ? existente.getCpf().replaceAll("[^0-9]", "") : "";
                if (!cpfNovo.equals(cpfExistente)) {
                    throw new IllegalArgumentException("E-mail já cadastrado");
                }
                boolean nomeDiferente = cadastro.getNome() != null && !cadastro.getNome().equalsIgnoreCase(existente.getNome());
                boolean senhaDiferente = cadastro.getSenha() != null && !cadastro.getSenha().equals(existente.getSenha());
                if (nomeDiferente || senhaDiferente) {
                    throw new IllegalArgumentException("Informações incorretas, verifique os dados anteriores.");
                }
                throw new LoginSucessoException(existente);
            }
        }
        return cadastroRepository.save(cadastro);
    }
    
    public void deleteById(Long id) {
        cadastroRepository.deleteById(id);
    }
    
    public Cadastro update(Long id, Cadastro cadastro) {
        cadastro.setId(id);
        return cadastroRepository.save(cadastro);
    }

    public Cadastro updateCodStatus(Long id) {
        Cadastro cadastro = cadastroRepository.findById(id).get();
        cadastro.setAtivo(true);

        System.out.println("Cod Ativo " + cadastro.getAtivo());
        return cadastroRepository.save(cadastro);
    }

    public Cadastro inativarCadastro(Long id) {
        Cadastro cadastro = cadastroRepository.findById(id).get();
        cadastro.setAtivo(false);

        System.out.println("Cod Inativo " + cadastro.getAtivo());
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
        String cpfLimpo = emailOuCpf.replaceAll("[^0-9]", "");
        
        for (Cadastro usuario : usuarios) {
            // Buscar por email
            if (usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(emailOuCpf.trim())) {
                return usuario;
            }
            
            // Buscar por CPF
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