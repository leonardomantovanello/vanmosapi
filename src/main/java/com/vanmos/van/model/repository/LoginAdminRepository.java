package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.LoginAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoginAdminRepository extends JpaRepository<LoginAdmin, Long> {

    // Mantido por compatibilidade — será removido após migração completa para BCrypt
    LoginAdmin findByEmailOuCpfAndSenha(String emailOuCpf, String senha);

    // Busca apenas por email/CPF — senha comparada em Java com BCrypt
    Optional<LoginAdmin> findByEmailOuCpf(String emailOuCpf);
}