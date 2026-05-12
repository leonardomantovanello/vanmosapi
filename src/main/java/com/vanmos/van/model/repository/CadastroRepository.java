package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Cadastro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CadastroRepository extends JpaRepository<Cadastro, Long> {
    Optional<Cadastro> findByEmailIgnoreCase(String email);
}