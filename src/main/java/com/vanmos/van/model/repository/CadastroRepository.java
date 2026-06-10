package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Cadastro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CadastroRepository extends JpaRepository<Cadastro, Long> {

    Optional<Cadastro> findByEmailIgnoreCase(String email);

    @Query("SELECT c FROM Cadastro c WHERE REPLACE(REPLACE(REPLACE(c.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Cadastro> findByCpfDigits(@Param("cpfDigits") String cpfDigits);

    /**
     * Busca por email (case-insensitive) OU por CPF com apenas dígitos.
     * Substitui o findAll() em memória do CadastroService.
     * REPLACE remove a formatação do CPF armazenado para comparar apenas dígitos.
     */
    @Query("SELECT c FROM Cadastro c WHERE LOWER(c.email) = LOWER(:email) " +
           "OR REPLACE(REPLACE(REPLACE(c.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Cadastro> findByEmailIgnoreCaseOrCpfDigits(
            @Param("email") String email,
            @Param("cpfDigits") String cpfDigits
    );
}