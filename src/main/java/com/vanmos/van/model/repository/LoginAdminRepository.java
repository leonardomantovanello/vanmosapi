package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.LoginAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAdminRepository extends JpaRepository<LoginAdmin, Long> {
    LoginAdmin findByEmailOuCpfAndSenha(String emailOuCpf, String senha);
}