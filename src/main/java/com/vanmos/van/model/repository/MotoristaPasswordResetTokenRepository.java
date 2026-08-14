package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.MotoristaPasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotoristaPasswordResetTokenRepository extends JpaRepository<MotoristaPasswordResetToken, Long> {
    Optional<MotoristaPasswordResetToken> findByToken(String token);
}
