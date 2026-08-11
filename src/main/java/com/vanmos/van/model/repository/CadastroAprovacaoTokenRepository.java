package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.CadastroAprovacaoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CadastroAprovacaoTokenRepository extends JpaRepository<CadastroAprovacaoToken, Long> {
    Optional<CadastroAprovacaoToken> findByToken(String token);
}
