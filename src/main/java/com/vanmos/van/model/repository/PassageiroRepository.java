package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Passageiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PassageiroRepository extends JpaRepository<Passageiro, Long> {

    Optional<Passageiro> findByEmailIgnoreCase(String email);

    @Query("SELECT p FROM Passageiro p WHERE REPLACE(REPLACE(REPLACE(p.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Passageiro> findByCpfDigits(@Param("cpfDigits") String cpfDigits);

    /**
     * Busca por email (case-insensitive) OU por CPF com apenas dígitos.
     * Substitui o findAll() em memória do PassageiroService.
     * REPLACE remove a formatação do CPF armazenado para comparar apenas dígitos.
     */
    @Query("SELECT p FROM Passageiro p WHERE LOWER(p.email) = LOWER(:email) " +
           "OR REPLACE(REPLACE(REPLACE(p.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Passageiro> findByEmailIgnoreCaseOrCpfDigits(
            @Param("email") String email,
            @Param("cpfDigits") String cpfDigits
    );
}
